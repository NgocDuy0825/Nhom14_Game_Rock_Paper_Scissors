package server;

import common.*;

public class GameRoom {

    private final ClientHandler p1;
    private final ClientHandler p2;

    private String c1, c2;
    private int replayCount = 0;

    public GameRoom(ClientHandler p1, ClientHandler p2) {
        this.p1 = p1;
        this.p2 = p2;

        p1.setRoom(this);
        p2.setRoom(this);

        startGame();
    }

    private synchronized void startGame() {
        c1 = c2 = null;
        replayCount = 0;

        p1.setPlayerState(PlayerState.PLAYING);
        p2.setPlayerState(PlayerState.PLAYING);

        sendBoth("Game started");
        sendBoth("Choose ROCK / PAPER / SCISSORS");
    }

    private void sendBoth(String msg) {
        p1.send(new Message(MessageType.INFO, msg));
        p2.send(new Message(MessageType.INFO, msg));
    }

    public synchronized void submitChoice(ClientHandler p, String choice) {
        if (p == p1 && c1 == null)
            c1 = choice;
        if (p == p2 && c2 == null)
            c2 = choice;

        if (c1 != null && c2 != null)
            evaluate();
    }

    private void evaluate() {
        int r = compare(c1, c2);

        if (r == 0)
            sendResult("DRAW", "DRAW");
        else if (r == 1)
            sendResult("WIN", "LOSE");
        else
            sendResult("LOSE", "WIN");

        p1.setPlayerState(PlayerState.FINISHED);
        p2.setPlayerState(PlayerState.FINISHED);

        sendBoth("PLAY_AGAIN or LEAVE or EXIT");
    }

    private void sendResult(String r1, String r2) {
        p1.send(new Message(MessageType.RESULT, r1));
        p2.send(new Message(MessageType.RESULT, r2));
    }

    public synchronized void requestReplay(ClientHandler p) {
        replayCount++;
        if (replayCount == 2)
            startGame();
    }

    private int compare(String a, String b) {
        if (a.equals(b))
            return 0;
        if (a.equals("ROCK") && b.equals("SCISSORS"))
            return 1;
        if (a.equals("SCISSORS") && b.equals("PAPER"))
            return 1;
        if (a.equals("PAPER") && b.equals("ROCK"))
            return 1;
        return 2;
    }

    public void playerDisconnected(ClientHandler p) {
        ClientHandler other = (p == p1) ? p2 : p1;
        if (other != null) {
            other.send(new Message(
                MessageType.INFO,
                "Opponent disconnected"
            ));
            other.setPlayerState(PlayerState.FINISHED);
        }
    }

}
            
            
            
            
            
            
            
            
                    
                    