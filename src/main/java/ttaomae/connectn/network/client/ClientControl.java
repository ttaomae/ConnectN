package ttaomae.connectn.network.client;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import ttaomae.connectn.Board;
import ttaomae.connectn.gui.BoardPanel;
import ttaomae.connectn.gui.MousePlayer;
import ttaomae.connectn.network.ConnectNProtocol;

public class ClientControl extends BorderPane implements ClientListener
{
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private Button connectButton;

    @FXML private BoardPanel boardPanel;

    @FXML private Label displayMessage;

    @FXML private Button yesButton;
    @FXML private Button noButton;

    private boolean connected;
    private Client client;

    public ClientControl()
    {
        initialize();
        load();

        this.connected = false;

        // display a default board to start
        this.boardPanel.setBoard(new Board());
    }

    private void initialize()
    {
        this.hostField = new TextField();
        this.portField = new TextField();
        this.connectButton = new Button();
        this.boardPanel = new BoardPanel();
        this.displayMessage = new Label();
        this.yesButton = new Button();
        this.noButton = new Button();
    }

    private void load()
    {
        FXMLLoader fxmlLoader =
                new FXMLLoader(getClass().getResource("/layout/client.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
    }

    @FXML
    private void connect()
    {
        if (!this.connected) {
            try {
                String host = hostField.getText();
                int port = Integer.parseInt(portField.getText());
                Board b = new Board();
                this.boardPanel.setBoard(b);
                MousePlayer mp = new MousePlayer(this.boardPanel);
                this.client = new Client(host, port, mp, b);
                this.client.addListener(this);

                Thread myThread = new Thread(client);
                myThread.setDaemon(true);
                myThread.start();

                this.updateMessage(String.format("Connected to %s:%d%n", host, port));
                this.connected = true;
                this.connectButton.setDisable(true);
            }
            catch (NumberFormatException e) {
                this.updateMessage("Invalid port number.");
            }
            catch (IOException e) {
                this.updateMessage("Could not connect to server.");
            }
        }
    }

    @FXML
    private void confirm()
    {
        this.client.confirmRematch();
        yesNoButtonsSetDisable(true);
    }

    @FXML
    private void deny()
    {
        this.client.denyRematch();
        yesNoButtonsSetDisable(true);
    }

    public void disconnect()
    {
        if (this.client != null) {
            this.client.disconnect();
        }
    }

    private void yesNoButtonsSetDisable(final boolean disable)
    {
        javafx.application.Platform.runLater(new Runnable() {
            @Override public void run() {
                ClientControl.this.yesButton.setDisable(disable);
                ClientControl.this.noButton.setDisable(disable);
            }
        });
    }

    /**
     * Sets the display to the specified message.
     *
     * @param message the message to update to
     */
    public void updateMessage(final String message)
    {
        javafx.application.Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                ClientControl.this.displayMessage.setText(message);
            }
        });
    }

    @Override
    public void clientReceivedMessage(String message)
    {
        switch (message) {
            case ConnectNProtocol.START:
                this.updateMessage("Starting game...");
                break;
            case ConnectNProtocol.READY:
                this.updateMessage("Select your move");
                break;
            case ConnectNProtocol.REMATCH:
                this.updateMessage("Game Over! Rematch?");
                yesNoButtonsSetDisable(false);
                break;
                break;
            case ConnectNProtocol.DICONNECTED:
                this.updateMessage("Opponent disconnected!");
        }
    }

    @Override
    public void clientSentMessage(String message)
    {
        if (message.startsWith(ConnectNProtocol.MOVE)) {
            this.updateMessage("Waiting for opponent...");
        }
    }
}
