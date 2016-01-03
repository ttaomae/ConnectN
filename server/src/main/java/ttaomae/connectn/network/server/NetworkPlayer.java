package ttaomae.connectn.network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.charset.Charset;

import ttaomae.connectn.Board;
import ttaomae.connectn.Player;
import ttaomae.connectn.network.ConnectNProtocol;

/**
 * A network Player.
 *
 * @author Todd Taomae
 */
public class NetworkPlayer implements Player
{
    private NetworkGameManager server;
    private PrintWriter socketOut;
    private BufferedReader socketIn;

    /**
     * Constructs a new NetworkPlayer which communicates using the specified
     * socket and is part of the server specified by a NetworkGameManager.
     *
     * @param server the server that this NetworkPlayer is part of
     * @param socket the socket that this NetworkPlayer will use to communicate
     *            with the client
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the server or socket is null
     */
    public NetworkPlayer(NetworkGameManager server, Socket socket) throws IOException
    {
        if (server == null) {
            throw new IllegalArgumentException("server must not be null");
        }

        if (socket == null) {
            throw new IllegalArgumentException("socket must not be null");
        }

        this.server = server;

        Writer writer = new OutputStreamWriter(socket.getOutputStream(), Charset.forName("UTF-8"));
        this.socketOut = new PrintWriter(writer, true);
        this.socketIn = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), Charset.forName("UTF-8")));
    }

    /**
     * Gets a move by sending a message through the socket and waiting for a
     * reply.
     */
    @Override
    public int getMove(Board board)
    {
        // notify the client that you are ready
        this.socketOut.println(ConnectNProtocol.READY);

        try {
            // get reply from client
            String reply = this.socketIn.readLine();
            if (reply != null && ConnectNProtocol.verifyMove(reply)) {
                int move = ConnectNProtocol.parseMove(reply);
                this.server.notifyOpponent(this, move);

                return move;
            }
        } catch (IOException e) {
            // player probably disconnected
            System.err.println("Error getting move from socket.");
            return -1;
        }

        return -1;
    }

    /**
     * Sends a message to the client on the other end of the socket.
     *
     * @param message the message to send
     */
    public void sendMessage(String message)
    {
        this.socketOut.println(message);
    }

    /**
     * Receives a message from the client on the other end of the socket.
     *
     * @return the message received from the client
     * @throws IOException if an I/O error occurs
     */
    public String receiveMessage() throws IOException
    {
        return this.socketIn.readLine();
    }
}
