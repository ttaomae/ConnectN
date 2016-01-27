package ttaomae.connectn.network.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ttaomae.connectn.network.LostConnectionException;
import ttaomae.connectn.network.ProtocolEvent.Message;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * Keeps track of the clients which are connected to a Connect-N server.
 *
 * @author Todd Taomae
 */
public class ClientManager implements Runnable
{
    private static final Logger logger = LoggerFactory.getLogger(ClientManager.class);

    private final ExecutorService gameManagerPool;
    private final Set<ClientHandler> connectedPlayers;
    private final Set<ClientHandler> eligiblePlayers;
    private final Map<ClientHandler, ClientHandler> lastMatches;

    /**
     * Constructs a new ClientManager for the specified server.
     */
    ClientManager()
    {
        this.connectedPlayers = ConcurrentHashMap.newKeySet();
        this.eligiblePlayers = ConcurrentHashMap.newKeySet();
        this.lastMatches = new HashMap<>();

        this.gameManagerPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setNameFormat("network-game-manager-%d")
                .setUncaughtExceptionHandler((thread, cause) -> {
                    if (cause instanceof ClientDisconnectedException) {
                        ClientDisconnectedException cde = (ClientDisconnectedException) cause;
                        playerDisconnected(cde.getClientHandler());
                    }
                    else if (cause instanceof NetworkGameException) {
                        NetworkGameException nge = (NetworkGameException) cause;
                        playerMatchEnded(nge.getNetworkGameManager().getPlayerOne());
                        playerMatchEnded(nge.getNetworkGameManager().getPlayerTwo());
                    }
                    else {
                        logger.error("Error while running game manager on thread " + thread, cause);
                    }
                })
                .build());
    }

    /**
     * Adds a player which is connected on the specified socket to this
     * ClientManager.
     *
     * @param playerSocket the player being added
     * @throws IllegalArgumentException if the player is null
     * @throws IllegalStateException if the specified socket is closed
     */
    void playerConnected(ClientHandler player)
    {
        checkNotNull(player, "player must not be null");

        this.connectedPlayers.add(player);
        addEligiblePlayer(player);
    }

    void playerDisconnected(ClientHandler player)
    {
        checkNotNull(player, "player must not be null");

        if (this.connectedPlayers.contains(player)) {
            logger.info("Player disconnected: {}", player);
            this.connectedPlayers.remove(player);
            this.eligiblePlayers.remove(player);
        }
        else {
            logger.warn("Attempted to remove player who was not connected: {}", player);
        }
    }

    void playerMatchEnded(ClientHandler player)
    {
        checkNotNull(player, "player must not be null");

        try {
            player.sendMessage(Message.PING);
            logger.info("Adding {} back to eligible player pool.", player);
            addEligiblePlayer(player);
        }
        catch (LostConnectionException e) {
            playerDisconnected(player);
        }
    }

    @Override
    public void run()
    {
        while (true) {
            try {
                this.eligiblePlayers.wait();
            }
            catch (InterruptedException e) {
                break;
            }

            Optional<NetworkGameManager> optionalGameManager = findMatchup();
            if (optionalGameManager.isPresent()) {
                NetworkGameManager gameManager = optionalGameManager.get();

                logger.info("Starting match between {} and {}.",
                        gameManager.getPlayerOne(), gameManager.getPlayerTwo());;

                this.eligiblePlayers.remove(gameManager.getPlayerOne());
                this.eligiblePlayers.remove(gameManager.getPlayerTwo());

                this.lastMatches.put(gameManager.getPlayerOne(), gameManager.getPlayerTwo());
                this.lastMatches.put(gameManager.getPlayerTwo(), gameManager.getPlayerOne());

                this.gameManagerPool.submit(gameManager);
            }
        }
    }
    /**
     * Finds a matchup between two players who have not just played each other.
     * If two players last matches were both against each other then they must
     * have played each other and at least one of them denied a rematch, so they
     * were added back to the pool. Since at least one denied a rematch we don't
     * want to match them up again.
     */
    private Optional<NetworkGameManager> findMatchup()
    {
        if (this.eligiblePlayers.size() < 2) {
            return Optional.empty();
        }

        for (ClientHandler playerOne : this.eligiblePlayers) {
            Optional<ClientHandler> optionalPlayerTwo = this.eligiblePlayers.stream()
                    // exclude playerOne
                    .filter(player -> !playerOne.equals(player))
                    // exclude playerOne's last opponent
                    .filter(player -> !this.lastMatches.get(playerOne).equals(player))
                    // exclude player's whose last opponent was playerOne
                    .filter(player -> !this.lastMatches.get(player).equals(playerOne))
                    .findAny();

            if (optionalPlayerTwo.isPresent()) {
                ClientHandler playerTwo = optionalPlayerTwo.get();
                return Optional.of(new NetworkGameManager(this, playerOne, playerTwo));
            }
        }

        return Optional.empty();
    }

    private void addEligiblePlayer(ClientHandler player)
    {
        this.eligiblePlayers.add(player);
        this.eligiblePlayers.notifyAll();
    }
}
