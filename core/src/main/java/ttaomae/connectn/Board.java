package ttaomae.connectn;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

/**
 * A Connect-N board. A board has a rectangular grid of, possibly empty, spaces.
 * Pieces are played in columns and are placed in the lowest possible row in
 * that column. Columns and rows are indexed starting with (0, 0) at the
 * lower-left. A board can have any win condition greater than 2 and up to the
 * max of the height and width of the board.
 *
 * @author Todd Taomae
 */
public class Board
{
    /** Value for an invalid move */
    public static final int INVALID_MOVE = -1;

    /** Default board height */
    private static final int DEFAULT_HEIGHT = 6;
    /** Default board width */
    private static final int DEFAULT_WIDTH = 7;
    /** Default board win condition */
    private static final int DEFAULT_WIN_CONDITION = 4;

    /**
     * The board. Array indexes are equivalent to the row and column indexes.
     */
    private final Piece[][] board;
    private final int winCondition;
    private int currentTurn;

    /** List of past plays on this board */
    private final Deque<Integer> playHistory;

    /** List of objects listening to this Board */
    private final List<BoardListener> listeners;

    /**
     * Constructs a new empty Board with the specified height, width, and win
     * condition. The height and width must be at least 2. The win condition
     * must be at least 2 and cannot be greater than the max of the height and
     * width.
     *
     * @param height the height of this Board
     * @param width the width of this Board
     * @param winCondition the win condition of this board
     */
    public Board(int height, int width, int winCondition)
    {
        checkArgument(height >= 2, "height must be at least 2");
        checkArgument(width >= 2, "width must be at least 2");
        checkArgument(winCondition >= 2 && winCondition <= Math.max(height, width),
                    "winCondition must be between 2 and max(height, width)");

        // create new empty board
        this.board = new Piece[height][width];
        for (Piece[] row : this.board) {
            Arrays.fill(row, Piece.NONE);
        }

        this.winCondition = winCondition;
        this.currentTurn = 0;

        this.playHistory = new ArrayDeque<>();
        this.listeners = new ArrayList<>();
    }

    /**
     * Constructs a new Board with the default parameters.
     */
    public Board()
    {
        this(DEFAULT_HEIGHT, DEFAULT_WIDTH, DEFAULT_WIN_CONDITION);
    }

    /**
     * Plays the next piece in the specified column. Columns start from 0 on the
     * far left and end with (width - 1) on the far right. The next piece is
     * determined by the current turn. The piece will be placed in the lowest
     * empty row in the specified column.
     *
     * <p> Notifies any threads waiting on this Board when the move is played.
     *
     * @param col the column to play the next piece
     * @throws IllegalMoveException if the column is not a valid column or if
     *             the column is full
     */
    public void play(int col) throws IllegalMoveException
    {
        if (!isValidMove(col)) {
            if (col < 0 || col >= this.getWidth()) {
                throw new IllegalMoveException("Illegal column: " + col);
            }
            else {
                throw new IllegalMoveException("column " + col + " full");
            }
        }

        // check each row starting from the bottom
        for (int row = 0; row < this.getHeight(); row++) {
            if (this.board[row][col] == Piece.NONE) {
                this.board[row][col] = this.getNextPiece();
                this.playHistory.addLast(col);
                currentTurn++;
                this.notifyListeners();
                break;
            }
        }
    }

    /**
     * Undoes the last play on this Board.
     *
     * @throws IllegalStateException if the board is empty (i.e. no plays have
     *             been made)
     */
    public boolean undoPlay()
    {
        if (this.playHistory.isEmpty()) {
            return false;
        }

        int lastPlayCol = this.playHistory.getLast();
        boolean moveUndone = false;

        // find highest non-empty row for last play column
        for (int row = this.getHeight() - 1; row >= 0; row--) {
            // make empty
            if (this.board[row][lastPlayCol] != Piece.NONE) {
                this.board[row][lastPlayCol] = Piece.NONE;
                this.playHistory.removeLast();
                this.currentTurn--;

                moveUndone = true;

                this.notifyListeners();
                break;
            }
        }

        // this should not happen
        if (!moveUndone) {
            throw new IllegalStateException("Board is in illegal state. Could not undo play.");
        }

        return true;
    }

