package ttaomae.connectn.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application
{
    @Override
    public void start(Stage stage) throws Exception
    {
        stage.setTitle("ConnectN");

        BorderPane root = new BorderPane();
        root.setCenter(new ConnectNPanel());

        stage.setScene(new Scene(root, 510, 730));
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
