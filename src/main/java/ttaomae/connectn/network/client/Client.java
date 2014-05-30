package ttaomae.connectn.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ttaomae.connectn.Board;
import ttaomae.connectn.Piece;
import ttaomae.connectn.Player;
import ttaomae.connectn.network.ConnectNProtocol;

// TODO: add constructor with board parameters
public class Client implements Runnable
{
    private Socket socket;
    private PrintWriter socketOut;
    private BufferedReader socketIn;

    private Player player;
    /** This client's copy of the board */
    private Board board;

    private List<ClientListener> listeners;

    public Client(String hostname, int portNumber, Player player, Board board)
            throws UnknownHostException, IOException
    {
        this.socket = new Socket(hostname, portNumber);
        this.socketOut = new PrintWriter(socket.getOutputStream(), true);
        this.socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        this.player = player;
        this.board = board;

        this.listeners = new ArrayList<>();
    }

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
                    while (this.board.undoPlay());

                    System.out.println("CLIENT: Starting game");
                    playGame();
                }
                // confirm a rematch
                else if (message.equals(ConnectNProtocol.REMATCH)) {
                    getRematch();
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
                System.out.println("opp dc");
                break;
            }
        }

        System.out.println(this.board.getWinner() + " wins!");
    }

    private boolean getRematch() throws IOException
    {
        // get input from user
        // TODO: very hack-y
        System.out.println("Type 'y' for a rematch.");
        if (new Scanner(System.in).nextLine().equals("y")) {
            this.sendMessageToServer(ConnectNProtocol.YES);
            // wait for server response; rematch if opponent agrees
            return this.getMessageFromServer().equals(ConnectNProtocol.YES);
        }
        else {
            this.sendMessageToServer(ConnectNProtocol.NO);
            return false;
        }
    }

    private String getMessageFromServer() throws IOException
    {
        String result = this.socketIn.readLine();
        this.notifyListeners(true, result);

        return result;
    }

    private void sendMessageToServer(String message)
    {
        this.socketOut.println(message);
        this.notifyListeners(false, message);
    }

    public void addListener(ClientListener cl)
    {
        this.listeners.add(cl);
    }

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
