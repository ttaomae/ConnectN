package ttaomae.connectn.network.client;

import java.io.IOException;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application
{
    private static final int DEFAULT_PORT = 4200;
    @Override
    public void start(Stage stage) throws Exception
    {
        // get host and port from parameters
        List<String> args = getParameters().getRaw();
        String hostname = "localhost";
        int portNumber = DEFAULT_PORT;
        if (args.size() == 2) {
            hostname = args.get(0);
            try {
                portNumber = Integer.parseInt(args.get(1));
            } catch (NumberFormatException e) {
                System.err.println("Using default port number.");
            }
        }

        stage.setTitle("Client");

        BorderPane root = new BorderPane();
        root.setCenter(new ClientPanel(hostname, portNumber));
        stage.setScene(new Scene(root, 450, 400));
        stage.show();
    }

    public static void main(String[] args) throws IOException
    {
        launch(args);
    }
}
