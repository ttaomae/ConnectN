package ttaomae.connectn;

import java.util.Arrays;

public class Board implements Cloneable
{
    private static final int DEFAULT_HEIGHT = 6;
    private static final int DEFAULT_WIDTH = 7;
    private static final int DEFAULT_WIN_CONDITION = 4;

    private final int winCondition;
    private Piece[][] board;
    private int currentTurn;

    public Board(int height, int width, int winCondition)
    {
        if (height < 2) {
            throw new IllegalArgumentException("height must be at least 2");
        }
        if (width < 2) {
            throw new IllegalArgumentException("width must be at least 2");
        }
        if (winCondition > Math.min(height, width)) {
            throw new IllegalArgumentException(
                    "winCondition must not be greater than min(height, width)");
        }

        // create new empty board
        this.board = new Piece[height][width];
        for (Piece[] row : this.board) {
            Arrays.fill(row, Piece.NONE);
        }

        this.winCondition = winCondition;
        this.currentTurn = 0;
    }

    public Board()
    {
        this(DEFAULT_HEIGHT, DEFAULT_WIDTH, DEFAULT_WIN_CONDITION);
    }

    public void play(int col) throws IllegalMoveException
    {
        if (col < 0 || col >= this.getWidth()) {
            throw new IllegalMoveException("Illegal column: " + col);
        }
        boolean columnFull = true;

        // check each row
        for (int row = 0; row < this.getHeight(); row++) {
            if (this.board[row][col] == Piece.NONE) {
                this.board[row][col] = this.getNextPiece();
                currentTurn++;
                columnFull = false;
                synchronized (this) {
                    // notify when a play has been made
                    this.notifyAll();
                }
                break;
            }
        }

        if (columnFull) {
            throw new IllegalMoveException("column " + col + " full");
        }
    }

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

    public boolean isValidMove(int col)
    {
        // invalid index
        if (col < 0 || col >= this.getWidth()) {
            return false;
        }

        return this.getPieceAt(col, this.getHeight() - 1) == Piece.NONE;
    }

    public Piece getPieceAt(int col, int row) throws IndexOutOfBoundsException
    {
        if (col < 0 || col >= this.getWidth()) {
            String message = String.format("Column: %d, Width: %d", col, this.getWidth());
            throw new IndexOutOfBoundsException(message);
        }

        if (row < 0 || row >= this.getHeight()) {
            String message = String.format("Row: %d, Height: %d", row, this.getHeight());
            throw new IndexOutOfBoundsException(message);
        }

        return this.board[row][col];
    }

    public Piece getNextPiece()
    {
        return (this.currentTurn % 2 == 0) ? Piece.BLACK : Piece.RED;
    }

    public Board copy()
    {
        Board copy = new Board(this.getHeight(), this.getWidth(), this.getWinCondition());

        for (int i = 0; i < this.board.length; i++) {
            System.arraycopy(this.board[i], 0, copy.board[i], 0, this.board[i].length);
        }
        copy.currentTurn = this.currentTurn;

        return copy;
    }

    public int getHeight()
    {
        return this.board.length;
    }

    public int getWidth()
    {
        return this.board[0].length;
    }

    public int getWinCondition()
    {
        return this.winCondition;
    }

}
