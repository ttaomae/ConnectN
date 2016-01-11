package ttaomae.connectn.network.client;

import java.io.IOException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application
{
    @Override
    public void start(Stage stage) throws Exception
    {
        stage.setTitle("Client");

        BorderPane root = new BorderPane();
        final ClientControl client = new ClientControl();
        root.setCenter(client);
        stage.setScene(new Scene(root, 450, 500));
        stage.setResizable(false);
        stage.setOnHidden(windowEvent -> client.disconnect());
        stage.show();
    }

    public static void main(String[] args) throws IOException
    {
        launch(args);
    }
}
