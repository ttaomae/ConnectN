package ttaomae.connectn;

/**
 * A Player which always selects an invalid move.
 * 
 * @author Todd
 * 
 */
public class InvalidPlayer implements Player
{
    @Override
    public int getMove(Board board)
    {
        return -1;
    }
}
