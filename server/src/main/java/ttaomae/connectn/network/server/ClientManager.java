package ttaomae.connectn.network.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
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

    private final CompletionService<Void> gameManagerPool;
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
        this.gameManagerPool = new ExecutorCompletionService<>(Executors.newCachedThreadPool(
                new ThreadFactoryBuilder().setNameFormat("network-game-manager-%d").build()));

        Thread cleanerThread = new Thread(new GameManagerCleaner(gameManagerPool),
                "game-manager-cleaner");
        cleanerThread.setDaemon(true);
        cleanerThread.start();
    }

    /**
     * Adds a player which is connected on the specified socket to this
     * ClientManager.
     *
     * @param player the player being added
     * @throws IllegalArgumentException if the player is null
     * @throws IllegalStateException if the specified socket is closed
     */
    void playerConnected(ClientHandler player)
    {
        checkNotNull(player, "player must not be null");

        this.connectedPlayers.add(player);
        addEligiblePlayer(player);
    }

    private void playerDisconnected(ClientHandler player)
    {
        checkNotNull(player, "player must not be null");

        if (this.connectedPlayers.remove(player)) {
            logger.info("Player disconnected: {}", player);
        }
        this.eligiblePlayers.remove(player);
    }

    void playerMatchEnded(ClientHandler player)
    {
        checkNotNull(player, "player must not be null");

        this.addEligiblePlayer(player);
    }

    // this should only be called if at least one of the two specified players
    // have disconnected
    private void checkConnections(ClientHandler playerOne, ClientHandler playerTwo)
    {
        assert playerOne != null : "playerOne must not be null";
        assert playerTwo != null : "playerTwo must not be null";

        logger.info("Checking connections for [{}] and [{}].", playerOne, playerTwo);
        if (!playerOne.isConnected()) {
            playerDisconnected(playerOne);
            try {
                // notify opponent
                playerTwo.sendMessage(Message.OPPONENT_DISCONNECTED);
                playerMatchEnded(playerTwo);
            }
            catch (LostConnectionException e) { // NOPMD
                // if p2 is disconnected, we don't care
                // they will be removed in following if block
            }
        }

        if (!playerTwo.isConnected()) {
            playerDisconnected(playerTwo);
            try {
                // notify opponent
                playerOne.sendMessage(Message.OPPONENT_DISCONNECTED);
                playerMatchEnded(playerOne);
            }
            catch (LostConnectionException e) {
                // it may be possible that p1 was connected during the first
                // check but is disconnected here
                playerDisconnected(playerOne);
            }
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
                    // handled by a GameManagerCleaner
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
            logger.debug("finding opponent for: {}", playerOne);
            Optional<ClientHandler> optionalPlayerTwo = this.eligiblePlayers.stream()
                    // don't choose a player that has disconnected
                    .filter(player -> {
                        if (player.isConnected()) {
                            return true;
                        } else {
                            this.eligiblePlayers.remove(player);
                            return false;
                        }
                    })
                    // exclude playerOne
                    .filter(player -> !playerOne.equals(player))
                    // exclude playerOne's last opponent
                    .filter(player -> !player.equals(this.lastMatches.get(playerOne)))
                    // exclude player's whose last opponent was playerOne
                    .filter(player -> !playerOne.equals(this.lastMatches.get(player)))
                    .findAny();

            if (optionalPlayerTwo.isPresent()) {
                ClientHandler playerTwo = optionalPlayerTwo.get();
                logger.debug("\tFound opponent: {}", playerTwo);
                return Optional.of(new NetworkGameManager(this, playerOne, playerTwo));
            }
            else {
                logger.debug("\tCould not find opponent");
            }
        }

        return Optional.empty();
    }

    private void addEligiblePlayer(ClientHandler player)
    {
        logger.info("Adding player to player pool: {}", player);
        synchronized (this.eligiblePlayers) {
            this.eligiblePlayers.add(player);
            // there is a new player so we want to check for new matchups
            this.possibleMatchups = true;
            this.eligiblePlayers.notifyAll();
        }
    }

    private class GameManagerCleaner implements Runnable
    {
        private final CompletionService<Void> completionService;

        private GameManagerCleaner(CompletionService<Void> completionService)
        {
            this.completionService = completionService;
        }

        @Override
        public void run()
        {
            while (true) {
                Future<Void> future;
                try {
                    logger.info("Waiting for game manager completion.");
                    future = this.completionService.take();
                    try {
                        logger.info("Game manager completed.");
                        // we only do this to check if there was an exception
                        future.get();
                    }
                    catch (ExecutionException e) {
                        Throwable cause = e.getCause();
                        if (cause instanceof ClientDisconnectedException) {
                            logger.info("client disconnected");
                            ClientDisconnectedException cde = (ClientDisconnectedException) cause;
                            NetworkGameManager ngm = cde.getNetworkGameManager();
                            checkConnections(ngm.getPlayerOne(), ngm.getPlayerTwo());
                        }
                        // unknown exception
                        else {
                            logger.error("Error while running game manager.", e);
                        }
                    }
                }
                catch (InterruptedException e) {
                    logger.error("GameManagerCleaner was interrupted", e);
                    // stop running
                    break;
                }
            }
        }
    }
}
