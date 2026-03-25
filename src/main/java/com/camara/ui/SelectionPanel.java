package com.camara.ui;

import com.camara.data.DataManager;
import com.camara.data.MicrophoneService;
import com.camara.data.PdfService;
import com.camara.model.Law;
import com.camara.model.SessionConfig;
import com.camara.model.Vereador;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectionPanel extends JPanel {
    private MainFrame mainFrame;
    private DataManager dataManager;
    private MicrophoneService micService;
    private JPanel gridPanel;
    private ButtonGroup timeGroup;
    private Map<Integer, JPanel> micStatusIndicators;
    private Map<Integer, Boolean> micStates; // Track mic states for toggling
    private JList<Law> lawsList;
    private DefaultListModel<Law> lawsModel;

    // Monitor Panel Components
    private JPanel monitorPanel;
    private JLabel monitorNameLabel;
    private JLabel monitorPartyLabel;
    private JLabel monitorTimeLabel;
    private JButton closeAparteBtn;
    private JButton closeSpeakerBtn;
    private boolean aparteActive = false;

    public SelectionPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.dataManager = new DataManager();
        this.micService = new MicrophoneService();
        this.micStatusIndicators = new HashMap<>();
        this.micStates = new HashMap<>();
        setLayout(new BorderLayout());

        // We'll use the monitor window from main frame
        // Wait, selectionPanel is created BEFORE monitorWindow in MainFrame.
        // I should probably set it after.
        // Actually, I can just use mainFrame.getMonitorWindow() when needed.

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Theme.BACKGROUND_DARK);
        JButton backButton = new JButton("Voltar");
        backButton.addActionListener(e -> mainFrame.showCard("Welcome"));
        headerPanel.add(backButton);

        JLabel headerLabel = new JLabel("Selecione o Vereador e o Tempo de Fala");
        headerLabel.setFont(Theme.FONT_SUBTITLE);
        headerLabel.setForeground(Theme.TEXT_PRIMARY);
        headerPanel.add(headerLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Center: Grid of Vereadores
        gridPanel = new JPanel(new GridLayout(0, 4, 15, 15)); // 4 columns, auto rows
        gridPanel.setBackground(Theme.BACKGROUND_DARK);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JScrollPane scrollPane = new JScrollPane(gridPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(Theme.BACKGROUND_DARK);
        add(scrollPane, BorderLayout.CENTER);

        // West: Laws List
        JPanel westPanel = new JPanel(new BorderLayout());
        westPanel.setBackground(Theme.BACKGROUND_DARK);
        westPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_COLOR),
                "ORDEM DO DIA", 0, 0, Theme.FONT_NORMAL, Theme.TEXT_PRIMARY));
        
        lawsModel = new DefaultListModel<>();
        lawsList = new JList<>(lawsModel);
        lawsList.setBackground(Theme.BACKGROUND_CARD);
        lawsList.setForeground(Theme.TEXT_PRIMARY);
        lawsList.setFont(new Font("Arial", Font.BOLD, 20));
        lawsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lawsList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (c instanceof JLabel) {
                    ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
                    ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
                }
                if (isSelected) {
                    c.setBackground(Theme.ACCENT_COLOR);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(Theme.BACKGROUND_CARD);
                    c.setForeground(Theme.TEXT_PRIMARY);
                }
                return c;
            }
        });
        
        JScrollPane lawsScroll = new JScrollPane(lawsList);
        lawsScroll.setPreferredSize(new Dimension(200, 0));
        lawsScroll.setBorder(null);
        westPanel.add(lawsScroll, BorderLayout.CENTER);
        
        add(westPanel, BorderLayout.WEST);

        // East: Monitor Panel
        monitorPanel = new JPanel();
        monitorPanel.setLayout(new BoxLayout(monitorPanel, BoxLayout.Y_AXIS));
        monitorPanel.setBackground(Theme.BACKGROUND_CARD);
        monitorPanel.setPreferredSize(new Dimension(300, 0));
        monitorPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.ACCENT_COLOR),
                "MONITORAMENTO", 0, 0, Theme.FONT_NORMAL, Theme.TEXT_PRIMARY));

        monitorNameLabel = new JLabel("---");
        monitorNameLabel.setFont(Theme.FONT_CARD_NAME);
        monitorNameLabel.setForeground(Theme.TEXT_PRIMARY);
        monitorNameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        monitorPartyLabel = new JLabel("---");
        monitorPartyLabel.setFont(Theme.FONT_CARD_PARTY);
        monitorPartyLabel.setForeground(Theme.TEXT_SECONDARY);
        monitorPartyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        monitorTimeLabel = new JLabel("00:00");
        monitorTimeLabel.setFont(new Font("Arial", Font.BOLD, 48));
        monitorTimeLabel.setForeground(Theme.ACCENT_COLOR);
        monitorTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        closeAparteBtn = new JButton("FECHAR APARTE");
        closeAparteBtn.setFont(new Font("Arial", Font.BOLD, 16));
        closeAparteBtn.setBackground(Theme.WARNING_COLOR);
        closeAparteBtn.setForeground(Color.BLACK);
        closeAparteBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeAparteBtn.setVisible(false);
        closeAparteBtn.addActionListener(e -> {
            MonitorWindow mw = mainFrame.getMonitorWindow();
            if (mw != null && mw.isVisible()) {
                mw.closeAparte();
            }
        });

        closeSpeakerBtn = new JButton("FECHAR JANELA");
        closeSpeakerBtn.setFont(new Font("Arial", Font.BOLD, 16));
        closeSpeakerBtn.setBackground(Theme.WARNING_COLOR);
        closeSpeakerBtn.setForeground(Color.BLACK);
        closeSpeakerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        closeSpeakerBtn.setVisible(false);
        closeSpeakerBtn.addActionListener(e -> {
            MonitorWindow mw = mainFrame.getMonitorWindow();
            if (mw != null) {
                mw.showCrest();
                updateMonitor("---", "---", "00:00");
                setAparteActive(false);
                closeSpeakerBtn.setVisible(false);
            }
        });

        monitorPanel.add(Box.createVerticalGlue());
        monitorPanel.add(monitorNameLabel);
        monitorPanel.add(Box.createVerticalStrut(10));
        monitorPanel.add(monitorPartyLabel);
        monitorPanel.add(Box.createVerticalStrut(30));
        monitorPanel.add(monitorTimeLabel);
        monitorPanel.add(Box.createVerticalStrut(20));
        monitorPanel.add(closeAparteBtn);
        monitorPanel.add(Box.createVerticalStrut(10));
        monitorPanel.add(closeSpeakerBtn);
        monitorPanel.add(Box.createVerticalGlue());

        add(monitorPanel, BorderLayout.EAST);

        // Bottom: Time Selection & Controls
        JPanel bottomPanel = new JPanel(new GridBagLayout());
        bottomPanel.setBackground(Theme.BACKGROUND_DARK);
        bottomPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.TEXT_SECONDARY),
                "Controles e Tempo", 0, 0, Theme.FONT_NORMAL, Theme.TEXT_PRIMARY));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Left: Radio Buttons
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        radioPanel.setOpaque(false);
        
        timeGroup = new ButtonGroup();
        String[] times = {"1", "3", "5", "7", "10"};
        String[] labels = {"1 Min", "3 Min", "5 Min", "7 Min", "10 Min"};
        
        for (int i = 0; i < times.length; i++) {
             JRadioButton rb = new JRadioButton(labels[i]);
             rb.setActionCommand(times[i]);
             rb.setFont(new Font("Arial", Font.BOLD, 14));
             rb.setForeground(Theme.TEXT_PRIMARY);
             rb.setOpaque(false);
            if (times[i].equals("10")) rb.setSelected(true);
            timeGroup.add(rb);
            radioPanel.add(rb);
        }

        gbc.gridx = 0;
        gbc.weightx = 0.3;
        bottomPanel.add(radioPanel, gbc);

        // Center: Macros
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        centerPanel.setOpaque(false);

        JButton connectBtn = createStyledButton("CONECTAR", new Color(150, 150, 255), 90);
        connectBtn.addActionListener(e -> {
            new Thread(() -> {
                boolean success = micService.connect();
                SwingUtilities.invokeLater(() -> {
                    if(success) {
                        JOptionPane.showMessageDialog(SelectionPanel.this, "Controladora Conectada!");
                    } else {
                        JOptionPane.showMessageDialog(SelectionPanel.this, "Falha ao conectar!");
                    }
                });
            }).start();
        });

        JButton disconnectBtn = createStyledButton("DESCONECTAR", new Color(200, 200, 200), 100);
        disconnectBtn.addActionListener(e -> {
            micService.releaseAndDisconnect();
            JOptionPane.showMessageDialog(SelectionPanel.this, "Controladora Desconectada.");
        });

        JButton openAllBtn = createStyledButton("ABRIR TODOS", new Color(150, 255, 150), 110);
        openAllBtn.addActionListener(e -> {
            new Thread(() -> {
                List<Vereador> vereadores = dataManager.loadVereadores();
                for (Vereador v : vereadores) {
                    boolean success = micService.activateMicrophone(v.getMicrofoneId());
                    SwingUtilities.invokeLater(() -> updateMicIndicator(v.getMicrofoneId(), success));
                    try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                }
            }).start();
        });

        JButton closeAllBtn = createStyledButton("FECHAR TODOS", new Color(255, 150, 150), 110);
        closeAllBtn.addActionListener(e -> {
            new Thread(() -> {
                List<Vereador> vereadores = dataManager.loadVereadores();
                for (Vereador v : vereadores) {
                    micService.deactivateMicrophone(v.getMicrofoneId());
                    SwingUtilities.invokeLater(() -> updateMicIndicator(v.getMicrofoneId(), false));
                    try { Thread.sleep(50); } catch (InterruptedException ignored) {}
                }
            }).start();
        });

        centerPanel.add(connectBtn);
        centerPanel.add(disconnectBtn);
        centerPanel.add(Box.createHorizontalStrut(5));
        centerPanel.add(openAllBtn);
        centerPanel.add(closeAllBtn);

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        bottomPanel.add(centerPanel, gbc);

        // Right: Monitor Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton addMinuteBtn = createStyledButton("+1 Minuto", Theme.ACCENT_COLOR, 110);
        addMinuteBtn.addActionListener(e -> {
            MonitorWindow mw = mainFrame.getMonitorWindow();
            if (mw != null && mw.isVisible()) mw.addExtraMinute();
        });

        JButton closeMonitorBtn = createStyledButton("FECHAR MONITOR", Theme.WARNING_COLOR, 150);
        closeMonitorBtn.addActionListener(e -> {
            MonitorWindow mw = mainFrame.getMonitorWindow();
            if (mw != null) mw.setVisible(false);
        });

        actionPanel.add(addMinuteBtn);
        actionPanel.add(closeMonitorBtn);

        gbc.gridx = 2;
        gbc.weightx = 0.3;
        bottomPanel.add(actionPanel, gbc);

        add(bottomPanel, BorderLayout.SOUTH);

        // Load data when shown (Need a way to refresh, simpler to just reload always or
        // expose refresh)
        refreshData();
    }

    private JButton createStyledButton(String text, Color bg, int width) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 11));
        btn.setPreferredSize(new Dimension(width, 35));
        btn.setBackground(bg);
        btn.setForeground(Color.BLACK);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setBorder(BorderFactory.createLineBorder(bg.darker()));
        return btn;
    }

    public void refreshData() {
        gridPanel.removeAll();
        micStatusIndicators.clear();
        micStates.clear();
        List<Vereador> vereadores = dataManager.loadVereadores();
        SessionConfig config = dataManager.loadConfig();

        PdfService pdfService = new PdfService();
        lawsModel.clear();
        
        // Add default "PALAVRA LIVRE"
        lawsModel.addElement(new Law("00", "PALAVRA LIVRE", "Palavra Livre"));

        if (config.getPautaPath() != null && !config.getPautaPath().isEmpty()) {
            List<Law> laws = pdfService.extractLawsFromPdf(config.getPautaPath());
            for (Law law : laws) {
                lawsModel.addElement(law);
            }
        }

        for (Vereador v : vereadores) {
            JPanel card = new JPanel();
            card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
            card.setBorder(BorderFactory.createLineBorder(Theme.ACCENT_COLOR, 2));
            card.setBackground(Theme.BACKGROUND_CARD);
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel nameLabel = new JLabel(v.getName());
            nameLabel.setFont(Theme.FONT_CARD_NAME);
            nameLabel.setForeground(Theme.TEXT_PRIMARY);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel partyLabel = new JLabel(v.getParty());
            partyLabel.setFont(Theme.FONT_CARD_PARTY);
            partyLabel.setForeground(Theme.TEXT_SECONDARY);
            partyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            partyLabel.setHorizontalAlignment(SwingConstants.CENTER);

            card.add(Box.createVerticalGlue());
            card.add(nameLabel);
            card.add(Box.createVerticalStrut(10));
            card.add(partyLabel);

            // Status Indicator Dot
            int micId = v.getMicrofoneId();
            JPanel statusDot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Default to Red (closed)
                    Color color = (Color) getClientProperty("micColor");
                    if (color == null)
                        color = new Color(255, 0, 0);

                    g2d.setColor(color);
                    g2d.fillOval(5, 5, 15, 15);
                }
            };
            statusDot.setOpaque(false);
            statusDot.setPreferredSize(new Dimension(25, 25));
            statusDot.setMaximumSize(new Dimension(25, 25));
            statusDot.setAlignmentX(Component.CENTER_ALIGNMENT);
            micStatusIndicators.put(micId, statusDot);

            card.add(Box.createVerticalStrut(5));
            card.add(statusDot);

            // Two separate buttons: ABRIR and FECHAR (manual mic control per vereador)
            JButton micOpenBtn = new JButton("ABRIR");
            micOpenBtn.setFont(new Font("Arial", Font.BOLD, 12));
            micOpenBtn.setBackground(new Color(30, 150, 30));
            micOpenBtn.setForeground(Color.WHITE);
            micOpenBtn.setFocusPainted(false);
            micOpenBtn.setOpaque(true);
            micOpenBtn.setBorderPainted(false);
            micOpenBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            micOpenBtn.setPreferredSize(new Dimension(75, 30));

            JButton micCloseBtn = new JButton("FECHAR");
            micCloseBtn.setFont(new Font("Arial", Font.BOLD, 12));
            micCloseBtn.setBackground(new Color(180, 30, 30));
            micCloseBtn.setForeground(Color.WHITE);
            micCloseBtn.setFocusPainted(false);
            micCloseBtn.setOpaque(true);
            micCloseBtn.setBorderPainted(false);
            micCloseBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            micCloseBtn.setPreferredSize(new Dimension(75, 30));

            micOpenBtn.addActionListener(e -> new Thread(() -> {
                boolean success = micService.activateMicrophone(micId);
                SwingUtilities.invokeLater(() -> updateMicIndicator(micId, success));
            }).start());

            micCloseBtn.addActionListener(e -> new Thread(() -> {
                boolean success = micService.deactivateMicrophone(micId);
                SwingUtilities.invokeLater(() -> updateMicIndicator(micId, !success ? micStates.getOrDefault(micId, false) : false));
            }).start());

            // Store references so updateMicIndicator can sync button appearance
            statusDot.putClientProperty("micOpenBtn", micOpenBtn);
            statusDot.putClientProperty("micCloseBtn", micCloseBtn);

            JPanel micBtnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            micBtnPanel.setOpaque(false);
            micBtnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            micBtnPanel.add(micOpenBtn);
            micBtnPanel.add(micCloseBtn);

            card.add(Box.createVerticalStrut(5));
            card.add(micBtnPanel);
            card.add(Box.createVerticalGlue());

            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    // Only trigger if click is NOT on the toggle button
                    if (e.getSource() == card) {
                        if ("TRIBUNA".equalsIgnoreCase(v.getName())) {
                            toggleTribunaMic(v);
                        } else {
                            openSpeakerWindow(v);
                        }
                    }
                }
            });

            gridPanel.add(card);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    public MicrophoneService getMicService() {
        return micService;
    }

    public void updateMonitor(String name, String party, String time) {
        monitorNameLabel.setText(name);
        monitorPartyLabel.setText(party);
        monitorTimeLabel.setText(time);

        if (!"---".equals(name)) {
            if (aparteActive) {
                closeSpeakerBtn.setVisible(false);
                closeAparteBtn.setVisible(true);
            } else {
                closeSpeakerBtn.setVisible(true);
                closeAparteBtn.setVisible(false);
            }
        } else {
            closeSpeakerBtn.setVisible(false);
            closeAparteBtn.setVisible(false);
        }

        monitorPanel.revalidate();
        monitorPanel.repaint();
    }

    public void setAparteActive(boolean active) {
        this.aparteActive = active;
        if (active) {
            closeSpeakerBtn.setVisible(false);
            closeAparteBtn.setVisible(true);
        } else {
            closeSpeakerBtn.setVisible(true);
            closeAparteBtn.setVisible(false);
        }
        monitorPanel.revalidate();
        monitorPanel.repaint();
    }

    public void updateMicIndicator(int micId, boolean active) {
        micStates.put(micId, active);
        JPanel indicator = micStatusIndicators.get(micId);
        if (indicator != null) {
            indicator.putClientProperty("micColor", active ? new Color(0, 255, 0) : new Color(255, 0, 0));
            indicator.repaint();

            // Highlight the active button (ABRIR=green bright, FECHAR=red bright)
            Object openObj = indicator.getClientProperty("micOpenBtn");
            Object closeObj = indicator.getClientProperty("micCloseBtn");
            if (openObj instanceof JButton && closeObj instanceof JButton) {
                JButton openBtn = (JButton) openObj;
                JButton closeBtn = (JButton) closeObj;
                if (active) {
                    openBtn.setBackground(new Color(0, 200, 0));   // bright green = ativo
                    closeBtn.setBackground(new Color(120, 20, 20)); // dim red
                } else {
                    openBtn.setBackground(new Color(20, 100, 20));  // dim green
                    closeBtn.setBackground(new Color(220, 30, 30)); // bright red = ativo
                }
            }
        }
    }

    private void toggleTribunaMic(Vereador v) {
        int micId = v.getMicrofoneId();
        boolean currentStatus = micStates.getOrDefault(micId, false);
        boolean newStatus = !currentStatus;

        if (newStatus) {
            boolean success = micService.activateMicrophone(micId);
            if (success) updateMicIndicator(micId, true);
        } else {
            boolean success = micService.deactivateMicrophone(micId);
            if (success) updateMicIndicator(micId, false);
        }
    }

    private void openSpeakerWindow(Vereador vereador) {
        String cmd = timeGroup.getSelection().getActionCommand();
        int minutes = Integer.parseInt(cmd);

        // Try to activate microphone
        boolean micSuccess = micService.activateMicrophone(vereador.getMicrofoneId());

        // Update indicator color
        updateMicIndicator(vereador.getMicrofoneId(), micSuccess);

        SwingUtilities.invokeLater(() -> {
            MonitorWindow mw = mainFrame.getMonitorWindow();
            if (mw != null) {
                Law selectedLaw = lawsList.getSelectedValue();
                if (mw.isVisible() && !mw.isShowingCrest()) {
                    mw.setAparte(vereador, minutes, selectedLaw);
                } else {
                    mw.showSpeaker(vereador, minutes, selectedLaw);
                    mw.setVisible(true);
                }
                mw.toFront();
            }
        });
    }
}
