package ttaomae.connectn.player;

import java.util.Optional;

import ttaomae.connectn.Board;
import ttaomae.connectn.ImmutableBoard;

/**
 * A Player which always selects an invalid move.
 *
 * @author Todd
 *
 */
public class InvalidPlayer implements Player
{
    @Override
    public Optional<Integer> getMove(ImmutableBoard board)
    {
        return Optional.of(Board.INVALID_MOVE);
    }
}
