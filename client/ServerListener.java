package client;

import common.Message;

import java.io.*;
import java.net.SocketException;

public class ServerListener extends Thread {

    private final ObjectInputStream in;

    public ServerListener(ObjectInputStream in) {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                
                if (obj instanceof Message) {
                    Message msg = (Message) obj;
                    System.out.println("SERVER: " + msg.getType() + " - " + msg.getContent());
                }
            }
        }
        catch (EOFException e) {
            System.out.println("CLIENT: Server closed connection");
        }
        catch (SocketException e) {
            System.out.println("CLIENT: Connection lost");
        }
        catch (ClassNotFoundException e) {
            System.out.println("CLIENT ERROR: Unknown object received");
        }
        catch (IOException e) {
            System.out.println("CLIENT IO ERROR: " + e.getMessage());
        }
    }
}
