package com.barraca.view;

import com.barraca.model.FormaPagamento;
import com.barraca.model.ItemPedido;
import com.barraca.model.PedidoResumo;
import com.barraca.model.StatusPedido;
import com.barraca.service.PedidoService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PedidoListPanel extends JPanel {

    private final PedidoService service = new PedidoService();

    private final JTextField txtDe = new JTextField(10);
    private final JTextField txtAte = new JTextField(10);
    private final JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"TODOS", "ABERTO", "FECHADO", "CANCELADO"});
    private final JComboBox<String> cmbPagamento = new JComboBox<>(new String[]{"TODOS", "DINHEIRO", "CARTAO", "PIX"});
    private final JTextField txtBuscar = new JTextField(10);

    private final PedidoResumoTableModel model = new PedidoResumoTableModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<PedidoResumoTableModel> sorter = new TableRowSorter<>(model);

    public PedidoListPanel() {
        setLayout(new BorderLayout());
        setBackground(UiColors.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTable(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        refresh();
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel title = new JLabel("Lista de Pedidos");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(UiColors.TEXT);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filtros.setOpaque(false);

        filtros.add(new JLabel("De (yyyy-MM-dd):"));
        filtros.add(txtDe);
        filtros.add(new JLabel("Até:"));
        filtros.add(txtAte);
        filtros.add(new JLabel("Status:"));
        filtros.add(cmbStatus);
        filtros.add(new JLabel("Pagamento:"));
        filtros.add(cmbPagamento);
        filtros.add(new JLabel("Buscar:"));
        filtros.add(txtBuscar);

        JButton btnFiltrar = primaryButton("Consultar");
        btnFiltrar.addActionListener(e -> refresh());

        JButton btnLimpar = new JButton("Limpar");
        btnLimpar.addActionListener(e -> {
            txtDe.setText("");
            txtAte.setText("");
            cmbStatus.setSelectedIndex(0);
            cmbPagamento.setSelectedIndex(0);
            txtBuscar.setText("");
            refresh();
        });

        filtros.add(btnLimpar);
        filtros.add(btnFiltrar);

        // Filtros locais (sem ir ao DB)
        cmbStatus.addActionListener(e -> applyLocalFilter());
        cmbPagamento.addActionListener(e -> applyLocalFilter());
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyLocalFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyLocalFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyLocalFilter(); }
        });

        p.add(title, BorderLayout.WEST);
        p.add(filtros, BorderLayout.EAST);
        return p;
    }

    private JComponent buildTable() {
        table.setRowHeight(28);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        return sp;
    }

    private JComponent buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        p.setOpaque(false);

        JButton btnExportar = new JButton("Exportar CSV");
        btnExportar.addActionListener(e -> exportarCsv());

        JButton btnDetalhes = new JButton("Detalhes");
        btnDetalhes.addActionListener(e -> mostrarDetalhes());

        JButton btnCancelar = new JButton("Cancelar Pedido");
        btnCancelar.addActionListener(e -> cancelarPedido());

        JButton btnReabrir = new JButton("Reabrir Pedido");
        btnReabrir.addActionListener(e -> reabrirPedido());


        JButton btnRecarregar = new JButton("Recarregar");
        btnRecarregar.addActionListener(e -> refresh());

        p.add(btnRecarregar);
        p.add(btnExportar);
        p.add(btnDetalhes);
        p.add(btnReabrir);
        p.add(btnCancelar);
        return p;
    }

    private JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(UiColors.SECONDARY);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        return b;
    }

    private PedidoResumo getSelecionado() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.get(modelRow);
    }

    public void refresh() {
        try {
            LocalDate de = Formatters.parseDate(txtDe.getText());
            LocalDate ate = Formatters.parseDate(txtAte.getText());
            List<PedidoResumo> list = service.listarResumo(de, ate);
            model.setData(list);
            applyLocalFilter();
        } catch (Exception ex) {
            Dialogs.error(this, "Filtro inválido. Use yyyy-MM-dd (ex.: 2026-01-12).");
        }
    }

    private void applyLocalFilter() {
        final String statusSel = (String) cmbStatus.getSelectedItem();
        final String pagSel = (String) cmbPagamento.getSelectedItem();
        final String q = txtBuscar.getText() == null ? "" : txtBuscar.getText().trim().toLowerCase();

        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends PedidoResumoTableModel, ? extends Integer> entry) {
                PedidoResumo pr = model.get(entry.getIdentifier());

                boolean okStatus = "TODOS".equals(statusSel) || pr.getStatus().name().equals(statusSel);
                boolean okPag = "TODOS".equals(pagSel) || (pr.getPagamento() != null && pr.getPagamento().name().equals(pagSel));

                boolean okBusca = true;
                if (!q.isEmpty()) {
                    String id = String.valueOf(pr.getId());
                    String total = pr.getTotal() == null ? "" : pr.getTotal().toPlainString();
                    okBusca = id.contains(q) || total.toLowerCase().contains(q);
                }

                return okStatus && okPag && okBusca;
            }
        });
    }

    
    private void cancelarPedido() {
        PedidoResumo sel = getSelecionado();
        if (sel == null) {
            Dialogs.info(this, "Selecione um pedido.");
            return;
        }
        if (sel.getStatus() == StatusPedido.CANCELADO) {
            Dialogs.info(this, "Este pedido já está CANCELADO.");
            return;
        }
        int opt = JOptionPane.showConfirmDialog(
                this,
                "Tem certeza que deseja CANCELAR o pedido #" + sel.getId() + "?",
                "Confirmar cancelamento",
                JOptionPane.YES_NO_OPTION
        );
        if (opt != JOptionPane.YES_OPTION) return;

        service.cancelarPedido(sel.getId());
        refresh();
    }

    private void reabrirPedido() {
        PedidoResumo sel = getSelecionado();
        if (sel == null) {
            Dialogs.info(this, "Selecione um pedido.");
            return;
        }
        if (sel.getStatus() == StatusPedido.ABERTO) {
            Dialogs.info(this, "Este pedido já está ABERTO.");
            return;
        }
        if (sel.getStatus() == StatusPedido.CANCELADO) {
            Dialogs.info(this, "Pedidos CANCELADOS não podem ser reabertos.");
            return;
        }

        int opt = JOptionPane.showConfirmDialog(
                this,
                "Reabrir o pedido #" + sel.getId() + "?\n" +
                        "O pagamento será removido e o status voltará para ABERTO.",
                "Confirmar reabertura",
                JOptionPane.YES_NO_OPTION
        );
        if (opt != JOptionPane.YES_OPTION) return;

        service.reabrirPedido(sel.getId());
        refresh();
    }

    private void mostrarDetalhes() {
        PedidoResumo sel = getSelecionado();
        if (sel == null) {
            Dialogs.info(this, "Selecione um pedido.");
            return;
        }

        List<ItemPedido> itens = service.itensDoPedido(sel.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("Pedido #").append(sel.getId()).append("\n");
        sb.append("Data: ").append(Formatters.date(sel.getData())).append("\n");
        sb.append("Status: ").append(sel.getStatus()).append("\n");
        sb.append("Pagamento: ").append(sel.getPagamento() == null ? "-" : sel.getPagamento()).append("\n");
        sb.append("Recebido: ").append(sel.isRecebido() ? "SIM" : "NÃO").append("\n\n");
        sb.append("Itens:\n");
        for (ItemPedido it : itens) {
            sb.append("- ").append(it.getProduto().getNome())
                    .append(" x").append(it.getQuantidade())
                    .append(" = ").append(Formatters.money(it.getSubtotal()))
                    .append("\n");
        }
        sb.append("\nTotal: ").append(Formatters.money(sel.getTotal()));

        JTextArea area = new JTextArea(sb.toString(), 18, 45);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JOptionPane.showMessageDialog(this, new JScrollPane(area), "Detalhes do Pedido", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportarCsv() {
        if (model.getRowCount() == 0) {
            Dialogs.info(this, "Não há pedidos para exportar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Salvar CSV");
        chooser.setSelectedFile(new File("pedidos.csv"));
        int opt = chooser.showSaveDialog(this);
        if (opt != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            w.write("id;data;itens;total;status;pagamento;recebido");
            w.newLine();

            for (int viewRow = 0; viewRow < table.getRowCount(); viewRow++) {
                int modelRow = table.convertRowIndexToModel(viewRow);
                PedidoResumo p = model.get(modelRow);
                String pagamento = (p.getPagamento() == null) ? "" : p.getPagamento().name();
                w.write(p.getId() + ";" + Formatters.date(p.getData()) + ";" + p.getItens() + ";" +
                        p.getTotal() + ";" + p.getStatus().name() + ";" + pagamento + ";" + (p.isRecebido() ? "1" : "0"));
                w.newLine();
            }

            Dialogs.info(this, "CSV salvo em: " + file.getAbsolutePath());
        } catch (IOException ex) {
            Dialogs.error(this, "Falha ao salvar CSV: " + ex.getMessage());
        }
    }

    static class PedidoResumoTableModel extends AbstractTableModel {
        private final String[] cols = {"Pedido", "Data", "Itens", "Total", "Status", "Pagamento", "Recebido"};
        private final List<PedidoResumo> data = new ArrayList<>();

        public void setData(List<PedidoResumo> list) {
            data.clear();
            if (list != null) data.addAll(list);
            fireTableDataChanged();
        }

        public PedidoResumo get(int row) {
            return data.get(row);
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int column) { return cols[column]; }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 6) return Boolean.class;
            return Object.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if (columnIndex != 6) return false;
            PedidoResumo p = data.get(rowIndex);
            // só faz sentido marcar "Recebido" quando está FECHADO
            return p.getStatus() == StatusPedido.FECHADO;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex != 6) return;
            PedidoResumo p = data.get(rowIndex);
            boolean novo = Boolean.TRUE.equals(aValue);
            // Atualiza no banco via service e recarrega
            try {
                new PedidoService().atualizarPagamentoRecebido(p.getId(), novo);
                // recria o objeto na lista (PedidoResumo é imutável)
                data.set(rowIndex, new PedidoResumo(p.getId(), p.getData(), p.getStatus(), p.getPagamento(), novo, p.getTotal(), p.getItens()));
                fireTableRowsUpdated(rowIndex, rowIndex);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Falha ao atualizar recebido: " + ex.getMessage());
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            PedidoResumo p = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> p.getId();
                case 1 -> Formatters.date(p.getData());
                case 2 -> p.getItens();
                case 3 -> Formatters.money(p.getTotal());
                case 4 -> p.getStatus().name();
                case 5 -> (p.getPagamento() == null ? "-" : p.getPagamento().name());
                case 6 -> p.isRecebido();
                default -> "";
            };
        }
    }
}
