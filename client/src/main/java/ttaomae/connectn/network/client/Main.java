package ttaomae.connectn.network.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application
{
    @Override
    public void start(Stage stage) throws Exception
    {
        stage.setTitle("Client");

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
