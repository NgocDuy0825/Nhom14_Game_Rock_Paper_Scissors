package server;

import common.Message;
import common.MessageType;
import common.PlayerState;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private PlayerState state = PlayerState.CONNECTED;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void send(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (Exception e) {
            System.out.println("SERVER: Send failed");
        }
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in  = new ObjectInputStream(socket.getInputStream());

            send(new Message(MessageType.INFO, "Connected to server"));

            while (true) {
                Message msg = (Message) in.readObject();
                if (msg.getType() == MessageType.EXIT) {
                    break;
                }
            }
        } catch (Exception e) {
            System.out.println("SERVER: Client error");
        }
    }
}
