package ttaomae.connectn;

import java.util.Optional;

/**
 * A Player which always selects an invalid move.
 *
 * @author Todd
 *
 */
public class InvalidPlayer implements Player
{
    @Override
    public Optional<Integer> getMove(Board board)
    {
        return Optional.of(Board.INVALID_MOVE);
    }
}
