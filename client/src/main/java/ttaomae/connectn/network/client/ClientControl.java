package ttaomae.connectn.network.client;

import java.io.IOException;
import java.net.Socket;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import ttaomae.connectn.ArrayBoard;
import ttaomae.connectn.Board;
import ttaomae.connectn.gui.BoardPanel;
import ttaomae.connectn.gui.MousePlayer;
import ttaomae.connectn.network.LostConnectionException;
import ttaomae.connectn.network.ProtocolEvent;
import ttaomae.connectn.network.ProtocolEvent.Message;
import ttaomae.connectn.network.ProtocolListener;

/**
 * A JavaFX component which provides an interface for the client of a network
 * multiplayer Connect-N game.
 *
 * @author Todd Taomae
 */
public class ClientControl extends BorderPane implements ProtocolListener
{
    @FXML private TextField hostField;
    @FXML private TextField portField;
    @FXML private Button connectButton;

    @FXML private BoardPanel boardPanel;

    @FXML private Label displayMessage;

    @FXML private Button yesButton;
    @FXML private Button noButton;

    private boolean connected;
    private ServerHandler serverHandler;

    /**
     * Constructs a new ClientControl
     */
    public ClientControl()
    {
        initialize();
        load();

        this.connected = false;

        // display a default board to start
        this.boardPanel.setBoard(new ArrayBoard());
    }

    /**
     * Initializes the components.
     */
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

    /**
     * Loads the layout.
     */
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

    /**
     * Connects to the server.
     */
    @FXML
    private void connect() // NOPMD
    {
        if (!this.connected) {
            try {
                String host = hostField.getText();
                int port = Integer.parseInt(portField.getText());
                Socket socket = new Socket(host, port);

                Board board = new ArrayBoard();
                this.boardPanel.setBoard(board);

                MousePlayer player = new MousePlayer(this.boardPanel);
                this.serverHandler = new ServerHandler(socket, player, board);
                this.serverHandler.addProtocolListener(this);

                Thread myThread = new Thread(serverHandler, "Server Handler");
                myThread.setDaemon(true);
                myThread.start();

                this.updateMessage(String.format("Connected to %s:%d%n", host, port));
                this.connected = true;
                javafx.application.Platform.runLater(() ->
                    ClientControl.this.connectButton.setDisable(true)
                );
            }
            catch (NumberFormatException e) {
                this.updateMessage("Invalid port number.");
            }
            catch (IOException e) {
                this.updateMessage("Could not connect to server.");
            }
        }
    }

    /**
     * Confirms a rematch.
     */
    @FXML
    private void confirm() // NOPMD
    {
        try {
            this.serverHandler.confirmRematch();
        } catch (LostConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        yesNoButtonsSetDisable(true);
    }

    /**
     * Denies a rematch.
     */
    @FXML
    private void deny() // NOPMD
    {
        try {
            this.serverHandler.denyRematch();
        } catch (LostConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        yesNoButtonsSetDisable(true);
    }

    /**
     * Disconnects the client from the server.
     */
    public void disconnect()
    {
        if (this.serverHandler != null) {
            this.serverHandler.disconnect();
        }
    }

    /**
     * Enables or disables the 'yes' and 'no' buttons.
     *
     * @param disable true if the buttons should be disabled; false if they should be enabled
     */
    private void yesNoButtonsSetDisable(final boolean disable)
    {
        javafx.application.Platform.runLater(() -> {
            ClientControl.this.yesButton.setDisable(disable);
            ClientControl.this.noButton.setDisable(disable);
        });
    }

    /**
     * Sets the display to the specified message.
     *
     * @param message the message to update to
     */
    private void updateMessage(final String message)
    {
        javafx.application.Platform.runLater(() ->
            ClientControl.this.displayMessage.setText(message)
        );
    }

    @Override
    public void eventReceived(ProtocolEvent receivedEvent)
    {
        switch (receivedEvent.getMessage()) {
            case START_GAME:
                this.updateMessage("Starting game... Waiting for opponent...");
                break;
            case REQUEST_MOVE:
                this.updateMessage("Select your move");
                break;
            case REQUEST_REMATCH:
                this.updateMessage("Game Over! Rematch?");
                yesNoButtonsSetDisable(false);
                break;
            case DENY_REMATCH:
                this.updateMessage("Opponent denied rematch.");
                break;
            case OPPONENT_DISCONNECTED:
                this.updateMessage("Opponent disconnected!");
                break;
            default:
                // ignore other cases
        }
    }

    @Override
    public void messageSent(Message sentMessage)
    {

    }

    @Override
    public void moveSent(Message moveMessage, int move)
    {

    }
}
