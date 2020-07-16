package ttaomae.connectn.local.gui;

import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ttaomae.connectn.util.ResourceBundleUtil;

public class ConnectNLocal extends Application
{

    @Override
    public void start(Stage stage) throws Exception
    {
        ResourceBundle guiStrings = ResourceBundleUtil.getResourceBundle(
                "gui", "locale.properties");
        stage.setTitle(guiStrings.getString("window_title"));

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
