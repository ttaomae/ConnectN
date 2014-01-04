package ttaomae.connectn;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class RandomPlayerTest
{
    @Test
    public void testRandomPlayer()
    {
        Board board = new Board();
        Player player = new RandomPlayer();

        for (int i = 0; i < board.getHeight() * board.getWidth(); i++) {
            int move = player.getMove(board);
            assertTrue("failure - selects valid move", board.isValidMove(move));
            board.play(move);
        }

        assertFalse("failure - full board", board.isValidMove(player.getMove(board)));
    }
}
