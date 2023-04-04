package hw2.chess.game.piece;

import java.util.ArrayList;
import java.util.List;

import edu.cwru.sepia.util.Direction;
import hw2.chess.game.Board;
import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.player.Player;
import hw2.chess.game.player.PlayerType;
import hw2.chess.utils.Coordinate;

public class Pawn extends Piece
{

//	Pawn(int pieceID, int unitID, Player player)
//	{
//		super(pieceID, unitID, player, PieceType.PAWN);
//	}
//
//	Pawn(int pieceID, int unitID, Player player, Coordinate pos)
//	{
//		super(pieceID, unitID, player, PieceType.PAWN, pos);
//	}

	Pawn(int pieceID, Player player)
	{
		super(pieceID, player, PieceType.PAWN);
	}

	private boolean isOnStartingRow(Board board)
	{
		return board.getPiecePosition(this.getPlayer(), this.getPieceID()).getXPosition() == board.getPawnStartingRowIdx(this.getPlayer());
	}

	/**
	 * TODO en-paussant
	 */
	@Override
	public List<Move> getAllCaptureMoves(Game game)
	{
		List<Move> captureMoves = new ArrayList<Move>(2);
		Direction captureDirection1 = null, captureDirection2 = null;
		if(this.getPlayer().getPlayerType() == PlayerType.WHITE)
		{
			// white's pawn...wants to go UP
			captureDirection1 = Direction.NORTHWEST;
			captureDirection2 = Direction.NORTHEAST;
		} else
		{
			// black's pawn...wants to go DOWN
			captureDirection1 = Direction.SOUTHWEST;
			captureDirection2 = Direction.SOUTHEAST;
		}

		Coordinate currentPosition = this.getCurrentPosition(game.getBoard());

		// can we take a piece?
		Coordinate capturePosition1 = currentPosition.getNeighbor(captureDirection1);
		Coordinate capturePosition2 = currentPosition.getNeighbor(captureDirection2);

		if(game.getBoard().isInbounds(capturePosition1) && game.getBoard().isPositionOccupied(capturePosition1) &&
				this.isEnemyPiece(game.getBoard().getPieceAtPosition(capturePosition1)))
		{
			captureMoves.add(Move.createCaptureMove(this, game.getBoard().getPieceAtPosition(capturePosition1)));
		}

		if(game.getBoard().isInbounds(capturePosition2) && game.getBoard().isPositionOccupied(capturePosition2) &&
				this.isEnemyPiece(game.getBoard().getPieceAtPosition(capturePosition2)))
		{
			captureMoves.add(Move.createCaptureMove(this, game.getBoard().getPieceAtPosition(capturePosition2)));
		}
		return captureMoves;
	}

	/**
	 * TODO: en paussant
	 */
	@Override
	public List<Move> getAllMoves(Game game)
	{
		List<Move> allMoves = new ArrayList<Move>(5);

		Direction movementDirection = null, captureDirection1 = null, captureDirection2 = null;
		if(this.getPlayer().getPlayerType() == PlayerType.WHITE)
		{
			// white's pawn...wants to go UP
			movementDirection = Direction.NORTH;
			captureDirection1 = Direction.NORTHWEST;
			captureDirection2 = Direction.NORTHEAST;
		} else
		{
			// black's pawn...wants to go DOWN
			movementDirection = Direction.SOUTH;
			captureDirection1 = Direction.SOUTHWEST;
			captureDirection2 = Direction.SOUTHEAST;
		}

		Coordinate currentPosition = this.getCurrentPosition(game.getBoard());
		Coordinate oneSquareForward = currentPosition.getNeighbor(movementDirection);
		if(game.getBoard().isInbounds(oneSquareForward) && !game.getBoard().isPositionOccupied(oneSquareForward))
		{
			// we can move there!
			allMoves.add(Move.createMovementMove(this, oneSquareForward));
		}

		if(this.isOnStartingRow(game.getBoard()))
		{
			Coordinate twoSquaresForward = oneSquareForward.getNeighbor(movementDirection);
			if(game.getBoard().isInbounds(oneSquareForward) && !game.getBoard().isPositionOccupied(oneSquareForward)
				&& game.getBoard().isInbounds(twoSquaresForward) && !game.getBoard().isPositionOccupied(twoSquaresForward))
			{
				// on starting row so we can move 2 squares up!
				allMoves.add(Move.createMovementMove(this, twoSquaresForward));
			}
		}

		// can we take a piece?
		Coordinate capturePosition1 = currentPosition.getNeighbor(captureDirection1);
		Coordinate capturePosition2 = currentPosition.getNeighbor(captureDirection2);

		if(game.getBoard().isInbounds(capturePosition1) && game.getBoard().isPositionOccupied(capturePosition1) &&
				this.isEnemyPiece(game.getBoard().getPieceAtPosition(capturePosition1)))
		{
			allMoves.add(Move.createCaptureMove(this, game.getBoard().getPieceAtPosition(capturePosition1)));
		}

		if(game.getBoard().isInbounds(capturePosition2) && game.getBoard().isPositionOccupied(capturePosition2) &&
				this.isEnemyPiece(game.getBoard().getPieceAtPosition(capturePosition2)))
		{
			allMoves.add(Move.createCaptureMove(this, game.getBoard().getPieceAtPosition(capturePosition2)));
		}

		// System.out.println("Pawn.getAllMoves [INFO]: playerType=" + this.getPlayer().getPlayerType()
		// 		+ " currentPosition=" + currentPosition);
		// pawn promotion
		if((this.getPlayer().getPlayerType() == PlayerType.WHITE && currentPosition.getYPosition() == Board.Constants.WHITEROWIDXFORPROMOTION)
				|| (this.getPlayer().getPlayerType() == PlayerType.BLACK && currentPosition.getYPosition() == Board.Constants.BLACKROWIDXFORPROMOTION))
		{
			for(PieceType promotionType : PieceType.values())
			{
				if(!promotionType.equals(PieceType.PAWN) && !promotionType.equals(PieceType.KING))
				{
					allMoves.add(Move.createPromotePawnMove(this, promotionType));
				}
			}
		}

		return allMoves;
	}

}
