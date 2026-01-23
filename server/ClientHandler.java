package server;

import common.Message;
import java.io.*;
import java.net.Socket;

/**
 * ClientHandler - Handles communication with one client
 * 
 * FLOW:
 * 1. Outside room: READY -> add to matchmaking
 * 2. Inside room: START -> begin game, MOVE -> submit move
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final GameServer server;
    private final int clientId;

    private PrintWriter out;
    private BufferedReader in;

    private String playerName = "Player";
    private volatile GameRoom currentRoom;
    private boolean readySent = false;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        this.clientId = server.nextClientId();
    }

    // ============ GETTERS ============

    public int getClientId() {
        return clientId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public GameRoom getCurrentRoom() {
        return currentRoom;
    }

    // ============ ROOM MANAGEMENT ============

    public void setCurrentRoom(GameRoom room) {
        this.currentRoom = room;
        if (room == null) {
            // Returned to lobby
            readySent = false;
            sendStatus("Returned to lobby. Type READY to find a match.");
        }
    }

    // ============ SEND METHODS ============

    public void sendMessage(Message msg) {
        if (out != null) {
            out.println(msg.toWireString());
        }
    }

    public void sendStatus(String text) {
        Message msg = new Message(Message.Type.STATUS);
        msg.fields.put("message", text);
        sendMessage(msg);
    }

    public void sendWelcome() {
        Message msg = new Message(Message.Type.WELCOME);
        msg.fields.put("id", String.valueOf(clientId));
        sendMessage(msg);
    }

    public void sendStart(int roomId, String opponentName) {
        Message msg = new Message(Message.Type.START);
        msg.fields.put("roomId", String.valueOf(roomId));
        msg.fields.put("opponent", opponentName);
        sendMessage(msg);
    }

    public void sendResult(String myChoice, String oppChoice, String result) {
        Message msg = new Message(Message.Type.RESULT);
        msg.fields.put("yourChoice", myChoice);
        msg.fields.put("opponentChoice", oppChoice);
        msg.fields.put("result", result);
        sendMessage(msg);
    }

    // ============ MAIN LOOP ============

    @Override
    public void run() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            sendWelcome();
            sendStatus("Connected! Set name: NAME|name=YourName");
            sendStatus("Type READY to join matchmaking");

            String line;
            while ((line = in.readLine()) != null) {
                Message msg = Message.parse(line);
                if (msg == null) {
                    sendStatus("Invalid message format");
                    continue;
                }
                handleMessage(msg);
            }

        } catch (IOException e) {
            // Connection lost
        } finally {
            cleanup();
        }
    }

    // ============ MESSAGE HANDLING ============

    private void handleMessage(Message msg) {
        switch (msg.type) {
            case NAME:
                handleName(msg);
                break;

            case READY:
                handleReady();
                break;

            case START:
                handleStart();
                break;

            case MOVE:
                handleMove(msg);
                break;

            case DISCONNECT:
                cleanup();
                break;

            default:
                sendStatus("Unknown command: " + msg.type);
        }
    }

    private void handleName(Message msg) {
        String name = msg.fields.get("name");
        if (name != null && !name.trim().isEmpty()) {
            playerName = name.trim();
            sendStatus("Name set to: " + playerName);
        }
    }

    private void handleReady() {
        if (currentRoom != null) {
            sendStatus("ERROR: You are already in a room. Type START to begin.");
            return;
        }

        if (readySent) {
            sendStatus("You are already in matchmaking queue. Please wait...");
            return;
        }

        readySent = true;
        sendStatus("Added to matchmaking queue. Waiting for opponent...");
        server.addToWaiting(this);
    }

    private void handleStart() {
        if (currentRoom == null) {
            sendStatus("ERROR: You are not in a room. Type READY first.");
            return;
        }

        currentRoom.requestStart(this);
    }

    private void handleMove(Message msg) {
        if (currentRoom == null) {
            sendStatus("ERROR: You are not in a game. Type READY to find a match.");
            return;
        }

        String choice = msg.fields.get("choice");
        if (choice == null || choice.trim().isEmpty()) {
            sendStatus("ERROR: Invalid move. Use: MOVE|choice=ROCK (or 1, 2, 3)");
            return;
        }

        currentRoom.submitChoice(this, choice.trim());
    }

    // ============ CLEANUP ============

    private void cleanup() {
        try {
            server.removeFromWaiting(this);

            if (currentRoom != null) {
                currentRoom.handleDisconnect(this);
                currentRoom = null;
            }

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
}