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

            // Execute any one of them at random.
            int randomInt = (int) (Math.random() * moves.size);
            Move randomMove = moves.m[randomInt];
            return new Action(randomMove.from + "," + randomMove.to);

        } catch (ChessParseError chessParseError) {
            chessParseError.printStackTrace();
            return new Action("0,0");
        }
    }

}
