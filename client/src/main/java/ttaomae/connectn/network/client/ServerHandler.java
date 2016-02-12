package ttaomae.connectn.network.client;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ttaomae.connectn.Board;
import ttaomae.connectn.Piece;
import ttaomae.connectn.Player;
import ttaomae.connectn.network.LostConnectionException;
import ttaomae.connectn.network.ProtocolEvent;
import ttaomae.connectn.network.ProtocolEvent.Message;
import ttaomae.connectn.network.ProtocolException;
import ttaomae.connectn.network.ProtocolHandler;
import ttaomae.connectn.network.ProtocolListener;

/**
 * A Connect-N network multiplayer client.
 *
 * @author Todd Taomae
 */
public class ServerHandler implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private final Socket socket;
    private final ProtocolHandler protocolHandler;

    private Player player;
    /** This client's copy of the board */
    private Board board;

    /**
     * Indicates whether or not this Client is waiting for some kind of
     * response. For example, it may be waiting to confirm whether or not to
     * accept a rematch.
     */
    private volatile boolean waitingForResponse;

    /**
     * Constructs a new Client which connects to the server specified by the
     * host and port. Moves are selected by the specified Player and are played
     * on the specified Board. It is the client's responsibility to keep a
     * properly updated board.
     *
     * @param host the host name, or null for the loopback address
     * @param port the port number
     * @param player the player that will select moves
     * @param board the client's copy of the board
     * @throws UnknownHostException - if the IP address of the host could not be determined
     * @throws IOException if an I/O error occurs when connecting to the server
     * @throws IllegalArgumentException if the player or board is null or if the port is invalid
     */
    public ServerHandler(Socket socket, Player player, Board board) throws IOException
    {
        checkNotNull(socket, "socket must not be null");
        checkNotNull(player, "player must not be null");
        checkNotNull(board, "board must not be null");

        this.socket = socket;
        this.protocolHandler = new ProtocolHandler(socket);

        this.player = player;
        this.board = board;
    }

    /**
     * Closes the socket.
     */
    public void disconnect()
    {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Continuously reads from the server and responds to each message based on
     * the Connect-N network protocol.
     */
    @Override
    public void run()
    {
        while (true) {
            try {
                ProtocolEvent event;
                event = this.protocolHandler.receiveEvent();
                if (event.getMessage() == Message.START_GAME) {
                    // make sure the board is empty by undoing everything
                    while (this.board.getCurrentTurn() != 0) {
                        this.board.undoPlay();
                    }

                    boolean gameSuccessful = playGame();

                    if (gameSuccessful) {
                        // wait for rematch request
                        event = this.protocolHandler.receiveEvent();

                        if (event.getMessage() == Message.REQUEST_REMATCH) {
                            getRematch();
                        }
                        else {
                            throw new ProtocolException(protocolExceptionMessage(
                                    Message.REQUEST_REMATCH, event.getMessage()));
                        }

                        // wait for rematch response
                        event = this.protocolHandler.receiveEvent();
                        if (!event.getMessage().isRematchResponse()) {
                            throw new ProtocolException("Expected rematch response but recieved "
                                    + event.getMessage());
                        }
                    }
                }
                else {
                    throw new ProtocolException(protocolExceptionMessage(
                            Message.START_GAME, event.getMessage()));
                }
            }
            catch (LostConnectionException e) {
                logger.info("Lost connection with server.");
                break;
            }
        }
    }

    /**
     * Plays a game.
     *
     * @return whether or not the game finished successfully
     */
    private boolean playGame() throws LostConnectionException
    {
        while (this.board.getWinner() == Piece.NONE) {
            ProtocolEvent event = this.protocolHandler.receiveEvent();
            switch (event.getMessage()) {
                case REQUEST_MOVE:
                    // get a move and make sure if its valid
                    // if it's not, try again
                    int move;
                    do {
                        move = player.getMove(this.board.getImmutableView())
                                .orElse(Board.INVALID_MOVE);
                    } while (!this.board.isValidMove(move));

                    // update our local copy of the board
                    this.board.play(move);
                    // send move to server
                    this.protocolHandler.sendPlayerMove(move);
                    break;
                case OPPONENT_MOVE:
                    this.board.play(event.getMove().orElseThrow(() ->
                            new ProtocolException("Received invalid opponent move")));
                    break;
                case OPPONENT_DISCONNECTED:
                    return false;
                default:
                    throw new ProtocolException("Recieved unexpected message: "
                            + event.getMessage());
            }
        }

        logger.info(this.board.getWinner() + " wins!");
        return true;
    }

    /**
     * Waits for someone to call confirmRematch or denyRematch.
     * @throws LostConnectionException
     */
    private synchronized void getRematch() throws LostConnectionException
    {
        this.waitingForResponse = true;
        try {
            while (this.waitingForResponse) {
                this.wait();
            }
        } catch (InterruptedException e) {
            this.protocolHandler.sendMessage(Message.DENY_REMATCH);
        }
    }

    /**
     * Confirms a rematch. If the client is waiting for a response a message
     * will be sent to the server. Otherwise nothing will happen.
     * @throws LostConnectionException
     */
    public synchronized void confirmRematch() throws LostConnectionException
    {
        if (this.waitingForResponse) {
            this.protocolHandler.sendMessage(Message.ACCEPT_REMATCH);
            this.waitingForResponse = false;
            this.notifyAll();
        }
    }

    /**
     * Denies a rematch. If the client is waiting for a response a message will
     * be sent to the server. Otherwise nothing will happen.
     * @throws LostConnectionException
     */
    public synchronized void denyRematch() throws LostConnectionException
    {
        if (this.waitingForResponse) {
            this.protocolHandler.sendMessage(Message.DENY_REMATCH);
            this.waitingForResponse = false;
            this.notifyAll();
        }
    }

    public void addProtocolListener(ProtocolListener listener)
    {
        this.protocolHandler.addListener(listener);
    }

    private String protocolExceptionMessage(Message expected, Message received)
    {
        return String.format("Expected %s but received %s", expected, received);
    }
}
