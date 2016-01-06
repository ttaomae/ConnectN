package ttaomae.connectn.local.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
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

        BorderPane root = new BorderPane();
        root.setCenter(new ConnectNControl());

        stage.setScene(new Scene(root, 500, 660));
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
