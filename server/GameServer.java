package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * GameServer - Main server handling connections and matchmaking
 */
public class GameServer {

    private final int port;

    // Matchmaking queue
    private final ConcurrentLinkedQueue<ClientHandler> waitingQueue = new ConcurrentLinkedQueue<>();

    // Thread pools
    private final ExecutorService clientPool = Executors.newCachedThreadPool();
    private final ExecutorService roomPool = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);

    // ID generators
    private final AtomicInteger clientIdGen = new AtomicInteger(1);
    private final AtomicInteger roomIdGen = new AtomicInteger(1);

    public GameServer(int port) {
        this.port = port;
    }

    // ============ ID GENERATION ============

    public int nextClientId() {
        return clientIdGen.getAndIncrement();
    }

    public int nextRoomId() {
        return roomIdGen.getAndIncrement();
    }

    // ============ SERVER START ============

    public void start() {
        System.out.println("=== Rock-Paper-Scissors Game Server ===");
        System.out.println("Starting server on port " + port + "...");

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server ready! Waiting for connections...\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(clientSocket, this);
                clientPool.submit(handler);

                System.out.println("[SERVER] New connection from " + clientSocket.getInetAddress());
            }

        } catch (IOException e) {
            System.err.println("[SERVER] Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private void shutdown() {
        System.out.println("\n[SERVER] Shutting down...");

        clientPool.shutdownNow();
        roomPool.shutdownNow();
        scheduler.shutdownNow();

        try {
            clientPool.awaitTermination(2, TimeUnit.SECONDS);
            roomPool.awaitTermination(2, TimeUnit.SECONDS);
            scheduler.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // Ignore
        }

        System.out.println("[SERVER] Stopped.");
    }

    // ============ MATCHMAKING ============

    /**
     * Add a client to waiting queue
     * Called by ClientHandler when player sends READY
     */
    public synchronized void addToWaiting(ClientHandler client) {
        if (client == null)
            return;

        // Don't add if already in room
        if (client.getCurrentRoom() != null) {
            System.out.println("[MATCHMAKING] Client already in room: " + client.getPlayerName());
            return;
        }

        // Don't add if already in queue
        if (waitingQueue.contains(client)) {
            System.out.println("[MATCHMAKING] Client already in queue: " + client.getPlayerName());
            return;
        }

        waitingQueue.add(client);
        System.out.println("[MATCHMAKING] Added to queue: " + client.getPlayerName() + " (Queue size: "
                + waitingQueue.size() + ")");

        // Try to match players
        matchPlayers();
    }

    /**
     * Remove a client from waiting queue
     * Called when client disconnects
     */
    public synchronized void removeFromWaiting(ClientHandler client) {
        if (client == null)
            return;

        boolean removed = waitingQueue.remove(client);
        if (removed) {
            System.out.println("[MATCHMAKING] Removed from queue: " + client.getPlayerName());
        }
    }

    /**
     * Try to match two players from the queue
     */
    private synchronized void matchPlayers() {
        while (waitingQueue.size() >= 2) {
            ClientHandler p1 = waitingQueue.poll();
            ClientHandler p2 = waitingQueue.poll();

            if (p1 == null || p2 == null) {
                // Shouldn't happen, but re-add if it does
                if (p1 != null)
                    waitingQueue.add(p1);
                if (p2 != null)
                    waitingQueue.add(p2);
                break;
            }

            // Check if still available (not in room, not disconnected)
            boolean p1Available = isAvailable(p1);
            boolean p2Available = isAvailable(p2);

            if (!p1Available || !p2Available) {
                // Re-add available player back to queue
                if (p1Available)
                    waitingQueue.add(p1);
                if (p2Available)
                    waitingQueue.add(p2);
                continue;
            }

            // Create room
            createRoom(p1, p2);
        }
    }

    private boolean isAvailable(ClientHandler client) {
        try {
            return client.getCurrentRoom() == null;
        } catch (Exception e) {
            return false;
        }
    }

    private void createRoom(ClientHandler p1, ClientHandler p2) {
        int roomId = nextRoomId();

        System.out.println("\n[MATCHMAKING] Creating Room #" + roomId);
        System.out.println("  Player 1: " + p1.getPlayerName());
        System.out.println("  Player 2: " + p2.getPlayerName());

        GameRoom room = new GameRoom(roomId, p1, p2, scheduler);

        p1.setCurrentRoom(room);
        p2.setCurrentRoom(room);

        roomPool.submit(room);
    }

    // ============ MAIN ============

    public static void main(String[] args) {
        int port = 3000;

        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default: 3000");
            }
        }

        GameServer server = new GameServer(port);
        server.start();
    }
}