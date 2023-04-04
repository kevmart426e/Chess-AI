package hw2.chess.game.piece;

import java.util.ArrayList;
import java.util.List;

import edu.cwru.sepia.util.Direction;
import hw2.chess.game.Board;
import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Coordinate;

public class Rook extends Piece implements Castleable
{

	private boolean canCastle;

//	Rook(int pieceID, int unitID, Player player)
//	{
//		super(pieceID, unitID, player, PieceType.ROOK);
//		this.canCastle = true;
//	}
//
//	Rook(int pieceID, int unitID, Player player, Coordinate pos)
//	{
//		super(pieceID, unitID, player, PieceType.ROOK, pos);
//		this.canCastle = true;
//	}

	Rook(int pieceID, Player player)
	{
		super(pieceID, player, PieceType.ROOK);
		this.canCastle = true;
	}

	public boolean canCastle() { return this.canCastle; }

	public void disqualify() { this.canCastle = false; }

	@Override
	public List<Move> getAllCaptureMoves(Game game)
	{
		// max number of moves for a rook is the (NROWS - 1) + (NCOLS - 1) of the board...can move along each major direction
		List<Move> captureMoves = new ArrayList<Move>(4);

		for(Direction direction : new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH})
		{
			// try to go from the current position all the way down until we either go off the board or the square is occupied
			Coordinate newPosition = this.getCurrentPosition(game.getBoard()).getNeighbor(direction);
			while(game.getBoard().isInbounds(newPosition) && !game.getBoard().isPositionOccupied(newPosition))
			{
				newPosition = newPosition.getNeighbor(direction);
			}

			Piece enemyPiece = game.getBoard().getPieceAtPosition(newPosition);
			if(game.getBoard().isPositionOccupied(newPosition) && this.isEnemyPiece(enemyPiece))
			{
				captureMoves.add(Move.createCaptureMove(this, enemyPiece));
			}
		}

		return captureMoves;
	}

	@Override
	public List<Move> getAllMoves(Game game)
	{
		// max number of moves for a rook is the (NROWS - 1) + (NCOLS - 1) of the board...can move along each major direction
		List<Move> allMoves = new ArrayList<Move>(Board.Constants.NROWS + Board.Constants.NCOLS - 2);

		for(Direction direction : new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH})
		{
			// try to go from the current position all the way down until we either go off the board or the square is occupied
			Coordinate newPosition = this.getCurrentPosition(game.getBoard()).getNeighbor(direction);
			while(game.getBoard().isInbounds(newPosition) && !game.getBoard().isPositionOccupied(newPosition))
			{
				allMoves.add(Move.createMovementMove(this, newPosition));
				newPosition = newPosition.getNeighbor(direction);
			}

			Piece enemyPiece = game.getBoard().getPieceAtPosition(newPosition);
			if(game.getBoard().isPositionOccupied(newPosition) && this.isEnemyPiece(enemyPiece))
			{
				allMoves.add(Move.createCaptureMove(this, enemyPiece));
			}
		}

		return allMoves;
	}

}
