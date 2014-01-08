package ttaomae.connectn.gui;

import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBoxBuilder;
 class PlayerSelectPanel extends BorderPane
{
    private Slider cpuDifficultySlider;
    private ToggleGroup playerSelect;
    private RadioButton playerHuman;
    private RadioButton playerComputer;

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

    public boolean isHuman()
    {
        return this.playerHuman.isSelected();
    }

    public int getCpuDifficulty()
    {
        return (int) this.cpuDifficultySlider.getValue();
    }
}
