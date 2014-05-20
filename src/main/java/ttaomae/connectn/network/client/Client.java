package ttaomae.connectn.network.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import ttaomae.connectn.AlphaBetaPlayer;
import ttaomae.connectn.Board;
import ttaomae.connectn.Player;
import ttaomae.connectn.network.ConnectNProtocol;

// TODO: add constructor with board parameters
public class Client implements Runnable
{
    private Socket socket;

    private Player player;
    /** This client's copy of the board */
    private Board board;

    public Client(String hostname, int portNumber)
            throws UnknownHostException, IOException
    {
        this(hostname, portNumber, new AlphaBetaPlayer());
    }

    public Client(String hostname, int portNumber, Player player)
            throws UnknownHostException, IOException
    {
        this.socket = new Socket(hostname, portNumber);
        this.board = new Board();

        this.player = player;
    }

    @Override
    public void run()
    {
        try (
            PrintWriter socketOut = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader socketIn =
                new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // TODO: change condition
            while (true) {
                try {
                    // get message from server
                    String message = socketIn.readLine();

                    // server is waiting for move
                    if (message.equals(ConnectNProtocol.READY)) {
                        // get a move and
                        int move = player.getMove(this.board.copy());
                        this.board.play(move);

                        socketOut.println(ConnectNProtocol.constructMessage(move));
                    }
                    else if (message.startsWith(ConnectNProtocol.MOVE)) {
                        this.board.play(ConnectNProtocol.parseMove(message));
                    }
                } catch (IOException e) {
                    System.err.println("Error reading from socket.");
                }
            }
        } catch (IOException e) {
            System.err.println("Couldn't connect to server");
        }
    }
}
