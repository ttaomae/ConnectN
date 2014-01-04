package ttaomae.connectn;

import java.util.Arrays;

public class Board implements Cloneable
{
    private final int winCondition;
    private Piece[][] board;
    private int currentTurn;

    public Board()
    {
        this.winCondition = 4;

        this.board = new Piece[6][7];
        for (Piece[] row : this.board) {
            Arrays.fill(row, Piece.NONE);
        }

        this.currentTurn = 0;
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

        for (int row = 0; row < this.getHeight(); row++) {
            for (int col = 0; col < this.getWidth(); col++) {
                // check horizontal win
                if (col <= this.getWidth() - this.getWinCondition()) {
                    int horizontalBlack = 0;
                    int horizontalRed = 0;

                    for (int i = 0; i < this.getWinCondition(); i++) {
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

                    if (horizontalBlack == this.getWinCondition()) {
                        return Piece.BLACK;
                    }
                    else if (horizontalRed == this.getWinCondition()) {
                        return Piece.RED;
                    }
                }

                // check vertical win
                if (row <= this.getHeight() - this.getWinCondition()) {
                    int verticalBlack = 0;
                    int verticalRed = 0;

                    for (int i = 0; i < this.getWinCondition(); i++) {
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

                    if (verticalBlack == this.getWinCondition()) {
                        return Piece.BLACK;
                    }
                    if (verticalRed == this.getWinCondition()) {
                        return Piece.RED;
                    }
                }

                // check up-right diagonal win
                if (col <= this.getWidth() - this.getWinCondition()
                    && row <= this.getHeight() - this.getWinCondition()) {
                    int diagonalBlack = 0;
                    int diagonalRed = 0;

                    for (int i = 0; i < this.getWinCondition(); i++) {
                        switch (this.getPieceAt(col + i, row + i)) {
                            case BLACK:
                                diagonalBlack++;
                                break;
                            case RED:
                                diagonalRed++;
                                break;
                            case NONE:
                            case DRAW:
                                break;
                        }
                    }

                    if (diagonalBlack == this.getWinCondition()) {
                        return Piece.BLACK;
                    }
                    if (diagonalRed == this.getWinCondition()) {
                        return Piece.RED;
                    }
                }

                // check up-left diagonal win
                if (col >= this.getWinCondition() - 1
                    && row <= this.getHeight() - this.getWinCondition()) {
                    int diagonalBlack = 0;
                    int diagonalRed = 0;

                    for (int i = 0; i < this.getWinCondition(); i++) {
                        switch (this.getPieceAt(col - i, row + i)) {
                            case BLACK:
                                diagonalBlack++;
                                break;
                            case RED:
                                diagonalRed++;
                                break;
                            case NONE:
                            case DRAW:
                                break;
                        }
                    }

                    if (diagonalBlack == this.getWinCondition()) {
                        return Piece.BLACK;
                    }
                    if (diagonalRed == this.getWinCondition()) {
                        return Piece.RED;
                    }
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
        Board copy = new Board();

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
