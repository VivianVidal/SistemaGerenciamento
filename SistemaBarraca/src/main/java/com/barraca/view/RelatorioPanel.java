
package com.barraca.view;

import javax.swing.*;
import java.awt.*;

public class RelatorioPanel extends JPanel {

    public RelatorioPanel() {
        setLayout(new BorderLayout());
        setBackground(UiColors.BG);
        setBorder(BorderFactory.createEmptyBorder(16,16,16,16));

        JLabel title = new JLabel("Relatórios (Melhoria futura)");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setForeground(UiColors.TEXT);

        JTextArea info = new JTextArea();
        info.setEditable(false);
        info.setText(
                "Esta tela foi prevista como melhoria futura no wireframe.\n\n" +
                "Sugestões para próxima etapa:\n" +
                "- Cartões com total vendido no período\n" +
                "- Produto mais vendido\n" +
                "- Gráfico de vendas por dia\n"
        );

        add(title, BorderLayout.NORTH);
        add(new JScrollPane(info), BorderLayout.CENTER);
    }
}
