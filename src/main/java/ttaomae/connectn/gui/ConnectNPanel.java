package ttaomae.connectn.gui;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.GridPaneBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ttaomae.connectn.AlphaBetaPlayer;
import ttaomae.connectn.Board;
import ttaomae.connectn.GameManager;
import ttaomae.connectn.Piece;
import ttaomae.connectn.Player;

/**
 * A panel for a Connect-N game. Provides an interface for selecting the height,
 * width, and win condition; whether the players are human or computer; and for
 * starting and reseting games. Also contains a display for messages to the
 * user.
 *
 * @author Todd Taomae
 */
public class ConnectNPanel extends GridPane implements Runnable
{
    private static final String START_MESSAGE = "Click \"Start\" to start a new game.";
    private static final String BLACK_TURN = "BLACK's turn.";
    private static final String RED_TURN = "RED's turn.";
    private static final String BLACK_WIN = "BLACK Wins!";
    private static final String RED_WIN = "RED Wins!";
    private static final String DRAW = "Draw!";
    private static final int BOARD_WIDTH = 440;
    private static final int BOARD_HEIGHT = 380;

    private Label title;

    private Board board;
    private BoardPanel boardPanel;
    private GameManager gameManager;
    private Thread gameManagerThread;

    private Slider heightSlider;
    private Slider widthSlider;
    private Slider winConditionSlider;

    private PlayerSelectPanel playerOne;
    private PlayerSelectPanel playerTwo;

    private Button startButton;
    private boolean running;

    private Label displayMessage;
    private Thread myThread;

    /**
     * Constructs a new ConnectNPanel. The range of heights is 2 to 12, the
     * range of widths is 2 to 14, and the range of win conditions is 2 to 14.
     */
    public ConnectNPanel()
    {
        this.running = false;

        this.title = new Label();


        // set up height slider
        this.heightSlider = new Slider(2, 12, 6);
        this.heightSlider.setOrientation(Orientation.VERTICAL);
        this.heightSlider.setMajorTickUnit(2.0);
        this.heightSlider.setMinorTickCount(1);
        this.heightSlider.setShowTickMarks(true);
        this.heightSlider.setShowTickLabels(true);
        this.heightSlider.setSnapToTicks(true);
        this.heightSlider.setMaxHeight(BOARD_HEIGHT);

        // set up width slider
        this.widthSlider = new Slider(2, 14, 7);
        this.widthSlider.setMajorTickUnit(2.0);
        this.widthSlider.setMinorTickCount(1);
        this.widthSlider.setShowTickMarks(true);
        this.widthSlider.setShowTickLabels(true);
        this.widthSlider.setSnapToTicks(true);
        this.widthSlider.setMaxWidth(BOARD_WIDTH);

        // set up win condition slider
        this.winConditionSlider = new Slider(2, 14, 4);
        this.winConditionSlider.setMajorTickUnit(2.0);
        this.winConditionSlider.setMinorTickCount(1);
        this.winConditionSlider.setShowTickMarks(true);
        this.winConditionSlider.setShowTickLabels(true);
        this.winConditionSlider.setSnapToTicks(true);
        this.winConditionSlider.setMaxWidth(BOARD_WIDTH);

        // set up start button
        this.startButton = new Button("Start");
        this.startButton.setFont(Font.font("Sans Serif", 12));
        this.title = new Label();
        this.title.setFont(Font.font("Serif", FontWeight.BOLD, 30));
        this.displayMessage = new Label(START_MESSAGE);

        this.playerOne = new PlayerSelectPanel(2, 10, 1, true);
        this.playerOne.setMaxWidth(BOARD_WIDTH / 2);
        this.playerTwo = new PlayerSelectPanel(2, 10, 2, false);
        this.playerTwo.setMaxWidth(BOARD_WIDTH / 2);

        // set up handlers and listeners
        this.startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e)
            {
                if (ConnectNPanel.this.running) {
                    ConnectNPanel.this.resetGame();
                    ConnectNPanel.this.startButton.setText("Start");
                    ConnectNPanel.this.running = false;
                }
                else {
                    ConnectNPanel.this.startGame();
                    ConnectNPanel.this.startButton.setText("Reset");
                    ConnectNPanel.this.running = true;
                }
            }
        });
        this.heightSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val)
            {
                ConnectNPanel.this.checkWinConditionSlider();

                if (!ConnectNPanel.this.running) {
                    ConnectNPanel.this.resetBoard();
                }
            }
        });
        this.widthSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val)
            {
                ConnectNPanel.this.checkWinConditionSlider();

                if (!ConnectNPanel.this.running) {
                    ConnectNPanel.this.resetBoard();
                }
            }
        });
        this.winConditionSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val)
            {
                ConnectNPanel.this.checkWinConditionSlider();

                if (!ConnectNPanel.this.running) {
                    ConnectNPanel.this.resetBoard();
                }
            }
        });

            this.board = new Board((int) this.heightSlider.getValue(),
                                   (int) this.widthSlider.getValue(),
                                   (int) this.winConditionSlider.getValue());
            this.boardPanel = new BoardPanel(BOARD_WIDTH, BOARD_WIDTH, this.board);
        synchronized (this.board) {
            this.resetBoard();
        }

        // this.setGridLinesVisible(true);
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(10, 10, 10, 10));

        this.add(this.title, 1, 0);
        this.add(this.winConditionSlider, 1, 1);
        this.add(this.heightSlider, 0, 2);
        this.add(this.boardPanel, 1, 2);
        this.add(this.widthSlider, 1, 3);
        this.add(BorderPaneBuilder.create().left(this.playerOne)
                                           .right(this.playerTwo)
                                           .build(),
                 1, 4);
        GridPane bottomRow = GridPaneBuilder.create().hgap(20.0).build();
        bottomRow.add(this.startButton, 0, 0);
        bottomRow.add(this.displayMessage, 1, 0);
        this.add(bottomRow, 1, 5);

        this.myThread = new Thread(this, "ConnectN Panel");
        this.myThread.setDaemon(true);
        this.myThread.start();
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
        this.displayMessage.setText(BLACK_TURN);
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

        synchronized (this.board) {
            this.myThread.interrupt();
            this.resetBoard();
        }
    }

    /**
     * Resets the board to match the current settings.
     */
    private void resetBoard()
    {
        this.title.setText("Connect " + ((int) this.winConditionSlider.getValue()));
        this.board = new Board((int) this.heightSlider.getValue(),
                               (int) this.widthSlider.getValue(),
                               (int) this.winConditionSlider.getValue());
        this.boardPanel.setBoard(this.board);
        this.displayMessage.setText(START_MESSAGE);
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
        if (winCondition > Math.max(height, width)) {
            this.winConditionSlider.setValue(Math.max(height, width));
        }
    }

    /**
     * Updates the display message whenever necessary.
     */
    @Override
    public void run()
    {
        while (true) {
            // wait for next play
            synchronized (this.board) {
                try {
                    this.board.wait();
                } catch (InterruptedException e) {
                    continue;
                }
            }
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
    }

    /**
     * Sets the display to the specified message.
     *
     * @param message the message to update to
     */
    public void updateMessage(final String message)
    {

        javafx.application.Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                ConnectNPanel.this.displayMessage.setText(message);
            }
        });
    }
}
