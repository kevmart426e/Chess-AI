package hw2.chess.game.planning;

import java.util.LinkedList;
import java.util.List;

import edu.cwru.sepia.util.Direction;
import hw2.chess.game.Game;
import hw2.chess.game.move.CaptureMove;
import hw2.chess.game.move.Move;
import hw2.chess.game.move.MovementMove;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.planning.command.Command;
import hw2.chess.utils.Coordinate;

public class SimplePlanner extends BasePlanner
{

	private List<Command> handleMovementMove(MovementMove move, Game game)
	{
		List<Command> commands = new LinkedList<Command>();

		commands.add(Command.createMovementCommand(move.getActorPlayer(), move.getActorPieceID(), move.getTargetPosition()));

		return commands;
	}

	private List<Command> handleCaptureMove(CaptureMove move, Game game)
	{
		List<Command> commands = new LinkedList<Command>();

		Coordinate attkPiecePosition = game.getCurrentPosition(move.getAttackingPlayer(), move.getAttackingPieceID());
		Coordinate tgtPiecePosition = game.getCurrentPosition(move.getTargetPlayer(), move.getTargetPieceID());
		Direction direction = attkPiecePosition.getDirectionTo(tgtPiecePosition);

		if(direction == null)
		{
			this.failAndExit("SimplePlanner.handleCaptureMove [ERROR] cannot determine direction from src="
					+ attkPiecePosition + " to tgt=" + tgtPiecePosition
					+ " (...this case should be impossible, how did we get here?)");
		}

		// calculate square NEXT to the enemy piece
		Coordinate currentPosition = attkPiecePosition;
		while(!currentPosition.getNeighbor(direction).equals(tgtPiecePosition))
		{
			currentPosition = currentPosition.getNeighbor(direction);
		}

		// move to the square NEXT to the enemy piece
		commands.add(Command.createMovementCommand(move.getAttackingPlayer(), move.getAttackingPieceID(), currentPosition));

		// kill the enemy piece
		commands.add(Command.createKillCommand(move.getAttackingPlayer(), move.getAttackingPieceID(),
				move.getTargetPlayer(), move.getTargetPieceID()));

		// now move to the enemy's location
		commands.add(Command.createMovementCommand(move.getAttackingPlayer(), move.getAttackingPieceID(), tgtPiecePosition));
		return commands;
	}

	@Override
	protected List<Command> makeCommands(Move move, Game game)
	{
		List<Command> commands = null;

		// no knights allowed here
		Piece actorPiece = game.getBoard().getPiece(move.getActorPlayer(), move.getActorPieceID());
		if(actorPiece.getType() == PieceType.KNIGHT)
		{
			this.failAndExit("SimplePlanner.makePlan [ERROR]: unsupported piece type=" + actorPiece.getType());
		}

		switch(move.getType())
		{
		case MOVEMENTMOVE:
			commands = this.handleMovementMove((MovementMove)move, game);
			break;
		case CAPTUREMOVE:
			commands = this.handleCaptureMove((CaptureMove)move, game);
			break;
		default:
			this.failAndExit("SimplePlanner.makePlan [ERROR]: unsupported move type=" + move.getType());
			break;
		}

		return commands;
	}

}
