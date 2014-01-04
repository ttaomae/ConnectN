package ttaomae.connectn;

public class IllegalMoveException extends RuntimeException
{
    private static final long serialVersionUID = 1L;

    public IllegalMoveException()
    {
        super();
    }

    public IllegalMoveException(String message)
    {
        super(message);
    }
}
