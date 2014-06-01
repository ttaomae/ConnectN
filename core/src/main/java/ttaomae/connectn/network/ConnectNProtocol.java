package ttaomae.connectn.network;

import ttaomae.connectn.IllegalMoveException;

/**
 * A class providing static methods and constants to facilitate communication
 * between the client and server.
 *
 * @author Todd Taomae
 */
public class ConnectNProtocol
{
    public static final String START = "START";
    public static final String READY = "READY";
    public static final String MOVE = "MOVE";
    public static final String REMATCH = "REMATCH";
    public static final String YES = "YES";
    public static final String NO = "NO";
    public static final String DICONNECTED = "DISCONNECTED";
    public static final String PING = "PING";
    public static final long PING_INTERVAL = 5000;

    /**
     * Constructs a move message with the specified move.
     *
     * @param move the move
     * @return the constructed move
     */
    public static String constructMove(int move)
    {
        return MOVE + move;
    }

    /**
     * Verifies that the specified message is a valid move message.
     *
     * @param message the message being verified
     * @return true if the message is a valid move message, false otherwise
     */
    public static boolean verifyMove(String message)
    {
        if (!message.startsWith(MOVE)) {
            return false;
        }

        try {
            Integer.parseInt(message.substring(MOVE.length()));
            // if we reach here, the above line did not throw an exception so it
            // must be a valid int
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns the move described by the specified message.
     *
     * @param message the message to parse
     * @return the move described by the specified message
     *
     * @throws IllegalMoveException if the message is not a valid move message
     */
    public static int parseMove(String message)
    {
        if (!verifyMove(message)) {
            throw new IllegalMoveException("Message does not describe a valid move: " + message);
        }

        return Integer.parseInt(message.substring(MOVE.length()));
    }

}
