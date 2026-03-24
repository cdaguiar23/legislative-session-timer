import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * VotechController - Cliente de teste para controladora IP Votech
 * Versão 4 - Numeração direta + sufixo corrigido
 *
 * ═══════════════════════════════════════════════════════════════════════════
 * PROTOCOLO CONFIRMADO NOS TESTES
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * [UDP Broadcast — Controladora → Rede] (passivo, ignorar)
 *   Origem : 192.168.132.100:6769  →  255.255.255.255:9
 *   Payload: 1024 bytes (heartbeat do chip Microchip, MAC 00:04:A3:01:02:03)
 *
 * [TCP — Computador ↔ Controladora] porta 9760
 *   Frames fixos de 6 bytes ASCII. Controladora ecoa o mesmo comando de volta.
 *
 *   Comandos:
 *     MICCA1      → Ativa a controladora (apenas na 1ª conexão TCP)
 *     MIC{nn}0    → ABRE / desmuta o microfone nn   (sufixo 0 = abrir)
 *     MIC{nn}1    → FECHA / muta o microfone nn     (sufixo 1 = fechar)
 *
 * NUMERAÇÃO — Direta, sem offset:
 *   Microfone físico N → comando MIC{N}x
 *   Exemplo: mic físico 1 → MIC010 (abrir) / MIC011 (fechar)
 *            mic físico 4 → MIC040 (abrir) / MIC041 (fechar)
 *
 *   Faixa válida: microfones físicos 1 a 12
 *
 * SUFIXO — Confirmado nos testes reais (inverso do intuitivo):
 *   0 = ABRE     1 = FECHA
 *
 * Configuração de rede: computador em 192.168.132.50 / máscara 255.255.255.0
 * ═══════════════════════════════════════════════════════════════════════════
 */
public class VotechController {

    // ── Configurações da controladora ──────────────────────────────────────
    private static final String CONTROLLER_IP   = "192.168.132.100";
    private static final int    CONTROLLER_PORT = 9760;
    private static final int    TIMEOUT_MS      = 5000;

    // Faixa de microfones físicos: 1 a 12
    private static final int MIC_FIRST = 1;
    private static final int MIC_LAST  = 12;

    // Sufixos confirmados nos testes reais
    private static final char SUFFIX_OPEN  = '0'; // 0 = ABRE o microfone
    private static final char SUFFIX_CLOSE = '1'; // 1 = FECHA o microfone

    // ── Estado interno ─────────────────────────────────────────────────────
    private Socket       socket;
    private OutputStream out;
    private InputStream  in;
    private boolean      connected = false;

    // ── Métodos de conexão ─────────────────────────────────────────────────

    /**
     * Abre conexão TCP com a controladora.
     *
     * @param sendActivation true  → envia MICCA1 (obrigatório na 1ª conexão após ligar)
     *                       false → conecta sem MICCA1 (controladora já estava ativa)
     */
    public void connect(boolean sendActivation) throws IOException {
        System.out.println("Conectando em " + CONTROLLER_IP + ":" + CONTROLLER_PORT + " ...");
        socket = new Socket();
        socket.connect(new InetSocketAddress(CONTROLLER_IP, CONTROLLER_PORT), TIMEOUT_MS);
        socket.setSoTimeout(TIMEOUT_MS);
        out = socket.getOutputStream();
        in  = socket.getInputStream();
        connected = true;
        System.out.println("Conexão TCP estabelecida.");

        if (sendActivation) {
            sendCommand("MICCA1");
            System.out.println("Controladora ativada (MICCA1 enviado).");
        } else {
            System.out.println("Conectado sem MICCA1 (controladora já deve estar ativa).");
        }
    }

    /** Atalho: conecta enviando MICCA1. */
    public void connect() throws IOException {
        connect(true);
    }

