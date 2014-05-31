package ttaomae.connectn;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class GameManagerTest
{
    @Test
    public void testGameManagerConstructors()
    {
        Board board = new Board();
        Player playerOne = new RandomPlayer();
        Player playerTwo = new RandomPlayer();

        // test 2 argument constructor
        try {
            new GameManager(null, playerTwo);
        } catch (IllegalArgumentException e) {
            assertEquals("failure - playerOne argument null", "playerOne must not be null",
                    e.getMessage());
        }
        try {
            new GameManager(playerOne, null);
        } catch (IllegalArgumentException e) {
            assertEquals("failure - playerTwo argument null", "playerTwo must not be null",
                    e.getMessage());
        }

        // test 3 argument constructor (Player, Player, int)
        try {
            new GameManager(playerOne, playerTwo);
        } catch (IllegalArgumentException e) {
            assertEquals("failure - board argument null", "board must not be null", e.getMessage());
        }
        try {
            new GameManager(null, playerTwo);
        } catch (IllegalArgumentException e) {
            assertEquals("failure - playerOne argument null", "playerOne must not be null",
                    e.getMessage());
        }
        try {
            new GameManager(playerOne, playerTwo, -1);
        } catch (IllegalArgumentException e) {
            assertEquals("failure - attemptsAllowed argument negative",
                    "attemptsAllowed must be positive", e.getMessage());
        }

        // test 3 argument constructor (Board, Player, Player)
        try {
            new GameManager(null, playerOne, playerTwo);
        } catch (IllegalArgumentException e) {
            assertEquals("failure - board argument null", "board must not be null",
                    e.getMessage());
        }
        try {
            new GameManager(board, null, playerTwo);
        } catch (IllegalArgumentException e) {
            assertEquals("failure - playerOne argument null", "playerOne must not be null",
                    e.getMessage());
        }
        try {
            new GameManager(board, playerOne, null);
        } catch (IllegalArgumentException e) {
            assertEquals("failure - playerTwo argument null", "playerTwo must not be null",
                    e.getMessage());
        }

        // test 4 argument constructor
        try {
            new GameManager(null, playerOne, playerTwo, 1);
        } catch (IllegalArgumentException e) {
            assertEquals("failure - board argument null", "board must not be null", e.getMessage());
        }
        try {
            new GameManager(board, null, playerTwo, 1);
        } catch (IllegalArgumentException e) {
            assertEquals("failure - playerOne argument null", "playerOne must not be null",
                    e.getMessage());
        }
        try {
            new GameManager(board, playerOne, null, 1);
        } catch (IllegalArgumentException e) {
            assertEquals("failure - playerTwo argument null", "playerTwo must not be null",
                    e.getMessage());
        }
        try {
            new GameManager(board, playerOne, playerTwo, -1);
        } catch (IllegalArgumentException e) {
            assertEquals("failure - attemptsAllowed argument negative",
                    "attemptsAllowed must be positive", e.getMessage());
        }
    }

    @Test
    public void testGameManagerWithSequentialPlayers()
    {
        Board board = new Board();

        GameManager gm = new GameManager(board, new SequentialPlayer(), new SequentialPlayer());
        gm.run();
        assertEquals("failure - first sequential player wins ", Piece.BLACK, board.getWinner());
    }

    @Test
    public void testGameManagerWithRandomPlayers()
    {
        Board board = new Board();

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
        } catch (IllegalMoveException e) {
            assertEquals("failure - throws exception", "BLACK attempted 10 illegal moves",
                    e.getMessage());
        }
    }
}
