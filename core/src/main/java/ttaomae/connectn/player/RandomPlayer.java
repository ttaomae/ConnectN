package ttaomae.connectn.player;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import ttaomae.connectn.ImmutableBoard;

/**
 * A random Player. Selects a new valid, random move.
 *
 * @author Todd Taomae
 */
public class RandomPlayer implements Player
{
    private Random rand;

    /**
     * Constructs a new RandomPlayer.
     */
    public RandomPlayer()
    {
        this.rand = new Random();
    }

    @Override
    public Optional<Integer> getMove(ImmutableBoard board)
    {
        checkNotNull(board, "board must not be null");

        // find all valid moves
        List<Integer> validMoves = new ArrayList<>();
        for (int col = 0; col < board.getWidth(); col++) {
            if (board.isValidMove(col)) {
                validMoves.add(col);
            }
        }

        // pick a random valid move
        if (validMoves.size() != 0) {
            return Optional.of(validMoves.get(this.rand.nextInt(validMoves.size())));
        }

        // there are no valid moves
        return Optional.empty();
    }

}
