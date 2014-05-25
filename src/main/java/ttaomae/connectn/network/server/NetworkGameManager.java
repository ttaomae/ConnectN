package ttaomae.connectn.network.server;

import java.io.IOException;
import java.net.Socket;

import ttaomae.connectn.GameManager;
import ttaomae.connectn.network.ConnectNProtocol;

public class NetworkGameManager implements Runnable
{
    private Server server;
    private Socket playerOneSocket;
    private Socket playerTwoSocket;
    private NetworkPlayer playerOne;
    private NetworkPlayer playerTwo;

    public NetworkGameManager(Server server, Socket playerOneSocket,
            Socket playerTwoSocket)
            throws IOException
    {
        this.server = server;
        this.playerOneSocket = playerOneSocket;
        this.playerTwoSocket = playerTwoSocket;
        this.playerOne = new NetworkPlayer(this, playerOneSocket);
        this.playerTwo = new NetworkPlayer(this, playerTwoSocket);
    }

    public void notifyOpponent(NetworkPlayer player, int move)
    {
        if (player == this.playerOne) {
            this.playerTwo.sendMessage(ConnectNProtocol.constructMove(move));
        }
        else if (player == this.playerTwo) {
            this.playerOne.sendMessage(ConnectNProtocol.constructMove(move));
        }
    }

    @Override
    public void run()
    {
        boolean playAgain = true;
        boolean playerOneFirst = true;

        while (playAgain) {
            runGame(playerOneFirst);

            try {
                playAgain = checkRematch();
            } catch (IOException e) {
                System.err.println("Error verifying rematch");
                playAgain = false;
            }

            // switch player order
            playerOneFirst = !playerOneFirst;
        }

        this.server.addToPlayerPool(this.playerOneSocket);
        this.server.addToPlayerPool(this.playerTwoSocket);
    }

    public void runGame(boolean playerOneFirst) {
        Thread myThread;
        if (playerOneFirst) {
            myThread = new Thread(new GameManager(this.playerOne, this.playerTwo));
        } else {
            myThread = new Thread(new GameManager(this.playerTwo, this.playerOne));
        }

        this.server.printMessage("Starting game.");
        this.playerOne.sendMessage(ConnectNProtocol.START);
        this.playerTwo.sendMessage(ConnectNProtocol.START);
        myThread.start();
        try {
            myThread.join();
        } catch (InterruptedException e) {
            System.err.println("Game interrupted");
            e.printStackTrace();
        }
    }

    public boolean checkRematch() throws IOException
    {
        this.playerOne.sendMessage(ConnectNProtocol.REMATCH);
        this.playerTwo.sendMessage(ConnectNProtocol.REMATCH);

        String p1Reply = this.playerOne.receiveMessage();
        String p2Reply = this.playerTwo.receiveMessage();

        // let players know each other's responses
        this.playerOne.sendMessage(p2Reply);
        this.playerTwo.sendMessage(p1Reply);

        // only play again if both reply 'yes'
        if (p1Reply.equals(ConnectNProtocol.YES)
            && p2Reply.equals(ConnectNProtocol.YES)) {
            return true;
        }

        return false;
    }
}
