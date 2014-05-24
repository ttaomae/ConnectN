package ttaomae.connectn.network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable
{
    private NetworkPlayer playerOne;
    private NetworkPlayer playerTwo;
    private int portNumber;

    public Server(int portNumber)
    {
        this.portNumber = portNumber;
    }

    @Override
    public void run()
    {
        try (ServerSocket serverSocket = new ServerSocket(this.portNumber);) {
            printMessage("Waiting for connections...");

            Socket one = serverSocket.accept();
            Socket two = serverSocket.accept();
            // start new game
            Thread t = new Thread(new NetworkGameManager(this, one, two));
            t.start();
            t.join();

        } catch (IOException e) {
            System.err.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.err.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void printMessage(String message)
    {
        System.out.println("SERVER: " + message);
    }
}
