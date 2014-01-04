package ttaomae.connectn;

public enum Piece
{
    BLACK, RED, NONE;

    public Piece opposite()
    {
        switch (this) {
            case BLACK:
                return RED;
            case RED:
                return BLACK;
            case NONE:
            default:
                return NONE;
        }
    }
}
