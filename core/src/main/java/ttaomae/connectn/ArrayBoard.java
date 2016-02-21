package ttaomae.connectn;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkElementIndex;
import static com.google.common.base.Preconditions.checkNotNull;

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
        if (this.getCurrentTurn() == this.getHeight() * this.getWidth()) {
            return Piece.DRAW;
        }
        // there cannot be a winner until the first player has played at least
        // winCondition pieces
        if (this.getCurrentTurn() < (this.getWinCondition() * 2) - 1) {
            return Piece.NONE;
        }

        // check each board position
        for (int row = 0; row < this.getHeight(); row++) {
            for (int col = 0; col < this.getWidth(); col++) {
                // if the current location is empty, it cannot be part of the
                // winning line, so skip to the next location
                if (this.getPieceAt(col, row) == Piece.NONE) {
                    continue;
                }

                // For each direction (horizontal, vertical, up-right, up-left)
                // check if there are n pieces of the same color in a line.
                // This is done by keeping a count for each direction, one color
                // increments the count and the other decrements the count.
                // When checking the nth piece in a line, if the absolute value
                // of the count is not n, then there cannot be a winner for that
                // line, so we can stop checking.
                int horizontal = 0;
                int vertical = 0;
                int upLeft = 0;
                int upRight = 0;

                boolean rightPossible = col <= this.getWidth() - this.getWinCondition();
                boolean leftPossible = col >= this.getWidth() - this.getWinCondition();
                boolean vertcalPossible = row <= this.getHeight() - this.getWinCondition();
                if (rightPossible) {
                    loop:
                    for (int i = 0; i < this.getWinCondition(); i++) {
                        switch (this.getPieceAt(col + i, row)) {
                            case BLACK:
                                if (horizontal < i) break loop;
                                horizontal++;
                                break;
                            case RED:
                                if (horizontal > -i) break loop;
                                horizontal--;
                                break;
                            default:
                                break;
                        }
                    }
                    if (horizontal == this.getWinCondition()) {
                        return Piece.BLACK;
                    }
                    else if (horizontal == -this.getWinCondition()) {
                        return Piece.RED;
                    }
                }
                if (vertcalPossible) {
                    loop:
                    for (int i = 0; i < this.getWinCondition(); i++) {
                        switch (this.getPieceAt(col, row + i)) {
                            case BLACK:
                                if (vertical < i) break loop;
                                vertical++;
                                break;
                            case RED:
                                if (vertical > -i) break loop;
                                vertical--;
                                break;
                            default:
                                break;
                        }
                    }
                    if (vertical == this.getWinCondition()) {
                        return Piece.BLACK;
                    }
                    else if (vertical == -this.getWinCondition()) {
                        return Piece.RED;
                    }
                }
                if (vertcalPossible && rightPossible) {
                    loop:
                    for (int i = 0; i < this.getWinCondition(); i++) {
                        switch (this.getPieceAt(col + i, row + i)) {
                            case BLACK:
                                if (upRight < i) break loop;
                                upRight++;
                                break;
                            case RED:
                                if (upRight > -i) break loop;
                                upRight--;
                                break;
                            default:
                                break;
                        }
                    }
                    if (upRight == this.getWinCondition()) {
                        return Piece.BLACK;
                    }
                    else if (upRight == -this.getWinCondition()) {
                        return Piece.RED;
                    }
                }
                if (vertcalPossible && leftPossible) {
                    loop:
                    for (int i = 0; i < this.getWinCondition(); i++) {
                        switch (this.getPieceAt(col - i, row + i)) {
                            case BLACK:
                                if (upLeft < i) break loop;
                                upLeft++;
                                break;
                            case RED:
                                if (upLeft > -i) break loop;
                                upLeft--;
                                break;
                            default:
                                break;
                        }
                    }
                    if (upLeft == this.getWinCondition()) {
                        return Piece.BLACK;
                    }
                    else if (upLeft == -this.getWinCondition()) {
                        return Piece.RED;
                    }
                }
            }
        }

        return Piece.NONE;
    }

    @Override
    public Piece getPieceAt(int col, int row)
    {
        checkElementIndex(col, this.getWidth(), "column");
        checkElementIndex(row, this.getHeight(), "row");

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
