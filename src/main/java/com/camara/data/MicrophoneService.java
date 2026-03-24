package com.camara.data;

import com.camara.model.SessionConfig;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MicrophoneService {
    private DataManager dataManager;
    private static Socket socket;
    private static OutputStream out;
    private static InputStream in;
    private static ScheduledExecutorService scheduler;
    private static final int DEFAULT_PORT = 9760;

    public MicrophoneService() {
        this.dataManager = new DataManager();
    }

    /**
     * Connects to the VORTECH controller via TCP.
     * Maintains a persistent connection and starts a heartbeat.
     * 
     * @return true if successfully connected.
     */
    public synchronized boolean connect() {
        SessionConfig config = dataManager.loadConfig();
        String ip = config.getControllerIp();
        int port = config.getControllerPort() > 0 ? config.getControllerPort() : DEFAULT_PORT;

        if (ip == null || ip.isEmpty()) {
            return false;
        }

        try {
            if (socket != null && !socket.isClosed() && socket.isConnected()) {
                return true;
            }

            socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), 5000);
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            out = socket.getOutputStream();
            in = socket.getInputStream();

            startHeartbeat();
            
            // Send activation command (MICCA1) on connect
            sendRaw("MICCA1"); 
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private synchronized void startHeartbeat() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (socket == null || socket.isClosed() || !socket.isConnected()) {
                    // Try to reconnect if connection lost
                    connect();
                }
            } catch (Exception e) {
                // Connection lost
                disconnect();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    public synchronized boolean isConnected() {
        return socket != null && !socket.isClosed() && socket.isConnected();
    }

    public synchronized void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {}
        
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        
        socket = null;
        out = null;
        in = null;
        scheduler = null;
    }

    /**
     * Envia comando para abrir todos os microfones (1 a 12) e depois desconecta.
     * Usar este método apenas quando a desconexão é proposital (ex: botão desconectar).
     */
    public synchronized void releaseAndDisconnect() {
        if (isConnected()) {
            // Tenta abrir todos os microfones antes de fechar a conexão
            for (int i = 1; i <= 12; i++) {
                String cmd = String.format("MIC%02d0", i);
                sendRaw(cmd);
                try { Thread.sleep(30); } catch (Exception ignored) {}
            }
            // Tenta desativar o controle automático
            sendRaw("MICCA0");
            try { Thread.sleep(100); } catch (Exception ignored) {}
        }
        disconnect();
    }

    /**
     * Sends a command to activate the microphone.
     * Command format: MIC%02d0 (e.g., MIC010)
     */
    public boolean activateMicrophone(int micId) {
        if (!ensureConnected()) return false;
        String cmd = String.format("MIC%02d0", micId);
        return sendRaw(cmd);
    }

    /**
     * Sends a command to deactivate the microphone.
     * Command format: MIC%02d1 (e.g., MIC011)
     */
    public boolean deactivateMicrophone(int micId) {
        if (!ensureConnected()) return false;
        String cmd = String.format("MIC%02d1", micId);
        return sendRaw(cmd);
    }

    private synchronized boolean ensureConnected() {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            return connect();
        }
        return true;
    }

    private synchronized boolean sendRaw(String cmd) {
        try {
            if (out != null) {
                byte[] payload = cmd.getBytes("ASCII");
                out.write(payload);
                out.flush();
                
                // Discard echo asynchronously or silently to prevent buffer filling
                if (in != null && in.available() > 0) {
                    byte[] garbage = new byte[in.available()];
                    in.read(garbage);
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
        }
        return false;
    }
}
