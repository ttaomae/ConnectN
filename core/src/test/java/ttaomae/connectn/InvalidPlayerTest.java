package ttaomae.connectn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

public class InvalidPlayerTest
{
    @Test
    public void testInvalidPlayer()
    {
        Board board = new ArrayBoard();
        Player player = new InvalidPlayer();
        Player rand = new RandomPlayer();

        Optional<Integer> optionalMove = player.getMove(board.getImmutableView());
        assertTrue(optionalMove.isPresent());
        assertEquals("failure - first play invalid",
                new Integer(Board.INVALID_MOVE), optionalMove.get());

        for (int i = 0; i < board.getHeight() * board.getWidth(); i++) {
            board.play(rand.getMove(board.getImmutableView()).get());
            optionalMove = player.getMove(board.getImmutableView());
            assertTrue(optionalMove.isPresent());
            assertEquals("failure - all moves invalid",
                    new Integer(Board.INVALID_MOVE), optionalMove.get());
        }
    }
}
