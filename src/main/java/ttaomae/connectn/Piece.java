package ttaomae.connectn;

public enum Piece
{
    BLACK, RED, NONE, DRAW;

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
