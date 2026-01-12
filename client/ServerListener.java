package client;

import common.Message;

import java.io.*;

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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
