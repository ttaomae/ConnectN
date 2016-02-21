package ttaomae.connectn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An Player which uses a minimax algorithm with alpha-beta pruning.
 *
 * @author Todd Taomae
 */
public class AlphaBetaPlayer implements Player
{
    private static final Logger logger = LoggerFactory.getLogger(AlphaBetaPlayer.class);

    private static final double WIN_VALUE = 10000.0;
    private static final int DEFAULT_DEPTH = 5;
    private final int maxDepth;

    private ExecutorService executorService;

    /**
     * Constructs a new AlphaBetaPlayer which uses the default search depth.
     *
     * @param executorService the ExecutorService on which to perform
     *          computations
     */
    public AlphaBetaPlayer(ExecutorService executorService)
    {
        this(DEFAULT_DEPTH, executorService);
    }

    /**
     * Constructs a new AlphaBetaPlayer which uses the specified search depth.
     *
     * @param maxDepth the search depth
     * @param executorService the ExecutorService on which to perform
     *          computations
     */
    public AlphaBetaPlayer(int maxDepth, ExecutorService executorService)
    {
        checkArgument(maxDepth >= 0, "maxDepth must be non-negative");
        checkNotNull(executorService, "executorService must not be null");

        this.maxDepth = maxDepth;
        this.executorService = executorService;
    }

    @Override
    public Optional<Integer> getMove(ImmutableBoard board)
    {
        checkNotNull(board, "board must not be null");

        Piece myPiece = board.getNextPiece();

        // if depth is 0, select a random move
        if (this.maxDepth == 0) {
            List<Integer> validMoves = getValidMoves(board);
            Collections.shuffle(validMoves);
            return Optional.of(validMoves.get(0));
        }

        try {
            ArrayList<Integer> bestMoves = new ArrayList<>(
                    getBestMoves(board, this.maxDepth, myPiece));
            Collections.shuffle(bestMoves);
            return Optional.of(bestMoves.get(0));
        }
        catch (ExecutionException | InterruptedException e) {
            logger.warn("Error occurred while getting move.", e);
            return Optional.empty();
        }
    }

    private Collection<Integer> getBestMoves(ImmutableBoard board, int depth, Piece myPiece)
            throws ExecutionException, InterruptedException
    {
        assert this.maxDepth > 0 : "getBestMoves should only be used with maxDepth > 0";

        List<Integer> validMoves = getValidMoves(board);
        Collections.shuffle(validMoves);

        Multimap<Double, Integer> moveHeuristics = Multimaps.newListMultimap(
                new HashMap<>(), () -> new ArrayList<>());

        Collection<Callable<Void>> tasks = new ArrayList<>();
        for (int move : validMoves) {
            tasks.add(() -> {
                Board copy = board.getMutableCopy();
                copy.play(move);
                moveHeuristics.put(alphaBeta(copy, depth - 1, Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY, myPiece), move);
                return null;
            });
        }
        List<Future<Void>> futures = this.executorService.invokeAll(tasks);
        // check for ExecutionExceptions
        for (Future<Void> future : futures) {
            future.get();
        }

        return moveHeuristics.get(Collections.max(moveHeuristics.keySet()));
    }

