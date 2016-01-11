package ttaomae.connectn;

/**
 * An immutable Connect-N {@linkplain Board board}. This board may be backed by
 * a mutable board.
 *
 * @author Todd Taomae
 */
public interface ImmutableBoard
{
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
     * Returns the next piece to be played. Turns alternate between Black and
     * Red, starting with Black on the first turn.
     *
     * @return the next piece to be played
     */
    Piece getNextPiece();

    /**
     * Returns a mutable copy of this board.
     *
     * @return a mutable copy of this board
     */
    Board getMutableCopy();
}