    /** Encerra a conexão TCP. */
    public void disconnect() {
        connected = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Conexão encerrada.");
            }
        } catch (IOException e) {
            System.err.println("Erro ao fechar socket: " + e.getMessage());
        }
    }

    // ── Comandos de microfone ──────────────────────────────────────────────

    /**
     * ABRE (desmuta) o microfone indicado.
     * Mic físico N → envia MIC{N}0
     * Exemplo: openMic(1) → "MIC010",  openMic(4) → "MIC040"
     *
     * @param micNumber número do microfone físico (1 a 12)
     */
    public void openMic(int micNumber) throws IOException {
        validateMicNumber(micNumber);
        sendCommand(String.format("MIC%02d%c", micNumber, SUFFIX_OPEN));
    }

    /**
     * FECHA (muta) o microfone indicado.
     * Mic físico N → envia MIC{N}1
     * Exemplo: closeMic(1) → "MIC011",  closeMic(4) → "MIC041"
     *
     * @param micNumber número do microfone físico (1 a 12)
     */
    public void closeMic(int micNumber) throws IOException {
        validateMicNumber(micNumber);
        sendCommand(String.format("MIC%02d%c", micNumber, SUFFIX_CLOSE));
    }

    /** Abre (desmuta) todos os microfones físicos (1 a 12). */
    public void openAllMics() throws IOException {
        System.out.println("Abrindo todos os microfones (1 a " + MIC_LAST + ")...");
        for (int i = MIC_FIRST; i <= MIC_LAST; i++) {
            openMic(i);
            sleep(50);
        }
        System.out.println("Todos os microfones abertos.");
    }

    /** Fecha (muta) todos os microfones físicos (1 a 12). */
    public void closeAllMics() throws IOException {
        System.out.println("Fechando todos os microfones (1 a " + MIC_LAST + ")...");
        for (int i = MIC_FIRST; i <= MIC_LAST; i++) {
            closeMic(i);
            sleep(50);
        }
        System.out.println("Todos os microfones fechados.");
    }

    // ── Núcleo de comunicação TCP ──────────────────────────────────────────

    /**
     * Envia um comando de 6 bytes ASCII e lê o echo da controladora.
     * Visibilidade pública para permitir envio de comandos manuais.
     */
    public void sendCommand(String command) throws IOException {
        if (!connected || socket == null || socket.isClosed())
            throw new IOException("Não conectado. Use a opção Conectar primeiro.");
        if (command.length() != 6)
            throw new IllegalArgumentException(
                    "Comando deve ter exatamente 6 caracteres ASCII. " +
                    "Recebido: [" + command + "] com " + command.length() + " chars.");

        byte[] payload = command.getBytes("ASCII");
        out.write(payload);
        out.flush();
        System.out.printf("  → Enviado : [%s]  hex: %s%n", command, toHex(payload));

        // Lê echo de confirmação (6 bytes, pode chegar fragmentado)
        byte[] response  = new byte[6];
        int    totalRead = 0;
        long   deadline  = System.currentTimeMillis() + TIMEOUT_MS;

        while (totalRead < 6 && System.currentTimeMillis() < deadline) {
            int n = in.read(response, totalRead, 6 - totalRead);
            if (n == -1)
                throw new IOException("Controladora encerrou a conexão durante leitura.");
            totalRead += n;
        }
        if (totalRead < 6)
            throw new IOException(
                    "Timeout aguardando echo (" + totalRead + "/6 bytes recebidos).");

        String echo = new String(response, 0, totalRead, "ASCII");
        boolean ok  = echo.equals(command);
        System.out.printf("  ← Recebido: [%s]  hex: %s  %s%n",
                echo, toHex(response),
                ok ? "✓ OK" : "✗ ECHO DIVERGE! (esperado: " + command + ")");
    }

    // ── Utilitários ────────────────────────────────────────────────────────

    private void validateMicNumber(int n) {
        if (n < MIC_FIRST || n > MIC_LAST)
            throw new IllegalArgumentException(
                    "Microfone inválido: " + n + ". Faixa válida: " + MIC_FIRST + " a " + MIC_LAST + ".");
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02X ", b));
        return sb.toString().trim();
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }

    // ── Menu interativo de testes ──────────────────────────────────────────

    public static void main(String[] args) {
        VotechController ctrl = new VotechController();

        System.out.println("╔══════════════════════════════════════════════════════════╗");
        System.out.println("║   Votech IP Controller — Cliente de Teste  v4.0         ║");
        System.out.println("║   Controladora : " + CONTROLLER_IP + ":" + CONTROLLER_PORT + "                    ║");
        System.out.println("║   Microfones   : 1 a 12  |  sufixo 0=ABRE  1=FECHA      ║");
        System.out.println("╚══════════════════════════════════════════════════════════╝");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println();
            System.out.println("┌─ Menu ───────────────────────────────────────────────────┐");
            System.out.println("│  1 - Conectar + Ativar controladora (envia MICCA1)       │");
            System.out.println("│  2 - Conectar SEM MICCA1 (controladora já estava ativa)  │");
            System.out.println("│  3 - Desconectar                                         │");
            System.out.println("│  4 - Abrir microfone individual (desmuta)                │");
            System.out.println("│  5 - Fechar microfone individual (muta)                  │");
            System.out.println("│  6 - Abrir TODOS os microfones (1 a 12)                 │");
            System.out.println("│  7 - Fechar TODOS os microfones (1 a 12)                │");
            System.out.println("│  8 - Enviar comando manual (exatos 6 chars ASCII)        │");
            System.out.println("│  9 - Teste sequencial: abre e fecha cada microfone       │");
            System.out.println("│  0 - Sair                                                │");
            System.out.println("└──────────────────────────────────────────────────────────┘");
            System.out.print("Opção: ");

            String option = scanner.nextLine().trim();

            try {
                switch (option) {

                    case "1":
                        ctrl.connect(true);
                        break;

                    case "2":
                        ctrl.connect(false);
                        break;

                    case "3":
                        ctrl.disconnect();
                        break;

                    case "4":
                        System.out.print("Número do microfone (" + MIC_FIRST + "-" + MIC_LAST + "): ");
                        int micOpen = Integer.parseInt(scanner.nextLine().trim());
                        ctrl.openMic(micOpen);
                        break;

                    case "5":
                        System.out.print("Número do microfone (" + MIC_FIRST + "-" + MIC_LAST + "): ");
                        int micClose = Integer.parseInt(scanner.nextLine().trim());
                        ctrl.closeMic(micClose);
                        break;

                    case "6":
                        ctrl.openAllMics();
                        break;

                    case "7":
                        ctrl.closeAllMics();
                        break;

                    case "8":
                        System.out.print("Comando (exatamente 6 caracteres ASCII): ");
                        ctrl.sendCommand(scanner.nextLine().trim());
                        break;

                    case "9":
                        System.out.println("Teste sequencial mic 1 a " + MIC_LAST + "...");
                        for (int i = MIC_FIRST; i <= MIC_LAST; i++) {
                            System.out.println("-- Microfone " + i + " --");
                            ctrl.openMic(i);
                            sleep(800);
                            ctrl.closeMic(i);
                            sleep(400);
                        }
                        System.out.println("Teste sequencial concluído.");
                        break;

                    case "0":
                        ctrl.disconnect();
                        running = false;
                        System.out.println("Encerrando.");
                        break;

                    default:
                        System.out.println("Opção inválida.");
                }

            } catch (NumberFormatException e) {
                System.err.println("Número inválido: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.err.println("Erro de parâmetro: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("Erro de comunicação: " + e.getMessage());
                ctrl.connected = false;
            }
        }

        scanner.close();
    }
}
