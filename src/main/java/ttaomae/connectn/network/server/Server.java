package ttaomae.connectn.network.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Server implements Runnable
{
    private int portNumber;
    private Set<Socket> playerPool;

    public Server(int portNumber)
    {
        this.portNumber = portNumber;
        this.playerPool = new HashSet<>();
    }

    @Override
    public void run()
    {
        try (ServerSocket serverSocket = new ServerSocket(this.portNumber);) {
            while (true) {
                printMessage("Waiting for connection...");

                this.addToPlayerPool(serverSocket.accept());
            }
        } catch (IOException e) {
            System.err.println("Exception caught when trying to listen on port "
                + portNumber + " or listening for a connection");
            System.err.println(e.getMessage());
        }
    }

    private void startGame() throws IOException
    {
        if (this.playerPool.size() >= 2) {
            Iterator<Socket> iter = this.playerPool.iterator();
            Socket one = iter.next();
            iter.remove();
            Socket two = iter.next();
            iter.remove();
            new Thread(new NetworkGameManager(this, one, two)).start();
        }
    }

    public void addToPlayerPool(Socket player)
    {
        this.playerPool.add(player);
        if (this.playerPool.size() >= 2) {
            try {
                startGame();
            } catch (IOException e) {
                System.err.println("Error starting match");
            }
        }
    }

    public void printMessage(String message)
    {
        System.out.println("SERVER: " + message);
    }

}
