package ttaomae.connectn.network.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ttaomae.connectn.network.ConnectNProtocol;

/**
 * Keeps track of the clients which are connected to a Connect-N server, but not
 * currently in a game.
 *
 * @author Todd Taomae
 */
public class ClientManager implements Runnable
{
    private Server server;
    private Set<Socket> playerPool;
    private Map<Socket, Socket> lastMatches;

    /**
     * Constructs a new ClientManager for the specified server.
     *
     * @param server the server whose clients are being managed
     * @throws IllegalArgumentException if the server is null
     */
    public ClientManager(Server server)
    {
        if (server == null) {
            throw new IllegalArgumentException("server must not be null");
        }

        this.server = server;
        this.playerPool = new HashSet<>();
        this.lastMatches = new HashMap<>();
    }

    /**
     * Adds a player which is connected on the specified socket to this
     * ClientManager.
     * 
     * @param player the player being added
     * @throws IllegalArgumentException if the player is null
     */
    public void addPlayer(Socket player)
    {
        if (player == null) {
            throw new IllegalArgumentException("player must not be null");
        }

        if (!player.isClosed()) {
            this.playerPool.add(player);
            this.server.printMessage("Adding player to pool...");
        }

        findMatchup();
    }

    /**
     * Finds a matchup between two players who have not just played each other.
     * If two players last matches wwere both against each other then they must
     * have played each other and at least one of them denied a rematch, so they
     * were added back to the pool. Since at least one denied a rematch we don't
     * want to match them up again.
     */
    private void findMatchup()
    {
        if (this.playerPool.size() >= 2) {
            Socket[] players = new Socket[this.playerPool.size()];
            this.playerPool.toArray(players);

            for (int i = 0; i < players.length; i++) {
                Socket playerOne = players[i];
                for (int j = i + 1; j < players.length; j++) {
                    Socket playerTwo = players[j];

                    Socket playerOneLastMatch = lastMatches.get(playerOne);
                    Socket playerTwoLastMatch = lastMatches.get(playerTwo);

                    // if each player is either new (last match is null) or they
                    // have not just player the other player
                    if ((playerOneLastMatch == null || !playerOneLastMatch.equals(playerTwo))
                        && (playerTwoLastMatch == null || !playerTwoLastMatch.equals(playerOne))) {
                        try {
                            startGame(playerOne, playerTwo);
                        } catch (IOException e) {
                            System.err.println("Error starting match.");
                        }

                        // exit the method so that we don't try to start another
                        // match
                        return;
                    }
                }
            }
        }
    }

    /**
     * Starts a game between two players, on a new thread.
     *
     * @throws IOException if there is an error starting the game
     */
    private void startGame(Socket playerOne, Socket playerTwo) throws IOException
    {
        synchronized(this)   {
            this.playerPool.remove(playerOne);
            this.playerPool.remove(playerTwo);
        }
        this.lastMatches.put(playerOne, playerTwo);
        this.lastMatches.put(playerTwo, playerOne);

        new Thread(new NetworkGameManager(this.server, playerOne, playerTwo)).start();
    }

    /**
     * Pings clients at regular intervals to check if they are still connected.
     */
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

    /**
     * Sends a PING message to each client then wait s for a reply.
     */
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

    /**
     * Closes the specified socket.
     *
     * @param socket the socket to be closed
     */
    private void closeSocket(Socket socket)
    {
        try {
            this.server.printMessage("Player has disconnected.");
            this.playerPool.remove(socket);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
