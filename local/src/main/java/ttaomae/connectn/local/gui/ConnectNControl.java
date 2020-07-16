package ttaomae.connectn.local.gui;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import ttaomae.connectn.ArrayBoard;
import ttaomae.connectn.Board;
import ttaomae.connectn.BoardListener;
import ttaomae.connectn.GameManager;
import ttaomae.connectn.Piece;
import ttaomae.connectn.gui.BoardPanel;
import ttaomae.connectn.gui.MousePlayer;
import ttaomae.connectn.player.AlphaBetaPlayer;
import ttaomae.connectn.player.Player;
import ttaomae.connectn.util.ResourceBundleUtil;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * A JavaFX component which provides an interface for a Connect-N game. Provides
 * an interface for selecting the height, width, and win condition; whether the
 * players are human or computer; and for starting and resetting games. Also
 * contains a display for messages to the user.
 *
 * @author Todd Taomae
 */
public class ConnectNControl extends GridPane implements BoardListener
{
    private static final ResourceBundle GUI_STRINGS
            = ResourceBundleUtil.getResourceBundle("gui", "locale.properties");

    @FXML private Label title;

    @FXML private BoardPanel boardPanel;

    @FXML private PlayerSelectControl playerOne;
    @FXML private PlayerSelectControl playerTwo;

    @FXML private Slider heightSlider;
    @FXML private Slider widthSlider;
    @FXML private Slider winConditionSlider;

    @FXML private Label displayMessage;
    @FXML private Button startButton;

    private boolean running;
    private GameManager gameManager;
    private Thread gameManagerThread;
    private Board board;

    private final ExecutorService executorService;

    /**
     * Constructs a new ConnectNControl.
     */
    public ConnectNControl()
    {
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors(),
                new ThreadFactoryBuilder()
                        .setNameFormat("alpha-beta-%d")
                        .setDaemon(true)
                        .build());

        load();

        this.running = false;

        final class SliderListener implements ChangeListener<Number> {
            @Override
            public void changed(ObservableValue<? extends Number> ov,
                                Number old_val, Number new_val)
            {
                ConnectNControl.this.checkWinConditionSlider();

                if (!ConnectNControl.this.running) {
                    ConnectNControl.this.resetBoard();
                }
            }
        }

        this.heightSlider.valueProperty().addListener(new SliderListener());
        this.widthSlider.valueProperty().addListener(new SliderListener());
        this.winConditionSlider.valueProperty().addListener(new SliderListener());

        this.resetBoard();
    }

    /**
     * Loads the layout.
     */
    private void load()
    {
        FXMLLoader fxmlLoader =
                new FXMLLoader(getClass().getResource("/layout/connect_n.fxml"));
        fxmlLoader.setResources(GUI_STRINGS);
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    private void handleStartButton() // NOPMD
    {
        if (this.running) {
            this.resetGame();
            this.updateStartButtonText(GUI_STRINGS.getString("start_button"));
            this.running = false;
        }
        else {
            this.startGame();
            this.updateStartButtonText(GUI_STRINGS.getString("reset_button"));
            this.running = true;
        }
    }

    /**
     * Checks that the win condition slider is valid and sets it to a valid
     * range if it is not.
     */
    private void checkWinConditionSlider()
    {
        int height = (int) this.heightSlider.getValue();
        int width = (int) this.widthSlider.getValue();
        int winCondition = (int) this.winConditionSlider.getValue();

        final int maxWinCondition = Math.max(height, width);
        if (winCondition > maxWinCondition) {
            this.winConditionSlider.setValue(maxWinCondition);
        }
    }

    /**
     * Starts a game based on this ConnectNPanel's current settings.
     */
    private void startGame()
    {
        Player p1;
        Player p2;
        if (this.playerOne.isHuman()) {
            p1 = new MousePlayer(this.boardPanel);
        }
        else {
            p1 = new AlphaBetaPlayer(this.playerOne.getCpuDifficulty(), this.executorService);
        }

        if (this.playerTwo.isHuman()) {
            p2 = new MousePlayer(this.boardPanel);
        }
        else {
            p2 = new AlphaBetaPlayer(this.playerTwo.getCpuDifficulty(), this.executorService);
        }

        this.gameManager = new GameManager(this.board, p1, p2);
        this.updateMessage(GUI_STRINGS.getString("black_turn"));
        this.gameManagerThread = new Thread(this.gameManager, "Game Manager");
        this.gameManagerThread.setDaemon(false);
        this.gameManagerThread.start();
    }

    /**
     * Ends the current game and resets the board.
     */
    private void resetGame()
    {
        this.gameManager.stop();
        this.gameManagerThread.interrupt();

        this.resetBoard();
    }

    /**
     * Resets the board to match the current settings.
     */
    private void resetBoard()
    {
        javafx.application.Platform.runLater(() -> {
            int winCond = (int) this.winConditionSlider.getValue();
            this.title.setText(GUI_STRINGS.getString("title_prefix") + winCond);
        });
        this.board = new ArrayBoard((int) this.heightSlider.getValue(),
                               (int) this.widthSlider.getValue(),
                               (int) this.winConditionSlider.getValue());
        this.board.addBoardListener(this);
        this.boardPanel.setBoard(this.board);
        this.updateMessage(GUI_STRINGS.getString("start_message"));
    }

    /**
     * Updates the display each time the underlying board is changed
     */
    @Override
    public void boardChanged()
    {
        switch (this.board.getWinner()) {
            case NONE:
                if (this.board.getNextPiece() == Piece.BLACK) {
                    this.updateMessage(GUI_STRINGS.getString("black_turn"));
                }
                else if (this.board.getNextPiece() == Piece.RED) {
                    this.updateMessage(GUI_STRINGS.getString("red_turn"));
                }
                break;
            case BLACK:
                this.updateMessage(GUI_STRINGS.getString("black_win"));
                break;
            case RED:
                this.updateMessage(GUI_STRINGS.getString("red_win"));
                break;
            case DRAW:
                this.updateMessage(GUI_STRINGS.getString("draw"));
                break;
        }
    }

    /**
     * Sets the text of the start button.
     *
     * @param text the text to update to
     */
    private void updateStartButtonText(final String text)
    {
        javafx.application.Platform.runLater(() ->
            this.startButton.setText(text)
        );
    }

    /**
     * Sets the display to the specified message.
     *
     * @param message the message to update to
     */
    private void updateMessage(final String message)
    {
        javafx.application.Platform.runLater(() ->
            this.displayMessage.setText(message)
        );
    }
}
