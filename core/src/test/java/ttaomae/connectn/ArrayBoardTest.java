package ttaomae.connectn;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.allOf;

import static org.hamcrest.core.StringContains.containsString;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
public class ArrayBoardTest
{
    private Board board;

    @Before
    public void init()
    {
        this.board = new ArrayBoard();
    }

    @Test
    public void testNewBoard()
    {
        assertEquals("failure - new board height", 6, board.getHeight());
        assertEquals("failure - new board width", 7, board.getWidth());
        assertEquals("failure - new board win condition", 4, board.getWinCondition());

        assertEquals("failure - first piece is black", Piece.BLACK, board.getNextPiece());

        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                assertEquals("failure - new board is empty", Piece.NONE, board.getPieceAt(col, row));
            }
        }

        assertEquals("failure - no winner for new board", Piece.NONE, board.getWinner());
    }

    @Test
    public void testBoardConstructor()
    {
        board = new ArrayBoard(5, 5, 5);
        assertEquals("failure - new Board(5, 5, 5) height", 5, board.getHeight());
        assertEquals("failure - new Board(5, 5, 5) width", 5, board.getWidth());
        assertEquals("failure - new Board(5, 5, 5) win condition", 5, board.getWinCondition());

        try {
            new ArrayBoard(1, 5, 5);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("failure - illegal height", "height must be at least 2", expected.getMessage());
        }
        try {
            new ArrayBoard(5, 1, 5);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("failure - illegal width", "width must be at least 2", expected.getMessage());
        }
        try {
            new ArrayBoard(5, 5, 6);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("failure - illegal win condition",
                    "winCondition must be between 2 and max(height, width)", expected.getMessage());
        }
        try {
            new ArrayBoard(5, 5, 1);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("failure - illegal win condition",
                    "winCondition must be between 2 and max(height, width)", expected.getMessage());
        }
        try {
            new ArrayBoard(5, 5, -1);
            fail();
        } catch (IllegalArgumentException expected) {
            assertEquals("failure - illegal win condition",
                    "winCondition must be between 2 and max(height, width)", expected.getMessage());
        }
    }

    @Test
    public void testOnePlay()
    {
        for (int col = 0; col < board.getWidth(); col++) {
            board = new ArrayBoard();
            board.play(col);
            assertEquals("failure - first play is black", Piece.BLACK, board.getPieceAt(col, 0));
            assertEquals("failure - second piece is red", Piece.RED, board.getNextPiece());
        }
    }

    @Test(expected = IllegalMoveException.class)
    public void testPlaySingleColumn()
    {
        for (int row = 0; row < this.board.getHeight(); row++) {
            Piece lastPiece = this.board.getNextPiece();
            board.play(0);
            assertEquals("failure - last piece is opposite of next piece", lastPiece, board
                    .getNextPiece().opposite());
            assertEquals("failure - play on a single column", lastPiece, board.getPieceAt(0, row));
        }

        // illegal move
        board.play(0);
    }

    @Test
    public void testIllegalPlay()
    {
        try {
            board.play(-1);
            fail();
        } catch (IllegalMoveException expected) {
            assertEquals("failure - illegal move: -1", "Illegal column: -1", expected.getMessage());
        }

        try {
            board.play(board.getWidth());
            fail();
        } catch (IllegalMoveException expected) {
            assertEquals("failure - illegal move: 7", "Illegal column: 7", expected.getMessage());
        }
    }

    @Test
    public void testIllegalPieceAt()
    {
        try {
            board.getPieceAt(-1, 0);
            fail();
        } catch (IndexOutOfBoundsException expected) {
            Assert.assertThat("failure - get piece at (-1, 0)", expected.getMessage(),
                    allOf(containsString("-1"), containsString("must not be negative")));
        }

        try {
            board.getPieceAt(board.getWidth(), 0);
            fail();
        } catch (IndexOutOfBoundsException expected) {
            Assert.assertThat("failure - get piece at (7, 0)", expected.getMessage(),
                    allOf(containsString("7"), containsString("must be less than")));
        }

        try {
            board.getPieceAt(0, -1);
            fail();
        } catch (IndexOutOfBoundsException expected) {
            Assert.assertThat("failure - get piece at (0, -1)", expected.getMessage(),
                    allOf(containsString("-1"), containsString("must not be negative")));
        }

        try {
            board.getPieceAt(0, board.getHeight());
            fail();
        } catch (IndexOutOfBoundsException expected) {
            Assert.assertThat("failure - get piece at (0, 6)", expected.getMessage(),
                    allOf(containsString("6"), containsString("must be less than")));
        }
    }

    @Test
    public void testIsValidMove()
    {
        assertFalse("failure - isValidMove: -1", board.isValidMove(-1));
        assertFalse("failure - isValidMove: 7", board.isValidMove(7));
        for (int i = 0; i < board.getWidth(); i++) {
            assertTrue("failure - isValidMove for empty board", board.isValidMove(i));
        }

        for (int i = 0; i < board.getWidth(); i++) {
            board = new ArrayBoard();
            // fill a single column
            for (int j = 0; j < board.getHeight(); j++) {
                board.play(i);
            }
            assertFalse("failure - isValidMove for full column", board.isValidMove(i));
        }
    }

    @Test
    public void testBlackHorizontalWin()
    {
        board.play(0); // black
        board.play(0);
        board.play(1); // black
        board.play(1);
        board.play(2); // black
        board.play(2);
        board.play(3); // black
        assertEquals("failure - black horizontal winner, bottom left", Piece.BLACK,
                board.getWinner());

        board = new ArrayBoard();
        board.play(3); // black
        board.play(3);
        board.play(4); // black
        board.play(4);
        board.play(5); // black
        board.play(5);
        board.play(6); // black
        assertEquals("failure - black horizontal winner, bottom right", Piece.BLACK,
                board.getWinner());

        board = new ArrayBoard();
        // fill left side of board without a winner
        for (int i = 0; i < 3; i++) {
            board.play(0);
            board.play(1);
            board.play(2);
            board.play(3);
        }
        for (int i = 0; i < 2; i++) {
            board.play(1);
            board.play(0);
            board.play(3);
            board.play(2);
        }
        // black plays 0, 1, 2, 3
        board.play(0); // black
        board.play(6);
        board.play(1); // black
        board.play(6);
        board.play(2); // black
        board.play(5);
        board.play(3); // black
        assertEquals("failure - black horizontal winner, top left", Piece.BLACK, board.getWinner());


        board = new ArrayBoard();
        // fill right side of board without a winner
        for (int i = 0; i < 3; i++) {
            board.play(3);
            board.play(4);
            board.play(5);
            board.play(6);
        }
        for (int i = 0; i < 2; i++) {
            board.play(4);
            board.play(3);
            board.play(6);
            board.play(5);
        }
        // black plays 3, 4, 5, 6
        board.play(3); // black
        board.play(0);
        board.play(4); // black
        board.play(0);
        board.play(5); // black
        board.play(1);
        board.play(6); // black
        assertEquals("failure - black horizontal winner, top right", Piece.BLACK, board.getWinner());
    }

    @Test
    public void testBlackVerticalWin()
    {
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        assertEquals("failure - black vertical winner, bottom left", Piece.BLACK, board.getWinner());

        board = new ArrayBoard();
        board.play(6); // black
        board.play(0);
        board.play(6); // black
        board.play(0);
        board.play(6); // black
        board.play(0);
        board.play(6); // black
        assertEquals("failure - black vertical winner, bottom right", Piece.BLACK,
                board.getWinner());

        board = new ArrayBoard();
        // fill left side of board without a winner
        for (int i = 0; i < 2; i++) {
            board.play(1);
            board.play(0);
        }
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        assertEquals("failure - black vertical winner, top left", Piece.BLACK, board.getWinner());

        board = new ArrayBoard();
        // fill right side of board without a winner
        for (int i = 0; i < 2; i++) {
            board.play(5);
            board.play(6);
        }
        board.play(6); // black
        board.play(0);
        board.play(6); // black
        board.play(0);
        board.play(6); // black
        board.play(0);
        board.play(6); // black
        assertEquals("failure - black vertical winner, top left", Piece.BLACK,
                board.getWinner());
    }

    @Test
    public void testBlackDiagonalWin()
    {
        board.play(0); // black
        board.play(1);
        board.play(1); // black
        board.play(2);
        board.play(2);
        board.play(3);
        board.play(2); // black
        board.play(3);
        board.play(4);
        board.play(3);
        board.play(3); // black
        assertEquals("failure - black diagonal winner, bottom left", Piece.BLACK, board.getWinner());

        board = new ArrayBoard();
        board.play(6); // black
        board.play(5);
        board.play(5); // black
        board.play(4);
        board.play(4);
        board.play(3);
        board.play(4); // black
        board.play(3);
        board.play(2);
        board.play(3);
        board.play(3); // black
        assertEquals("failure - black diagonal winner, bottom right", Piece.BLACK,
                board.getWinner());

        board = new ArrayBoard();
        for (int i = 0; i < 5; i++) {
            board.play(0);
        }
        board.play(1);
        board.play(0); // black
        for (int i = 0; i < 3; i++) {
            board.play(1);
        }
        board.play(1); // black
        for (int i = 0; i < 3; i++) {
            board.play(2);
        }
        board.play(2); // black
        board.play(3);
        board.play(4);
        board.play(3);
        board.play(3); // black
        assertEquals("failure - black diagonal winner, top left", Piece.BLACK, board.getWinner());


        board = new ArrayBoard();
        for (int i = 0; i < 5; i++) {
            board.play(6);
        }
        board.play(5);
        board.play(6); // black
        for (int i = 0; i < 3; i++) {
            board.play(5);
        }
        board.play(5); // black
        for (int i = 0; i < 3; i++) {
            board.play(4);
        }
        board.play(4); // black
        board.play(3);
        board.play(2);
        board.play(3);
        board.play(3); // black
        assertEquals("failure - black diagonal winner, top right", Piece.BLACK, board.getWinner());
    }

    @Test
    public void testRedHorizontalWin()
    {
        board.play(6);
        board.play(0); // red
        board.play(5);
        board.play(1); // red
        board.play(6);
        board.play(2); // red
        board.play(5);
        board.play(3); // red
        assertEquals("failure - red horizontal winner, bottom left", Piece.RED, board.getWinner());

        board = new ArrayBoard();
        board.play(0);
        board.play(3); // red
        board.play(1);
        board.play(4); // red
        board.play(0);
        board.play(5); // red
        board.play(1);
        board.play(6); // red

        assertEquals("failure - red horizontal winner, bottom right", Piece.RED,
                board.getWinner());

        board = new ArrayBoard();
        // fill left side of board without a winner
        for (int i = 0; i < 3; i++) {
            board.play(0);
            board.play(1);
            board.play(2);
            board.play(3);
        }
        for (int i = 0; i < 2; i++) {
            board.play(1);
            board.play(0);
            board.play(3);
            board.play(2);
        }
        // red plays 0, 1, 2, 3
        board.play(6);
        board.play(0); // red
        board.play(6);
        board.play(1); // red
        board.play(5);
        board.play(2); // red
        board.play(5);
        board.play(3); // red
        assertEquals("failure - red horizontal winner, top left", Piece.RED, board.getWinner());

        board = new ArrayBoard();
        // fill right side of board without a winner
        for (int i = 0; i < 3; i++) {
            board.play(3);
            board.play(4);
            board.play(5);
            board.play(6);
        }
        for (int i = 0; i < 2; i++) {
            board.play(4);
            board.play(3);
            board.play(6);
            board.play(5);
        }
        // red plays 3, 4, 5, 6
        board.play(0);
        board.play(3); // red
        board.play(0);
        board.play(4); // red
        board.play(1);
        board.play(5); // red
        board.play(1);
        board.play(6); // red
        assertEquals("failure - red horizontal winner, top right", Piece.RED, board.getWinner());
    }

    @Test
    public void testRedVerticalWin()
    {
        board.play(1);
        board.play(0); // red
        board.play(1);
        board.play(0); // red
        board.play(1);
        board.play(0); // red
        board.play(2);
        board.play(0); // red
        assertEquals("failure - red vertical winner, bottom left", Piece.RED, board.getWinner());

        board = new ArrayBoard();
        board.play(0);
        board.play(6); // red
        board.play(0);
        board.play(6); // red
        board.play(0);
        board.play(6); // red
        board.play(1);
        board.play(6); // red
        assertEquals("failure - red vertical winner, bottom right", Piece.RED, board.getWinner());

        board = new ArrayBoard();
        // fill left side of board without a winner
        for (int i = 0; i < 2; i++) {
            board.play(0);
            board.play(1);
        }
        board.play(1);
        board.play(0); // red
        board.play(1);
        board.play(0); // red
        board.play(1);
        board.play(0); // red
        board.play(2);
        board.play(0); // red
        assertEquals("failure - red vertical winner, top left", Piece.RED, board.getWinner());

        board = new ArrayBoard();
        // fill right side of board without a winner
        for (int i = 0; i < 2; i++) {
            board.play(6);
            board.play(5);
        }
        board.play(0);
        board.play(6); // red
        board.play(0);
        board.play(6); // red
        board.play(0);
        board.play(6); // red
        board.play(1);
        board.play(6); // red
        assertEquals("failure - red vertical winner, top left", Piece.RED, board.getWinner());
    }

    @Test
    public void testRedDiagonalWin()
    {
        board.play(1);
        board.play(0); // red
        board.play(2);
        board.play(1); // red
        board.play(2);
        board.play(2); // red
        board.play(3);
        board.play(3);
        board.play(3);
        board.play(3); // red
        assertEquals("failure - red diagonal winner, bottom left", Piece.RED, board.getWinner());

        board = new ArrayBoard();
        board.play(5);
        board.play(6); // red
        board.play(4);
        board.play(5); // red
        board.play(4);
        board.play(4); // red
        board.play(3);
        board.play(3);
        board.play(3);
        board.play(3); // red
        assertEquals("failure - red diagonal winner, bottom right", Piece.RED, board.getWinner());

        board = new ArrayBoard();
        for (int i = 0; i < 5; i++) {
            board.play(0);
        }
        board.play(0); // red
        for (int i = 0; i < 4; i++) {
            board.play(1);
        }
        board.play(2);
        board.play(1); // red
        board.play(2);
        board.play(3);
        board.play(2);
        board.play(2); // red
        board.play(3);
        board.play(3); // red
        assertEquals("failure - red diagonal winner, top left", Piece.RED, board.getWinner());

        board = new ArrayBoard();
        for (int i = 0; i < 5; i++) {
            board.play(6);
        }
        board.play(6); // red
        for (int i = 0; i < 4; i++) {
            board.play(5);
        }
        board.play(4);
        board.play(5); // red
        board.play(4);
        board.play(3);
        board.play(4);
        board.play(4); // red
        board.play(3);
        board.play(3); // red
        assertEquals("failure - red diagonal winner, top right", Piece.RED, board.getWinner());
    }

    @Test
    public void testDraw()
    {
        // play pattern twice
        for (int i = 0; i < 2; i++) {
            // play three rows
            for (int j = 0; j < 3; j++) {
                // play first six columns
                for (int k = 0; k < 6; k++) {
                    board.play(k);
                }
            }
            // play last column
            board.play(6);
            board.play(6);
            board.play(6);
        }

        assertEquals("failure - board full draw", Piece.DRAW, board.getWinner());
    }

    @Test
    public void testUndoSinglePlay() {
        for (int col = 0; col < board.getWidth(); col++) {
            board.play(col);
            board.undoPlay();

            assertEquals("failure - undo single play " + col, Piece.NONE, board.getPieceAt(col, 0));
            // should be first player's turn now
            assertEquals("failure - next piece after undo single play",
                    Piece.BLACK, board.getNextPiece());
        }
    }

    @Test
    public void testUndoTwoPlaysSameColumn() {
        for(int col = 0; col < board.getWidth(); col++) {
            board.play(col);
            board.play(col);

            board.undoPlay();
            // should be second player's turn now
            assertEquals("failure - undo second play " + col, Piece.NONE, board.getPieceAt(col, 1));
            assertEquals("failure - next piece after undo single play",
                    Piece.RED, board.getNextPiece());

            board.undoPlay();
            // should be first player's turn now

            assertEquals("failure - undo first play " + col, Piece.NONE, board.getPieceAt(col, 0));
            assertEquals("failure - next piece after undo single play",
                    Piece.BLACK, board.getNextPiece());
        }
    }

    @Test
    public void testUndoFullColumn() {
        for (int col = 0; col < board.getWidth(); col++) {
            // fill a single column
            for (int row = 0; row < board.getHeight(); row++) {
                board.play(col);
            }

            for (int row = board.getHeight() - 1; row >= 0; row--) {
                board.undoPlay();
                assertEquals(String.format("failure - undo col: %d, row: %d%n", col, row),
                        Piece.NONE, board.getPieceAt(col, row));
            }
        }
    }

    @Test
    public void testUndoWithNoPlays() {
        try {
            board.undoPlay();
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("No moves to undo.", expected.getMessage());
        }
    }

    @Test
    public void testUndoExtraPlays() {
        board.play(0);
        board.play(1);
        board.play(2);
        board.play(3);
        board.play(4);
        board.play(5);
        board.play(6);

        // undo all plays
        board.undoPlay();
        board.undoPlay();
        board.undoPlay();
        board.undoPlay();
        board.undoPlay();
        board.undoPlay();
        board.undoPlay();

        try {
            // undo extra play
            board.undoPlay();
            fail();
        } catch (IllegalStateException expected) {
            assertEquals("No moves to undo.", expected.getMessage());
        }
    }

    @Test
    public void testListener()
    {
        // this is a workaround for Java's restriction of only being able to
        // capture final variables in anonymous classes/lambdas
        final int[] eventCount = {0};
        board.addBoardListener(() -> eventCount[0]++);

        board.play(0);
        board.play(1);
        board.play(2);
        board.play(3);
        board.play(4);

        board.undoPlay();
        board.undoPlay();
        board.undoPlay();
        board.undoPlay();
        board.undoPlay();

        assertEquals(10, eventCount[0]);
    }

    @Test
    public void testAddNullListener()
    {
        try {
            board.addBoardListener(null);
            fail();
        } catch (NullPointerException expected) {
            assertEquals("boardListener must not be null", expected.getMessage());
        }
    }
}
