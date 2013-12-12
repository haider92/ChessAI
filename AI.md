# CA318 Practical
## Writing an AI for Chess

Chess is strategy board game with two players. It is played on a checkered board with an 8x8 grid. The objective of the game is to put the opponent's king into a position where escape is impossible.

Chess can be generalized to an NxN board and thus defining it into the complexity class, expspace complete i.e. it requires time exponential in n on a normal machine. 

Chess is difficult, the pieces can be arranged on the 64 squares in 10^44 different ways. Even with the assumption that each move could be tested within a nanosecond computers could still not contemplate each and every possibility within a reasonable amount of time. Using heuristics it is possible to create a machine that can beat a human however it will not be perfect program.

---------------------------------------

## Exploring the world
The supplied world `w2m.ChessWorldG` outputs a string in FEN(Forsyth-Edwards Notation) representing the board. In order to decide on the move our program executes it is necessary to parse and analysis this string.

The world supplies a static method `TextIO.readFen(<string>)` which will parse a FEN string and return a `position` object. Using this `position` object it is possible to execute several functions:

- `Position.getPiece(int square)` returns an integer representing a players piece. Integers are defined as:
	
  - 0  = Empty
  - 1  = White King
  - 2  = White Queen
  - 3  = White Rook
  - 4  = White Bishop
  - 5  = White Knight
  - 6  = White Pawn
  - 7  = Black King
  - 8  = Black Queen
  - 9  = Black Rook
  - 10 = Black Bishop
  - 11 = Black Knight
  - 12 = Black Pawn

- `Position.getCastleMask()` returns an integer. Using bitwise operations it is possible to determine whether or not the white or black player has castled.

- `Position.makeMove(<move> <undoinfo>)` allows execution of a move on the board.

---------------------------------------

Using the `MoveGen` class it is possible to generate a `MoveList` of legal moves and legal captures:

- `MoveGen.pseudoLegalMoves(<position>)` returns a `MoveList` of legal moves.

- `MoveGen.pseudoLegalCaptures(<position>)` returns a `MoveList` of legal captures.

A `MoveList` consists of:

- An array m of generated moves.
- A integer size, the length of array m.

From the above information we can begin the journey into writing our mind(s).


## Mind Creation

### Make any random legal move 
#### http://w2mind.computing.dcu.ie/sys/mind.php?mind=AnyRandomLegalMove

Using the information noted under the "Exploring the world" section of this document the first mind was created.

This mind was done as a learning exercise. It contains no intelligence, it simply executes any legal move at random.

---------------------------------------

```java
import chess.*;
import org.w2mind.net.Action;
import org.w2mind.net.Mind;
import org.w2mind.net.RunError;
import org.w2mind.net.State;

public class AnyRandomLegalMove implements Mind {

    public void newrun() throws RunError {
    }

    public void endrun() throws RunError {
    }

    public Action getaction(State s) throws RunError {

        String fen = s.toString();

        try {
            // Parse the FEN.
            Position pos = TextIO.readFEN(fen);

            // Generate all possible moves.
            MoveGen.MoveList moves = new MoveGen().pseudoLegalMoves(pos);

            // Remove any illegal moves.
            MoveGen.removeIllegal(pos, moves);

            // Execute any one of the moves at random.
            int randomInt = (int) (Math.random() * moves.size);
            Move randomMove = moves.m[randomInt];
            return new Action(randomMove.from + "," + randomMove.to);

        } catch (ChessParseError chessParseError) {
            chessParseError.printStackTrace();
            // Return an invalid move and forfit.
            return new Action("0,0");
        }
    }

}

```

---------------------------------------

### Make any random legal move with capture priority
#### http://w2mind.computing.dcu.ie/sys/mind.php?mind=CapturePriorityRandomLegalMove

This mind was another learning exercise. It also contains no intelligence. It will execute a legal capture at random should one exist, otherwise it executes a legal move at random.

```java
import chess.*;
import org.w2mind.net.Action;
import org.w2mind.net.Mind;
import org.w2mind.net.RunError;
import org.w2mind.net.State;

public class CapturePriorityRandomLegalMove implements Mind {

    public void newrun() throws RunError {
    }

    public void endrun() throws RunError {
    }

    public Action getaction(State s) throws RunError {

        String fen = s.toString();

        try {
        		// Parse the FEN.
            Position pos = TextIO.readFEN(fen);
            
            // Generate all possible moves.
            MoveGen.MoveList moves = new MoveGen().pseudoLegalMoves(pos);
            MoveGen.removeIllegal(pos, moves);

            // Generate all possible captures.
            MoveGen.MoveList captures = new MoveGen().pseudoLegalCaptures(pos);
            MoveGen.removeIllegal(pos, captures);

            Move randomMove = null;

            if (captures.size > 0) {
            		// If a captures exist pick one at random.
                int randomInt = (int) (Math.random() * captures.size);
                randomMove = captures.m[randomInt];
            } else {
            		// Otherwise pick a move at random.
                int randomInt = (int) (Math.random() * moves.size);
                randomMove = moves.m[randomInt];
            }

            return new Action(randomMove.from + "," + randomMove.to);

        } catch (ChessParseError chessParseError) {
            chessParseError.printStackTrace();
            // Return an invalid move and forfit.
            return new Action("0,0");
        }
    }
}
```

---------------------------------------

### Evaluation

None of the above approaches used any form of evaluation, thus making them unintellegent. In order to stay in the game longer it is important to evaluate the moves before taking them. But how can we evaluate?

#### Materials
Within Chess a piece is considered to have a value. This value is relative to the strength of the Piece. Based on research( http://home.comcast.net/~danheisman/Articles/evaluation_of_material_imbalance.htm ) the following values were used:

 - Pawn 100
 - Knight 325
 - Bishop 325
 - Rook 500
 - Queen 975

Along with this a value of 30 was also assigned to castling as it provides extra protection to the king.

To enhance this evaluation and take the pieces location into consideration Piece-SquareTables can be used. They assign values to specific pieces on specific locations. They contain a table for each piece and each color of that piece. ( http://chessprogramming.wikispaces.com/Simplified+evaluation+function )

Using the above information the following evaluation method was derieved:

```java
class PieceSquareTableEvaluation {
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
```

---------------------------------------

### Negamax (Minimax)
#### http://w2mind.computing.dcu.ie/sys/mind.php?mind=MiniMax

Negamax is a simple algorithm used to influence decisions within game theory. In the case of chess it can be used along side an evaluation function to determine an intellegent move.

The algorithm works of the symmetric scoring. That is, the score is always minus the score of the opposition.

In order to calculate white's move, We must first calculate black's move, In order to calculate blacks move, We must first.... 

This is a recursive algorithm, in order for this process to not continue in definitely it is necessary to set a search depth.

Taking all this into consideration the following mind was created:

```java
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
```

---------------------------------------

### Alphabeta
#### http://w2mind.computing.dcu.ie/sys/mind.php?mind=AlphaBeta

Negamax is a simple algorithm used to influence decisions within game theory. In the case of chess it can be used along side an evaluation function to determine an intellegent move.

The algorithm works of the symmetric scoring. That is, the score is always minus the score of the opposition.

In order to calculate white's move, We must first calculate black's move, In order to calculate blacks move, We must first.... 

This is a recursive algorithm, in order for this process to not continue in definitely it is necessary to set a search depth.

Taking all this into consideration the following mind was created:

```java
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
```
