package ttaomae.connectn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


public class AlphaBetaPlayer implements Player
{
    private static final int DEFAULT_DEPTH = 5;
    private final int depth;
    private Random rand;

    public AlphaBetaPlayer()
    {
        this(DEFAULT_DEPTH);
    }

    public AlphaBetaPlayer(int depth)
    {
        this.depth = depth;
        this.rand = new Random();
    }

    @Override
    public int getMove(Board board)
    {
        Map<Integer, Double> possibleMoves = new HashMap<Integer, Double>();
        Piece myPiece = board.getNextPiece();

        // get minimax value for all valid moves
        for (int move = 0; move < board.getWidth(); move++) {
            if (board.isValidMove(move)) {
                Board copy = board.copy();
                copy.play(move);
                double heuristic = alphaBeta(copy, depth - 1, Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY, myPiece);
                possibleMoves.put(move, heuristic);
                // possibleMoves.put(move, minimax(copy, this.depth - 1, myPiece));
            }
        }

        // find max value
        double maxHeuristic = Double.NEGATIVE_INFINITY;
        for (Double value : possibleMoves.values()) {
            maxHeuristic = Math.max(maxHeuristic, value);
        }

        // find all moves with max heuristic
        List<Integer> bestMoves = new ArrayList<Integer>();
        for (Integer key : possibleMoves.keySet()) {
            if (possibleMoves.get(key) == maxHeuristic) {
                bestMoves.add(key);
            }
        }

        // pick a random best move
        if (bestMoves.size() != 0) {
            int move = bestMoves.get(this.rand.nextInt(bestMoves.size()));
            System.out.println(myPiece + ": best moves: " + maxHeuristic + ": " + bestMoves
                               + " --> " + move);
            return move;
        }

        return -1;
    }

    private double alphaBeta(Board board, int depth, double alpha, double beta, Piece maxPlayer)
    {
        if (depth == 0 || board.getWinner() != Piece.NONE) {
            return getHeuristic(board, maxPlayer);
        }

        // max player
        if (board.getNextPiece() == maxPlayer) {
            for (int move = 0; move < board.getWidth(); move++) {
                if (board.isValidMove(move)) {
                    Board copy = board.copy();
                    copy.play(move);
                    alpha = Math.max(alpha, alphaBeta(copy, depth - 1, alpha, beta, maxPlayer));
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
            for (int move = 0; move < board.getWidth(); move++) {
                if (board.isValidMove(move)) {
                    Board copy = board.copy();
                    copy.play(move);
                    beta = Math.min(beta, alphaBeta(copy, depth - 1, alpha, beta, maxPlayer));
                    // beta cut off
                    if (beta <= alpha) {
                        break;
                    }
                }
            }
            return beta;
        }
    }

    private double getHeuristic(Board board, Piece maxPlayer)
    {
        if (board.getWinner() == maxPlayer) {
            return Double.POSITIVE_INFINITY;
        }
        if (board.getWinner() == maxPlayer.opposite()) {
            return Double.NEGATIVE_INFINITY;
        }
        if (board.getWinner() == Piece.DRAW) {
            // slightly worse than neutral
            return -1.0f;
        }

        return 0.0f;
    }
}
