package ttaomae.connectn.gui;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import ttaomae.connectn.Board;
import ttaomae.connectn.Player;

/**
 * A mouse player. Selects move based on click on a BoardPanel.
 *
 * @author Todd Taomae
 */
public class MousePlayer implements Player
{
    private static final int INVALID_MOVE = -2;
    private int move;

    private BoardPanel boardPanel;

    /**
     * Constructs a new MousePlayer which plays on the specified BoardPanel.
     *
     * @param boardPanel the BoardPanel that this MousePlayer will use
     */
    public MousePlayer(BoardPanel boardPanel)
    {
        this.move = INVALID_MOVE;
        this.boardPanel = boardPanel;

        this.boardPanel.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me)
            {
                synchronized (MousePlayer.this) {
                    MousePlayer.this.move = MousePlayer.this.boardPanel.getBoardColumn(me.getX());
                    // let this MousePlayer know that the board has been clicked.
                    MousePlayer.this.notifyAll();
                }
            }
        });
    }

    @Override
    public int getMove(Board board)
    {
        this.move = INVALID_MOVE;
        synchronized (this) {
            while (this.move == INVALID_MOVE) {
                try {
                    // wait for the handler to notify
                    this.wait();
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
        }
        int result = this.move;
        this.move = INVALID_MOVE;
        return result;
    }
}
