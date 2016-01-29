package ttaomae.connectn.network.server;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Connect-N network multiplayer server.
 *
 * @author Todd Taomae
 */
public class Server implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private int port;
    private ClientManager clientManager;

    /**
     * Constructs a new Server bound to the specified port.
     *
     * @param port the port number
     * @throws IllegalArgumentException if the port parameter is outside the
     *             specified range of valid port values, which is between 0 and
     *             65535, inclusive.
     */
    public Server(int port)
    {
        checkArgument(port >= 0 && port <= 65535, "port out of range: " + port);

        this.port = port;
        this.clientManager = new ClientManager();
    }

    /**
     * Continuously accepts connections and adds them to the pool of players.
     */
    @Override
    public void run()
    {
        Thread clientManagerThread = new Thread(this.clientManager, "Client Manager");
        clientManagerThread.setDaemon(true);
        clientManagerThread.start();

        try (ServerSocket serverSocket = new ServerSocket(this.port);) {
            logger.info("Waiting for connections...");
            while (true) {
                Socket socket = serverSocket.accept();
                logger.info("Player connected!");
                this.addToPlayerPool(socket);
            }
        }
        catch (IOException e) {
            logger.error("Exception caught when trying to listen on port "
                             + port + " or listening for a connection");
            logger.error(e.getMessage());
        }
        finally {
            clientManagerThread.interrupt();
        }
    }

    /**
     * Adds a player which is connected on the specified socket to the pool of
     * players.
     *
     * @param playerSocket the player being added
     */
    private void addToPlayerPool(Socket playerSocket)
    {
        checkNotNull(playerSocket, "playerSocket must not be null");
        checkState(!playerSocket.isClosed(), "playerSocket must not be closed");

        try {
            ClientHandler player = new ClientHandler(playerSocket);
            this.clientManager.playerConnected(player);
            logger.info("Player connected on socket [{}] added to player pool.", playerSocket);
        } catch (IOException e) {
            String message = String.format(
                    "Could not add player connected on socket [%s] to player pool.",
                    playerSocket);
            logger.error(message, e);
        }
    }
}
