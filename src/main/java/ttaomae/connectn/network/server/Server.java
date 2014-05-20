package ttaomae.connectn.network.server;

import java.io.IOException;
import java.net.ServerSocket;

import ttaomae.connectn.GameManager;

public class Server implements Runnable
{
    private NetworkPlayer playerOne;
    private NetworkPlayer playerTwo;
    private int portNumber;

    public Server(int portNumber)
    {
        this.portNumber = portNumber;
    }

    @Override
    public void run()
    {
        try (ServerSocket serverSocket = new ServerSocket(this.portNumber);) {
            // get two connections from players
            playerOne = new NetworkPlayer(this, serverSocket.accept());
            playerTwo = new NetworkPlayer(this, serverSocket.accept());

            // start new game
            GameManager gm = new GameManager(playerOne, playerTwo);

            Thread t = new Thread(gm);
            t.start();
            t.join();

        } catch (IOException e) {
            System.err.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void notifyOpponent(NetworkPlayer player, int move)
    {
        if (player == this.playerOne) {
            this.playerTwo.sendOpponentMove(move);
        }
        else if (player == this.playerTwo) {
            this.playerOne.sendOpponentMove(move);
        }
    }
}
