
package com.barraca.view;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);

    private ProdutoListPanel produtosLista;
    private ProdutoFormPanel produtosCad;
    private PedidoCreatePanel pedidoCriar;
    private PedidoListPanel pedidoLista;

    public MainFrame() {
        setTitle("Sistema Barraca - Gerenciamento de Pedidos");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1250, 720);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildSideMenu(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);

        cards.show(content, "PRODUTOS_LISTA");
    }

    private JComponent buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(UiColors.PRIMARY);
        top.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel title = new JLabel("Sistema Barraca");
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        top.add(title, BorderLayout.WEST);

        JLabel sub = new JLabel("Gerenciamento de Produtos e Pedidos");
        sub.setForeground(new Color(255,255,255,200));
        top.add(sub, BorderLayout.EAST);

        return top;
    }

    private JComponent buildSideMenu() {
        JPanel side = new JPanel();
        side.setPreferredSize(new Dimension(220, 0));
        side.setBackground(Color.WHITE);
        side.setBorder(BorderFactory.createMatteBorder(0,0,0,1, new Color(220,220,220)));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));

        side.add(Box.createVerticalStrut(14));
        side.add(menuButton("Produtos (Lista)", () -> {
            produtosLista.refresh();
            cards.show(content, "PRODUTOS_LISTA");
        }));
        side.add(menuButton("Produtos (Cadastro)", () -> {
            produtosCad.editar(null);
            cards.show(content, "PRODUTOS_CAD");
        }));
        side.add(Box.createVerticalStrut(8));
        side.add(menuButton("Criar Pedido", () -> {
            pedidoCriar.novoPedido();
            cards.show(content, "PEDIDO_CRIAR");
        }));
        side.add(menuButton("Lista de Pedidos", () -> {
            pedidoLista.refresh();
            cards.show(content, "PEDIDO_LISTA");
        }));
        side.add(Box.createVerticalStrut(8));
        side.add(menuButton("Relatórios (futuro)", () -> cards.show(content, "RELATORIO")));
        side.add(Box.createVerticalGlue());
        side.add(menuButton("Sair", () -> System.exit(0)));

        return side;
    }

    private JButton menuButton(String text, Runnable action) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(200, 40));
        b.setFocusPainted(false);
        b.addActionListener(e -> action.run());
        return b;
    }

    private JComponent buildContent() {
        content.setBackground(UiColors.BG);

        produtosLista = new ProdutoListPanel(this::goCadastroProduto);
        produtosCad = new ProdutoFormPanel(() -> {
            produtosLista.refresh();
            cards.show(content, "PRODUTOS_LISTA");
        });

        pedidoCriar = new PedidoCreatePanel(() -> {
            pedidoLista.refresh();
            cards.show(content, "PEDIDO_LISTA");
        });
        pedidoLista = new PedidoListPanel();

        RelatorioPanel rel = new RelatorioPanel();

        content.add(produtosLista, "PRODUTOS_LISTA");
        content.add(produtosCad, "PRODUTOS_CAD");
        content.add(pedidoCriar, "PEDIDO_CRIAR");
        content.add(pedidoLista, "PEDIDO_LISTA");
        content.add(rel, "RELATORIO");

        return content;
    }

    private void goCadastroProduto(Integer produtoId) {
        produtosCad.editar(produtoId);
        cards.show(content, "PRODUTOS_CAD");
    }
}
