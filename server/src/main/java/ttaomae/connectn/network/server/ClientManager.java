package ttaomae.connectn.network.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

    private volatile boolean possibleMatchups;

    /**
     * Constructs a new ClientManager for the specified server.
     */
    ClientManager()
    {
        this.connectedPlayers = ConcurrentHashMap.newKeySet();
        this.eligiblePlayers = ConcurrentHashMap.newKeySet();
        this.lastMatches = new HashMap<>();

        this.possibleMatchups = false;
        this.gameManagerPool = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
                .setNameFormat("network-game-manager-%d")
                // TODO: A possible alternative is to use a CompleteionService
                // which would repeatedly take Futures and handle any exceptions
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
                synchronized (this.eligiblePlayers) {
                    while (!this.possibleMatchups) {
                        this.eligiblePlayers.wait();
                    }
                }
            }
            catch (InterruptedException e) {
                logger.warn("Client Manager was interrupted.");
                break;
            }

            synchronized(this.eligiblePlayers) {
                Optional<NetworkGameManager> optionalGameManager = findMatchup();

                if (optionalGameManager.isPresent()) {
                    NetworkGameManager gameManager = optionalGameManager.get();

                    logger.info("Found match: {} and {}.",
                            gameManager.getPlayerOne(), gameManager.getPlayerTwo());

                    this.eligiblePlayers.remove(gameManager.getPlayerOne());
                    this.eligiblePlayers.remove(gameManager.getPlayerTwo());

                    this.lastMatches.put(gameManager.getPlayerOne(), gameManager.getPlayerTwo());
                    this.lastMatches.put(gameManager.getPlayerTwo(), gameManager.getPlayerOne());

                    @SuppressWarnings("unused")
                    // if we do not assign to a variable, FindBugs will mark
                    // this as: RV_RETURN_VALUE_IGNORED_BAD_PRACTICE
                    // we do not care about the return value and exceptions are
                    // handled by the gameManagerPool's UncaughtExceptionHandler
                    Future<Void> unused = this.gameManagerPool.submit(gameManager);
                }
                // there was no matchup found so don't look again until a new
                // player is added
                else {
                    this.possibleMatchups = false;
                }
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

        logger.info("searching through players: {}", this.eligiblePlayers);
        for (ClientHandler playerOne : this.eligiblePlayers) {
            Optional<ClientHandler> optionalPlayerTwo = this.eligiblePlayers.stream()
                    // exclude playerOne
                    .filter(player -> !playerOne.equals(player))
                    // exclude playerOne's last opponent
                    .filter(player -> !player.equals(this.lastMatches.get(playerOne)))
                    // exclude player's whose last opponent was playerOne
                    .filter(player -> !playerOne.equals(this.lastMatches.get(player)))
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
        synchronized (this.eligiblePlayers) {
            this.eligiblePlayers.add(player);
            // there is a new player so we want to check for new matchups
            this.possibleMatchups = true;
            this.eligiblePlayers.notifyAll();
        }
    }
}
