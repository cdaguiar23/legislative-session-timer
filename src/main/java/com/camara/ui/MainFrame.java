package com.camara.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private JPanel cardPanel;
    private CardLayout cardLayout;
    private SelectionPanel selectionPanel;
    private MonitorWindow monitorWindow;

    public MainFrame() {
        setTitle("Câmara de Vereadores de Canelinha");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (selectionPanel != null) {
                    selectionPanel.getMicService().releaseAndDisconnect();
                }
                System.exit(0);
            }
        });
        setSize(1024, 768);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BACKGROUND_DARK);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(Theme.BACKGROUND_DARK);

        selectionPanel = new SelectionPanel(this);
        monitorWindow = new MonitorWindow(selectionPanel);

        cardPanel.add(new WelcomePanel(this), "Welcome");
        cardPanel.add(selectionPanel, "Selection");

        add(cardPanel);
    }

    public MonitorWindow getMonitorWindow() {
        return monitorWindow;
    }

    public void showCard(String cardName) {
        if ("Selection".equals(cardName)) {
            selectionPanel.refreshData();
        }
        cardLayout.show(cardPanel, cardName);
    }

    public JPanel getCardPanel() {
        return cardPanel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new MainFrame().setVisible(true);
        });
    }
}
