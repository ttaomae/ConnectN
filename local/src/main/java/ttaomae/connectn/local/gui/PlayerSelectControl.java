package ttaomae.connectn.local.gui;

import java.io.IOException;
import java.util.ResourceBundle;

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
import ttaomae.connectn.util.ResourceBundleUtil;

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
    private static final ResourceBundle GUI_STRINGS
            = ResourceBundleUtil.getResourceBundle("gui", "locale.properties");

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
        load();
    }

    /**
     * Loads the layout.
     */
    private void load()
    {
        FXMLLoader fxmlLoader =
                new FXMLLoader(getClass().getResource("/layout/player_select.fxml"));
        fxmlLoader.setResources(GUI_STRINGS);
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
        this.label.setText(GUI_STRINGS.getString("player_label") + playerNumber);
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
            this.disableCpuDifficultySlider();
        }
        else {
            this.playerComputer.setSelected(true);
            this.enableCpuDifficultySlider();
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

    @FXML
    private void disableCpuDifficultySlider() // NOPMD
    {
        this.cpuDifficultySlider.setDisable(true);
    }

    @FXML
    private void enableCpuDifficultySlider() // NOPMD
    {
        this.cpuDifficultySlider.setDisable(false);
    }
}
