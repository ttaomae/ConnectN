package ttaomae.connectn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

public class GameManagerTest
{
    @Test
    public void testGameManagerConstructors()
    {
        Board board = new ArrayBoard();
        Player playerOne = new RandomPlayer();
        Player playerTwo = new RandomPlayer();

        // test 2 argument constructor
        try {
            new GameManager(null, playerTwo);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("failure - playerOne argument null", "playerOne must not be null",
                    expected.getMessage());
        }
        try {
            new GameManager(playerOne, null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("failure - playerTwo argument null", "playerTwo must not be null",
                    expected.getMessage());
        }

        // test 3 argument constructor (Player, Player, int)
        try {
            new GameManager(null, playerTwo);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("failure - playerOne argument null", "playerOne must not be null",
                    expected.getMessage());
        }
        try {
            new GameManager(playerOne, playerTwo, -1);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("failure - attemptsAllowed argument negative",
                    "attemptsAllowed must be positive", expected.getMessage());
        }

        // test 3 argument constructor (Board, Player, Player)
        try {
            new GameManager(null, playerOne, playerTwo);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("failure - board argument null", "board must not be null",
                    expected.getMessage());
        }
        try {
            new GameManager(board, null, playerTwo);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("failure - playerOne argument null", "playerOne must not be null",
                    expected.getMessage());
        }
        try {
            new GameManager(board, playerOne, null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("failure - playerTwo argument null", "playerTwo must not be null",
                    expected.getMessage());
        }

        // test 4 argument constructor
        try {
            new GameManager(null, playerOne, playerTwo, 1);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("failure - board argument null", "board must not be null",
                    expected.getMessage());
        }
        try {
            new GameManager(board, null, playerTwo, 1);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("failure - playerOne argument null", "playerOne must not be null",
                    expected.getMessage());
        }
        try {
            new GameManager(board, playerOne, null, 1);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("failure - playerTwo argument null", "playerTwo must not be null",
                    expected.getMessage());
        }
        try {
            new GameManager(board, playerOne, playerTwo, -1);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("failure - attemptsAllowed argument negative",
                    "attemptsAllowed must be positive", expected.getMessage());
        }
    }

    @Test
    public void testGameManagerWithSequentialPlayers()
    {
        Board board = new ArrayBoard();

        GameManager gm = new GameManager(board, new SequentialPlayer(), new SequentialPlayer());
        gm.run();
        assertEquals("failure - first sequential player wins ", Piece.BLACK, board.getWinner());
    }

    @Test
    public void testGameManagerWithRandomPlayers()
    {
        Board board = new ArrayBoard();

        GameManager gm = new GameManager(board, new RandomPlayer(), new RandomPlayer());
        gm.run();
        assertTrue("failure - random game ends", board.getWinner() != Piece.NONE);
    }

    @Test
    public void testGameManagerWithInvalidPlayers()
    {
        GameManager gm = new GameManager(new InvalidPlayer(), new InvalidPlayer(), 10);
        try {
            gm.run();
            fail();
        } catch (IllegalMoveException expected) {
            assertEquals("failure - throws exception", "BLACK attempted 10 illegal moves",
                    expected.getMessage());
        }
    }
}
