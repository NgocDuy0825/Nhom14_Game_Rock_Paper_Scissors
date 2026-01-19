package client;

import common.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class GameClient {

    private static final String HOST = "localhost";
    private static final int PORT = 9999;

    public static void main(String[] args) {

        Socket socket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        Scanner scanner = null;

        try {
            socket = new Socket(HOST, PORT);

            out = new ObjectOutputStream(socket.getOutputStream());
            in  = new ObjectInputStream(socket.getInputStream());

            System.out.println("CLIENT: Connected to server");

            ServerListener listener = new ServerListener(in);
            listener.start();

            scanner = new Scanner(System.in);

            while (true) {
                System.out.print(">> ");

                if (!scanner.hasNextLine()) {
                    System.out.println("CLIENT: Input closed");
                    break;
                }

                String input = scanner.nextLine().trim().toUpperCase();

                if (input.equals("EXIT")) {
                    out.writeObject(new Message(MessageType.EXIT, ""));
                    out.flush();
                    break;
                }

                if (input.equals("PLAY_AGAIN")) {
                    out.writeObject(new Message(MessageType.PLAY_AGAIN, ""));
                    out.flush();
                    continue;
                }

                if (input.equals("READY")) {
                    out.writeObject(new Message(MessageType.READY, ""));
                    out.flush();
                    continue;
                }

                if (input.equals("ROCK") ||
                    input.equals("PAPER") ||
                    input.equals("SCISSORS")) {

                    out.writeObject(new Message(MessageType.CHOICE, input));
                    out.flush();
                } else {
                    System.out.println(
                        "Commands: READY / ROCK / PAPER / SCISSORS / PLAY_AGAIN / EXIT"
                    );
                }
            }

        }
        catch (SocketException e) {
            System.out.println("CLIENT ERROR: Connection lost");
        }
        catch (EOFException e) {
            System.out.println("CLIENT ERROR: Server closed connection");
        }
        catch (IOException e) {
            System.out.println("CLIENT IO ERROR: " + e.getMessage());
        }
        finally {
            try {
                if (scanner != null) scanner.close();
                if (out != null) out.close();
                if (in != null) in.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                System.out.println("CLIENT CLEANUP ERROR");
            }

            System.out.println("CLIENT: Terminated");
        }
    }
}
