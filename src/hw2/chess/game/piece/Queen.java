package hw2.chess.game.piece;

import java.util.ArrayList;
import java.util.List;

import edu.cwru.sepia.util.Direction;
import hw2.chess.game.Board;
import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Coordinate;

public class Queen extends Piece
{

//	Queen(int pieceID, int unitID, Player player)
//	{
//		super(pieceID, unitID, player, PieceType.QUEEN);
//	}
//
//	Queen(int pieceID, int unitID, Player player, Coordinate pos)
//	{
//		super(pieceID, unitID, player, PieceType.QUEEN, pos);
//	}

	Queen(int pieceID, Player player)
	{
		super(pieceID, player, PieceType.QUEEN);
	}

	@Override
	public List<Move> getAllCaptureMoves(Game game)
	{
		List<Move> captureMoves = new ArrayList<Move>(8);

		for(Direction direction : Direction.values()) // try all directions
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
		// queen can move like a bishop and a rook...so max possible moves is the number of moves a bishop can make
		// + number of moves a rook can make (which happen to be the same number)
		List<Move> allMoves = new ArrayList<Move>(2 * (Board.Constants.NROWS + Board.Constants.NCOLS - 2));

		for(Direction direction : Direction.values()) // try all directions
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
