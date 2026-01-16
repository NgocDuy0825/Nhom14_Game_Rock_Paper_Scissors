package server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GameServer {

    private static final int PORT = 9999;
    private static final Queue<ClientHandler> waiting = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("SERVER: Running on port " + PORT);

            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientHandler client = new ClientHandler(socket);
                    client.start();

                    waiting.add(client);
                    matchPlayers();

                } catch (IOException e) {
                    System.out.println("SERVER: Accept failed");
                }
            }

        } catch (BindException e) {
            System.out.println("SERVER ERROR: Port already in use");
        } catch (IOException e) {
            System.out.println("SERVER ERROR: " + e.getMessage());
        }
    }

    private static synchronized void matchPlayers() {
        while (waiting.size() >= 2) {
            ClientHandler p1 = waiting.poll();
            ClientHandler p2 = waiting.poll();
            new GameRoom(p1, p2);
        }
    }
}
