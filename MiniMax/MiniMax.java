import chess.*;
import org.w2mind.net.Action;
import org.w2mind.net.Mind;
import org.w2mind.net.RunError;
import org.w2mind.net.State;


public class MiniMax implements Mind {

    public void newrun() throws RunError {
    }

    public void endrun() throws RunError {
    }

    public Action getaction(State s) throws RunError {

        String fen = s.toString();

        try {
            // Parse the FEN.
            Position pos = TextIO.readFEN(fen);

            // Create a new instance of MiniMaxSearcher.
            MiniMaxSearcher search = new MiniMaxSearcher();

            // Find the best move.
            Move best = search.getBestMove(pos);
            return new Action(best.from + "," + best.to);
        } catch (ChessParseError chessParseError) {
            chessParseError.printStackTrace();
        }

        System.out.println("Making invalid move");
        return new Action(0 + "," + 0);

    }

    private static class MiniMaxSearcher {
        private static PieceSquareTableEvaluation evaluator;
        private static Integer depth;

        public static Move getBestMove(Position board) {
            evaluator = new PieceSquareTableEvaluation();
            depth = 2;

            // Generate all possible moves.
            MoveGen.MoveList moves = new MoveGen().pseudoLegalMoves(board);
            MoveGen.removeIllegal(board, moves);

            int max = Integer.MIN_VALUE;
            Move bestMove = null;

            // For each move.
            for (int i = 0; i < moves.size; i++) {
                // Execute the move
                UndoInfo ui = new UndoInfo();
                board.makeMove(moves.m[i], ui);

                // Evaluate it of -BestOppositions move
                int value = -minimax(board, depth);
                board.unMakeMove(moves.m[i], ui);

                // If value is greater than the stored max value
                if (value > max) {
                    // Update max and the bestMove.
                    max = value;
                    bestMove = moves.m[i];
                }
            }

            return bestMove;
        }

        private static int minimax(Position board, int depth) {
            // If a depth of zero is reached.
            if (depth == 0) {
                // Return an evaluation.
                return evaluator.evaluate(board);
            }

            // Generate all possible moves.
            MoveGen.MoveList moves = new MoveGen().pseudoLegalMoves(board);
            MoveGen.removeIllegal(board, moves);

            int max = Integer.MIN_VALUE;

            // For each move.
            for (int i = 0; i < moves.size; i++) {
                // Execute the move.
                UndoInfo ui = new UndoInfo();
                board.makeMove(moves.m[i], ui);

                // Evaluate it against -BestOppositionsMove
                int value = -minimax(board, depth - 1);
                board.unMakeMove(moves.m[i], ui);

                // If value is greater than the stored max value
                if (value > max) {
                    // Update the max value
                    max = value;
                }
            }
            return max;
        }
    }
}
