package hw2.chess.game.piece;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import edu.cwru.sepia.util.Direction;
import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Coordinate;

public class King extends Piece implements Castleable
{

	private boolean canCastle;

//	King(int pieceID, int unitID, Player player)
//	{
//		super(pieceID, unitID, player, PieceType.KING);
//		this.canCastle = true;
//	}
//
//	King(int pieceID, int unitID, Player player, Coordinate pos)
//	{
//		super(pieceID, unitID, player, PieceType.KING, pos);
//		this.canCastle = true;
//	}

	King(int pieceID, Player player)
	{
		super(pieceID, player, PieceType.KING);
		this.canCastle = true;
	}

	public boolean canCastle() { return this.canCastle; }

	public void disqualify() { this.canCastle = false; }

	@Override
	public List<Move> getAllCaptureMoves(Game game)
	{
		List<Move> captureMoves = new ArrayList<Move>(8);
		Coordinate currentPosition = this.getCurrentPosition(game.getBoard());
		for(Direction direction : Direction.values())
		{
			Coordinate newPosition = currentPosition.getNeighbor(direction);
			if(game.getBoard().isInbounds(newPosition) && game.getBoard().isPositionOccupied(newPosition)
					&& this.isEnemyPiece(game.getBoard().getPieceAtPosition(newPosition)))
			{
				captureMoves.add(Move.createCaptureMove(this, game.getBoard().getPieceAtPosition(newPosition)));
			}
		}
		return captureMoves;
	}

	@Override
	public List<Move> getAllMoves(Game game)
	{
		// System.out.println("King.getAllMoves [INFO]: init");
		List<Move> allMoves = new ArrayList<Move>(Direction.values().length + 2); // + 2 for potential castling

		// System.out.println("King.getAllMoves [INFO]: trying movement/capturing");
		Coordinate currentPosition = this.getCurrentPosition(game.getBoard());
		for(Direction direction : Direction.values()) // try all directions
		{
			// try to go from the current position all the way down until we either go off the board or the square is occupied
			Coordinate newPosition = currentPosition.getNeighbor(direction);

			if(game.getBoard().isInbounds(newPosition))
			{
				if(!game.getBoard().isPositionOccupied(newPosition))
				{
					// System.out.println("King.getAllMoves [INFO]: making movement move to position=" + newPosition);
					allMoves.add(Move.createMovementMove(this, newPosition));
				} else if(this.isEnemyPiece(game.getBoard().getPieceAtPosition(newPosition)))
				{
					allMoves.add(Move.createCaptureMove(this, game.getBoard().getPieceAtPosition(newPosition)));
				}
			}
		}

		// try castling
		if(this.canCastle())
		{
			// System.out.println("King.getAllMoves [INFO]: trying castling");
			// get the rooks from my team
			Set<Piece> rooks = game.getBoard().getPieces(this.getPlayer(), PieceType.ROOK);
			if(rooks != null && rooks.size() > 0)
			{
				for(Piece rook: rooks)
				{
					if(((Castleable)rook).canCastle())
					{
						// can only castle if there are no pieces in the way
						boolean canCastle = true;
						boolean isPathClear = true;
						currentPosition = this.getCurrentPosition(game.getBoard());
						Coordinate currentRookPosition = rook.getCurrentPosition(game.getBoard());
						Direction directionKingMustTake = currentPosition.getDirectionTo(currentRookPosition);

						// don't want to start at king position exactly...it is occupied
						currentPosition = currentPosition.getNeighbor(directionKingMustTake);
						while(!currentPosition.equals(currentRookPosition) && isPathClear)
						{
							if(game.getBoard().isInbounds(currentPosition) && game.getBoard().isPositionAvailable(currentPosition))
							{
								;
							} else if(!game.getBoard().isPositionAvailable(currentPosition))
							{
								isPathClear = false;
							} else
							{
								// how did we get here? an invalid position?
//								System.err.println("King.getAllMoves [ERROR] out of bounds position=" + currentPosition
//										+ " with direction=" + directionKingMustTake + " kingPosition=" + this.getCurrentPosition(game.getBoard())
//										+ " rookPosition=" + currentRookPosition);
//								System.exit(-1);
								isPathClear = false;
								break;
							}

							currentPosition = currentPosition.getNeighbor(directionKingMustTake);
						}

						canCastle &= isPathClear;
						if(isPathClear)
						{
							// king cannot castle through check
							boolean noPositionInPathPutsKingInCheck = true;
							currentPosition = this.getCurrentPosition(game.getBoard());
	
							// king always moves two positions towards the rook so we will look 3 positions away so that the loop runs inclusively
							Coordinate finalKingPosition = currentPosition.getNeighbor(directionKingMustTake, 3);
							while(!currentPosition.equals(finalKingPosition) && noPositionInPathPutsKingInCheck)
							{
								Game newGame = game.copy();
								newGame.getBoard().updatePiecePosition(this, currentPosition);
								if(newGame.isInCheck(this.getPlayer()))
								{
									noPositionInPathPutsKingInCheck = false;
								}
								currentPosition = currentPosition.getNeighbor(directionKingMustTake);
							}
							canCastle &= noPositionInPathPutsKingInCheck;
						}

						if(canCastle)
						{
							allMoves.add(Move.createCastleMove(this, (Rook)rook));
						}
					}
				}
			}
		}

		// System.out.println("King.getAllMoves [INFO]: allMoves=" + allMoves);
		return allMoves;
	}

}
