
package com.barraca.view;

import com.barraca.model.Produto;
import com.barraca.service.ProdutoService;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ProdutoFormPanel extends JPanel {

    private final ProdutoService service = new ProdutoService();
    private final Runnable onVoltar;

    private Integer editId = null;

    private final JTextField txtNome = new JTextField();
    private final JTextArea txtDescricao = new JTextArea(4, 20);
    private final JTextField txtPreco = new JTextField();
    private final JCheckBox chkAtivo = new JCheckBox("Ativo");

    public ProdutoFormPanel(Runnable onVoltar) {
        this.onVoltar = onVoltar;

        setLayout(new BorderLayout());
        setBackground(UiColors.BG);
        setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildForm(), BorderLayout.CENTER);
        add(buildActions(), BorderLayout.SOUTH);

        limpar();
    }

    private JComponent buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);

        JLabel title = new JLabel("Cadastro de Produtos");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(UiColors.TEXT);

        p.add(title, BorderLayout.WEST);
        return p;
    }

    private JComponent buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        form.add(new JLabel("Nome do Produto:"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1;
        form.add(txtNome, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        form.add(new JLabel("Preço (ex.: 12.50):"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1;
        form.add(txtPreco, gbc);

        y++;
        gbc.gridx = 0; gbc.gridy = y; gbc.weightx = 0;
        form.add(new JLabel("Descrição:"), gbc);
        gbc.gridx = 1; gbc.gridy = y; gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane sp = new JScrollPane(txtDescricao);
        sp.setBorder(BorderFactory.createLineBorder(new Color(220,220,220)));
        form.add(sp, gbc);

        y++;
        gbc.gridx = 1; gbc.gridy = y;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        chkAtivo.setOpaque(false);
        form.add(chkAtivo, gbc);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(form, BorderLayout.NORTH);
        return wrap;
    }

    private JComponent buildActions() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        p.setOpaque(false);

        JButton btnSalvar = primaryButton("Salvar");
        btnSalvar.addActionListener(e -> salvar());

        JButton btnLimpar = new JButton("Limpar");
        btnLimpar.addActionListener(e -> limpar());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> onVoltar.run());

        p.add(btnCancelar);
        p.add(btnLimpar);
        p.add(btnSalvar);

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

    public void editar(Integer produtoId) {
        this.editId = produtoId;
        if (produtoId == null) {
            limpar();
            return;
        }

        try {
            List<Produto> all = service.listar(true);
            Produto p = all.stream().filter(x -> produtoId.equals(x.getId())).findFirst().orElse(null);
            if (p == null) {
                Dialogs.error(this, "Produto não encontrado.");
                limpar();
                return;
            }
            txtNome.setText(p.getNome());
            txtDescricao.setText(p.getDescricao() == null ? "" : p.getDescricao());
            txtPreco.setText(p.getPreco() == null ? "" : p.getPreco().toString());
            chkAtivo.setSelected(p.isAtivo());
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage());
        }
    }

    private void limpar() {
        txtNome.setText("");
        txtDescricao.setText("");
        txtPreco.setText("");
        chkAtivo.setSelected(true);
    }

    private void salvar() {
        try {
            Produto p = new Produto();
            p.setId(editId);
            p.setNome(txtNome.getText());
            p.setDescricao(txtDescricao.getText());
            p.setPreco(parsePreco(txtPreco.getText()));
            p.setAtivo(chkAtivo.isSelected());

            service.salvar(p);

            Dialogs.info(this, "Produto salvo com sucesso.");
            onVoltar.run();
        } catch (Exception ex) {
            Dialogs.error(this, ex.getMessage());
        }
    }

    private BigDecimal parsePreco(String s) {
        if (s == null) return null;
        s = s.trim().replace(",", ".");
        if (s.isEmpty()) return null;
        return new BigDecimal(s);
    }
}
