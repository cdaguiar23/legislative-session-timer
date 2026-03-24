package com.camara.ui;

import com.camara.data.DataManager;
import com.camara.model.SessionConfig;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class CrestPanel extends JPanel {
    private JLabel brasaoLabel;
    private JLabel footerLabel;
    private DataManager dataManager;
    private java.awt.image.BufferedImage originalImage;
    private Timer clockTimer;

    public CrestPanel() {
        this.dataManager = new DataManager();
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND_DARK);

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Theme.BACKGROUND_DARK);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("Câmara de Vereadores de Canelinha".toUpperCase());
        titleLabel.setFont(Theme.FONT_TITLE);
        titleLabel.setForeground(Theme.TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String dateFull = java.time.LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'DE' MMMM 'DE' yyyy", new Locale("pt", "BR"))).toUpperCase();
        JLabel sessionLabel = new JLabel("SESSÃO ORDINÁRIA DE " + dateFull);
        sessionLabel.setFont(Theme.FONT_TITLE);
        sessionLabel.setForeground(Theme.TEXT_PRIMARY);
        sessionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(25));
        headerPanel.add(sessionLabel);

        add(headerPanel, BorderLayout.NORTH);

        // Center (Brasão only)
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Theme.BACKGROUND_DARK);

        brasaoLabel = new JLabel();
        brasaoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        brasaoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        brasaoLabel.setForeground(Theme.TEXT_SECONDARY);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(brasaoLabel);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);

        // Footer (Centered Date and Time)
        JPanel footerPanel = new JPanel();
        footerPanel.setLayout(new BorderLayout());
        footerPanel.setBackground(Theme.BACKGROUND_DARK);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 30, 20));

        footerLabel = new JLabel();
        footerLabel.setFont(new Font("Arial", Font.BOLD, 36));
        footerLabel.setForeground(Theme.TEXT_PRIMARY);
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        updateDateTime();

        footerPanel.add(footerLabel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);

        // Clock timer - update every second
        clockTimer = new Timer(1000, e -> updateDateTime());
        clockTimer.start();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                updateBrasaoScale();
            }
        });

        loadBrasao();
    }

    private void updateDateTime() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        String datePart = now
                .format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR")));
        String timePart = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        footerLabel.setText(datePart + "  |  " + timePart);
    }

    public void loadBrasao() {
        SessionConfig config = dataManager.loadConfig();
        String path = config.getBrasaoPath();
        if (path != null && !path.isEmpty()) {
            try {
                java.io.File file = new java.io.File(path);
                if (file.exists()) {
                    originalImage = javax.imageio.ImageIO.read(file);
                    updateBrasaoScale();
                } else {
                    brasaoLabel.setIcon(null);
                    brasaoLabel.setText("Arquivo do Brasão não encontrado.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                brasaoLabel.setIcon(null);
                brasaoLabel.setText("Erro ao carregar o Brasão.");
            }
        } else {
            brasaoLabel.setIcon(null);
            brasaoLabel.setText("Nenhum Brasão configurado.");
        }
    }

    private void updateBrasaoScale() {
        if (originalImage == null)
            return;

        int panelHeight = getHeight();
        if (panelHeight <= 0)
            panelHeight = 768; // Default if not yet visible

        // Scale to 55% of panel height now that footer is separated
        int targetHeight = (int) (panelHeight * 0.55);
        if (targetHeight < 100)
            targetHeight = 100;

        Image scaledImg = originalImage.getScaledInstance(-1, targetHeight, Image.SCALE_SMOOTH);
        brasaoLabel.setIcon(new ImageIcon(scaledImg));
        brasaoLabel.setText("");
        revalidate();
        repaint();
    }
}
