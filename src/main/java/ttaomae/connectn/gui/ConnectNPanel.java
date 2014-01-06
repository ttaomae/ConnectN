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
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import ttaomae.connectn.AlphaBetaPlayer;
import ttaomae.connectn.Board;
import ttaomae.connectn.GameManager;

public class ConnectNPanel extends GridPane
{
    private Board board;
    private BoardPanel boardPanel;

    private Slider heightSlider;
    private Slider widthSlider;
    private Slider winConditionSlider;

    private Label title;
    private Button startButton;
    private boolean running;

    public ConnectNPanel()
    {
        this.running = false;

        // set up start button
        this.startButton = new Button("Start");
        this.title = new Label();
        this.title.setFont(new Font("Arial", 30));

        // set up height slider
        this.heightSlider = new Slider(2, 12, 6);
        this.heightSlider.setOrientation(Orientation.VERTICAL);
        this.heightSlider.setMajorTickUnit(2.0);
        this.heightSlider.setMinorTickCount(1);
        this.heightSlider.setShowTickMarks(true);
        this.heightSlider.setShowTickLabels(true);
        this.heightSlider.setSnapToTicks(true);

        // set up width slider
        this.widthSlider = new Slider(2, 14, 7);
        this.widthSlider.setMajorTickUnit(2.0);
        this.widthSlider.setMinorTickCount(1);
        this.widthSlider.setShowTickMarks(true);
        this.widthSlider.setShowTickLabels(true);
        this.widthSlider.setSnapToTicks(true);

        // set up win condition slider
        this.winConditionSlider = new Slider(2, 14, 4);
        this.winConditionSlider.setMajorTickUnit(2.0);
        this.winConditionSlider.setMinorTickCount(1);
        this.winConditionSlider.setShowTickMarks(true);
        this.winConditionSlider.setShowTickLabels(true);
        this.winConditionSlider.setSnapToTicks(true);

        // set up handlers and listeners
        this.startButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e)
            {
                if (ConnectNPanel.this.running) {
                    ConnectNPanel.this.startButton.setText("Start");
                    ConnectNPanel.this.resetGame();
                    ConnectNPanel.this.running = false;
                }
                else {
                    ConnectNPanel.this.startButton.setText("Reset");
                    ConnectNPanel.this.startGame();
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
        this.boardPanel = new BoardPanel(440, 380, this.board);
        this.resetBoard();

        // this.setGridLinesVisible(true);
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(10, 10, 10, 10));

        this.add(this.title, 1, 0);
        this.add(this.winConditionSlider, 1, 1);
        this.add(this.heightSlider, 0, 2);
        this.add(this.boardPanel, 1, 2);
        this.add(this.widthSlider, 1, 3);
        this.add(this.startButton, 1, 4);
    }

    private void startGame()
    {
        new Thread(new GameManager(this.board, new MousePlayer(this.boardPanel),
                new AlphaBetaPlayer()))
                .start();
    }

    private void resetGame()
    {
        this.resetBoard();
    }

    private void resetBoard()
    {
        this.title.setText("Connect " + ((int) this.winConditionSlider.getValue()));
        this.title.setFont(Font.font("Serif", FontWeight.BOLD, 30));
        this.board = new Board((int) this.heightSlider.getValue(),
                               (int) this.widthSlider.getValue(),
                               (int) this.winConditionSlider.getValue());
        this.boardPanel.setBoard(this.board);
    }

    private void checkWinConditionSlider()
    {
        int height = (int) this.heightSlider.getValue();
        int width = (int) this.widthSlider.getValue();
        int winCondition = (int) this.winConditionSlider.getValue();
        if (winCondition > Math.max(height, width)) {
            this.winConditionSlider.setValue(Math.max(height, width));
        }
    }
}
