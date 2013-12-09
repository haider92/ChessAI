import chess.*;
import org.w2mind.net.Action;
import org.w2mind.net.Mind;
import org.w2mind.net.RunError;
import org.w2mind.net.State;


public class AlphaBeta implements Mind {

    public void newrun() throws RunError {
    }

    public void endrun() throws RunError {
    }

    public Action getaction(State s) throws RunError {

        String fen = s.toString();

        try {
            Position pos = TextIO.readFEN(fen);
            AlphaBetaSearcher search = new AlphaBetaSearcher();
            Move best = search.getBestMove(pos);
            return new Action(best.from + "," + best.to);
        } catch (ChessParseError chessParseError) {
            chessParseError.printStackTrace();
        }

        System.out.println("Making invalid move");
        return new Action(0 + "," + 0);

    }

    private interface Evaluator {
        public int evaluate(Position board);
    }

    private static class PieceSquareTableEvaluation implements Evaluator {
        private final static int PAWN_VALUE = 100;
        private final static int KNIGHT_VALUE = 320;
        private final static int BISHOP_VALUE = 325;
        private final static int ROOK_VALUE = 500;
        private final static int QUEEN_VALUE = 900;
        private final static int CASTLED_BONUS = 30;
        private static final int[] pawnTableBlack = {
                0, 0, 0, 0, 0, 0, 0, 0,
                50, 50, 50, 50, 50, 50, 50, 50,
                10, 10, 20, 30, 30, 20, 10, 10,
                5, 5, 10, 25, 25, 10, 5, 5,
                0, 0, 0, 20, 20, 0, 0, 0,
                5, -5, -10, 0, 0, -10, -5, 5,
                5, 10, 10, -20, -20, 10, 10, 5,
                0, 0, 0, 0, 0, 0, 0, 0
        };
        private static final int[] pawnTableWhite = {
                0, 0, 0, 0, 0, 0, 0, 0,
                5, 10, 10, -20, -20, 10, 10, 5,
                5, -5, -10, 0, 0, -10, -5, 5,
                0, 0, 0, 20, 20, 0, 0, 0,
                5, 5, 10, 25, 25, 10, 5, 5,
                10, 10, 20, 30, 30, 20, 10, 10,
                50, 50, 50, 50, 50, 50, 50, 50,
                0, 0, 0, 0, 0, 0, 0, 0
        };
        private static final int[] knightTableBlack = {
                -50, -40, -30, -30, -30, -30, -40, -50,
                -40, -20, 0, 0, 0, 0, -20, -40,
                -30, 0, 10, 15, 15, 10, 0, -30,
                -30, 5, 15, 20, 20, 15, 5, -30,
                -30, 0, 15, 20, 20, 15, 0, -30,
                -30, 5, 10, 15, 15, 10, 5, -30,
                -40, -20, 0, 5, 5, 0, -20, -40,
                -50, -40, -30, -30, -30, -30, -40, -50,
        };
        private static final int[] knightTableWhite = {
                -50, -40, -30, -30, -30, -30, -40, -50,
                -40, -20, 0, 5, 5, 0, -20, -40,
                -30, 5, 10, 15, 15, 10, 5, -30,
                -30, 0, 15, 20, 20, 15, 0, -30,
                -30, 5, 15, 20, 20, 15, 5, -30,
                -30, 0, 10, 15, 15, 10, 0, -30,
                -40, -20, 0, 0, 0, 0, -20, -40,
                -50, -40, -30, -30, -30, -30, -40, -50,
        };
        private static final int[] bishopTableBlack = {
                -20, -10, -10, -10, -10, -10, -10, -20,
                -10, 0, 0, 0, 0, 0, 0, -10,
                -10, 0, 5, 10, 10, 5, 0, -10,
                -10, 5, 5, 10, 10, 5, 5, -10,
                -10, 0, 10, 10, 10, 10, 0, -10,
                -10, 10, 10, 10, 10, 10, 10, -10,
                -10, 5, 0, 0, 0, 0, 5, -10,
                -20, -10, -10, -10, -10, -10, -10, -20,
        };
        private static final int[] bishopTableWhite = {
                -20, -10, -10, -10, -10, -10, -10, -20,
                -10, 5, 0, 0, 0, 0, 5, -10,
                -10, 10, 10, 10, 10, 10, 10, -10,
                -10, 0, 10, 10, 10, 10, 0, -10,
                -10, 5, 5, 10, 10, 5, 5, -10,
                -10, 0, 5, 10, 10, 5, 0, -10,
                -10, 0, 0, 0, 0, 0, 0, -10,
                -20, -10, -10, -10, -10, -10, -10, -20,
        };
        private static final int[] rookTableBlack = {
                0, 0, 0, 0, 0, 0, 0, 0,
                5, 10, 10, 10, 10, 10, 10, 5,
                -5, 0, 0, 0, 0, 0, 0, -5,
                -5, 0, 0, 0, 0, 0, 0, -5,
                -5, 0, 0, 0, 0, 0, 0, -5,
                -5, 0, 0, 0, 0, 0, 0, -5,
                -5, 0, 0, 0, 0, 0, 0, -5,
                0, 0, 0, 5, 5, 0, 0, 0
        };
        private static final int[] rookTableWhite = {
                0, 0, 0, 5, 5, 0, 0, 0,
                -5, 0, 0, 0, 0, 0, 0, -5,
                -5, 0, 0, 0, 0, 0, 0, -5,
                -5, 0, 0, 0, 0, 0, 0, -5,
                -5, 0, 0, 0, 0, 0, 0, -5,
                -5, 0, 0, 0, 0, 0, 0, -5,
                5, 10, 10, 10, 10, 10, 10, 5,
                0, 0, 0, 0, 0, 0, 0, 0,
        };
        private static final int[] queenTableBlack = {
                -20, -10, -10, -5, -5, -10, -10, -20,
                -10, 0, 0, 0, 0, 0, 0, -10,
                -10, 0, 5, 5, 5, 5, 0, -10,
                -5, 0, 5, 5, 5, 5, 0, -5,
                0, 0, 5, 5, 5, 5, 0, -5,
                -10, 5, 5, 5, 5, 5, 0, -10,
                -10, 0, 5, 0, 0, 0, 0, -10,
                -20, -10, -10, -5, -5, -10, -10, -20
        };
        private static final int[] queenTableWhite = {
                -20, -10, -10, -5, -5, -10, -10, -20,
                -10, 0, 5, 0, 0, 0, 0, -10,
                -10, 5, 5, 5, 5, 5, 0, -10,
                0, 0, 5, 5, 5, 5, 0, -5,
                -5, 0, 5, 5, 5, 5, 0, -5,
                -10, 0, 5, 5, 5, 5, 0, -10,
                -10, 0, 0, 0, 0, 0, 0, -10,
                -20, -10, -10, -5, -5, -10, -10, -20,
        };
        private static final int[] kingTableBlack = {
                -30, -40, -40, -50, -50, -40, -40, -30,
                -30, -40, -40, -50, -50, -40, -40, -30,
                -30, -40, -40, -50, -50, -40, -40, -30,
                -30, -40, -40, -50, -50, -40, -40, -30,
                -20, -30, -30, -40, -40, -30, -30, -20,
                -10, -20, -20, -20, -20, -20, -20, -10,
                20, 20, 0, 0, 0, 0, 20, 20,
                20, 30, 10, 0, 0, 10, 30, 20
        };
        private static final int[] kingTableWhite = {
                20, 30, 10, 0, 0, 10, 30, 20,
                20, 20, 0, 0, 0, 0, 20, 20,
                -10, -20, -20, -20, -20, -20, -20, -10,
                -20, -30, -30, -40, -40, -30, -30, -20,
                -30, -40, -40, -50, -50, -40, -40, -30,
                -30, -40, -40, -50, -50, -40, -40, -30,
                -30, -40, -40, -50, -50, -40, -40, -30,
                -30, -40, -40, -50, -50, -40, -40, -30,
        };

