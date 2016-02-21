package ttaomae.connectn;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import ttaomae.connectn.player.Player;
import ttaomae.connectn.player.RandomPlayer;

public class RandomPlayerTest
{
    @Test
    public void testRandomPlayer()
    {
        Board board = new ArrayBoard();
        Player player = new RandomPlayer();

        for (int i = 0; i < board.getHeight() * board.getWidth(); i++) {
            Optional<Integer> optionalMove = player.getMove(board.getImmutableView());
            assertTrue(optionalMove.isPresent());
            assertTrue("failure - selects valid move", board.isValidMove(optionalMove.get()));
            board.play(optionalMove.get());
        }

        Optional<Integer> optionalMove = player.getMove(board.getImmutableView());
        assertFalse(optionalMove.isPresent());
    }
}
