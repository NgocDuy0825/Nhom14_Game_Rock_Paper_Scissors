package server;

public class GameRoom {

    private ClientHandler player1;
    private ClientHandler player2;

    private String choice1;
    private String choice2;

    public GameRoom(ClientHandler p1, ClientHandler p2) {
        this.player1 = p1;
        this.player2 = p2;
    }

    public synchronized void setChoice(ClientHandler player, String choice) {
        if (player == player1) {
            choice1 = choice;
        } else if (player == player2) {
            choice2 = choice;
        }

        if (choice1 != null && choice2 != null) {
            processResult();
        }
    }

    private void processResult() {
        int result = GameUtils.compare(choice1, choice2);

        if (result == 0) {
            player1.send("RESULT:HÒA");
            player2.send("RESULT:HÒA");
        } else if (result == 1) {
            player1.send("RESULT:THẮNG");
            player2.send("RESULT:THUA");
        } else {
            player1.send("RESULT:THUA");
            player2.send("RESULT:THẮNG");
        }
    }
}