        public int evaluate(Position board) {
            int white = 0, black = 0;

            for (int i = 0; i < board.squares.length; i++) {
                int piece = board.squares[i];
                if (piece == Piece.EMPTY) {
                    continue;
                } else if (piece == Piece.WPAWN) {
                    white += PAWN_VALUE;
                    white += pawnTableWhite[i];
                } else if (piece == Piece.WKNIGHT) {
                    white += KNIGHT_VALUE;
                    white += knightTableWhite[i];
                } else if (piece == Piece.WBISHOP) {
                    white += BISHOP_VALUE;
                    white += bishopTableWhite[i];
                } else if (piece == Piece.WROOK) {
                    white += ROOK_VALUE;
                    white += rookTableWhite[i];
                } else if (piece == Piece.WQUEEN) {
                    white += QUEEN_VALUE;
                    white += queenTableWhite[i];
                } else if (piece == Piece.WKING) {
                    white += kingTableWhite[i];
                } else if (piece == Piece.BPAWN) {
                    black += PAWN_VALUE;
                    black += pawnTableBlack[i];
                } else if (piece == Piece.BKNIGHT) {
                    black += KNIGHT_VALUE;
                    black += knightTableBlack[i];
                } else if (piece == Piece.BBISHOP) {
                    black += BISHOP_VALUE;
                    black += bishopTableBlack[i];
                } else if (piece == Piece.BROOK) {
                    black += ROOK_VALUE;
                    black += rookTableBlack[i];
                } else if (piece == Piece.BQUEEN) {
                    black += QUEEN_VALUE;
                    black += queenTableBlack[i];
                } else if (piece == Piece.BKING) {
                    black += kingTableBlack[i];
                }
            }

            int castleMask = board.getCastleMask();
            if (castleMask >> Position.A1_CASTLE == 1) {
                white += CASTLED_BONUS;
            } else if (castleMask >> Position.H1_CASTLE == 1) {
                white += CASTLED_BONUS;
            } else if (castleMask >> Position.A8_CASTLE == 1) {
                black += CASTLED_BONUS;
            } else if (castleMask >> Position.H8_CASTLE == 1) {
                black += CASTLED_BONUS;
            }

            if (board.whiteMove) {
                return white - black;
            } else {
                return black - white;
            }
        }
    }

