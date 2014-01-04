package ttaomae.connectn;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SequentialPlayerTest
{
    @Test
    public void testSequentialPlayer()
    {
        Player player = new SequentialPlayer();
        Board board = new Board();
        assertEquals("failure - first move is 0", 0, player.getMove(board));

        for (int col = 0; col < board.getWidth(); col++) {
            // fill column
            for (int row = 0; row < board.getHeight(); row++) {
                board.play(col);
            }
            // do not test after the last column (board is full)
            if (col != board.getWidth() - 1) {
                assertEquals("failure - " + (col + 1) + " full columns", col + 1,
                        player.getMove(board));
            }
        }

        assertEquals("failure - full board", -1, player.getMove(board));
    }
}
