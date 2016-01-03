package ttaomae.connectn.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import ttaomae.connectn.Board;
import ttaomae.connectn.Piece;
import ttaomae.connectn.Player;
import ttaomae.connectn.network.ConnectNProtocol;

/**
 * A Connect-N network multiplayer client.
 *
 * @author Todd Taomae
 */
public class Client implements Runnable
{
    private Socket socket;
    private PrintWriter socketOut;
    private BufferedReader socketIn;

    private Player player;
    /** This client's copy of the board */
    private Board board;

    private List<ClientListener> listeners;

    /**
     * Indicates whether or not this Client is waiting for some kind of
     * response. For example, it may be waiting to confirm whether or not to
     * accept a rematch.
     */
    private volatile boolean waitingForResponse;

    /**
     * Constructs a new Client which connects to the server specified by the
     * host and port. Moves are selected by the specified Player and are played
     * on the specified Board. It is the client's responsibility to keep a
     * properly updated board.
     *
     * @param host the host name, or null for the loopback address
     * @param port the port number
     * @param player the player that will select moves
     * @param board the client's copy of the board
     * @throws UnknownHostException - if the IP address of the host could not be determined
     * @throws IOException if an I/O error occurs when connecting to the server
     * @throws IllegalArgumentException if the player or board is null or if the port is invalid
     */
    public Client(String host, int port, Player player, Board board)
            throws UnknownHostException, IOException
    {
        if (player == null) {
            throw new IllegalArgumentException("player must not be null");
        }
        if (board == null) {
            throw new IllegalArgumentException("board must not be null");
        }

        this.socket = new Socket(host, port);
        this.socketOut = new PrintWriter(socket.getOutputStream(), true);
        this.socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        this.player = player;
        this.board = board;

        this.listeners = new ArrayList<>();
    }

    /**
     * Closes the socket.
     */
    public void disconnect()
    {
        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Continuously reads from the server and responds to each message based on
     * the Connect-N network protocol.
     */
    @Override
    public void run()
    {
        while (true) {
            try {
                String message = this.getMessageFromServer();

                // start a new game
                if (message.equals(ConnectNProtocol.START)) {
                    // make sure the board is empty by undoing everything
                    // TODO: kind of hack-y. must ensure that the panel and client have the same board
                    while (this.board.undoPlay()); // NOPMD

                    System.out.println("CLIENT: Starting game");
                    playGame();
                }

                // confirm a rematch
                else if (message.equals(ConnectNProtocol.REMATCH)) {
                    getRematch();
                }

                // server is pinging so verify connection
                else if (message.equals(ConnectNProtocol.PING)) {
                    sendMessageToServer(ConnectNProtocol.PING);
                }

                // not what we expected so start over and get a new message
                else {
                    continue;
                }
            } catch (IOException e) {
                System.err.println("Error reading from socket.");
                break;
            }
        }
        System.out.println("CLIENT: Done!");
    }

    /**
     * Plays a game.
     *
     * @throws IOException if there is an error communicating with the server
     */
    private void playGame() throws IOException
    {
        while (this.board.getWinner() == Piece.NONE) {
            // get message from server
            String message = this.getMessageFromServer();

            // server is waiting for move
            if (message.equals(ConnectNProtocol.READY)) {
                // get a move and play it
                int move = -1;
                while (!this.board.isValidMove(move)) {
                    move = player.getMove(this.board.copy());
                }
                this.board.play(move);

                this.sendMessageToServer(ConnectNProtocol.constructMove(move));
            }
            // server sent opponent move
            else if (ConnectNProtocol.verifyMove(message)) {
                // play opponent's move
                this.board.play(ConnectNProtocol.parseMove(message));
            }
            // server is pinging so verify connection
            else if (message.equals(ConnectNProtocol.PING)) {
                this.sendMessageToServer(ConnectNProtocol.PING);
            }
            // opponent has disconnected; end game
            else if (message.equals(ConnectNProtocol.DICONNECTED)) {
                break;
            }
        }

        System.out.println(this.board.getWinner() + " wins!");
    }

    /**
     * Waits for someone to call confirmRematch or denyRematch.
     */
    private synchronized void getRematch()
    {
        this.waitingForResponse = true;
        try {
            while (this.waitingForResponse) {
                this.wait();
            }
        } catch (InterruptedException e) {
            this.sendMessageToServer(ConnectNProtocol.NO);
        }
    }

    /**
     * Confirms a rematch. If the client is waiting for a response a message
     * will be sent to the server. Otherwise nothing will happen.
     */
    public synchronized void confirmRematch()
    {
        if (this.waitingForResponse) {
            this.sendMessageToServer(ConnectNProtocol.YES);
            this.waitingForResponse = false;
            this.notifyAll();
        }
    }

    /**
     * Denies a rematch. If the client is waiting for a response a message will
     * be sent to the server. Otherwise nothing will happen.
     */
    public synchronized void denyRematch()
    {
        if (this.waitingForResponse) {
            this.sendMessageToServer(ConnectNProtocol.NO);
            this.waitingForResponse = false;
            this.notifyAll();
        }
    }

    /**
     * Gets a message from the server and notifies listeners of the event.
     *
     * @return the message received from the server
     * @throws IOException if an I/O error occurs while reading from the server
     */
    private String getMessageFromServer() throws IOException
    {
        String result = this.socketIn.readLine();
        this.notifyListeners(true, result);

        return result;
    }

    /**
     * Sends a message to the server and notifies listeners of the event.
     *
     * @param message the message to send to the server
     */
    private void sendMessageToServer(String message)
    {
        this.socketOut.println(message);
        this.notifyListeners(false, message);
    }

    /**
     * Adds a ClientListener to this Client.
     *
     * @param cl the ClientListener
     */
    public void addListener(ClientListener cl)
    {
        this.listeners.add(cl);
    }

    /**
     * Notifies the listeners on this Client that a message has been received
     * from or sent to the server.
     *
     * @param received true if a message was received, false if a message was sent
     * @param message the message received from or sent to the server
     */
    private void notifyListeners(boolean received, String message)
    {
        for (ClientListener cl : this.listeners) {
            if (cl != null) {
                if (received) {
                    cl.clientReceivedMessage(message);
                }
                else {
                    cl.clientSentMessage(message);
                }
            }
        }
    }
}
