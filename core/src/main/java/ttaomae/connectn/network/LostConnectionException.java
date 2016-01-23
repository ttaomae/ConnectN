package ttaomae.connectn.network;

/**
 * Signals that the connection between the server and a client was lost.
 *
 * @author Todd Taomae
 */
public class LostConnectionException extends Exception
{
    private static final long serialVersionUID = -6751419202289960753L;

    public LostConnectionException()
    {
        super();
    }

    public LostConnectionException(String message)
    {
        super(message);
    }

    public LostConnectionException(Throwable cause)
    {
        super(cause);
    }

    public LostConnectionException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
