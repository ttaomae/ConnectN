package ttaomae.connectn;

public class GameManager implements Runnable
{
    private static final int MAX_ATTEMPTS = Integer.MAX_VALUE;
    private Board board;
    private Player playerOne;
    private Player playerTwo;
    private int attemptsAllowed;

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
    }

    @Override
    public void run()
    {
        int attempts = 0;
        while (this.board.getWinner() == Piece.NONE) {
            attempts++;
            int move;
            switch (this.board.getNextPiece()) {
                case BLACK:
                    move = this.playerOne.getMove(this.board.copy());
                    break;
                case RED:
                    move = this.playerTwo.getMove(this.board.copy());
                    break;
                case NONE:
                case DRAW:
                default:
                    move = -1;
                    break;
            }

            // only play valid moves
            if (this.board.isValidMove(move)) {
                this.board.play(move);
                attempts = 0;
            }
            else {
                if (attempts == this.attemptsAllowed) {
                    throw new IllegalMoveException(this.board.getNextPiece() + " attempted "
                                                   + this.attemptsAllowed + " illegal moves");
                }
            }
        }
    }
}