    /**
     * Returns the winner based on the current state of the board. Assumes that
     * the board is in a valid state and that there is only one player who has
     * n-in-a-row.
     *
     * @return the winner
     */
    public Piece getWinner()
    {
        // if the board is full it is a draw
        if (this.currentTurn == this.getHeight() * this.getWidth()) {
            return Piece.DRAW;
        }

        // check each board position
        for (int row = 0; row < this.getHeight(); row++) {
            for (int col = 0; col < this.getWidth(); col++) {
                int horizontalBlack = 0;
                int horizontalRed = 0;
                int verticalBlack = 0;
                int verticalRed = 0;
                int diagonalBlack1 = 0;
                int diagonalRed1 = 0;
                int diagonalBlack2 = 0;
                int diagonalRed2 = 0;

                for (int i = 0; i < this.getWinCondition(); i++) {
                    // check horizontal win
                    if (col <= this.getWidth() - this.getWinCondition()) {
                        switch (this.getPieceAt(col + i, row)) {
                            case BLACK:
                                horizontalBlack++;
                                break;
                            case RED:
                                horizontalRed++;
                                break;
                            case NONE:
                            case DRAW:
                                break;
                        }
                    }

                    // check vertical win
                    if (row <= this.getHeight() - this.getWinCondition()) {
                        switch (this.getPieceAt(col, row + i)) {
                            case BLACK:
                                verticalBlack++;
                                break;
                            case RED:
                                verticalRed++;
                                break;
                            case NONE:
                            case DRAW:
                                break;
                        }
                    }

                    // check up-right diagonal win
                    if (col <= this.getWidth() - this.getWinCondition()
                        && row <= this.getHeight() - this.getWinCondition()) {
                        switch (this.getPieceAt(col + i, row + i)) {
                            case BLACK:
                                diagonalBlack1++;
                                break;
                            case RED:
                                diagonalRed1++;
                                break;
                            case NONE:
                            case DRAW:
                                break;
                        }
                    }

                    // check up-left diagonal win
                    if (col >= this.getWinCondition() - 1
                        && row <= this.getHeight() - this.getWinCondition()) {
                        switch (this.getPieceAt(col - i, row + i)) {
                            case BLACK:
                                diagonalBlack2++;
                                break;
                            case RED:
                                diagonalRed2++;
                                break;
                            case NONE:
                            case DRAW:
                                break;
                        }
                    }
                }

                if (horizontalBlack == this.getWinCondition()
                    || verticalBlack == this.getWinCondition()
                    || diagonalBlack1 == this.getWinCondition()
                    || diagonalBlack2 == this.getWinCondition()) {
                    return Piece.BLACK;
                }
                if (horizontalRed == this.getWinCondition()
                    || verticalRed == this.getWinCondition()
                    || diagonalRed1 == this.getWinCondition()
                    || diagonalRed2 == this.getWinCondition()) {
                    return Piece.RED;
                }
            }
        }

        return Piece.NONE;
    }

    /**
     * Checks if playing a piece in the specified column is valid. A move is
     * valid if the column is greater than or equal to 0 (far left column) and
     * less than the width of this Board (far right column) and the column is
     * not full. This method assumes that the board is in a valid state and only
     * checks if the top row of the column is empty.
     *
     * @param col the column to play the next piece
     * @return true if the move is valid, false otherwise.
     */
    public boolean isValidMove(int col)
    {
        // invalid index
        if (col < 0 || col >= this.getWidth()) {
            return false;
        }

        return this.getPieceAt(col, this.getHeight() - 1) == Piece.NONE;
    }

    /**
     * Returns the piece in the specified column and row.
     *
     * @param col the column
     * @param row the row
     * @return the Piece at the specified position
     */
    public Piece getPieceAt(int col, int row)
    {
        checkElementIndex(col, this.getWidth(), "Column: " + col + ", Width: " + this.getWidth());
        checkElementIndex(row, this.getHeight(), "Row: " + row + ", Height: " + this.getHeight());

        return this.board[row][col];
    }

    /**
     * Returns the next piece to be played. Turns alternate between Black and
     * Red, starting with Black on the first turn.
     *
     * @return the next piece to be played
     */
    public Piece getNextPiece()
    {
        return (this.currentTurn % 2 == 0) ? Piece.BLACK : Piece.RED;
    }

    /**
     * Creates a copy of this Board.
     *
     * @return the new copy of this Board
     */
    public Board copy()
    {
        Board copy = new Board(this.getHeight(), this.getWidth(), this.getWinCondition());

        for (int i = 0; i < this.board.length; i++) {
            System.arraycopy(this.board[i], 0, copy.board[i], 0, this.board[i].length);
        }
        copy.currentTurn = this.currentTurn;

        return copy;
    }

    /**
     * Returns the height of this Board.
     *
     * @return the height of this Board
     */
    public int getHeight()
    {
        return this.board.length;
    }

    /**
     * Returns the width of this Board.
     *
     * @return the width of this Board
     */
    public int getWidth()
    {
        return this.board[0].length;
    }

    /**
     * Returns the win condition of this Board.
     *
     * @return the win condition of this Board
     */
    public int getWinCondition()
    {
        return this.winCondition;
    }

    /**
     * Adds a BoardListener to this Board.
     *
     * @param boardListener the BoardListener being added
     */
    public void addBoardListener(BoardListener boardListener)
    {
        checkNotNull(boardListener, "boardListener must not be null");
        this.listeners.add(boardListener);
    }

    /**
     * Notifies all listeners that this Board has been changed.
     */
    private void notifyListeners()
    {
        for (BoardListener bl : this.listeners) {
            assert bl != null : "BoardListener should not be null";
            bl.boardChanged();
        }
    }
}
