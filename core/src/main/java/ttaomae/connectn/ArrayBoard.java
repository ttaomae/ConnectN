package ttaomae.connectn;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Board} implementation backed by a 2-dimensional array.
 *
 * @author Todd Taomae
 */
public class ArrayBoard implements Board, ImmutableBoard
{
    private static final Logger logger = LoggerFactory.getLogger(ArrayBoard.class);

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
    public ArrayBoard(int height, int width, int winCondition)
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
    public ArrayBoard()
    {
        this(DEFAULT_HEIGHT, DEFAULT_WIDTH, DEFAULT_WIN_CONDITION);
    }

    @Override
    public int getHeight()
    {
        return this.board.length;
    }

    @Override
    public int getWidth()
    {
        return this.board[0].length;
    }

    @Override
    public int getWinCondition()
    {
        return this.winCondition;
    }

    @Override
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

    @Override
    public Piece getPieceAt(int col, int row)
    {
        checkElementIndex(col, this.getWidth(), "Column: " + col + ", Width: " + this.getWidth());
        checkElementIndex(row, this.getHeight(), "Row: " + row + ", Height: " + this.getHeight());

        return this.board[row][col];
    }

    @Override
    public int getCurrentTurn() {
        return this.currentTurn;
    }

    @Override
    public Piece getNextPiece()
    {
        return (this.getCurrentTurn() % 2 == 0) ? Piece.BLACK : Piece.RED;
    }

    @Override
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

        logger.debug("Playing move on column: {}", col);

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

    @Override
    public void undoPlay()
    {
        if (this.playHistory.isEmpty()) {
            throw new IllegalStateException("No moves to undo.");
        }

        int lastPlayCol = this.playHistory.getLast();

        logger.debug("Undoing move on column: {}", lastPlayCol);

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
    }

    @Override
    public boolean isValidMove(int col)
    {
        // invalid index
        if (col < 0 || col >= this.getWidth()) {
            return false;
        }

        return this.getPieceAt(col, this.getHeight() - 1) == Piece.NONE;
    }

    @Override
    public Board getMutableCopy()
    {
        ArrayBoard copy = new ArrayBoard(this.getHeight(), this.getWidth(), this.getWinCondition());

        for (int i = 0; i < this.board.length; i++) {
            System.arraycopy(this.board[i], 0, copy.board[i], 0, this.board[i].length);
        }
        copy.currentTurn = this.currentTurn;

        return copy;
    }

    @Override
    public ImmutableBoard getImmutableView()
    {
        return this;
    }

    @Override
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
