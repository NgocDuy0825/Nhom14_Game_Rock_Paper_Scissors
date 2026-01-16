package server;

import common.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ClientHandler extends Thread {

    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    private volatile PlayerState state = PlayerState.CONNECTED;
    private GameRoom room;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public synchronized void setRoom(GameRoom room) {
        this.room = room;
    }

    public synchronized void setPlayerState(PlayerState s) {
        this.state = s;
    }

    public synchronized PlayerState getPlayerState() {
        return state;
    }

    public void send(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            System.out.println("SERVER: Send failed");
            disconnect();
        }
    }

    private synchronized void disconnect() {
        if (state == PlayerState.DISCONNECTED) return;

        setPlayerState(PlayerState.DISCONNECTED);

        if (room != null) {
            room.playerDisconnected(this);
        }

        try {
            socket.close();
        } catch (IOException ignored) {}

        System.out.println("SERVER: Client disconnected");
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in  = new ObjectInputStream(socket.getInputStream());

            send(new Message(MessageType.INFO,
                "Welcome! Type READY to join game"));

            while (state != PlayerState.DISCONNECTED) {
                Message msg = (Message) in.readObject();

                switch (msg.getType()) {

                    case READY:
                        setPlayerState(PlayerState.READY);
                        send(new Message(MessageType.INFO,
                            "Waiting for opponent..."));
                        break;

                    case CHOICE:
                        if (state == PlayerState.PLAYING) {
                            room.submitChoice(this, msg.getContent());
                        } else {
                            send(new Message(MessageType.ERROR,
                                "Not in PLAYING state"));
                        }
                        break;

                    case PLAY_AGAIN:
                        if (state == PlayerState.FINISHED) {
                            room.requestReplay(this);
                            setPlayerState(PlayerState.READY);
                        }
                        break;

                    case EXIT:
                        disconnect();
                        return;

                    default:
                        send(new Message(MessageType.ERROR,
                            "Invalid command"));
                }
            }
        }
        catch (EOFException e) {
            System.out.println("SERVER: Client closed connection");
            disconnect();
        }
        catch (SocketException e) {
            System.out.println("SERVER: Network error");
            disconnect();
        }
        catch (ClassNotFoundException e) {
            System.out.println("SERVER: Invalid object");
            disconnect();
        }
        catch (IOException e) {
            System.out.println("SERVER IO ERROR: " + e.getMessage());
            disconnect();
        }
    }
}
