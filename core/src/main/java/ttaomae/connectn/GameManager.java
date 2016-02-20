package ttaomae.connectn;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Connect-N game manager. Manages a single game between two Players. Can be
 * run in a separate thread.
 *
 * @author Todd Taomae
 */
public class GameManager implements Runnable
{
    /** The maximum number of attempts that a GameManager might allow */
    private static final int MAX_ATTEMPTS = Integer.MAX_VALUE;
    private final Board board;
    private final Player playerOne;
    private final Player playerTwo;
    private final int attemptsAllowed;

    private volatile boolean running;


    /**
     * Constructs a new GameManager with the specified Players. Uses a new board
     * and allows the maximum number of attempts.
     *
     * @param playerOne the first player
     * @param playerTwo the second player
     */
    public GameManager(Player playerOne, Player playerTwo)
    {
        this(new ArrayBoard(), playerOne, playerTwo, MAX_ATTEMPTS);
    }

    /**
     * Constructs a new GameManager with the specified Players and attempts
     * allowed. Uses a new Board.
     *
     * @param playerOne the first player
     * @param playerTwo the second player
     * @param attemptsAllowed the number of attempts allowed by a single player
     *            on a single turn
     */
    public GameManager(Player playerOne, Player playerTwo, int attemptsAllowed)
    {
        this(new ArrayBoard(), playerOne, playerTwo, attemptsAllowed);
    }

    /**
     * Constructs a new GameManager with the specified Board and Player. Allows
     * the maximum number of attempts.
     *
     * @param board the board to play on
     * @param playerOne the first player
     * @param playerTwo the second player
     */
    public GameManager(Board board, Player playerOne, Player playerTwo)
    {
        this(board, playerOne, playerTwo, MAX_ATTEMPTS);
    }

    /**
     * Constructs a new GameManager with the specified Board, Players, and
     * number of attempts allowed by a single player before terminating the game
     * and throwing an exception.
     *
     * @param board the board to play on
     * @param playerOne the first player
     * @param playerTwo the second player
     * @param attemptsAllowed the number of attempts allowed by a single player
     *            on a single turn
     */
    public GameManager(Board board, Player playerOne, Player playerTwo, int attemptsAllowed)
    {
        checkNotNull(board, "board must not be null");
        checkNotNull(playerOne, "playerOne must not be null");
        checkNotNull(playerTwo, "playerTwo must not be null");
        checkArgument(attemptsAllowed > 0, "attemptsAllowed must be positive");

        this.board = board;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.attemptsAllowed = attemptsAllowed;
        this.running = false;
    }

    /**
     * Runs a single game with this GameManager's Players.
     */
    @Override
    public void run()
    {
        ExecutorService executorService = Executors.newSingleThreadExecutor(
                new ThreadFactoryBuilder().setNameFormat("Get Move").setDaemon(true).build());
        int attempts = 0;

        this.running = true;
        while (this.running && this.board.getWinner() == Piece.NONE) {
            final Player player = getNextPlayer(this.board.getNextPiece());

            // run getMove on a separate thread
            Future<Optional<Integer>> future = executorService.submit(
                    () -> player.getMove(board.getImmutableView()));
            try {
                int move = future.get().orElse(Board.INVALID_MOVE);

                // only play valid moves
                if (this.board.isValidMove(move)) {
                    this.board.play(move);
                    attempts = 0;
                }
                else {
                    attempts++;
                    if (attempts == this.attemptsAllowed) {
                        throw new IllegalMoveException(this.board.getNextPiece() + " attempted "
                                + this.attemptsAllowed + " illegal moves");
                    }
                }
            }
            // if getMove throws an exception or
            // if the game manager is interrupted while waiting for a move
            catch (ExecutionException | InterruptedException e) {
                future.cancel(true);
                // end game
                this.running = false;
            }

        }
        executorService.shutdownNow();
        this.running = false;
    }

    /**
     * Tells this GameManager to stop.
     */
    public void stop()
    {
        this.running = false;
    }

    private Player getNextPlayer(Piece nextPiece)
    {
        switch (nextPiece) {
            case BLACK:
                return this.playerOne;
            case RED:
                return this.playerTwo;
            default:
                assert false : "invalid option: " + nextPiece;
                throw new IllegalArgumentException("invalid option: " + nextPiece);
        }
    }
}
