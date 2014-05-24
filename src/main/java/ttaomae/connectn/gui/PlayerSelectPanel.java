package ttaomae.connectn.gui;

import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBoxBuilder;

/**
 * A panel providing an interface for selecting between a human or computer
 * player and the computer difficulty. Provides two radio buttons to select
 * between 'human' or 'computer' and a slider to select the computer difficulty.
 * 
 * @author Todd Taomae
 */
public class PlayerSelectPanel extends BorderPane
{
    private Slider cpuDifficultySlider;
    private ToggleGroup playerSelect;
    private RadioButton playerHuman;
    private RadioButton playerComputer;

    /**
     * Constructs a new PlayerSelectPanel with the specified min and max
     * computer difficulty, the specified player number, and the specified
     * initial state of the human/computer radio buttons.
     *
     * @param min the minimum computer difficulty
     * @param max the maximum computer difficulty
     * @param playerNum the player number
     * @param human whether or not the human radio button should be initially
     *            selected
     */
    public PlayerSelectPanel(int min, int max, int playerNum, boolean human)
    {

        this.cpuDifficultySlider = new Slider(min, max, (min + max) / 2);
        this.cpuDifficultySlider.setMajorTickUnit(2.0);
        this.cpuDifficultySlider.setMinorTickCount(1);
        this.cpuDifficultySlider.setShowTickMarks(true);
        this.cpuDifficultySlider.setShowTickLabels(true);
        this.cpuDifficultySlider.setSnapToTicks(true);

        this.playerSelect = new ToggleGroup();
        this.playerHuman = new RadioButton("Human");
        this.playerHuman.setToggleGroup(this.playerSelect);
        this.playerComputer = new RadioButton("Computer");
        this.playerComputer.setToggleGroup(this.playerSelect);

        if (human) {
            this.playerHuman.setSelected(true);
        }
        else {
            this.playerComputer.setSelected(true);
        }

        Label label = new Label("Player " + playerNum);
        this.setCenter(new FlowPane());
        this.setTop(label);
        this.setLeft(HBoxBuilder.create().children(this.playerHuman, this.playerComputer)
                .build());
        this.setBottom(VBoxBuilder.create()
                .children(new Label("Computer Difficulty"), this.cpuDifficultySlider).build());
        // this.getChildren().addAll(this.cpuDifficultySlider, this.playerHuman,
        // this.playerComputer);
    }

    /**
     * Returns whether or not the human player is currently selected.
     *
     * @return true if the human radio button is selected, false otherwise
     */
    public boolean isHuman()
    {
        return this.playerHuman.isSelected();
    }

    /**
     * Returns the currently selected computer difficulty.
     *
     * @return the computer difficulty
     */
    public int getCpuDifficulty()
    {
        return (int) this.cpuDifficultySlider.getValue();
    }
}
