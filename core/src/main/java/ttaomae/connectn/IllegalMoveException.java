package ttaomae.connectn;

/**
 * Thrown to indicate that an illegal move has been attempted.
 * 
 * @author Todd Taomae
 */
public class IllegalMoveException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new IllegalMoveException.
     */
    public IllegalMoveException()
    {
        super();
    }

    /**
     * Constructs a new IllegalMoveException with the specified message.
     *
     * @param message the message for this exception
     */
    public IllegalMoveException(String message)
    {
        super(message);
    }
}
