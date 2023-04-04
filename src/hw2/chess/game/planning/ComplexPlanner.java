package hw2.chess.game.planning;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import edu.cwru.sepia.util.Direction;
import hw2.chess.game.Game;
import hw2.chess.game.move.CaptureMove;
import hw2.chess.game.move.CastleMove;
import hw2.chess.game.move.Move;
import hw2.chess.game.move.MoveType;
import hw2.chess.game.move.MovementMove;
import hw2.chess.game.move.PromotePawnMove;
import hw2.chess.game.piece.Pawn;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.planning.command.Command;
import hw2.chess.utils.Coordinate;
import hw2.chess.utils.Distance;
import hw2.chess.utils.Pair;

public class ComplexPlanner extends BasePlanner
{

	private Pair<List<Command>, List<Command> > getPotentialKnightRoutesToPosition(Piece piece, Coordinate src,
			Coordinate tgt, Game game)
	{
		List<Command> routeA = new ArrayList<Command>(3);
		List<Command> routeB = new ArrayList<Command>(3);
		Pair<List<Command>, List<Command> > potentialRoutes = null;

		// handle routeA
		int xDist = Distance.xDist(src, tgt), yDist = Distance.yDist(src, tgt);
		Direction xDirection = new Coordinate(src.getXPosition(), src.getYPosition()).getDirectionTo(new Coordinate(tgt.getXPosition(), src.getYPosition()));
		Direction yDirection = new Coordinate(src.getXPosition(), src.getYPosition()).getDirectionTo(new Coordinate(src.getXPosition(), tgt.getYPosition()));

		if(xDist < yDist)
		{
			// routeA...move 2 units in y THEN 1 in x
			Coordinate firstPositionA = src.getNeighbor(yDirection);
			Coordinate secondPositionA = firstPositionA.getNeighbor(yDirection);
			if(!game.getBoard().isPositionOccupied(firstPositionA) && !game.getBoard().isPositionOccupied(secondPositionA))
			{
				// valid route!
				routeA.add(Command.createMovementCommand(piece, firstPositionA));
				routeA.add(Command.createMovementCommand(piece, secondPositionA));
				routeA.add(Command.createMovementCommand(piece, tgt));
			}

			// routeB...move 1 unit in x THEN 2 in x
			Coordinate firstPositionB = src.getNeighbor(xDirection);
			Coordinate secondPositionB = firstPositionA.getNeighbor(yDirection);
			if(!game.getBoard().isPositionOccupied(firstPositionB) && !game.getBoard().isPositionOccupied(secondPositionB))
			{
				// valid route!
				routeB.add(Command.createMovementCommand(piece, firstPositionB));
				routeB.add(Command.createMovementCommand(piece, secondPositionB));
				routeB.add(Command.createMovementCommand(piece, tgt));
			}
		} else if(xDist > yDist)
		{
			// routeA...move 2 units in x THEN one in y
			Coordinate firstPositionA = src.getNeighbor(xDirection);
			Coordinate secondPositionA = firstPositionA.getNeighbor(xDirection);
			if(!game.getBoard().isPositionOccupied(firstPositionA) && !game.getBoard().isPositionOccupied(secondPositionA))
			{
				// valid route!
				routeA.add(Command.createMovementCommand(piece, firstPositionA));
				routeA.add(Command.createMovementCommand(piece, secondPositionA));
				routeA.add(Command.createMovementCommand(piece, tgt));
			}

			// routeB...move 1 unit in y THEN two in x
			Coordinate firstPositionB = src.getNeighbor(yDirection);
			Coordinate secondPositionB = firstPositionA.getNeighbor(xDirection);
			if(!game.getBoard().isPositionOccupied(firstPositionB) && !game.getBoard().isPositionOccupied(secondPositionB))
			{
				// valid route!
				routeB.add(Command.createMovementCommand(piece, firstPositionB));
				routeB.add(Command.createMovementCommand(piece, secondPositionB));
				routeB.add(Command.createMovementCommand(piece, tgt));
			}
		} else
		{
			this.failAndExit("ComplexPlanner.getPotentialKnightRoutesToPosition [ERROR]: knight xDist and yDist are equal "
					+ "(...this should be impossible, how did we get here?)");
		}

		return potentialRoutes;
	}

