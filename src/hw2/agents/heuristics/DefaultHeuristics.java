package hw2.agents.heuristics;

import edu.cwru.sepia.util.Direction;
import hw2.chess.game.move.PromotePawnMove;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.search.DFSTreeNode;
import hw2.chess.utils.Coordinate;

public class DefaultHeuristics
{

	public static class OffensiveHeuristics extends Object
	{

		public static int getNumberOfPiecesWeAreThreatening(DFSTreeNode node)
		{
			int numPiecesWeAreThreatening = 0;
			for(Piece piece : node.getGame().getBoard().getPieces(node.getGame().getCurrentPlayer()))
			{
				numPiecesWeAreThreatening = piece.getAllCaptureMoves(node.getGame()).size();
			}
			return numPiecesWeAreThreatening;
		}

	}

	public static class DefensiveHeuristics extends Object
	{

		public static int getNumberOfAlivePieces(DFSTreeNode node)
		{
			int numPiecesAlive = 0;
			for(PieceType pieceType : PieceType.values())
			{
				numPiecesAlive += node.getGame().getNumberOfAlivePieces(node.getGame().getCurrentPlayer(), pieceType);
			}
			return numPiecesAlive;
		}

		public static int getClampedPieceValueTotalSurroundingKing(DFSTreeNode node)
		{
			// what is the state of the pieces next to the king? add up the values of the neighboring pieces
			// positive value for friendly pieces and negative value for enemy pieces (will clamp at 0)
			int kingSurroundingPiecesValueTotal = 0;

			Piece kingPiece = node.getGame().getBoard().getPieces(node.getGame().getCurrentPlayer(), PieceType.KING).iterator().next();
			Coordinate kingPosition = node.getGame().getCurrentPosition(kingPiece);
			for(Direction direction : Direction.values())
			{
				Coordinate neightborPosition = kingPosition.getNeighbor(direction);
				if(node.getGame().getBoard().isInbounds(neightborPosition) && node.getGame().getBoard().isPositionOccupied(neightborPosition))
				{
					Piece piece = node.getGame().getBoard().getPieceAtPosition(neightborPosition);
					int pieceValue = Piece.getPointValue(piece.getType());
					if(piece != null && kingPiece.isEnemyPiece(piece))
					{
						kingSurroundingPiecesValueTotal -= pieceValue;
					} else if(piece != null && !kingPiece.isEnemyPiece(piece))
					{
						kingSurroundingPiecesValueTotal += pieceValue;
					}
				}
			}
			// kingSurroundingPiecesValueTotal cannot be < 0 b/c the utility of losing a game is 0, so all of our utility values should be at least 0
			kingSurroundingPiecesValueTotal = Math.max(kingSurroundingPiecesValueTotal, 0);
			return kingSurroundingPiecesValueTotal;
		}

		public static int getNumberOfPiecesThreateningUs(DFSTreeNode node)
		{
			// how many pieces are threatening us?
			int numPiecesThreateningUs = 0;
			for(Piece piece : node.getGame().getBoard().getPieces(node.getGame().getOtherPlayer()))
			{
				numPiecesThreateningUs = piece.getAllCaptureMoves(node.getGame()).size();
			}
			return numPiecesThreateningUs;
		}
		
	}

	public static double getOffensiveHeuristicValue(DFSTreeNode node)
	{
		// remember the action has already taken affect at this point, so capture moves have already resolved
		// and the targeted piece will not exist inside the game anymore.
		// however this value was recorded in the amount of points that the player has earned in this node
		double damageDealtInThisNode = node.getGame().getBoard().getPointsEarned(node.getGame().getCurrentPlayer());

		switch(node.getMove().getType())
		{
		case PROMOTEPAWNMOVE:
			PromotePawnMove promoteMove = (PromotePawnMove)node.getMove();
			damageDealtInThisNode += Piece.getPointValue(promoteMove.getPromotedPieceType());
			break;
		default:
			break;
		}
		// offense can typically include the number of pieces that our pieces are currently threatening
		int numPiecesWeAreThreatening = OffensiveHeuristics.getNumberOfPiecesWeAreThreatening(node);

		return damageDealtInThisNode + numPiecesWeAreThreatening;
	}

	public static double getDefensiveHeuristicValue(DFSTreeNode node)
	{
		// how many pieces exist on our team?
		int numPiecesAlive = DefensiveHeuristics.getNumberOfAlivePieces(node);

		// what is the state of the pieces next to the king? add up the values of the neighboring pieces
		// positive value for friendly pieces and negative value for enemy pieces (will clamp at 0)
		int kingSurroundingPiecesValueTotal = DefensiveHeuristics.getClampedPieceValueTotalSurroundingKing(node);

		// how many pieces are threatening us?
		int numPiecesThreateningUs = DefensiveHeuristics.getNumberOfPiecesThreateningUs(node);

		return numPiecesAlive + kingSurroundingPiecesValueTotal + numPiecesThreateningUs;
	}

	public static double getNonlinearPieceCombinationHeuristicValue(DFSTreeNode node)
	{
		// both bishops are worth more together than a single bishop alone
		// same with knights...we want to encourage keeping pairs of elements
		double multiPieceValueTotal = 0.0;

		double exponent = 1.5; // f(numberOfKnights) = (numberOfKnights)^exponent

		// go over all the piece types that have more than one copy in the game (including pawn promotion)
		for(PieceType pieceType : new PieceType[] {PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK, PieceType.QUEEN})
		{
			multiPieceValueTotal += Math.pow(node.getGame().getNumberOfAlivePieces(node.getGame().getCurrentPlayer(), pieceType), exponent);
		}

		return multiPieceValueTotal;
	}

	public static double getHeuristicValue(DFSTreeNode node)
	{
		double offenseHeuristicValue = DefaultHeuristics.getOffensiveHeuristicValue(node);
		double defenseHeuristicValue = DefaultHeuristics.getDefensiveHeuristicValue(node);
		double nonlinearHeuristicValue = DefaultHeuristics.getNonlinearPieceCombinationHeuristicValue(node);

		return offenseHeuristicValue + defenseHeuristicValue + nonlinearHeuristicValue;
	}

}
