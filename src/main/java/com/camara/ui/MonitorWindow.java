package com.camara.ui;

import com.camara.data.MicrophoneService;
import com.camara.model.Law;
import com.camara.model.Vereador;

import javax.swing.*;
import java.awt.*;

public class MonitorWindow extends JFrame {
    private JPanel mainContainer;
    private SpeakerPanel primarySpeaker;
    private SpeakerPanel secondarySpeaker;
    private SelectionPanel selectionPanel;
    private MicrophoneService micService;
    private CardLayout cardLayout;
    private JPanel cardPanel;
    private CrestPanel crestPanel;
    private String currentCard = "Crest"; // Added field to track current card
    private JLabel lawDisplayLabel;

    public MonitorWindow(SelectionPanel selectionPanel) {
        this.selectionPanel = selectionPanel;
        this.micService = new MicrophoneService();
        setTitle("Câmara de Canelinha - Monitor");
        setSize(1024, 768);

        // Detect multi-monitor setup
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();

        if (gd.length > 1) {
            // Target the second monitor (index 1) handles extended mode
            GraphicsConfiguration gc = gd[1].getDefaultConfiguration();
            Rectangle bounds = gc.getBounds();
            setLocation(bounds.x, bounds.y);
        } else {
            setLocationRelativeTo(null);
        }

        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true); // Remove borders for a cleaner look on the second monitor
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        crestPanel = new CrestPanel();
        cardPanel.add(crestPanel, "Crest");

        JPanel timerCardContainer = new JPanel(new BorderLayout());
        timerCardContainer.setBackground(Theme.BACKGROUND_DARK);

        mainContainer = new JPanel(new GridLayout(1, 1));
        mainContainer.setBackground(Theme.BACKGROUND_DARK);
        timerCardContainer.add(mainContainer, BorderLayout.CENTER);
        
