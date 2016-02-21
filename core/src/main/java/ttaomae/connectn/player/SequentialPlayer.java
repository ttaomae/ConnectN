package ttaomae.connectn.player;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

import ttaomae.connectn.ImmutableBoard;

/**
 * A sequential Player. Always selects the leftmost valid move.
 *
 * @author Todd Taomae
 */
public class SequentialPlayer implements Player
{
    @Override
    public Optional<Integer> getMove(ImmutableBoard board)
    {
        checkNotNull(board, "board must not be null");

        for (int move = 0; move < board.getWidth(); move++) {
            if (board.isValidMove(move)) {
                return Optional.of(move);
            }
        }

        // there are no valid moves
        return Optional.empty();
    }
}
