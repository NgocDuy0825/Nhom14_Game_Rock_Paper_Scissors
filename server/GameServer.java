package server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameServer {

    private static final int PORT = 9999;
    private static final Queue<ClientHandler> waiting =
            new ConcurrentLinkedQueue<>();

    private static volatile boolean running = true;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("SERVER: Running on port " + PORT);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                running = false;
                System.out.println("SERVER: Shutting down...");
            }));

            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println(
                        "SERVER: Client connected " + socket.getRemoteSocketAddress()
                    );

                    ClientHandler client = new ClientHandler(socket);
                    client.start();

                    waiting.add(client);
                    tryMatchPlayers();

                } catch (IOException e) {
                    System.out.println("SERVER: Accept error - " + e.getMessage());
                }
            }

        }
        catch (BindException e) {
            System.out.println("SERVER ERROR: Port " + PORT + " already in use");
        }
        catch (IOException e) {
            System.out.println("SERVER ERROR: " + e.getMessage());
        }
    }

    private static synchronized void tryMatchPlayers() {
        while (waiting.size() >= 2) {
            ClientHandler p1 = waiting.poll();
            ClientHandler p2 = waiting.poll();

            if (p1 == null || p2 == null) return;

            System.out.println("SERVER: Creating game room");
            new GameRoom(p1, p2);
        }
    }
}
