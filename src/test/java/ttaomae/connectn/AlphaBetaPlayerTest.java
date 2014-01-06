package ttaomae.connectn;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class AlphaBetaPlayerTest
{
    private Board board;
    private Player player;

    @Before
    public void init()
    {
        this.board = new Board();
        this.player = new AlphaBetaPlayer();
    }

    @Test
    public void testBlackDepthOne()
    {
        board.play(0); // black
        board.play(0);
        board.play(1); // black
        board.play(1);
        board.play(2); // black
        board.play(2);
        assertEquals("failure - black selects horizontal winning move", 3, player.getMove(board));

        board = new Board();
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        board.play(1);
        assertEquals("failure - black selects vertical winning move", 0, player.getMove(board));


        board = new Board();
        board.play(0); // black
        board.play(1);
        board.play(1); // black
        board.play(2);
        board.play(2);
        board.play(3);
        board.play(2); // black
        board.play(3);
        board.play(4);
        board.play(3);
        assertEquals("failure - black selects diagonal winning move", 3, player.getMove(board));
    }

    @Test
    public void testRedDepthOne()
    {
        board.play(6);
        board.play(0); // red
        board.play(5);
        board.play(1); // red
        board.play(6);
        board.play(2); // red
        board.play(5);
        assertEquals("failure - red selects horizontal winning move", 3, player.getMove(board));

        board = new Board();
        board.play(1);
        board.play(0); // red
        board.play(1);
        board.play(0); // red
        board.play(1);
        board.play(0); // red
        board.play(2);
        assertEquals("failure - red selects vertical winning move", 0, player.getMove(board));

        board = new Board();
        board.play(1);
        board.play(0); // red
        board.play(2);
        board.play(1); // red
        board.play(2);
        board.play(2); // red
        board.play(3);
        board.play(3);
        board.play(3);
        assertEquals("failure - red selects diagonal winning move", 3, player.getMove(board));
    }

    @Test
    public void testBlockOpponentWin()
    {
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        board.play(6);
        board.play(6); // black
        assertEquals("failure - red blocks black", 0, player.getMove(board));

        board = new Board();
        board.play(1);
        board.play(0); // red
        board.play(1);
        board.play(0); // red
        board.play(2);
        board.play(0); // red
        assertEquals("failure - black blocks red", 0, player.getMove(board));
    }

    @Test
    public void testWinOverBlock()
    {
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        board.play(1);
        assertEquals("failure - black wins instead of blocking", 0, player.getMove(board));

        board = new Board();
        board.play(0);
        board.play(1); // red
        board.play(0);
        board.play(1); // red
        board.play(0);
        board.play(1); // red
        board.play(6);
        assertEquals("failure - red wins instead of blocking", 1, player.getMove(board));


    }
}
