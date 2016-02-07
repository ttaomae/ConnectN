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
    public Void call() throws ClientDisconnectedException, NetworkGameException
    {
        // this will be used to asynchronously get responses from both players
        CompletionService<Boolean> completionService
                = new ExecutorCompletionService<>(clientRequestThreadPool);
        boolean playerOneFirst = true;
        boolean rematch = true;

        while (rematch) {
            startMatch();

            Board board = new ArrayBoard();
            playMatch(board, playerOneFirst);

            completionService.submit(() -> handleRematchRequest(playerOneHandler));
            completionService.submit(() -> handleRematchRequest(playerTwoHandler));

            try {
                boolean firstReponse = completionService.take().get();
                boolean secondResponse = completionService.take().get();

                // only rematch if both accept
                rematch = firstReponse && secondResponse;
            }
            catch (InterruptedException | ExecutionException e) {
                throw new NetworkGameException("Error occured while waiting for rematch resposne.",
                        e, this);
            }
            // switch player order for next game
            playerOneFirst = !playerOneFirst;

        }

        threadPool.shutdownNow();
        return null;
    }

    private void startMatch() throws ClientDisconnectedException
    {
        logger.info("Starting match between {} and {}", playerOneHandler, playerTwoHandler);
        try {
            playerOneHandler.sendMessage(Message.START_GAME);
        }
        catch (LostConnectionException e) {
            throw new ClientDisconnectedException("Connection lost while starting match.",
                    e, playerOneHandler);
        }

        // if we reached here "startMatch" was successfully sent to player one
        try {
            playerTwoHandler.sendMessage(Message.START_GAME);
        }
        catch (LostConnectionException e) {
            throw new ClientDisconnectedException("Connection lost while starting match.",
                    e, playerTwoHandler);
        }
    }

    private void playMatch(Board board, boolean playerOneFirst) throws ClientDisconnectedException
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

            Optional<Integer> optionalMove;
            try {
                optionalMove = getMove(currentPlayer);
            } catch (LostConnectionException e) {
                throw new ClientDisconnectedException("Connection lost while getting move.",
                        e, currentPlayer);
            }

            if (!optionalMove.isPresent()) {
                throw new ProtocolException("Recieved empty move");
            }

            int move = optionalMove.get();
            if (!board.isValidMove(move)) {
                throw new IllegalMoveException("Client sent illegal move: " + move);
            }

            board.play(move);
            try {
                nextPlayer.sendOpponentMove(move);
            } catch (LostConnectionException e) {
                throw new ClientDisconnectedException(
                        "Connection lost while sending opponent move.",
                        e, nextPlayer);
            }
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

        Optional<Integer> move = player.getMove(null);

        if (move == null) {
            throw new LostConnectionException();
        }

        return move;
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
