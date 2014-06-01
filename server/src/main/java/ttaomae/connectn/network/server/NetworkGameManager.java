package ttaomae.connectn.network.server;

import java.io.IOException;
import java.net.Socket;

import ttaomae.connectn.GameManager;
import ttaomae.connectn.IllegalMoveException;
import ttaomae.connectn.network.ConnectNProtocol;

public class NetworkGameManager implements Runnable
{
    private Server server;
    private Socket playerOneSocket;
    private Socket playerTwoSocket;
    private NetworkPlayer playerOne;
    private NetworkPlayer playerTwo;

    public NetworkGameManager(Server server, Socket playerOneSocket, Socket playerTwoSocket)
            throws IOException
    {
        if (server == null) {
            throw new IllegalArgumentException("server must not be null");
        }

        if (playerOneSocket == null || playerTwoSocket == null) {
            throw new IllegalArgumentException("socket must not be null");
        }

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
            if (runGame(playerOneFirst)) {
                playAgain = checkRematch();
            }
            else {
                playAgain = checkConnections();
            }

            // switch player order
            playerOneFirst = !playerOneFirst;
        }

        this.server.addToPlayerPool(this.playerOneSocket);
        this.server.addToPlayerPool(this.playerTwoSocket);
    }

    /**
     * Runs a game between the two players managed by this NetworkGameManager.
     * Determines which player goes first based on the specified boolean.
     * Returns true if the game ended successfully, false otherwise.
     *
     * @param playerOneFirst true if player 1 will go first, false otherwise
     * @return true if the game ended successfully, false otherwise
     */
    private boolean runGame(boolean playerOneFirst)
    {
        // create game manager
        // the client should check for invalid moves, so only allow one bad attempt
        GameManager gm;
        if (playerOneFirst) {
            gm = new GameManager(this.playerOne, this.playerTwo, 1);
        } else {
            gm = new GameManager(this.playerTwo, this.playerOne, 1);
        }

        this.server.printMessage("Starting game.");
        this.playerOne.sendMessage(ConnectNProtocol.START);
        this.playerTwo.sendMessage(ConnectNProtocol.START);

        // run the game manager
        // if there is an error, it probably means that the client disconnected
        try {
            gm.run();
            return true;
        } catch (IllegalMoveException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    private boolean checkRematch()
    {
        this.playerOne.sendMessage(ConnectNProtocol.REMATCH);
        this.playerTwo.sendMessage(ConnectNProtocol.REMATCH);

        boolean p1Connected = true;
        // get a reply from player one
        // if anything went wrong, assume they disconnected
        String p1Reply = null;
        try {
            p1Reply = this.playerOne.receiveMessage();
            if (p1Reply == null) {
                p1Connected = false;
            }
        } catch (IOException e) {
            p1Connected = false;
        }

        boolean p2Connected = true;
        // get a reply from player two
        // if anything went wrong, assume they disconnected
        String p2Reply = null;
        try {
            p2Reply = this.playerTwo.receiveMessage();
            if (p2Reply == null) {
                p2Connected = false;
            }
        } catch (IOException e) {
            p2Connected = false;
        }

        if (!p1Connected) {
            this.closeSocket(PlayerNum.ONE);
        }
        if (!p2Connected) {
            this.closeSocket(PlayerNum.TWO);
        }

        // let players know each other's responses
        this.playerOne.sendMessage(p2Reply);
        this.playerTwo.sendMessage(p1Reply);

        // only play again if both reply 'yes'
        if (p1Reply != null && p1Reply.equals(ConnectNProtocol.YES)
            && p2Reply != null && p2Reply.equals(ConnectNProtocol.YES)) {
            return true;
        }
        return false;
    }

    private boolean checkConnections()
    {
        boolean result = true;

        // ping the players to make sure they are still connected
        this.playerOne.sendMessage(ConnectNProtocol.PING);
        this.playerTwo.sendMessage(ConnectNProtocol.PING);

        boolean p1Connected = true;
        try {
            String p1Reply = this.playerOne.receiveMessage();
            if (p1Reply == null) {
                p1Connected = false;
            }

        } catch (IOException e) {
            p1Connected = false;
        }

        boolean p2Connected = true;
        try {
            String p2Reply = this.playerTwo.receiveMessage();
            if (p2Reply == null) {
                p2Connected = false;
            }

        } catch (IOException e) {
            p2Connected = false;
        }

        if (!p1Connected) {
            this.closeSocket(PlayerNum.ONE);
            result = false;
        }
        if (!p2Connected) {
            this.closeSocket(PlayerNum.TWO);
            result = false;
        }

        return result;
    }

    private enum PlayerNum
    {
        ONE, TWO;
    }

    private void closeSocket(PlayerNum player)
    {
        try {
            switch (player) {
                case ONE:
                    this.playerOneSocket.close();
                    this.playerTwo.sendMessage(ConnectNProtocol.DICONNECTED);
                    break;
                case TWO:
                    this.playerTwoSocket.close();
                    this.playerOne.sendMessage(ConnectNProtocol.DICONNECTED);
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
