package com.camara.ui;

import javax.swing.*;
import java.awt.*;

public class WelcomePanel extends JPanel {
    private MainFrame mainFrame;
    private CrestPanel crestPanel;

    public WelcomePanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_DARK);

        crestPanel = new CrestPanel();
        add(crestPanel, BorderLayout.CENTER);

        // Footer (Buttons)
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(Theme.BACKGROUND_DARK);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton startButton = new JButton("Iniciar Sessão");
        startButton.setFont(new Font("Arial", Font.BOLD, 18));
        startButton.setPreferredSize(new Dimension(240, 45));
        startButton.addActionListener(e -> {
            mainFrame.getMonitorWindow().showCrest();
            mainFrame.getMonitorWindow().setVisible(true);
            mainFrame.showCard("Selection");
        });

        JButton configButton = new JButton("Configurações / Cadastros");
        configButton.setFont(new Font("Arial", Font.BOLD, 18));
        configButton.setPreferredSize(new Dimension(240, 45));
        configButton.addActionListener(e -> openRegistration());

        footerPanel.add(startButton);
        footerPanel.add(configButton);

        add(footerPanel, BorderLayout.SOUTH);
    }

    private void loadBrasao() {
        crestPanel.loadBrasao();
    }

    private void openRegistration() {
        new RegistrationDialog(mainFrame, this::loadBrasao).setVisible(true);
    }
}
