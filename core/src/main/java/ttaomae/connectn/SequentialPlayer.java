package ttaomae.connectn;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;

/**
 * A sequential Player. Always selects the leftmost valid move.
 *
 * @author Todd Taomae
 */
public class SequentialPlayer implements Player
{
    @Override
    public Optional<Integer> getMove(Board board)
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
