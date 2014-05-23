package ttaomae.connectn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    public int getMove(Board board)
    {
        // find all valid moves
        List<Integer> validMoves = new ArrayList<>();
        for (int col = 0; col < board.getWidth(); col++) {
            if (board.isValidMove(col)) {
                validMoves.add(col);
            }
        }

        // pick a random valid move
        if (validMoves.size() != 0) {
            return validMoves.get(this.rand.nextInt(validMoves.size()));
        }

        // there are no valid moves
        return -1;
    }

}