	private List<Command> computeSimpleKnightPath(Piece piece, Coordinate finalPosition, Game game)
	{
		List<Command> commands = null;
		// a well defined direction does not exist because a knight moves in an L shape
		// there will always be two potential routes to the finalPosition

		Pair<List<Command>, List<Command> > potentialRoutes = this.getPotentialKnightRoutesToPosition(
				piece, game.getCurrentPosition(piece), finalPosition, game);

		if(potentialRoutes == null || (potentialRoutes.getFirst() == null && potentialRoutes.getSecond() == null))
		{
			// no available knight-looking paths...make SEPIA calculate the path with A*
			commands = new ArrayList<Command>(1);
			commands.add(Command.createMovementCommand(piece, finalPosition));
		} else
		{
			// set actions to a path (if both available, just pick one)
			if(potentialRoutes.getFirst() != null)
			{
				commands = potentialRoutes.getFirst();
			} else
			{
				// second route is not null
				commands = potentialRoutes.getSecond();
			}
		}

		// System.out.println("ComplexPlanner.computeSimpleKnightPath [INFO]: commands=" + commands);
		return commands;
	}

	private List<Command> computeSimplePath(Piece piece, Coordinate finalPosition, Game game)
	{
		List<Command> commands = new LinkedList<Command>();

		// a well defined direction should exist
		Coordinate currentPosition = game.getCurrentPosition(piece);
		Direction direction = currentPosition.getDirectionTo(finalPosition);
		if(direction == null)
		{
			this.failAndExit("ComplexPlanner.computeSimplePath [ERROR]: cannot compute direction from src="
					+ currentPosition + ", to tgt=" + finalPosition
					+ " (...this case should be impossible, how did we get here?)");
		}
		Coordinate nextPosition = null;
		while(!currentPosition.equals(finalPosition))
		{
			nextPosition = currentPosition.getNeighbor(direction);
			if(!game.getBoard().isPositionOccupied(nextPosition))
			{
				commands.add(Command.createMovementCommand(piece, nextPosition));
				currentPosition = nextPosition;
			} else
			{
				// no simple path
				return null;
			}
		}

		return commands;
	}

	private List<Command> makeComplexMovementPath(Piece piece, Coordinate finalPosition, Game game)
	{
		List<Command> commands = null;
		// System.out.println("ComplexPlanner.makeComplexMovementPath [INFO]: piece=" + piece + " finalPosition=" + finalPosition);

		// determine if a path already exists from the piece to the final position
		// if a path is unblocked, then we're happy! Easy!
		// every piece has a simple path IF it is not a knight
		switch(piece.getType())
		{
		case KNIGHT:
			// System.out.println("ComplexPlanner.makeComplexMovementPath [INFO]: computing simple knight path with piece=" + piece);
			commands = this.computeSimpleKnightPath(piece, finalPosition, game);
			break;
		default:
			// System.out.println("ComplexPlanner.makeComplexMovementPath [INFO]: computing simple knight path with piece=" + piece);
			commands = this.computeSimplePath(piece, finalPosition, game);
			break;
		}

		if(commands == null)
		{
			commands = new ArrayList<Command>(1);
			// no simple path...let SEPIA run A* to find the shortest path...a path should exist because of the border of the game
			commands.add(Command.createMovementCommand(piece.getPlayer(), piece.getPieceID(), finalPosition));
		}

		return commands;
	}

	private List<Command> handleCastleMove(CastleMove move, Game game)
	{
		List<Command> commands = new ArrayList<Command>(2);

		// there is not allowed to be any pieces between the king and rook
		// the problem is that pieces cannot teleport or phase through each other
		// therefore, during the castle operation, we MUST move one piece "around" the other

		// moving a piece "around" another MAY require us to move a completely unrelated piece first
		// to free up space in order for the pieces to move around each other

		// will probably be easier to move king when we are in the center of the board (i.e. not trapped in the corner)
		// like we would be if we tried to move the rook after the king, so lets move the rook first

		move.makeFinalPositions(game.getBoard());

		// moving the rook first is a simple planner's job
		commands.add(Command.createMovementCommand(move.getRookPlayer(), move.getRookPieceID(), move.getFinalRookPosition()));

		// moving the king might be difficult...use the extra row and let SEPIA run A* to find the shortest path
		commands.add(Command.createMovementCommand(move.getKingPlayer(), move.getKingPieceID(), move.getFinalKingPosition()));

		return commands;
	}

