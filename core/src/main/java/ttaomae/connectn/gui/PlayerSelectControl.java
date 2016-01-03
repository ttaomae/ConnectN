package ttaomae.connectn.gui;

import java.io.IOException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;

/**
 * A JavaFX component which provides an interface for selecting between a human
 * or computer player and the computer difficulty. Provides two radio buttons to
 * select between 'human' or 'computer' and a slider to select the computer
 * difficulty.
 *
 * @author Todd Taomae
 */
public class PlayerSelectControl extends BorderPane
{
    @FXML private Label label;
    @FXML private Slider cpuDifficultySlider;
    @FXML private RadioButton playerHuman;
    @FXML private RadioButton playerComputer;

    private IntegerProperty playerNumber;
    private IntegerProperty minDifficulty;
    private IntegerProperty maxDifficulty;
    private BooleanProperty humanDefault;

    /**
     * Constructs a new PlayerSelectControl.
     */
    public PlayerSelectControl()
    {
        initialize();
        load();
    }

    /**
     * Constructs a new PlayerSelectControl with the specified properties.
     *
     * @param minDifficulty the minimum computer difficulty
     * @param maxDifficulty the maximum computer difficulty
     * @param playerNumber the number of the player being selected
     * @param humanDefault true if 'human' is selected by default, false if
     *            'computer' is selected by default
     */
    public PlayerSelectControl(int minDifficulty, int maxDifficulty,
                               int playerNumber, boolean humanDefault)
    {
        this();

        this.setMinDifficulty(minDifficulty);
        this.setMaxDifficulty(maxDifficulty);
        int midDifficulty = (minDifficulty + maxDifficulty) / 2;
        this.cpuDifficultySlider.setValue(midDifficulty);

        this.setPlayerNumber(playerNumber);
        this.setHumanDefault(humanDefault);

    }

    /**
     * Initializes the components.
     */
    private void initialize()
    {
        this.label = new Label();
        this.cpuDifficultySlider = new Slider();
        this.playerHuman = new RadioButton();
        this.playerComputer = new RadioButton();
    }

    /**
     * Loads the layout.
     */
    private void load()
    {
        FXMLLoader fxmlLoader =
                new FXMLLoader(getClass().getResource("/layout/player_select.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    public IntegerProperty playerNumberProperty()
    {
        if (this.playerNumber == null) {
            this.playerNumber = new SimpleIntegerProperty(1);
        }

        return this.playerNumber;
    }

    public final int getPlayerNumber()
    {
        return playerNumberProperty().get();
    }

    public final void setPlayerNumber(int playerNumber)
    {
        playerNumberProperty().set(playerNumber);
        this.label.setText("Player " + playerNumber);
    }

    public IntegerProperty minDifficultyProperty()
    {
        if (this.minDifficulty == null) {
            this.minDifficulty = new SimpleIntegerProperty(2);
        }

        return this.minDifficulty;
    }

    public final int getMinDifficulty()
    {
        return playerNumberProperty().get();
    }

    public final void setMinDifficulty(int minDifficulty)
    {
        minDifficultyProperty().set(minDifficulty);
        this.cpuDifficultySlider.setMin(minDifficulty);
    }

    public IntegerProperty maxDifficultyProperty()
    {
        if (this.maxDifficulty == null) {
            this.maxDifficulty = new SimpleIntegerProperty(10);
        }

        return this.minDifficulty;
    }

    public final int getMaxDifficulty()
    {
        return playerNumberProperty().get();
    }

    public final void setMaxDifficulty(int maxDifficulty)
    {
        minDifficultyProperty().set(maxDifficulty);
        this.cpuDifficultySlider.setMax(maxDifficulty);
    }

    public BooleanProperty humanDefaultProperty()
    {
        if (this.humanDefault == null) {
            this.humanDefault = new SimpleBooleanProperty(false);
        }

        return this.humanDefault;
    }

    public final boolean getHumanDefault()
    {
        return humanDefaultProperty().get();
    }

    public final void setHumanDefault(boolean humanDefault)
    {
        humanDefaultProperty().set(humanDefault);
        if (humanDefault) {
            this.playerHuman.setSelected(true);
        }
        else {
            this.playerComputer.setSelected(true);
        }
    }

    public boolean isHuman()
    {
        return this.playerHuman.isSelected();
    }

    public int getCpuDifficulty()
    {
        return (int) this.cpuDifficultySlider.getValue();
    }
}
