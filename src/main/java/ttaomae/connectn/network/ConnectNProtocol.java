package ttaomae.connectn.network;

import ttaomae.connectn.IllegalMoveException;

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

    public static String constructMove(int move)
    {
        return MOVE + move;
    }

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

    public static int parseMove(String message)
    {
        if (!verifyMove(message)) {
            throw new IllegalMoveException("Message does not describe a valid move: " + message);
        }

        return Integer.parseInt(message.substring(MOVE.length()));
    }

}
