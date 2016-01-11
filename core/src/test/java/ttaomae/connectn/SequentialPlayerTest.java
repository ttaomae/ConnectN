package ttaomae.connectn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class SequentialPlayerTest
{
    @Test
    public void testSequentialPlayer()
    {
        Player player = new SequentialPlayer();
        Board board = new ArrayBoard();
        assertEquals("failure - first move is 0", new Integer(0),
                player.getMove(board.getImmutableView()).get());

        for (int col = 0; col < board.getWidth(); col++) {
            // fill column
            for (int row = 0; row < board.getHeight(); row++) {
                board.play(col);
            }
            // do not test after the last column (board is full)
            if (col != board.getWidth() - 1) {
                assertEquals("failure - " + (col + 1) + " full columns",
                        new Integer(col + 1), player.getMove(board.getImmutableView()).get());
            }
        }

        assertFalse("failure - full board", player.getMove(board.getImmutableView()).isPresent());
    }
}
