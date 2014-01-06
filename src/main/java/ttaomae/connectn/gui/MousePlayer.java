package ttaomae.connectn.gui;

import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import ttaomae.connectn.Board;
import ttaomae.connectn.Player;

public class MousePlayer implements Player
{
    private static final int INVALID_MOVE = -1;
    private int move;

    private BoardPanel boardPanel;
    public MousePlayer(BoardPanel boardPanel)
    {
        this.move = INVALID_MOVE;
        this.boardPanel = boardPanel;

        boardPanel.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent me)
            {
                System.out.println("mouse clicked");
                synchronized (MousePlayer.this) {
                    MousePlayer.this.move = MousePlayer.this.boardPanel.getBoardColumn(me.getX());
                    MousePlayer.this.notifyAll();
                }
            }
        });
    }

    @Override
    public int getMove(Board board)
    {
        synchronized (this) {
            this.move = INVALID_MOVE;
            while (this.move == INVALID_MOVE) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    // do nothing
                }
            }
            int result = this.move;
            this.move = INVALID_MOVE;
            return result;
        }
    }
}
