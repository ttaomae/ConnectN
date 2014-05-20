package ttaomae.connectn.network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ttaomae.connectn.Board;
import ttaomae.connectn.Player;
import ttaomae.connectn.network.ConnectNProtocol;

public class NetworkPlayer implements Player
{
    Server server;
    private PrintWriter socketOut;
    private BufferedReader socketIn;

    public NetworkPlayer(Server server, Socket socket) throws IOException
    {
        this.server = server;

        this.socketOut = new PrintWriter(socket.getOutputStream(), true);
        this.socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public int getMove(Board board)
    {
        // notify the client that you are ready
        this.socketOut.println(ConnectNProtocol.READY);

        try {
            // get reply from client
            String reply = this.socketIn.readLine();
            if (reply.startsWith(ConnectNProtocol.MOVE)) {
                int move = ConnectNProtocol.parseMove(reply);
                this.server.notifyOpponent(this, move);

                return move;
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error getting move from socket.");
            return -1;
        }

        return -1;
    }

    public void sendOpponentMove(int move)
    {
        this.socketOut.println(ConnectNProtocol.constructMessage(move));
    }
}
