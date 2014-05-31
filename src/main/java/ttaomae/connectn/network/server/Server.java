package ttaomae.connectn.network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable
{
    private int portNumber;
    private ClientManager clientManager;

    public Server(int portNumber)
    {
        this.portNumber = portNumber;
        this.clientManager = new ClientManager(this);
    }

    @Override
    public void run()
    {
        new Thread(this.clientManager, "Client Manager").start();

        try (ServerSocket serverSocket = new ServerSocket(this.portNumber);) {
            printMessage("Waiting for connections...");
            while (true) {
                this.addToPlayerPool(serverSocket.accept());
                printMessage("Player connected!");
            }
        } catch (IOException e) {
            System.err.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.err.println(e.getMessage());
        }
    }

    public void addToPlayerPool(Socket player)
    {
        this.clientManager.addPlayer(player);
    }

    public void printMessage(String message)
    {
        System.out.println("SERVER: " + message);
    }
}
