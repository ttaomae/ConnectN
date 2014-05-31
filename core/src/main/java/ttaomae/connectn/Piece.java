package ttaomae.connectn;

/**
 * A Connect-N piece. Also used to represent the winner.
 *
 * @author Todd Taomae
 */
public enum Piece
{
    BLACK, RED, NONE, DRAW;

    /**
     * Returns the opposite of this Piece.
     *
     * @return the opposite of this Piece
     */
    public Piece opposite()
    {
        switch (this) {
            case BLACK:
                return RED;
            case RED:
                return BLACK;
            case NONE:
            case DRAW:
            default:
                return NONE;
        }
    }
}
