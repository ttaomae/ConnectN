package ttaomae.connectn;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import ttaomae.connectn.player.AlphaBetaPlayer;
import ttaomae.connectn.player.Player;

public class AlphaBetaPlayerTest
{
    private Board board;
    private Player player;

    @Before
    public void init()
    {
        this.board = new ArrayBoard();
        this.player = new AlphaBetaPlayer(Executors.newSingleThreadExecutor());
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
        assertEquals("failure - black selects horizontal winning move",
                new Integer(3), player.getMove(board.getImmutableView()).get());

        board = new ArrayBoard();
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        board.play(1);
        board.play(0); // black
        board.play(1);
        assertEquals("failure - black selects vertical winning move",
                new Integer(0), player.getMove(board.getImmutableView()).get());


        board = new ArrayBoard();
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
        assertEquals("failure - black selects diagonal winning move",
                new Integer(3), player.getMove(board.getImmutableView()).get());
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
        assertEquals("failure - red selects horizontal winning move",
                new Integer(3), player.getMove(board.getImmutableView()).get());

        board = new ArrayBoard();
        board.play(1);
        board.play(0); // red
        board.play(1);
        board.play(0); // red
        board.play(1);
        board.play(0); // red
        board.play(2);
        assertEquals("failure - red selects vertical winning move",
                new Integer(0), player.getMove(board.getImmutableView()).get());

        board = new ArrayBoard();
        board.play(1);
        board.play(0); // red
        board.play(2);
        board.play(1); // red
        board.play(2);
        board.play(2); // red
        board.play(3);
        board.play(3);
        board.play(3);
        assertEquals("failure - red selects diagonal winning move",
                new Integer(3), player.getMove(board.getImmutableView()).get());
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
        assertEquals("failure - red blocks black",
                new Integer(0), player.getMove(board.getImmutableView()).get());

        board = new ArrayBoard();
        board.play(1);
        board.play(0); // red
        board.play(1);
        board.play(0); // red
        board.play(2);
        board.play(0); // red
        assertEquals("failure - black blocks red",
                new Integer(0), player.getMove(board.getImmutableView()).get());
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
        assertEquals("failure - black wins instead of blocking",
                new Integer(0), player.getMove(board.getImmutableView()).get());

        board = new ArrayBoard();
        board.play(0);
        board.play(1); // red
        board.play(0);
        board.play(1); // red
        board.play(0);
        board.play(1); // red
        board.play(6);
        assertEquals("failure - red wins instead of blocking",
                new Integer(1), player.getMove(board.getImmutableView()).get());


    }
}
