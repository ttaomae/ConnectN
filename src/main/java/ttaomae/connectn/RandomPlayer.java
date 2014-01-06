package ttaomae.connectn;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RandomPlayer implements Player
{
    private Random rand;

    public RandomPlayer()
    {
        this.rand = new Random();
    }

    @Override
    public int getMove(Board board)
    {
        // find all valid moves
        List<Integer> validMoves = new ArrayList<Integer>();
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
