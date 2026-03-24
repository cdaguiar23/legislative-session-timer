package com.camara.ui;

import com.camara.data.DataManager;
import com.camara.data.MicrophoneService;
import com.camara.model.SessionConfig;
import com.camara.model.Vereador;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class RegistrationDialog extends JDialog {
    private DataManager dataManager;
    private MicrophoneService micService;
    private JTable vereadorTable;
    private DefaultTableModel tableModel;
    private Runnable onConfigChanged;

    public RegistrationDialog(Frame owner, Runnable onConfigChanged) {
        super(owner, "Cadastros e Configurações", true);
        this.onConfigChanged = onConfigChanged;
        this.dataManager = new DataManager();
        this.micService = new MicrophoneService();
        setSize(800, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Theme.BACKGROUND_DARK);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Theme.BACKGROUND_DARK);
        tabbedPane.setForeground(Theme.TEXT_SECONDARY);
        tabbedPane.addTab("Vereadores", createVereadorPanel());
        tabbedPane.addTab("Brasão", createBrasaoPanel());
        tabbedPane.addTab("Controladora", createControllerPanel());
        tabbedPane.addTab("Pauta", createPautaPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createVereadorPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND_DARK);

        // Table
        String[] columnNames = { "Nome", "Partido", "ID Mic", "Caminho da Foto" };
        tableModel = new DefaultTableModel(columnNames, 0);
        vereadorTable = new JTable(tableModel);
        loadVereadores();
        JScrollPane scrollPane = new JScrollPane(vereadorTable);
        scrollPane.getViewport().setBackground(Theme.BACKGROUND_DARK);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Form
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBackground(Theme.BACKGROUND_DARK);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Theme.TEXT_SECONDARY),
                "Novo Vereador", 0, 0, null, Theme.TEXT_PRIMARY));

        JTextField nameField = new JTextField();
        JTextField partyField = new JTextField();
        JTextField micIdField = new JTextField();
        JTextField photoPathField = new JTextField();
        photoPathField.setEditable(false);
        JButton choosePhotoButton = new JButton("Escolher Foto...");

        choosePhotoButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                photoPathField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        JButton addButton = new JButton("Adicionar");
        addButton.addActionListener(e -> {
            String name = nameField.getText();
            String party = partyField.getText();
            String photo = photoPathField.getText();
            String micIdStr = micIdField.getText();

            if (!name.isEmpty() && !party.isEmpty() && !micIdStr.isEmpty()) {
                try {
                    int micId = Integer.parseInt(micIdStr);
                    Vereador v = new Vereador(name, party, photo, micId);
                    List<Vereador> list = dataManager.loadVereadores();
                    list.add(v);
                    dataManager.saveVereadores(list);
                    loadVereadores();
                    nameField.setText("");
                    partyField.setText("");
                    micIdField.setText("");
                    photoPathField.setText("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "ID Microfone deve ser um número!");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Preencha nome, partido e ID!");
            }
        });

        JButton removeButton = new JButton("Remover Selecionado");
        removeButton.addActionListener(e -> {
            int row = vereadorTable.getSelectedRow();
            if (row >= 0) {
                List<Vereador> list = dataManager.loadVereadores();
                if (row < list.size()) {
                    list.remove(row);
                    dataManager.saveVereadores(list);
                    loadVereadores();
                }
            }
        });

        JLabel l1 = new JLabel("Nome:");
        l1.setForeground(Theme.TEXT_PRIMARY);
        JLabel l2 = new JLabel("Partido:");
        l2.setForeground(Theme.TEXT_PRIMARY);
        JLabel l3 = new JLabel("ID Mic:");
        l3.setForeground(Theme.TEXT_PRIMARY);
        JLabel l4 = new JLabel("Foto:");
        l4.setForeground(Theme.TEXT_PRIMARY);

        formPanel.add(l1);
        formPanel.add(nameField);
        formPanel.add(l2);
        formPanel.add(partyField);
        formPanel.add(l3);
        formPanel.add(micIdField);
        formPanel.add(l4);
        JPanel photoPanel = new JPanel(new BorderLayout());
        photoPanel.setBackground(Theme.BACKGROUND_DARK);
        photoPanel.add(photoPathField, BorderLayout.CENTER);
        photoPanel.add(choosePhotoButton, BorderLayout.EAST);
        formPanel.add(photoPanel);
        formPanel.add(removeButton);
        formPanel.add(addButton);

        panel.add(formPanel, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createControllerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND_DARK);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        SessionConfig config = dataManager.loadConfig();

        JPanel gridPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        gridPanel.setBackground(Theme.BACKGROUND_DARK);

        JTextField ipField = new JTextField(config.getControllerIp());
        JTextField portField = new JTextField(String.valueOf(config.getControllerPort()));
        JTextField hostField = new JTextField(config.getControllerHost());
        JTextField macField = new JTextField(config.getControllerMac());
        JTextField broadcastAddrField = new JTextField(config.getBroadcastAddress());
        JTextField broadcastPortField = new JTextField(String.valueOf(config.getBroadcastPort()));

        JLabel connectionStatusLabel = new JLabel(" ");
        connectionStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        connectionStatusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        // Initial status check
        if (micService.isConnected()) {
            connectionStatusLabel.setText("Conectado");
            connectionStatusLabel.setForeground(new Color(0, 200, 0));
        } else {
            connectionStatusLabel.setText("Desconectado");
            connectionStatusLabel.setForeground(Color.RED);
        }

        addLabeledField(gridPanel, "IP:", ipField);
        addLabeledField(gridPanel, "Porta:", portField);
        addLabeledField(gridPanel, "Host:", hostField);
        addLabeledField(gridPanel, "MAC:", macField);
        addLabeledField(gridPanel, "Endereço Broadcast:", broadcastAddrField);
        addLabeledField(gridPanel, "Porta Broadcast:", broadcastPortField);

        JButton saveButton = new JButton("Salvar Configurações");
        saveButton.addActionListener(e -> {
            try {
                config.setControllerIp(ipField.getText());
                config.setControllerPort(Integer.parseInt(portField.getText()));
                config.setControllerHost(hostField.getText());
                config.setControllerMac(macField.getText());
                config.setBroadcastAddress(broadcastAddrField.getText());
                config.setBroadcastPort(Integer.parseInt(broadcastPortField.getText()));

                dataManager.saveConfig(config);
                JOptionPane.showMessageDialog(this, "Configurações salvas com sucesso!");
                if (onConfigChanged != null)
                    onConfigChanged.run();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Porta deve ser um número válido!");
            }
        });

        JButton connectButton = new JButton("Conectar");
        connectButton.addActionListener(e -> {
            new Thread(() -> {
                boolean success = micService.connect();
                SwingUtilities.invokeLater(() -> {
                    if (success) {
                        connectionStatusLabel.setText("Conectado");
                        connectionStatusLabel.setForeground(new Color(0, 200, 0));
                    } else {
                        connectionStatusLabel.setText("Não foi possível conectar");
                        connectionStatusLabel.setForeground(Color.RED);
                    }
                });
            }).start();
        });

        JButton disconnectButton = new JButton("Desconectar");
        disconnectButton.addActionListener(e -> {
            micService.disconnect();
            connectionStatusLabel.setText("Desconectado");
            connectionStatusLabel.setForeground(Color.RED);
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Theme.BACKGROUND_DARK);
        buttonPanel.add(saveButton);
        buttonPanel.add(connectButton);
        buttonPanel.add(disconnectButton);

        panel.add(gridPanel, BorderLayout.NORTH);
        panel.add(connectionStatusLabel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void addLabeledField(JPanel panel, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setForeground(Theme.TEXT_PRIMARY);
        panel.add(label);
        panel.add(field);
    }

    private JPanel createPautaPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND_DARK);
        
        SessionConfig config = dataManager.loadConfig();
        JLabel currentPathLabel = new JLabel("PDF Atual: " + (config.getPautaPath() != null ? config.getPautaPath() : "Nenhum selecionado"));
        currentPathLabel.setForeground(Theme.TEXT_PRIMARY);
        currentPathLabel.setHorizontalAlignment(SwingConstants.CENTER);
        currentPathLabel.setFont(new Font("Arial", Font.ITALIC, 14));

        JButton changeButton = new JButton("Selecionar PDF da Pauta");
        changeButton.setFont(new Font("Arial", Font.BOLD, 16));
        changeButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            // Optional: Filter for PDF
            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                SessionConfig current = dataManager.loadConfig();
                current.setPautaPath(file.getAbsolutePath());
                dataManager.saveConfig(current);
                currentPathLabel.setText("PDF Atual: " + file.getAbsolutePath());
                if (onConfigChanged != null)
                    onConfigChanged.run();
                JOptionPane.showMessageDialog(this, "Pauta selecionada com sucesso!");
            }
        });

        panel.add(currentPathLabel, BorderLayout.CENTER);
        panel.add(changeButton, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBrasaoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BACKGROUND_DARK);
        JLabel currentPathLabel = new JLabel("Atual: " + dataManager.loadConfig().getBrasaoPath());
        currentPathLabel.setForeground(Theme.TEXT_PRIMARY);
        currentPathLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton changeButton = new JButton("Alterar Brasão");
        changeButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                SessionConfig config = new SessionConfig(file.getAbsolutePath());
                dataManager.saveConfig(config);
                currentPathLabel.setText("Atual: " + config.getBrasaoPath());
                if (onConfigChanged != null)
                    onConfigChanged.run();
            }
        });

        panel.add(currentPathLabel, BorderLayout.CENTER);
        panel.add(changeButton, BorderLayout.SOUTH);
        return panel;
    }

    private void loadVereadores() {
        tableModel.setRowCount(0);
        List<Vereador> list = dataManager.loadVereadores();
        for (Vereador v : list) {
            tableModel.addRow(new Object[] { v.getName(), v.getParty(), v.getMicrofoneId(), v.getPhotoPath() });
        }
    }
}
