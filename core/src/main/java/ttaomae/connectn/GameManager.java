package ttaomae.connectn;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A Connect-N game manager. Manages a single game between two Players. Can be
 * run in a separate thread.
 *
 * @author Todd Taomae
 */
public class GameManager implements Runnable
{
    /** The maxmimum number of attempts that a GameManager might allow */
    private static final int MAX_ATTEMPTS = Integer.MAX_VALUE;
    private Board board;
    private Player playerOne;
    private Player playerTwo;
    private int attemptsAllowed;

    private int move;
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
        this(new Board(), playerOne, playerTwo, MAX_ATTEMPTS);
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
        this(new Board(), playerOne, playerTwo, attemptsAllowed);
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
        if (board == null) {
            throw new IllegalArgumentException("board must not be null");
        }
        if (playerOne == null) {
            throw new IllegalArgumentException("playerOne must not be null");
        }
        if (playerTwo == null) {
            throw new IllegalArgumentException("playerTwo must not be null");
        }
        if (attemptsAllowed <= 0) {
            throw new IllegalArgumentException("attemptsAllowed must be positive");
        }

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
    @SuppressFBWarnings(value="UW_UNCOND_WAIT", justification="TODO")
    public void run()
    {
        this.running = true;
        int attempts = 0;
        while (this.running && this.board.getWinner() == Piece.NONE) {
            attempts++;
            this.move = -1;
            final Player player;
            switch (this.board.getNextPiece()) {
                case BLACK:
                    player = this.playerOne;
                    break;
                case RED:
                    player = this.playerTwo;
                    break;
                case NONE:
                case DRAW:
                default:
                    player = null;
                    this.move = -1;
                    break;
            }

            // use a new thread to get move
            Thread myThread = new Thread("Get Move") {
                @Override
                @SuppressFBWarnings(value="NN_NAKED_NOTIFY", justification="TODO")
                public void run()
                {
                    if (player != null) {
                        GameManager.this.move = player.getMove(GameManager.this.board.copy());

                        synchronized (this) {
                            this.notify();
                        }
                    }
                }
            };
            myThread.setDaemon(true);
            myThread.start();

            try {
                synchronized (myThread) {
                    // wait for thread to get move
                    myThread.wait();
                }
            }
            // if the game manager is interrupted while waiting for a move
            catch (InterruptedException e) {
                // interrupt thread that is getting move
                myThread.interrupt();
                // end game
                this.running = false;
            }

            // only play valid moves
            if (this.board.isValidMove(this.move)) {
                this.board.play(this.move);
                attempts = 0;
            }
            else {
                if (attempts == this.attemptsAllowed) {
                    throw new IllegalMoveException(this.board.getNextPiece() + " attempted "
                                                   + this.attemptsAllowed + " illegal moves");
                }
            }
        }
        this.running = false;
    }

    /**
     * Tells this GameManager to stop.
     */
    public void stop()
    {
        this.running = false;
    }
}
