package ttaomae.connectn;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class PieceTest
{
    @Test
    public void testOpposite()
    {
        assertEquals("failure - black opposite", Piece.RED, Piece.BLACK.opposite());
        assertEquals("failure - red opposite", Piece.BLACK, Piece.RED.opposite());
        assertEquals("failure - none opposite", Piece.NONE, Piece.NONE.opposite());
        assertEquals("failure - draw opposite", Piece.NONE, Piece.DRAW.opposite());
    }
}
