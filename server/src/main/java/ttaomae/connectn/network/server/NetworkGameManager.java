package ttaomae.connectn.network.server;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private final ClientManager clientManager;
    private final ClientHandler playerOneHandler;
    private final ClientHandler playerTwoHandler;

    public NetworkGameManager(ClientManager clientManager,
            ClientHandler playerOneHandler, ClientHandler playerTwoHandler)
    {
        checkNotNull(clientManager, "clientManager must not be null");
        checkNotNull(playerOneHandler, "playerOneHandler must not be null");
        checkNotNull(playerTwoHandler, "playerTwoHandler must not be null");

        this.clientManager = clientManager;
        this.playerOneHandler = playerOneHandler;
        this.playerTwoHandler = playerTwoHandler;
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
        final class RematchResponse {
            private final ClientHandler player;
            private final Optional<Boolean> acceptRematch;

            public RematchResponse(ClientHandler player, Optional<Boolean> acceptRematch) {
                assert player != null : "player must not be null";
                assert acceptRematch != null : "acceptRematch must not be null";

                this.player = player;
                this.acceptRematch = acceptRematch;
            }
        }

        // this will be used to asynchronously get responses from both players
        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        CompletionService<RematchResponse> completionService
                = new ExecutorCompletionService<>(threadPool);

        boolean playerOneFirst = true;
        boolean rematch = true;

        while (rematch) {
            startMatch();

            Board board = new ArrayBoard();
            playGame(board, playerOneFirst);

            completionService.submit(() ->
                    new RematchResponse(playerOneHandler, requestRematch(playerOneHandler)));
            completionService.submit(() ->
                    new RematchResponse(playerTwoHandler, requestRematch(playerTwoHandler)));

            RematchResponse firstReponse;
            try {
                firstReponse = completionService.take().get();
                handleRematchResponse(firstReponse.player, firstReponse.acceptRematch);
                RematchResponse secondResponse = completionService.take().get();
                handleRematchResponse(secondResponse.player, secondResponse.acceptRematch);

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
        try {
            playerOneHandler.startMatch();
        }
        catch (LostConnectionException e) {
            throw new ClientDisconnectedException("Connection lost while starting match.",
                    e, playerOneHandler);
        }

        // if we reached here "startMatch" was successfully sent to player one
        try {
            playerTwoHandler.startMatch();
        }
        catch (LostConnectionException e) {
            throw new ClientDisconnectedException("Connection lost while starting match.",
                    e, playerTwoHandler);
        }
    }

    private void playGame(Board board, boolean playerOneFirst) throws ClientDisconnectedException
    {
        assert board.getCurrentTurn() == 0 : "board must be empty";

        while (board.getWinner() == Piece.NONE) {
            // determine which is the current / next player
            ClientHandler currentPlayer = (playerOneFirst && board.getNextPiece() == Piece.BLACK)
                    ? playerOneHandler : playerTwoHandler;
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
            if (board.isValidMove(move)) {
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
        assert player == this.playerOneHandler || player == this.playerTwoHandler
                : "player must be either playerOneHandler or playerTwoHandler";

        if (player == this.playerOneHandler) {
            return this.playerTwoHandler;
        }
        else {
            return this.playerTwoHandler;
        }

    }
    private Optional<Integer> getMove(ClientHandler player) throws LostConnectionException
    {
        Optional<Integer> move = player.getMove(null);

        if (move == null) {
            throw new LostConnectionException();
        }

        return move;
    }

    private Optional<Boolean> requestRematch(ClientHandler player)
    {
        try {
            boolean acceptRematch = this.playerOneHandler.requestRematch();
            if (!acceptRematch) {
                // add back to player pool;
            }
            return Optional.of(acceptRematch);
        } catch (LostConnectionException e) {
            // remove from player pool?
            return Optional.empty();
        }
    }

    private void handleRematchResponse(ClientHandler player, Optional<Boolean> acceptRematch)
            throws ClientDisconnectedException
    {
        try {
            // player disconnected
            if (!acceptRematch.isPresent()) {
                // close socket? or pass to higher level
                this.clientManager.playerDisconnected(player);
                getOpponent(player).sendMessage(Message.OPPONENT_DISCONNECTED);
            }
            // accepted rematch
            else if (acceptRematch.get()) {
                    getOpponent(player).sendMessage(Message.ACCEPT_REMATCH);
            }
            // denied rematch
            else {
                this.clientManager.playerMatchEnded(player);
                getOpponent(player).sendMessage(Message.DENY_REMATCH);
            }
        }
        catch (LostConnectionException e) {
            throw new ClientDisconnectedException(
                    "Connection lost while sending rematch response to opponent.",
                    e, getOpponent(player));
        }
    }
}
