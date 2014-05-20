package ttaomae.connectn.network;

public class ConnectNProtocol
{
    public static final String READY = "READY";
    public static final String MOVE = "MOVE";

    public static int parseMove(String message)
    {
        try {
            return Integer.parseInt(message.substring(ConnectNProtocol.MOVE.length()));
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }

    public static String constructMessage(int move)
    {
        return MOVE + move;
    }
}