	private List<Command> handlePromotePawnMove(PromotePawnMove move, Game game)
	{

		// make the production...should always be possible thanks to the border of the game
		List<Command> commands = new LinkedList<Command>();

		Piece pawnPiece = game.getPiece(move.getActorPlayer(), move.getActorPieceID());
		Coordinate pawnPosition = game.getCurrentPosition(pawnPiece);
		Integer promotionTemplateID = game.getPieceType2TemplateIDs(pawnPiece.getPlayer()).get(move.getPromotedPieceType());
		if(promotionTemplateID == null)
		{
			System.err.println("ComplexPlanner.handlePromotePawnMove [ERROR]: template id for promotionType="
					+ move.getPromotedPieceType() + " does not exist!");
			System.exit(-1);
		}
		Piece promotedPiece = Piece.makePiece(pawnPiece.getPieceID(), pawnPiece.getPlayer(), move.getPromotedPieceType());

		commands.add(Command.createPromotePawnCommand((Pawn)pawnPiece, move.getPromotedPieceType(),
				promotionTemplateID));
		commands.add(Command.createKillCommand(pawnPiece, pawnPiece));
		commands.add(Command.createdDiscoverPieceCommand(pawnPiece.getPlayer(), pawnPiece.getPieceID(), move.getPromotedPieceType()));
		commands.add(Command.createMovementCommand(promotedPiece, pawnPosition));

		return commands;
	}

	private List<Command> handleKnighMove(Move move, Game game)
	{
		List<Command> commands = null;

		Piece actorPiece = game.getPiece(move.getActorPlayer(), move.getActorPieceID());
		switch(move.getType())
		{
		case MOVEMENTMOVE:
			// System.out.println("ComplexPlanner.handleKnightMove [INFO]: handling MovementMove=" + move);
			commands = this.makeComplexMovementPath(actorPiece, ((MovementMove)move).getTargetPosition(), game);
			break;
		case CAPTUREMOVE:
			// System.out.println("ComplexPlanner.handleKnightMove [INFO]: handling CaptureMove=" + move);
			// knights can kill from anywhere...makes life easier
			Piece tgtPiece = game.getPiece(((CaptureMove)move).getTargetPlayer(), ((CaptureMove)move).getTargetPieceID());
			Coordinate tgtPosition = game.getCurrentPosition(tgtPiece);
			commands = new LinkedList<Command>();
			commands.add(Command.createKillCommand(actorPiece, tgtPiece));
			commands.addAll(this.makeComplexMovementPath(actorPiece, tgtPosition, game));
			break;
		default:
			System.err.println("ComplexPlanner.handleKnightMove [ERROR]: unknown move type=" + move.getType() + " for knight=" + actorPiece);
			System.exit(-1);
			break;
		}

		return commands;
	}

	@Override
	protected List<Command> makeCommands(Move move, Game game)
	{
		// handle castles, knight moves, and pawn promotion moves
		List<Command> commands = null;

		if(move.getType() == MoveType.CASTLEMOVE)
		{
			// System.out.println("ComplexPlanner.makeCommands [INFO]: handling CastleMove=" + move);
			commands = this.handleCastleMove((CastleMove)move, game);
		} else if(move.getType() == MoveType.PROMOTEPAWNMOVE)
		{
			// System.out.println("ComplexPlanner.makeCommands [INFO]: handling PromotePawnMove=" + move);
			commands = this.handlePromotePawnMove((PromotePawnMove)move, game);
		} else
		{
			Piece actorPiece = game.getBoard().getPiece(move.getActorPlayer(), move.getActorPieceID());
			if(actorPiece.getType() != PieceType.KNIGHT)
			{
				// error
				this.failAndExit("ComplexPlanner.makePlan [ERROR]: unsupported piece type="
						+ actorPiece.getType());
			}
			// System.out.println("ComplexPlanner.makeCommands [INFO]: handling knight move=" + move);
			commands = this.handleKnighMove(move, game);
		}

		return commands;
	}

}
