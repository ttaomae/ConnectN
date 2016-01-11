package ttaomae.connectn;


/**
 * A Connect-N board. A board has a rectangular grid of, possibly empty, spaces.
 * Pieces are played in columns and are placed in the lowest possible row in
 * that column. Columns and rows are indexed starting with (0, 0) at the
 * lower-left. A board can have any win condition greater than 2 and up to the
 * max of the height and width of the board.
 *
 * @author Todd Taomae
 */
public interface Board
{
    /** Value for an invalid move */
    int INVALID_MOVE = -1;

    /**
     * Returns the height of this Board.
     *
     * @return the height of this Board
     */
    int getHeight();

    /**
     * Returns the width of this Board.
     *
     * @return the width of this Board
     */
    int getWidth();

    /**
     * Returns the win condition of this Board.
     *
     * @return the win condition of this Board
     */
    int getWinCondition();

    /**
     * Returns the winner based on the current state of the board. Assumes that
     * the board is in a valid state and that there is only one player who has
     * n-in-a-row.
     *
     * @return the winner
     */
    Piece getWinner();

    /**
     * Returns the piece in the specified column and row.
     *
     * @param col the column
     * @param row the row
     * @return the Piece at the specified position
     */
    Piece getPieceAt(int col, int row);

    /**
     * Returns the current turn. Turns start at 0.
     *
     * @return the current turn
     */
    int getCurrentTurn();

    /**
     * Returns the next piece to be played. Turns alternate between Black and
     * Red, starting with Black on the first turn.
     *
     * @return the next piece to be played
     */
    Piece getNextPiece();

    /**
     * Plays the next piece in the specified column. Columns start from 0 on the
     * far left and end with (width - 1) on the far right. The next piece is
     * determined by the current turn. The piece will be placed in the lowest
     * empty row in the specified column.
     *
     * @param col the column to play the next piece
     * @throws IllegalMoveException if the column is not a valid column or if
     *             the column is full
     */
    void play(int col) throws IllegalMoveException;

    /**
     * Undoes the last play on this Board.
     *
     * @throws IllegalStateException if the board is empty (i.e. no plays have
     *             been made)
     */
    void undoPlay();

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
    boolean isValidMove(int col);

    /**
     * Adds a BoardListener to this Board.
     *
     * @param boardListener the BoardListener being added
     */
    void addBoardListener(BoardListener boardListener);

    /**
     * Returns an immutable view of this board.
     *
     * @return an immutable view of this board
     */
    ImmutableBoard getImmutableView();
}
