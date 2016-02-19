package ttaomae.connectn.network.client;

import java.io.IOException;
import java.util.ResourceBundle;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ttaomae.connectn.util.ResourceBundleUtil;

public class Main extends Application
{
    @Override
    public void start(Stage stage) throws Exception
    {
        ResourceBundle guiStrings = ResourceBundleUtil.getResourceBundle(
                "gui", "locale.properties");
        stage.setTitle(guiStrings.getString("window_title"));

        final ClientControl client = new ClientControl();
        stage.setScene(new Scene(client));
        stage.setResizable(false);
        stage.setOnHidden(windowEvent -> client.disconnect());
        stage.show();
    }

    public static void main(String[] args) throws IOException
    {
        launch(args);
    }
}
