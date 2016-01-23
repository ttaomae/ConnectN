package ttaomae.connectn.network;

/**
 * Signals that an unexpected protocol message was received.
 *
 * @author Todd Taomae
 */
public class ProtocolException extends RuntimeException
{
    private static final long serialVersionUID = 6282071036244524072L;

    public ProtocolException()
    {
        super();
    }

    public ProtocolException(String message)
    {
        super(message);
    }

    public ProtocolException(Throwable cause)
    {
        super(cause);
    }

    public ProtocolException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