        lawDisplayLabel = new JLabel();
        lawDisplayLabel.setBackground(Theme.BACKGROUND_DARK);
        lawDisplayLabel.setOpaque(true);
        lawDisplayLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lawDisplayLabel.setForeground(Theme.TEXT_PRIMARY);
        lawDisplayLabel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));
        
        timerCardContainer.add(lawDisplayLabel, BorderLayout.SOUTH);

        cardPanel.add(timerCardContainer, "Timer");

        add(cardPanel, BorderLayout.CENTER);

        showCrest();
    }

    public void showCrest() {
        stopAllAndDeactivate();
        crestPanel.loadBrasao();
        cardLayout.show(cardPanel, "Crest");
        currentCard = "Crest"; // Update current card
    }

    public void stopAllAndDeactivate() {
        if (primarySpeaker != null) {
            micService.deactivateMicrophone(primarySpeaker.getVereador().getMicrofoneId());
            primarySpeaker.stopTimer();
            if (selectionPanel != null) {
                selectionPanel.updateMicIndicator(primarySpeaker.getVereador().getMicrofoneId(), false);
            }
        }
        if (secondarySpeaker != null) {
            micService.deactivateMicrophone(secondarySpeaker.getVereador().getMicrofoneId());
            secondarySpeaker.stopTimer();
            if (selectionPanel != null) {
                selectionPanel.updateMicIndicator(secondarySpeaker.getVereador().getMicrofoneId(), false);
            }
        }
    }

    @Override
    public void setVisible(boolean b) {
        if (!b) {
            stopAllAndDeactivate();
        }
        super.setVisible(b);
    }

    public boolean isShowingCrest() {
        return "Crest".equals(getCurrentCardName());
    }

    private String getCurrentCardName() {
        return currentCard;
    }

    public void showSpeaker(Vereador vereador, int minutes, Law law) {
        stopAllAndDeactivate();

        if (law != null) {
            updateLawText(law.getTitle());
        } else {
            lawDisplayLabel.setText("");
        }

        mainContainer.removeAll();
        primarySpeaker = new SpeakerPanel(vereador, minutes, false);
        secondarySpeaker = null;
        mainContainer.setLayout(new GridLayout(1, 1));
        mainContainer.add(primarySpeaker);

        cardLayout.show(cardPanel, "Timer");
        currentCard = "Timer";
        mainContainer.revalidate();
        mainContainer.repaint();
    }

    public void setAparte(Vereador vereador, int minutes, Law law) {
        if (secondarySpeaker != null) {
            mainContainer.remove(secondarySpeaker);
            secondarySpeaker.stopTimer();
        }

        if (primarySpeaker != null) {
            primarySpeaker.setDual(true);
        }

        if (law != null) {
            updateLawText(law.getTitle());
        }

        secondarySpeaker = new SpeakerPanel(vereador, minutes, true);

        mainContainer.setLayout(new GridLayout(1, 2, 20, 0));
        mainContainer.add(secondarySpeaker);
        mainContainer.revalidate();
        mainContainer.repaint();

        if (selectionPanel != null) {
            selectionPanel.setAparteActive(true);
        }
    }

    public void addExtraMinute() {
        if (secondarySpeaker != null) {
            secondarySpeaker.addTime(60);
        } else if (primarySpeaker != null) {
            primarySpeaker.addTime(60);
        }
    }

    public void closeAparte() {
        if (secondarySpeaker != null) {
            micService.deactivateMicrophone(secondarySpeaker.getVereador().getMicrofoneId());
            mainContainer.remove(secondarySpeaker);
            secondarySpeaker.stopTimer();
            secondarySpeaker = null;
            if (primarySpeaker != null) {
                primarySpeaker.setDual(false);
            }
            mainContainer.setLayout(new GridLayout(1, 1));
            mainContainer.revalidate();
            mainContainer.repaint();

            if (selectionPanel != null && primarySpeaker != null) {
                selectionPanel.setAparteActive(false);
                selectionPanel.updateMonitor(primarySpeaker.getVereador().getName(),
                        primarySpeaker.getVereador().getParty(), primarySpeaker.getFormattedRemainingTime());
            }
        }
    }

    private void updateLawText(String text) {
        lawDisplayLabel.setText(text);
        
        // Font scaling logic
        SwingUtilities.invokeLater(() -> {
            int width = lawDisplayLabel.getWidth() - 100; // Account for borders
            if (width <= 0) width = 900; // Fallback
            
            int fontSize = 50; // Starting max font size
            Font font = new Font("Arial", Font.BOLD, fontSize);
            FontMetrics metrics = lawDisplayLabel.getFontMetrics(font);
            
            while (metrics.stringWidth(text) > (width - 40) && fontSize > 10) {
                fontSize--;
                font = new Font("Arial", Font.BOLD, fontSize);
                metrics = lawDisplayLabel.getFontMetrics(font);
            }
            lawDisplayLabel.setFont(font);
            lawDisplayLabel.revalidate();
            lawDisplayLabel.repaint();
        });
    }

    private class SpeakerPanel extends JPanel {
        private CircularTimerPanel timerPanel;
        private Timer timer;
        private int remainingSeconds;
        private int totalInitialSeconds;
        private boolean isDual;
        private JPanel photoContainer;
        private JLabel photoLabel;
        private JLabel nameLabel;
        private JLabel partyLabel;
        private Vereador vereador;

        public SpeakerPanel(Vereador vereador, int minutes, boolean isAparte) {
            this.vereador = vereador;
            this.isDual = isAparte;

            setLayout(new BorderLayout());
            setBackground(Theme.BACKGROUND_DARK);
            setBorder(BorderFactory.createLineBorder(isAparte ? Theme.ACCENT_COLOR : Theme.BACKGROUND_DARK, 2));

            // Photo
            photoContainer = new JPanel(new GridBagLayout());
            photoContainer.setBackground(Theme.BACKGROUND_DARK);
            photoContainer.setPreferredSize(new Dimension(isAparte ? 450 : 650, 0));

            photoLabel = new JLabel();
            photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            updatePhoto();
            photoLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            photoContainer.add(photoLabel);
            
            add(photoContainer, BorderLayout.WEST);

            // Details Panel
            JPanel detailsPanel = new JPanel();
            detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
            detailsPanel.setBackground(Theme.BACKGROUND_DARK);
            detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            nameLabel = new JLabel(vereador.getName());
            nameLabel.setForeground(Theme.TEXT_PRIMARY);
            nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

            partyLabel = new JLabel(vereador.getParty());
            partyLabel.setForeground(Theme.ACCENT_COLOR);
            partyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            partyLabel.setHorizontalAlignment(SwingConstants.CENTER);

            timerPanel = new CircularTimerPanel();
            timerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

            updateFonts();

            detailsPanel.add(Box.createVerticalGlue());
            detailsPanel.add(nameLabel);
            detailsPanel.add(Box.createVerticalStrut(5));
            detailsPanel.add(partyLabel);
            detailsPanel.add(Box.createVerticalStrut(20));
            detailsPanel.add(timerPanel);

            detailsPanel.add(Box.createVerticalGlue());

            add(detailsPanel, BorderLayout.CENTER);

            // Timer Logic
            totalInitialSeconds = minutes * 60;
            remainingSeconds = totalInitialSeconds;
            timer = new Timer(1000, e -> {
                remainingSeconds--;
                timerPanel.updateTime(remainingSeconds, totalInitialSeconds, formatTime(remainingSeconds));
                if (remainingSeconds < 0) {
                    timer.stop();
                    micService.deactivateMicrophone(vereador.getMicrofoneId());
                    if (selectionPanel != null) {
                        selectionPanel.updateMicIndicator(vereador.getMicrofoneId(), false);
                    }
                }

                // Update operator monitor
                if (selectionPanel != null) {
                    Vereador primary = primarySpeaker != null ? primarySpeaker.getVereador() : vereador;
                    if (isDual && secondarySpeaker == this) {
                        selectionPanel.updateMonitor(primary.getName(), primary.getParty(),
                                formatTime(remainingSeconds));
                    } else if (!isDual && primarySpeaker == this) {
                        selectionPanel.updateMonitor(primary.getName(), primary.getParty(),
                                formatTime(remainingSeconds));
                    }
                }
            });
            timerPanel.updateTime(remainingSeconds, totalInitialSeconds, formatTime(remainingSeconds));
            if (selectionPanel != null) {
                Vereador primary = primarySpeaker != null ? primarySpeaker.getVereador() : vereador;
                selectionPanel.updateMonitor(primary.getName(), primary.getParty(), formatTime(remainingSeconds));
            }
            timer.start();
        }

        public void setDual(boolean dual) {
            this.isDual = dual;
            photoContainer.setPreferredSize(new Dimension(isDual ? 450 : 650, 0));
            updatePhoto();
            updateFonts();
            revalidate();
            repaint();
        }

        private void updatePhoto() {
            String path = vereador.getPhotoPath();
            if (path != null && !path.isEmpty()) {
                try {
                    java.io.File file = new java.io.File(path);
                    if (file.exists()) {
                        Image img = javax.imageio.ImageIO.read(file);
                        if (img != null) {
                            int targetWidth = isDual ? 350 : 500;
                            int targetHeight = isDual ? 450 : 650;

                            double scale = Math.min((double) targetWidth / img.getWidth(null),
                                    (double) targetHeight / img.getHeight(null));

                            int width = (int) (img.getWidth(null) * scale);
                            int height = (int) (img.getHeight(null) * scale);

                            img = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                            photoLabel.setIcon(new ImageIcon(img));
                            photoLabel.setText("");
                        } else {
                            photoLabel.setIcon(null);
                            photoLabel.setText("Erro ao ler foto");
                        }
                    } else {
                        photoLabel.setIcon(null);
                        photoLabel.setText("Foto não encontrada");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    photoLabel.setIcon(null);
                    photoLabel.setText("Erro ao carregar foto");
                }
            } else {
                photoLabel.setText("Sem Foto");
                photoLabel.setForeground(Theme.TEXT_SECONDARY);
            }
        }

        private void updateFonts() {
            if (isDual) {
                nameLabel.setFont(Theme.FONT_SUBTITLE);
                partyLabel.setFont(Theme.FONT_NORMAL);
                timerPanel.setPreferredSize(new Dimension(300, 300));
            } else {
                nameLabel.setFont(Theme.FONT_VEREADOR_NAME);
                partyLabel.setFont(Theme.FONT_VEREADOR_PARTY);
                timerPanel.setPreferredSize(new Dimension(450, 450));
            }
        }

        public void addTime(int seconds) {
            remainingSeconds += seconds;
            totalInitialSeconds += seconds;
            timerPanel.updateTime(remainingSeconds, totalInitialSeconds, formatTime(remainingSeconds));
            if (remainingSeconds > 0 && !timer.isRunning()) {
                timer.start();
            }
        }

        private String formatTime(int totalSeconds) {
            int m = Math.max(0, totalSeconds / 60);
            int s = Math.max(0, totalSeconds % 60);
            return String.format("%02d:%02d", m, s);
        }

        public String getFormattedRemainingTime() {
            return formatTime(remainingSeconds);
        }

        public Vereador getVereador() {
            return vereador;
        }

        public void stopTimer() {
            if (timer != null)
                timer.stop();
        }
    }

    public void setSelectionPanel(SelectionPanel selectionPanel) {
        this.selectionPanel = selectionPanel;
    }

    @Override
    public void dispose() {
        stopAllAndDeactivate();
        if (selectionPanel != null) {
            selectionPanel.updateMonitor("---", "---", "00:00");
        }
        super.setVisible(false);
    }
}
