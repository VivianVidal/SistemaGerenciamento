package com.barraca.view;

import com.barraca.model.*;
import com.barraca.service.PedidoService;
import com.barraca.service.ProdutoService;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PedidoCreatePanel extends JPanel {

    private final PedidoService pedidoService = new PedidoService();
    private final ProdutoService produtoService = new ProdutoService();
    private final Runnable onIrLista;

    private Pedido pedidoAtual;

    private final JComboBox<Produto> cmbProdutos = new JComboBox<>();
    private final JSpinner spQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
    private final JButton btnAdicionar = primaryButton("Adicionar Item");

    private final ItensTableModel itensModel = new ItensTableModel();
    private final JTable tblItens = new JTable(itensModel);

    private final JLabel lblTotal = new JLabel("Total: R$ 0,00");
    private final JComboBox<FormaPagamento> cmbPagamento = new JComboBox<>(FormaPagamento.values());

    private final JButton btnFechar = primaryButton("Fechar Pedido");

    public PedidoCreatePanel(Runnable onIrLista) {
        this.onIrLista = onIrLista;

        setLayout(new BorderLayout());
        setBackground(UiColors.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        // Atualiza total quando usuário edita quantidade direto na tabela
        itensModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                atualizarTotal();
                atualizarEstadoBotoes();
            }
        });

        novoPedido();
    }

    public void novoPedido() {
        pedidoAtual = pedidoService.criarNovo();
        carregarProdutos();
        itensModel.setData(pedidoAtual.getItens());
        atualizarTotal();
        atualizarEstadoBotoes();
        cmbPagamento.setSelectedItem(FormaPagamento.DINHEIRO);
    }

    private void carregarProdutos() {
        try {
            List<Produto> list = produtoService.listar(false); // somente ativos
            cmbProdutos.removeAllItems();
            for (Produto p : list) cmbProdutos.addItem(p);
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage());
        }
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel title = new JLabel("Criar Pedido");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(UiColors.TEXT);

        p.add(title, BorderLayout.WEST);
        return p;
    }

    private JComponent buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);

        body.add(buildNovaLinhaItem(), BorderLayout.NORTH);
        body.add(buildListaItens(), BorderLayout.CENTER);

        return body;
    }

    private JComponent buildNovaLinhaItem() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setBorder(BorderFactory.createTitledBorder("Novo Item"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        p.add(new JLabel("Produto:"), gbc);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1;
        p.add(cmbProdutos, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        p.add(new JLabel("Quantidade:"), gbc);
        gbc.gridx = 3;
        gbc.gridy = 0;
        gbc.weightx = 0;
        p.add(spQuantidade, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0;
        gbc.weightx = 0;
        p.add(btnAdicionar, gbc);

        btnAdicionar.addActionListener(e -> adicionarItem());

        return p;
    }

    private JComponent buildListaItens() {
        tblItens.setRowHeight(28);
        tblItens.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane sp = new JScrollPane(tblItens);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createTitledBorder("Itens do Pedido"));
        wrap.add(sp, BorderLayout.CENTER);

        JButton btnRemover = new JButton("Remover Selecionado");
        btnRemover.addActionListener(e -> removerItem());

        JButton btnLimpar = new JButton("Limpar Itens");
        btnLimpar.addActionListener(e -> {
            if (pedidoAtual.getItens().isEmpty()) return;
            if (Dialogs.confirm(this, "Remover todos os itens do pedido?")) {
                pedidoAtual.getItens().clear();
                itensModel.fireTableDataChanged();
                atualizarTotal();
                atualizarEstadoBotoes();
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        south.setOpaque(false);
        south.add(btnLimpar);
        south.add(btnRemover);

        wrap.add(south, BorderLayout.SOUTH);

        return wrap;
    }

    private JComponent buildFooter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        lblTotal.setFont(lblTotal.getFont().deriveFont(Font.BOLD, 16f));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        left.setOpaque(false);
        left.add(lblTotal);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        right.setOpaque(false);

        right.add(new JLabel("Pagamento:"));
        right.add(cmbPagamento);

        btnFechar.addActionListener(e -> fecharPedido());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> {
            if (Dialogs.confirm(this, "Cancelar o pedido atual?")) novoPedido();
        });

        JButton btnIrLista = new JButton("Ir para Lista");
        btnIrLista.addActionListener(e -> onIrLista.run());

        right.add(btnIrLista);
        right.add(btnCancelar);
        right.add(btnFechar);

        p.add(left, BorderLayout.WEST);
        p.add(right, BorderLayout.EAST);
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

    private void adicionarItem() {
        Produto pr = (Produto) cmbProdutos.getSelectedItem();
        if (pr == null) {
            Dialogs.error(this, "Cadastre produtos ativos antes de criar pedidos.");
            return;
        }
        int qtd = (Integer) spQuantidade.getValue();
        if (qtd < 1) {
            Dialogs.error(this, "Quantidade inválida.");
            return;
        }

        // Se já existe item do mesmo produto, soma quantidade
        for (ItemPedido it : pedidoAtual.getItens()) {
            if (it.getProduto().getId().equals(pr.getId())) {
                it.setQuantidade(it.getQuantidade() + qtd);
                itensModel.fireTableDataChanged();
                atualizarTotal();
                atualizarEstadoBotoes();
                spQuantidade.setValue(1);
                return;
            }
        }

        pedidoAtual.getItens().add(new ItemPedido(pr, qtd, pr.getPreco()));
        itensModel.fireTableDataChanged();
        atualizarTotal();
        atualizarEstadoBotoes();
        spQuantidade.setValue(1);
    }

    private void removerItem() {
        int row = tblItens.getSelectedRow();
        if (row < 0) {
            Dialogs.info(this, "Selecione um item para remover.");
            return;
        }
        ItemPedido it = itensModel.get(row);
        if (Dialogs.confirm(this, "Remover " + it.getProduto().getNome() + " do pedido?")) {
            pedidoAtual.getItens().remove(it);
            itensModel.fireTableDataChanged();
            atualizarTotal();
            atualizarEstadoBotoes();
        }
    }

    private void fecharPedido() {
        try {
            FormaPagamento fp = (FormaPagamento) cmbPagamento.getSelectedItem();
            pedidoService.fecharPedido(pedidoAtual, fp);
            Dialogs.info(this, "Pedido fechado! Total: " + Formatters.money(pedidoAtual.getTotal()));
            novoPedido();
            onIrLista.run();
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage());
        }
    }

    private void atualizarEstadoBotoes() {
        btnFechar.setEnabled(!pedidoAtual.getItens().isEmpty());
    }

    private void atualizarTotal() {
        lblTotal.setText("Total: " + Formatters.money(pedidoAtual.getTotal()));
    }

    static class ItensTableModel extends AbstractTableModel {
        private final String[] cols = {"Produto", "Qtd", "Preço Unit.", "Subtotal"};
        private List<ItemPedido> data = new ArrayList<>();

        public void setData(List<ItemPedido> itens) {
            this.data = (itens == null) ? new ArrayList<>() : itens;
            fireTableDataChanged();
        }

        public ItemPedido get(int row) {
            return data.get(row);
        }

        @Override public int getRowCount() { return data.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int column) { return cols[column]; }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            // Permite editar apenas a quantidade
            return columnIndex == 1;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            if (columnIndex != 1) return;
            ItemPedido it = data.get(rowIndex);
            try {
                int qtd;
                if (aValue instanceof Number) qtd = ((Number) aValue).intValue();
                else qtd = Integer.parseInt(String.valueOf(aValue));

                if (qtd < 1) return;
                it.setQuantidade(qtd);
                fireTableRowsUpdated(rowIndex, rowIndex);
            } catch (Exception ignored) {
                // Se inválido, apenas ignora
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ItemPedido it = data.get(rowIndex);
            BigDecimal subtotal = it.getSubtotal();
            return switch (columnIndex) {
                case 0 -> it.getProduto().getNome();
                case 1 -> it.getQuantidade();
                case 2 -> Formatters.money(it.getPrecoUnitario());
                case 3 -> Formatters.money(subtotal);
                default -> "";
            };
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return switch (columnIndex) {
                case 1 -> Integer.class;
                default -> String.class;
            };
        }
    }
}
