package com.barraca.view;

import com.barraca.model.Produto;
import com.barraca.service.ProdutoService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ProdutoListPanel extends JPanel {

    private final ProdutoService service = new ProdutoService();
    private final Consumer<Integer> onEditar;

    private final JTextField txtBuscar = new JTextField(18);
    private final JCheckBox chkIncluirInativos = new JCheckBox("Incluir inativos");

    private final ProdutoTableModel model = new ProdutoTableModel();
    private final JTable table = new JTable(model);
    private final TableRowSorter<ProdutoTableModel> sorter = new TableRowSorter<>(model);

    public ProdutoListPanel(Consumer<Integer> onEditar) {
        this.onEditar = onEditar;

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

        JLabel title = new JLabel("Lista de Produtos");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(UiColors.TEXT);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        chkIncluirInativos.setOpaque(false);
        chkIncluirInativos.addActionListener(e -> refresh());

        right.add(new JLabel("Buscar:"));
        right.add(txtBuscar);
        right.add(chkIncluirInativos);

        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });

        p.add(title, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
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

        JButton btnNovo = primaryButton("Novo");
        btnNovo.addActionListener(e -> onEditar.accept(null));

        JButton btnEditar = new JButton("Editar");
        btnEditar.addActionListener(e -> {
            Produto sel = getSelecionado();
            if (sel == null) {
                Dialogs.info(this, "Selecione um produto na tabela.");
                return;
            }
            onEditar.accept(sel.getId());
        });

        JButton btnAtivarInativar = new JButton("Ativar/Inativar");
        btnAtivarInativar.addActionListener(e -> {
            Produto sel = getSelecionado();
            if (sel == null) {
                Dialogs.info(this, "Selecione um produto na tabela.");
                return;
            }
            boolean novoAtivo = !sel.isAtivo();
            String acao = novoAtivo ? "ativar" : "inativar";
            if (Dialogs.confirm(this, "Deseja " + acao + " o produto \"" + sel.getNome() + "\"?")) {
                service.setAtivo(sel.getId(), novoAtivo);
                refresh();
            }
        });

        JButton btnRecarregar = new JButton("Recarregar");
        btnRecarregar.addActionListener(e -> refresh());

        p.add(btnRecarregar);
        p.add(btnAtivarInativar);
        p.add(btnEditar);
        p.add(btnNovo);
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

    private Produto getSelecionado() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        return model.get(modelRow);
    }

    private void applyFilter() {
        String q = txtBuscar.getText();
        if (q == null || q.trim().isEmpty()) {
            sorter.setRowFilter(null);
            return;
        }
        final String query = q.trim().toLowerCase();
        sorter.setRowFilter(new RowFilter<>() {
            @Override
            public boolean include(Entry<? extends ProdutoTableModel, ? extends Integer> entry) {
                Produto p = model.get(entry.getIdentifier());
                return p.getNome() != null && p.getNome().toLowerCase().contains(query);
            }
        });
    }

    public void refresh() {
        try {
            List<Produto> produtos = service.listar(chkIncluirInativos.isSelected());
            model.setData(produtos);
            applyFilter();
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage());
        }
    }

    static class ProdutoTableModel extends AbstractTableModel {
        private final String[] cols = {"ID", "Nome", "Preço", "Status"};
        private final List<Produto> data = new ArrayList<>();

        public void setData(List<Produto> list) {
            data.clear();
            if (list != null) data.addAll(list);
            fireTableDataChanged();
        }

        public Produto get(int row) { return data.get(row); }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int column) { return cols[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Produto p = data.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> p.getId();
                case 1 -> p.getNome();
                case 2 -> Formatters.money(p.getPreco());
                case 3 -> (p.isAtivo() ? "ATIVO" : "INATIVO");
                default -> "";
            };
        }
    }
}
