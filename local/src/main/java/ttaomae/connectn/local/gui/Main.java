package ttaomae.connectn.local.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Launches the ConnectN application.
 *
 * @author Todd Taomae
 */
public class Main extends Application
{
    @Override
    public void start(Stage stage) throws Exception
    {
        stage.setTitle("ConnectN");

        stage.setScene(new Scene(new ConnectNControl()));
        stage.setResizable(false);
        stage.show();
    }

    /**
     * Starts the application.
     *
     * @param args command line arguments; passed to the application.
     */
    public static void main(String[] args)
    {
        launch(args);
    }
}
