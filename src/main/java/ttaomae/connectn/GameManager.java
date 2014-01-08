package ttaomae.connectn;

public class GameManager implements Runnable
{
    private static final int MAX_ATTEMPTS = Integer.MAX_VALUE;
    private Board board;
    private Player playerOne;
    private Player playerTwo;
    private int attemptsAllowed;

    private int move;
    private volatile boolean running;


    public GameManager(Player playerOne, Player playerTwo)
    {
        this(new Board(), playerOne, playerTwo, MAX_ATTEMPTS);
    }

    public GameManager(Player playerOne, Player playerTwo, int attemptsAllowed)
    {
        this(new Board(), playerOne, playerTwo, attemptsAllowed);
    }

    public GameManager(Board board, Player playerOne, Player playerTwo)
    {
        this(board, playerOne, playerTwo, MAX_ATTEMPTS);
    }

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

    @Override
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
            Thread myThread = new Thread() {
                @Override
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

    public void stop()
    {
        this.running = false;
    }
}
