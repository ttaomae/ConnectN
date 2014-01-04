package ttaomae.connectn;

public class InvalidPlayer implements Player
{
    @Override
    public int getMove(Board board)
    {
        return -1;
    }
}
