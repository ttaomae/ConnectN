package ttaomae.connectn;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InvalidPlayerTest
{
    @Test
    public void testInvalidPlayer()
    {
        Board board = new Board();
        Player player = new InvalidPlayer();
        Player rand = new RandomPlayer();

        assertEquals("failure - first play invalid", -1, player.getMove(board));
        for (int i = 0; i < board.getHeight() * board.getWidth(); i++) {
            // play a random move
            board.play(rand.getMove(board));
            assertEquals("failure - all moves invalid", -1, player.getMove(board));
        }
    }
}
