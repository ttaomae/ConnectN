package ttaomae.connectn.network.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ttaomae.connectn.GameManager;
import ttaomae.connectn.IllegalMoveException;
import ttaomae.connectn.network.ConnectNProtocol;

/**
 * Manages a series of games between two clients.
 *
 * @author Todd Taomae
 */
public class NetworkGameManager implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(NetworkGameManager.class);

    private Server server;
    private Socket playerOneSocket;
    private Socket playerTwoSocket;
    private NetworkPlayer playerOne;
    private NetworkPlayer playerTwo;

    /**
     * Constructs a new NetworkGameManager which is part of the specified Server
     * and manages games between two clients specified by the sockets they are
     * connected to.
     *
     * @param server the server that this NetworkGameManager is part of
     * @param playerOneSocket the socket that the first player is connected to
     * @param playerTwoSocket the socket that the second player is connected to
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the server or either socket is null.
     */
    public NetworkGameManager(Server server, Socket playerOneSocket, Socket playerTwoSocket)
            throws IOException
    {
        checkNotNull(server, "server must not be null");
        checkNotNull(playerOneSocket, "playerOneSocket must not be null");
        checkNotNull(playerTwoSocket, "playerTwoSocket must not be null");

        this.server = server;
        this.playerOneSocket = playerOneSocket;
        this.playerTwoSocket = playerTwoSocket;
        this.playerOne = new NetworkPlayer(this, playerOneSocket);
        this.playerTwo = new NetworkPlayer(this, playerTwoSocket);
    }

    /**
     * Notify the opponent that the specified player has played the specified
     * move. If the player parameter is neither of this NetworkGameManagers
     * players then nothing happens.
     *
     * @param player the player that played a move
     * @param move the move that was played
     */
    public void notifyOpponent(NetworkPlayer player, int move)
    {
        if (player == this.playerOne) {
            this.playerTwo.sendMessage(ConnectNProtocol.constructMove(move));
        }
        else if (player == this.playerTwo) {
            this.playerOne.sendMessage(ConnectNProtocol.constructMove(move));
        }
    }

    /**
     * Repeatedly starts games between the players as long as they both agree to
     * a rematch.
     */
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

        logger.info("Starting game.");
        this.playerOne.sendMessage(ConnectNProtocol.START);
        this.playerTwo.sendMessage(ConnectNProtocol.START);

        // run the game manager
        // if there is an error, it probably means that the client disconnected
        try {
            gm.run();
            return true;
        } catch (IllegalMoveException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    /**
     * Asks both players if they want a rematch.
     *
     * @return true if both players agree to a rematch
     */
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

    /**
     * Checks if both players are still connected by sending a PING message and
     * waiting for a response.
     *
     * @return true if both players respond.
     */
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

    /**
     * Closes the socket of the specified player and sends a DISCONNECTED
     * message to the opponent.
     *
     * @param player the player whose socket is being closed.
     */
    private void closeSocket(PlayerNum player)
    {
        try {
            switch (player) {
                case ONE:
                    this.playerOneSocket.close();
                    this.playerTwo.sendMessage(ConnectNProtocol.DICONNECTED);
                    logger.info("Player has disconnected.");
                    break;
                case TWO:
                    this.playerTwoSocket.close();
                    this.playerOne.sendMessage(ConnectNProtocol.DICONNECTED);
                    logger.info("Player has disconnected.");
                    break;
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
