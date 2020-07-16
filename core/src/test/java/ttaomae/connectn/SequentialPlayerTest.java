package ttaomae.connectn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import ttaomae.connectn.player.Player;
import ttaomae.connectn.player.SequentialPlayer;

public class SequentialPlayerTest
{
    @Test
    public void testSequentialPlayer()
    {
        Player player = new SequentialPlayer();
        Board board = new ArrayBoard();
        assertEquals("failure - first move is 0", Integer.valueOf(0),
                player.getMove(board.getImmutableView()).get());

        for (int col = 0; col < board.getWidth(); col++) {
            // fill column
            for (int row = 0; row < board.getHeight(); row++) {
                board.play(col);
            }
            // do not test after the last column (board is full)
            if (col != board.getWidth() - 1) {
                assertEquals("failure - " + (col + 1) + " full columns",
                        Integer.valueOf(col + 1), player.getMove(board.getImmutableView()).get());
            }
        }

        assertFalse("failure - full board", player.getMove(board.getImmutableView()).isPresent());
    }
}
