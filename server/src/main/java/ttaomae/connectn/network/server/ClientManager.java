package ttaomae.connectn.network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import ttaomae.connectn.network.ConnectNProtocol;

public class ClientManager implements Runnable
{
    private Server server;
    private Set<Socket> playerPool;

    public ClientManager(Server server)
    {
        if (server == null) {
            throw new IllegalArgumentException("server must not be null");
        }

        this.server = server;
        this.playerPool = new HashSet<>();
    }

    public void addPlayer(Socket player)
    {
        if (!player.isClosed()) {
            this.playerPool.add(player);
            this.server.printMessage("Adding player to pool...");
        }

        if (this.playerPool.size() >= 2) {
            try {
                startGame();
            } catch (IOException e) {
                System.err.println("Error starting match");
            }
        }
    }

    private void startGame() throws IOException
    {
        synchronized (this) {
            if (this.playerPool.size() >= 2) {
                Iterator<Socket> iter = this.playerPool.iterator();
                Socket one = iter.next();
                iter.remove();
                Socket two = iter.next();
                iter.remove();
                new Thread(new NetworkGameManager(this.server, one, two)).start();
            }
        }
    }

    @Override
    public void run()
    {
        while (true) {
            synchronized (this) {
                pingClients();
            }
            try {
                Thread.sleep(ConnectNProtocol.PING_INTERVAL);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private void pingClients()
    {
        // send ping
        for (Socket s : this.playerPool) {
            try {
                PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                pw.println(ConnectNProtocol.PING);
                if (pw.checkError()) {
                    this.closeSocket(s);
                }
            } catch (IOException e) {
                this.closeSocket(s);
            }
        }

        // receive ping
        for (Socket s : this.playerPool) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String reply = br.readLine();
                if (reply == null) {
                    this.closeSocket(s);
                }
            } catch (IOException e) {
                this.closeSocket(s);
            }
        }
    }

    private void closeSocket(Socket socket)
    {
        try {
            this.playerPool.remove(socket);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
