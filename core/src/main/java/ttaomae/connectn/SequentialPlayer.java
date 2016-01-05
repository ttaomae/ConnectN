package ttaomae.connectn;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A sequential Player. Always selects the leftmost valid move.
 *
 * @author Todd Taomae
 */
public class SequentialPlayer implements Player
{
    @Override
    public int getMove(Board board)
    {
        checkNotNull(board, "board must not be null");

        for (int move = 0; move < board.getWidth(); move++) {
            if (board.isValidMove(move)) {
                return move;
            }
        }

        // there are no valid moves
        return -1;
    }
}
