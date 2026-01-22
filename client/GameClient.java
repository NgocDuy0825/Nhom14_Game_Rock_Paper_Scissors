package client;

import common.Message;
import java.io.*;
import java.net.Socket;

public class GameClient {
    private final String host;
    private final int port;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
    private volatile boolean running = true;
    private String myName = "Player";

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        socket = new Socket(host, port);
        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Start listener thread
        Thread listener = new Thread(this::listenLoop);
        listener.setDaemon(true);
        listener.start();

        System.out.println("=== Connected to server " + host + ":" + port + " ===");

        // Set name
        System.out.print("Enter your name: ");
        String name = console.readLine();
        if (name != null && !name.trim().isEmpty()) {
            myName = name.trim();
            Message msg = new Message(Message.Type.NAME);
            msg.fields.put("name", myName);
            sendMessage(msg);
        }

        // Main command loop
        printHelp();

        while (running) {
            try {
                System.out.print("> ");
                String cmd = console.readLine();
                if (cmd == null)
                    break;

                cmd = cmd.trim().toUpperCase();

                if (cmd.equals("READY")) {
                    sendMessage(new Message(Message.Type.READY));

                } else if (cmd.equals("START")) {
                    sendMessage(new Message(Message.Type.START));

                } else if (cmd.equals("MOVE")) {
                    System.out.print("Enter choice (ROCK/PAPER/SCISSORS or 1/2/3): ");
                    String choice = console.readLine();
                    if (choice != null && !choice.trim().isEmpty()) {
                        Message msg = new Message(Message.Type.MOVE);
                        msg.fields.put("choice", choice.trim().toUpperCase());
                        sendMessage(msg);
                    }

                } else if (cmd.equals("QUIT")) {
                    running = false;
                    sendMessage(new Message(Message.Type.DISCONNECT));
                    break;

                } else if (cmd.equals("HELP")) {
                    printHelp();

                } else {
                    System.out.println("Unknown command. Type HELP for available commands.");
                }

            } catch (IOException e) {
                System.err.println("Error reading input: " + e.getMessage());
                break;
            }
        }

        cleanup();
    }

    private void listenLoop() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                Message msg = Message.parse(line);
                if (msg != null) {
                    handleServerMessage(msg);
                }
            }
        } catch (IOException e) {
            // Connection closed
        } finally {
            running = false;
            System.out.println("\n[Disconnected from server]");
        }
    }

    private void handleServerMessage(Message msg) {
        switch (msg.type) {
            case WELCOME:
                System.out.println("[WELCOME] Your ID: " + msg.fields.get("id"));
                break;

            case STATUS:
                System.out.println("[STATUS] " + msg.fields.get("message"));
                break;

            case START:
                System.out.println("\n=== GAME ROOM CREATED ===");
                System.out.println("Room ID: " + msg.fields.get("roomId"));
                System.out.println("Opponent: " + msg.fields.get("opponent"));
                System.out.println("Type START to begin the game");
                System.out.println("========================\n");
                break;

            case RESULT:
                System.out.println("\n--- ROUND RESULT ---");
                System.out.println("Your choice    : " + msg.fields.get("yourChoice"));
                System.out.println("Opponent choice: " + msg.fields.get("opponentChoice"));
                System.out.println("Result         : " + msg.fields.get("result"));
                System.out.println("-------------------\n");
                break;

            case ERROR:
                System.out.println("[ERROR] " + msg.fields.get("message"));
                break;

            default:
                System.out.println("[SERVER] " + msg.type + ": " + msg.fields);
        }
    }

    private void sendMessage(Message msg) {
        if (out != null) {
            out.println(msg.toWireString());
        }
    }

    private void printHelp() {
        System.out.println("\n=== COMMANDS ===");
        System.out.println("READY  - Join matchmaking queue");
        System.out.println("START  - Start the game (when in room)");
        System.out.println("MOVE   - Submit your move (ROCK/PAPER/SCISSORS)");
        System.out.println("QUIT   - Disconnect from server");
        System.out.println("HELP   - Show this help");
        System.out.println("================\n");
    }

    private void cleanup() {
        try {
            if (in != null)
                in.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
        } catch (IOException e) {
            // Ignore
        }
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 3000;

        if (args.length >= 1)
            host = args[0];
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number, using default: 3000");
            }
        }

        GameClient client = new GameClient(host, port);
        try {
            client.start();
        } catch (IOException e) {
            System.err.println("Cannot connect to server: " + e.getMessage());
        }
    }
}