    /**
     * Returns the heuristic value given by searching the specified board with
     * the specified depth.
     *
     * @param board board to analyze
     * @param depth remaining search depth
     * @param alpha maximum score that maximizing player is assured of
     * @param beta minimum score that minimizing player is assured of
     * @param maxPlayer maximizing player
     * @return the heuristic value for the specified board
     */
    private double alphaBeta(Board board, int depth, double alpha, double beta, Piece maxPlayer)
    {
        if (Thread.currentThread().isInterrupted()) {
            return 0;
        }
        Piece winner = board.getWinner();
        if (depth == 0 || winner != Piece.NONE) {
            return getHeuristic(board, maxPlayer, depth, winner);
        }

        // max player
        if (board.getNextPiece() == maxPlayer) {
            // create randomly ordered list of possible moves
            List<Integer> moves = new ArrayList<>();
            for (int move = 0; move < board.getWidth(); move++) {
                moves.add(move);
            }
            Collections.shuffle(moves);

            // get minimax value for all valid moves
            for (int move : moves) {
                if (board.isValidMove(move)) {
                    board.play(move);
                    alpha = Math.max(alpha, alphaBeta(board, depth - 1, alpha, beta, maxPlayer));
                    board.undoPlay();

                    // beta cut off
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return alpha;
        }

        // min player
        else {
            // create randomly ordered list of possible moves
            List<Integer> moves = new ArrayList<>();
            for (int move = 0; move < board.getWidth(); move++) {
                moves.add(move);
            }
            Collections.shuffle(moves);

            // get minimax value for all valid moves
            for (int move : moves) {
                if (board.isValidMove(move)) {
                    board.play(move);
                    beta = Math.min(beta, alphaBeta(board, depth - 1, alpha, beta, maxPlayer));
                    board.undoPlay();

                    // beta cut off
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return beta;
        }
    }

    /**
     * Returns the heuristic value of the specified board.
     *
     * @param board board to evaluate
     * @param maxPlayer maximizing player
     * @param winner the winner of the specified board
     * @return the heuristic value of the specified board
     */
    private double getHeuristic(Board board, Piece maxPlayer, int depth, Piece winner)
    {
        if (winner == maxPlayer) {
            return (depth + 1) * WIN_VALUE;
        }
        if (winner == maxPlayer.opposite()) {
            return (depth + 1) * -WIN_VALUE;
        }
        if (winner == Piece.DRAW) {
            // slightly worse than neutral
            return -1.0f;
        }

        // check for n-in-a-row
        int nInARow = board.getWinCondition() - 1;
        int maxPlayerNInARow = 0;
        int minPlayerNInARow = 0;
        // check each board position
        for (int row = 0; row < board.getHeight(); row++) {
            for (int col = 0; col < board.getWidth(); col++) {
                int maxPlayerHorizontal = 0;
                int minPlayerHorizontal = 0;
                int maxPlayerVertical = 0;
                int minPlayerVertical = 0;
                int maxPlayerDiagonalA = 0;
                int minPlayerDiagonalA = 0;
                int maxPlayerDiagonalB = 0;
                int minPlayerDiagonalB = 0;

                for (int i = 0; i < nInARow; i++) {
                    // check horizontal
                    if (col <= board.getWidth() - nInARow) {
                        if (board.getPieceAt(col + i, row) == maxPlayer) {
                            maxPlayerHorizontal++;
                        }
                        else if (board.getPieceAt(col + i, row) == maxPlayer.opposite()) {
                            minPlayerHorizontal++;
                        }
                    }

                    // check vertical
                    if (row <= board.getHeight() - nInARow) {
                        if (board.getPieceAt(col, row + i) == maxPlayer) {
                            maxPlayerVertical++;
                        }
                        else if (board.getPieceAt(col, row + i) == maxPlayer.opposite()) {
                            minPlayerVertical++;
                        }
                    }

                    // check up-right diagonal
                    if (col <= board.getWidth() - nInARow && row <= board.getHeight() - nInARow) {
                        if (board.getPieceAt(col + i, row + i) == maxPlayer) {
                            maxPlayerDiagonalA++;
                        }
                        else if (board.getPieceAt(col + i, row + i) == maxPlayer.opposite()) {
                            minPlayerDiagonalA++;
                        }
                    }

                    // check up-left diagonal
                    if (col >= board.getWinCondition() - 1 && row <= board.getHeight() - nInARow) {
                        if (board.getPieceAt(col - i, row + i) == maxPlayer) {
                            maxPlayerDiagonalB++;
                        }
                        else if (board.getPieceAt(col - i, row + i) == maxPlayer.opposite()) {
                            minPlayerDiagonalB++;
                        }
                    }
                }

                if (maxPlayerHorizontal == nInARow || maxPlayerVertical == nInARow
                    || maxPlayerDiagonalA == nInARow || maxPlayerDiagonalB == nInARow) {
                        maxPlayerNInARow++;
                }
                if (minPlayerHorizontal == nInARow || minPlayerVertical == nInARow
                    || minPlayerDiagonalA == nInARow || minPlayerDiagonalB == nInARow) {
                        maxPlayerNInARow++;
                }
            }
        }

        return (maxPlayerNInARow - minPlayerNInARow) * (WIN_VALUE / 100.0) * (depth + 1);
    }

    private List<Integer> getValidMoves(ImmutableBoard board)
    {
        // create randomly ordered list of possible moves
        return IntStream.range(0, board.getWidth())
                .filter(board::isValidMove)
                .boxed()
                .collect(Collectors.toList());
    }
}
