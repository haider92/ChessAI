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
            Position pos = TextIO.readFEN(fen);
            // Legal moves
            MoveGen.MoveList moves = new MoveGen().pseudoLegalMoves(pos);
            MoveGen.removeIllegal(pos, moves);

            // Legal Captures
            MoveGen.MoveList captures = new MoveGen().pseudoLegalCaptures(pos);
            MoveGen.removeIllegal(pos, captures);

            Move randomMove = null;

            if (captures.size > 0) {
                int randomInt = (int) (Math.random() * captures.size);
                randomMove = captures.m[randomInt];
            } else {
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