    private static class AlphaBetaSearcher {
        private static Evaluator evaluator;
        private static Integer depth;

        public static Move getBestMove(Position board) {
            if (evaluator == null) {
                evaluator = new PieceSquareTableEvaluation();
            }
            if (depth == null) {
                depth = 4;
            }

            MoveGen.MoveList moves = new MoveGen().pseudoLegalMoves(board);
            MoveGen.removeIllegal(board, moves);

            int max = Integer.MIN_VALUE;
            Move bestMove = null;

            for (int i = 0; i < moves.size; i++) {
                UndoInfo ui = new UndoInfo();
                board.makeMove(moves.m[i], ui);
                int value = -alphabeta(board, Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, depth);
                board.unMakeMove(moves.m[i], ui);
                if (value > max) {
                    max = value;
                    bestMove = moves.m[i];
                }
            }

            return bestMove;
        }

        private static int alphabeta(Position board, int alpha, int beta, int depth) {
            if (depth == 0) {
                return evaluator.evaluate(board);
            }

            MoveGen.MoveList moves = new MoveGen().pseudoLegalMoves(board);
            MoveGen.removeIllegal(board, moves);

            for (int i = 0; i < moves.size; i++) {
                UndoInfo ui = new UndoInfo();
                board.makeMove(moves.m[i], ui);
                int value = -alphabeta(board, -beta, -alpha, depth - 1);
                board.unMakeMove(moves.m[i], ui);
                if (value >= beta) {
                    return beta;
                }
                if (value > alpha) {
                    alpha = value;
                }
            }
            return alpha;
        }
    }
}
