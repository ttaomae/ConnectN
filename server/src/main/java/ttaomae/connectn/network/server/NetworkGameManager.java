package ttaomae.connectn.network.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ttaomae.connectn.ArrayBoard;
import ttaomae.connectn.Board;
import ttaomae.connectn.IllegalMoveException;
import ttaomae.connectn.Piece;
import ttaomae.connectn.network.LostConnectionException;
import ttaomae.connectn.network.ProtocolEvent.Message;
import ttaomae.connectn.network.ProtocolException;

/**
 * Manages a game between two {@linkplain ClientHandler clients}.
 *
 * @author Todd Taomae
 */
public class NetworkGameManager implements Callable<Void>
{
    private final Logger logger = LoggerFactory.getLogger(NetworkGameManager.class);

    private final ClientManager clientManager;
    private final ClientHandler playerOneHandler;
    private final ClientHandler playerTwoHandler;

    /**
     * A thread pool used to send messages to both clients simultaneously. It
     * should contain only two threads and should reject anything beyond two
     * simultaneous tasks.
     */
    private final ExecutorService clientRequestThreadPool;

    public NetworkGameManager(ClientManager clientManager,
            ClientHandler playerOneHandler, ClientHandler playerTwoHandler)
    {
        checkNotNull(clientManager, "clientManager must not be null");
        checkNotNull(playerOneHandler, "playerOneHandler must not be null");
        checkNotNull(playerTwoHandler, "playerTwoHandler must not be null");

        this.clientManager = clientManager;
        this.playerOneHandler = playerOneHandler;
        this.playerTwoHandler = playerTwoHandler;

        this.clientRequestThreadPool = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS,
                new SynchronousQueue<>());
    }

    ClientHandler getPlayerOne()
    {
        return this.playerOneHandler;
    }

    ClientHandler getPlayerTwo()
    {
        return this.playerTwoHandler;
    }

    @Override
    public Void call() throws NetworkGameException
    {
        // this will be used to asynchronously get responses from both players
        CompletionService<Boolean> completionService
                = new ExecutorCompletionService<>(clientRequestThreadPool);
        boolean playerOneFirst = true;
        boolean rematch = true;

        while (rematch) {
            try {
                startMatch();
            } catch (LostConnectionException e) {
                String message = "A player disconnected while starting match.";
                logger.info(message);
                throw new ClientDisconnectedException(message, e, this);
            }

            Board board = new ArrayBoard();
            try {
                playMatch(board, playerOneFirst);
            } catch (LostConnectionException e) {
                String message = "A player disconnected while playing match.";
                logger.info(message);
                throw new ClientDisconnectedException(message, e, this);
            }

            completionService.submit(() -> handleRematchRequest(playerOneHandler));
            completionService.submit(() -> handleRematchRequest(playerTwoHandler));

            try {
                boolean firstResponse = completionService.take().get();
                boolean secondResponse = completionService.take().get();

                // only rematch if both accept
                rematch = firstResponse && secondResponse;
            }
            catch (ExecutionException | InterruptedException e) {
                if (e.getCause() instanceof LostConnectionException) {
                    String message = "A player disconnected while waiting for rematch response.";
                    logger.info(message);
                    throw new ClientDisconnectedException(message, e.getCause(), this);
                }
                else {
                    String errorMessage = "Error occurred while waiting for rematch response";
                    logger.error(errorMessage, e);
                    throw new NetworkGameException(errorMessage, e, this);
                }
            }

            if (!rematch) {
                this.clientManager.playerMatchEnded(playerOneHandler);
                this.clientManager.playerMatchEnded(playerTwoHandler);
            }
            // switch player order for next game
            playerOneFirst = !playerOneFirst;
        }

        return null;
    }

    private void startMatch() throws LostConnectionException
    {
        logger.info("Starting match between {} and {}", playerOneHandler, playerTwoHandler);
        playerOneHandler.sendMessage(Message.START_GAME);
        playerTwoHandler.sendMessage(Message.START_GAME);
    }

    private void playMatch(Board board, boolean playerOneFirst) throws LostConnectionException
    {
        assert board.getCurrentTurn() == 0 : "board must be empty";

        while (board.getWinner() == Piece.NONE) {
            // determine which is the current / next player
            ClientHandler currentPlayer;
            if (playerOneFirst) {
                currentPlayer = board.getNextPiece() == Piece.BLACK
                        ? playerOneHandler : playerTwoHandler;
            }
            else {
                currentPlayer = board.getNextPiece() == Piece.BLACK
                        ? playerTwoHandler : playerOneHandler;
            }
            ClientHandler nextPlayer = getOpponent(currentPlayer);

            Optional<Integer> optionalMove = getMove(currentPlayer);

            if (!optionalMove.isPresent()) {
                throw new ProtocolException("Received empty move");
            }

            int move = optionalMove.get();
            if (!board.isValidMove(move)) {
                throw new IllegalMoveException("Client sent illegal move: " + move);
            }

            board.play(move);
            nextPlayer.sendOpponentMove(move);
        }
    }

    private ClientHandler getOpponent(ClientHandler player)
    {
        assert managerOwnsPlayer(player) : "player does not belong to this game manager";

        if (player == this.playerOneHandler) {
            return this.playerTwoHandler;
        }
        else {
            return this.playerOneHandler;
        }

    }
    private Optional<Integer> getMove(ClientHandler player) throws LostConnectionException
    {
        assert managerOwnsPlayer(player) : "player does not belong to this game manager";

        try {
            return player.getMove(null);
        }
        catch (RuntimeException e) {
            if (e.getCause().getClass() == LostConnectionException.class) {
                throw (LostConnectionException)(e.getCause());
            }
            else {
                throw e;
            }
        }
    }

    private boolean handleRematchRequest(ClientHandler player) throws LostConnectionException
    {
        assert managerOwnsPlayer(player) : "player does not belong to this game manager";

        ClientHandler opponent = getOpponent(player);
        boolean acceptRematch = player.requestRematch();

        if (acceptRematch) {
            opponent.sendMessage(Message.ACCEPT_REMATCH);
        }
        else {
            opponent.sendMessage(Message.DENY_REMATCH);
        }

        return acceptRematch;
    }

    private boolean managerOwnsPlayer(ClientHandler player)
    {
        return player == playerOneHandler || player == playerTwoHandler;
    }
}
