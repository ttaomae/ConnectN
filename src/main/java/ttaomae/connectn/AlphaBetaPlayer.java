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
        Map<Integer, Double> possibleMoves = new HashMap<>();
        Piece myPiece = board.getNextPiece();

        // get minimax value for all valid moves
        for (int move = 0; move < board.getWidth(); move++) {
            if (board.isValidMove(move)) {
                Board copy = board.copy();
                copy.play(move);
                double heuristic = alphaBeta(copy, depth - 1, Double.NEGATIVE_INFINITY,
                        Double.POSITIVE_INFINITY, myPiece);
                possibleMoves.put(move, heuristic);
            }
        }

        // find max value
        double maxHeuristic = Double.NEGATIVE_INFINITY;
        for (Double value : possibleMoves.values()) {
            maxHeuristic = Math.max(maxHeuristic, value);
        }

        // find all moves with max heuristic
        List<Integer> bestMoves = new ArrayList<>();
        for (Integer key : possibleMoves.keySet()) {
            if (possibleMoves.get(key) == maxHeuristic) {
                bestMoves.add(key);
            }
        }

        // pick a random best move
        if (bestMoves.size() != 0) {
            int move = bestMoves.get(this.rand.nextInt(bestMoves.size()));
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

        return (maxPlayerNInARow - minPlayerNInARow) * 10;
    }
}
