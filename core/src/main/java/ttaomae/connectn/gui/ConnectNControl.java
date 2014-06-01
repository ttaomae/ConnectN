package ttaomae.connectn.gui;

import java.io.IOException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import ttaomae.connectn.AlphaBetaPlayer;
import ttaomae.connectn.Board;
import ttaomae.connectn.BoardListener;
import ttaomae.connectn.GameManager;
import ttaomae.connectn.Piece;
import ttaomae.connectn.Player;

/**
 * A JavaFX component which provides an interface for a Connect-N game. Provides
 * an interface for selecting the height, width, and win condition; whether the
 * players are human or computer; and for starting and reseting games. Also
 * contains a display for messages to the user.
 *
 * @author Todd Taomae
 */
public class ConnectNControl extends GridPane implements BoardListener
{
    private static final String START_MESSAGE = "Click \"Start\" to start a new game.";
    private static final String BLACK_TURN = "BLACK's turn.";
    private static final String RED_TURN = "RED's turn.";
    private static final String BLACK_WIN = "BLACK Wins!";
    private static final String RED_WIN = "RED Wins!";
    private static final String DRAW = "Draw!";

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

    /**
     * Constructs a new ConnectNControl.
     */
    public ConnectNControl()
    {
        initialize();
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
     * Initializes the components.
     */
    private void initialize()
    {
        this.title = new Label();

        this.playerOne = new PlayerSelectControl();
        this.playerTwo = new PlayerSelectControl();

        this.heightSlider = new Slider();
        this.widthSlider = new Slider();
        this.winConditionSlider = new Slider();

        this.boardPanel = new BoardPanel();
        this.displayMessage = new Label();
        this.startButton = new Button();
    }

    /**
     * Loads the layout.
     */
    private void load()
    {
        FXMLLoader fxmlLoader =
                new FXMLLoader(getClass().getResource("/layout/connect_n.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    private void handleStartButton()
    {
        if (this.running) {
            this.resetGame();
            this.updateStartButtonText("Start");
            this.running = false;
        }
        else {
            this.startGame();
            this.updateStartButtonText("Reset");
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
            javafx.application.Platform.runLater(new Runnable() {
                @Override public void run() {
                    ConnectNControl.this.winConditionSlider.setValue(maxWinCondition);
                }
            });
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
            p1 = new AlphaBetaPlayer(this.playerOne.getCpuDifficulty());
        }

        if (this.playerTwo.isHuman()) {
            p2 = new MousePlayer(this.boardPanel);
        }
        else {
            p2 = new AlphaBetaPlayer(this.playerTwo.getCpuDifficulty());
        }

        this.gameManager = new GameManager(this.board, p1, p2);
        this.updateMessage(BLACK_TURN);
        this.gameManagerThread = new Thread(this.gameManager, "Game Manager");
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
        javafx.application.Platform.runLater(new Runnable() {
            @Override public void run() {
                int winCond = (int) ConnectNControl.this.winConditionSlider.getValue();
                ConnectNControl.this.title.setText("Connect " + winCond);
            }
        });
        this.board = new Board((int) this.heightSlider.getValue(),
                               (int) this.widthSlider.getValue(),
                               (int) this.winConditionSlider.getValue());
        this.board.addBoardListener(this);
        this.boardPanel.setBoard(this.board);
        this.updateMessage(START_MESSAGE);
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
                    this.updateMessage(BLACK_TURN);
                }
                else if (this.board.getNextPiece() == Piece.RED) {
                    this.updateMessage(RED_TURN);
                }
                break;
            case BLACK:
                this.updateMessage(BLACK_WIN);
                break;
            case RED:
                this.updateMessage(RED_WIN);
                break;
            case DRAW:
                this.updateMessage(DRAW);
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
        javafx.application.Platform.runLater(new Runnable() {
            @Override public void run() {
                ConnectNControl.this.startButton.setText(text);
            }
        });
    }

    /**
     * Sets the display to the specified message.
     *
     * @param message the message to update to
     */
    private void updateMessage(final String message)
    {
        javafx.application.Platform.runLater(new Runnable() {
            @Override public void run() {
                ConnectNControl.this.displayMessage.setText(message);
            }
        });
    }
}
