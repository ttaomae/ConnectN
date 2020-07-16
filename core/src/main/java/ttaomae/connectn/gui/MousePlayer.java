package ttaomae.connectn.gui;

import java.util.Optional;

import javafx.scene.input.MouseEvent;
import ttaomae.connectn.ImmutableBoard;
import ttaomae.connectn.player.Player;

/**
 * A mouse player. Selects move based on click on a BoardPanel.
 *
 * @author Todd Taomae
 */
public class MousePlayer implements Player
{
    private Integer move;
    private final BoardPanel boardPanel;

    /**
     * Constructs a new MousePlayer which plays on the specified BoardPanel.
     *
     * @param boardPanel the BoardPanel that this MousePlayer will use
     */
    public MousePlayer(BoardPanel boardPanel)
    {
        this.move = null;
        this.boardPanel = boardPanel;

        this.boardPanel.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            synchronized (this) {
                this.move = this.boardPanel
                        .getBoardColumn(mouseEvent.getX());
                // let this MousePlayer know that the board has been clicked.
                this.notifyAll();
            }
        });
    }

    @Override
    public Optional<Integer> getMove(ImmutableBoard board)
    {
        synchronized (this) {
            Integer result = null;
            this.move = null;

            while (result == null || !board.isValidMove(result)) {
                try {
                    // wait for the handler to notify
                    this.wait();
                    result = this.move;
                } catch (InterruptedException e) {
                    return Optional.empty();
                }
            }
            return Optional.of(result);
        }
    }
}
