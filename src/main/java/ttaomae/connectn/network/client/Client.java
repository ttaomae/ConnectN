package ttaomae.connectn.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
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

    public Client(String hostname, int portNumber, Player player, Board board)
            throws UnknownHostException, IOException
    {
        this.socket = new Socket(hostname, portNumber);
        this.socketOut = new PrintWriter(socket.getOutputStream(), true);
        this.socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        this.player = player;
        this.board = board;
    }

    @Override
    public void run()
    {
        boolean rematch = true;
        while (rematch) {
            System.out.println("CLIENT: Starting game");

            // make sure the board is empty by undoing everything
            // TODO: kind of hack-y. must ensure that the panel and client have the same board
            while (this.board.undoPlay());

            try {
                playGame();

                String message = socketIn.readLine();
                if (message.equals(ConnectNProtocol.REMATCH)) {
                    rematch = getRematch();
                }
                // did not get a rematch message from the server, so quit
                else {
                    rematch = false;
                }
            } catch (IOException e) {
                System.err.println("Error reading from socket.");
                rematch = false;
            }
        }
        System.out.println("Done!");
    }

    private void playGame() throws IOException
    {
        while (this.board.getWinner() == Piece.NONE) {
            // get message from server
            String message = this.socketIn.readLine();

            // server is waiting for move
            if (message.equals(ConnectNProtocol.READY)) {
                // get a move and play it
                int move = -1;
                while (!this.board.isValidMove(move)) {
                    move = player.getMove(this.board.copy());
                }
                this.board.play(move);

                this.socketOut.println(ConnectNProtocol.constructMove(move));
            }
            // server sent opponent move
            else if (ConnectNProtocol.verifyMove(message)) {
                // play opponent's move
                this.board.play(ConnectNProtocol.parseMove(message));
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
            socketOut.println(ConnectNProtocol.YES);
            // wait for server response; rematch if opponent agrees
            return socketIn.readLine().equals(ConnectNProtocol.YES);

        }
        else {
            socketOut.println(ConnectNProtocol.NO);
            return false;
        }
    }
}
