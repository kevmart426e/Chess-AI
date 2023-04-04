package hw2.chess.game.piece;

import java.util.ArrayList;
import java.util.List;

import edu.cwru.sepia.util.Direction;
import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Coordinate;

public class Knight extends Piece
{

//	Knight(int pieceID, Integer unitID, Player player)
//	{
//		super(pieceID, unitID, player, PieceType.KNIGHT);
//	}
//
//	Knight(int pieceID, Integer unitID, Player player, Coordinate pos)
//	{
//		super(pieceID, unitID, player, PieceType.KNIGHT, pos);
//	}

	Knight(int pieceID, Player player)
	{
		super(pieceID, player, PieceType.KNIGHT);
	}

	@Override
	public List<Move> getAllCaptureMoves(Game game)
	{
		List<Move> captureMoves = new ArrayList<Move>(4);
		// try up first (2 moves)
		Coordinate currentPosition = this.getCurrentPosition(game.getBoard());
		Coordinate upPosition = currentPosition.getNeighbor(Direction.NORTH, 2);
		Coordinate downPosition = currentPosition.getNeighbor(Direction.SOUTH, 2);
		Coordinate rightPosition = currentPosition.getNeighbor(Direction.EAST, 2);
		Coordinate leftPosition = currentPosition.getNeighbor(Direction.WEST, 2);

		Coordinate possiblePositions[] = {upPosition.getNeighbor(Direction.WEST),
										  upPosition.getNeighbor(Direction.EAST),

										  downPosition.getNeighbor(Direction.WEST),
										  downPosition.getNeighbor(Direction.EAST),

										  rightPosition.getNeighbor(Direction.NORTH),
										  rightPosition.getNeighbor(Direction.SOUTH),

										  leftPosition.getNeighbor(Direction.NORTH),
										  leftPosition.getNeighbor(Direction.SOUTH)
									      };

		for(Coordinate position : possiblePositions)
		{
			if(game.getBoard().isInbounds(position))
			{
				if(game.getBoard().isPositionOccupied(position) && this.isEnemyPiece(game.getBoard().getPieceAtPosition(position)))
				{
					captureMoves.add(Move.createCaptureMove(this, game.getBoard().getPieceAtPosition(position)));
				}
			}
		}
		return captureMoves;
	}

	@Override
	public List<Move> getAllMoves(Game game)
	{
		List<Move> allMoves = new ArrayList<Move>(8);

		// try up first (2 moves)
		Coordinate currentPosition = this.getCurrentPosition(game.getBoard());
		Coordinate upPosition = currentPosition.getNeighbor(Direction.NORTH, 2);
		Coordinate downPosition = currentPosition.getNeighbor(Direction.SOUTH, 2);
		Coordinate rightPosition = currentPosition.getNeighbor(Direction.EAST, 2);
		Coordinate leftPosition = currentPosition.getNeighbor(Direction.WEST, 2);

		Coordinate possiblePositions[] = {upPosition.getNeighbor(Direction.WEST),
										  upPosition.getNeighbor(Direction.EAST),

										  downPosition.getNeighbor(Direction.WEST),
										  downPosition.getNeighbor(Direction.EAST),

										  rightPosition.getNeighbor(Direction.NORTH),
										  rightPosition.getNeighbor(Direction.SOUTH),

										  leftPosition.getNeighbor(Direction.NORTH),
										  leftPosition.getNeighbor(Direction.SOUTH)
									      };

		for(Coordinate position : possiblePositions)
		{
			if(game.getBoard().isInbounds(position))
			{
				if(!game.getBoard().isPositionOccupied(position))
				{
					allMoves.add(Move.createMovementMove(this, position));
				} else if(this.isEnemyPiece(game.getBoard().getPieceAtPosition(position)))
				{
					allMoves.add(Move.createCaptureMove(this, game.getBoard().getPieceAtPosition(position)));
				}
			}
		}

		return allMoves;
	}

}
