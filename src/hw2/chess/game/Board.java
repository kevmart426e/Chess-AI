package hw2.chess.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit;
import hw2.chess.game.move.Move;
import hw2.chess.game.move.CaptureMove;
import hw2.chess.game.move.CastleMove;
import hw2.chess.game.move.MovementMove;
import hw2.chess.game.move.PromotePawnMove;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.player.Player;
import hw2.chess.game.player.PlayerType;
import hw2.chess.utils.Coordinate;
import hw2.chess.utils.Copier;
import hw2.chess.utils.Triple;

public class Board extends Object
{

	public static class Constants extends Object
	{
		public static final int NROWS = 8;
		public static final int NCOLS = 8;
	
		public static final int NUMPIECESPERTEAM = 16;
		public static final int NUMPIECES = Constants.NUMPIECESPERTEAM * 2;
	
		public static final int WHITESTARTINGPAWNROWIDX = Constants.NROWS - 3;
		public static final int BLACKSTARTINGPAWNROWIDX = 2;
	
		public static final int WHITEROWIDXFORPROMOTION = 1;
		public static final int BLACKROWIDXFORPROMOTION = 8;
	
		public static final int NUMPIECETYPES = 6;
	}

	public static class MovementHandler extends Object
	{
		public static Board handleMovementMove(Board board, MovementMove move)
		{
			// make the new Board
			Board newBoard = board.copy();
			newBoard.updatePiecePosition(move.getActorPlayer(), move.getActorPieceID(), move.getTargetPosition());
			return newBoard;
		}

		private static Board handleCaptureMove(Board board, CaptureMove move)
		{
			// this is a movement action AND a attack action. This will move the actorPiece AND kill the enemy piece
			Board newBoard = board.copy();
			Coordinate tgtPiecePosition = board.getPiecePosition(move.getTargetPlayer(), move.getTargetPieceID());
			newBoard.removePiece(move.getTargetPlayer(), move.getTargetPieceID());
			newBoard.updatePiecePosition(move.getAttackingPlayer(), move.getAttackingPieceID(),
					tgtPiecePosition);
			newBoard.earnPoints(move.getActorPlayer(), Piece.getPointValue(board.getPiece(move.getTargetPlayer(), move.getTargetPieceID()).getType()));
			return newBoard;
		}

		private static Board handleCastleMove(Board board, CastleMove move)
		{
			// this is two movement actions
			Board newBoard = board.copy();

			move.makeFinalPositions(newBoard);
			newBoard.updatePiecePosition(move.getKingPlayer(), move.getKingPieceID(), move.getFinalKingPosition());
			newBoard.updatePiecePosition(move.getRookPlayer(), move.getRookPieceID(), move.getFinalRookPosition());
			return newBoard;
		}

		private static Board handlePromotePawnMove(Board board, PromotePawnMove move)
		{
			// kill pawn piece and create new piece at the new position
			Board newBoard = board.copy();

			Coordinate pawnPosition = board.getPiecePosition(move.getPawnPlayer(), move.getPawnPieceID());
			Piece promotedPiece = Piece.makePiece(move.getPawnPieceID(), move.getPawnPlayer(), move.getPromotedPieceType());

			newBoard.removePiece(move.getPawnPlayer(), move.getPawnPieceID());
			newBoard.addNewPiece(promotedPiece, pawnPosition);

			return newBoard;
		}
	}

	// black team
	private Map<Integer, Coordinate>					blackPieceID2Positions;
	private Map<Integer, Piece>							blackPieceID2Pieces;
	private Map<Integer, Integer>						blackPieceID2UnitIDs;
	private int											blackPointsEarned;

	// white team
	private Map<Integer, Coordinate>					whitePieceID2Positions;
	private Map<Integer, Piece>							whitePieceID2Pieces;
	private Map<Integer, Integer>						whitePieceID2UnitIDs;
	private int											whitePointsEarned;

	public Board(Map<Integer, Piece> blackPieceID2Pieces, Map<Integer, Integer> blackPieceID2UnitIDs, Map<Integer, Coordinate> blackPieceID2Positions,
			Map<Integer, Piece> whitePieceID2Pieces, Map<Integer, Integer> whitePieceID2UnitIDs, Map<Integer, Coordinate> whitePieceID2Positions,
			int blackPointsEarned, int whitePointsEarned)
	{
		this.blackPieceID2Pieces = Copier.copyMap(blackPieceID2Pieces);
		this.blackPieceID2UnitIDs = Copier.copyMap(blackPieceID2UnitIDs);
		this.blackPieceID2Positions = Copier.copyMap(blackPieceID2Positions);

		this.whitePieceID2Pieces = Copier.copyMap(whitePieceID2Pieces);
		this.whitePieceID2UnitIDs = Copier.copyMap(whitePieceID2UnitIDs);
		this.whitePieceID2Positions = Copier.copyMap(whitePieceID2Positions);

		this.blackPointsEarned = blackPointsEarned;
		this.whitePointsEarned = whitePointsEarned;
	}

	public Board(Map<Integer, Piece> blackPieceID2Pieces, Map<Integer, Integer> blackPieceID2UnitIDs, Map<Integer, Coordinate> blackPieceID2Positions,
			Map<Integer, Piece> whitePieceID2Pieces, Map<Integer, Integer> whitePieceID2UnitIDs, Map<Integer, Coordinate> whitePieceID2Positions)
	{
		this(blackPieceID2Pieces, blackPieceID2UnitIDs, blackPieceID2Positions,
				whitePieceID2Pieces, whitePieceID2UnitIDs, whitePieceID2Positions,
				0, 0);
	}

	private Map<Integer, Coordinate> getPieceID2Coordinate(PlayerType playerType)
	{
		Map<Integer, Coordinate> result = null;
		switch(playerType)
		{
		case BLACK:
			result = this.blackPieceID2Positions;
			break;
		case WHITE:
			result = this.whitePieceID2Positions;
			break;
		default:
			System.err.println("Board.getPosition2Pieces [ERROR]: invalid playerType=" + playerType);
			System.exit(-1);
			break;
		}
		return result;
	}
	private Map<Integer, Coordinate> getPieceID2Coordinate(Player player) { return this.getPieceID2Coordinate(player.getPlayerType()); }
	public Coordinate getPiecePosition(PlayerType playerType, int pieceID) { return this.getPieceID2Coordinate(playerType).get(pieceID); }
	public Coordinate getPiecePosition(Piece piece) { return this.getPiecePosition(piece.getPlayer(), piece.getPieceID()); }
	public Coordinate getPiecePosition(Player player, int pieceID) { return this.getPieceID2Coordinate(player).get(pieceID); }
	private Map<Integer, Piece> getPieceID2Pieces(PlayerType playerType)
	{
		Map<Integer, Piece> result = null;
		switch(playerType)
		{
		case BLACK:
			result = this.blackPieceID2Pieces;
			break;
		case WHITE:
			result = this.whitePieceID2Pieces;
			break;
		default:
			System.err.println("Board.getPieceID2Pieces [ERROR]: invalid playerType=" + playerType);
			System.exit(-1);
			break;
		}
		return result;
	}
	private Map<Integer, Piece> getPieceID2Pieces(Player player) { return this.getPieceID2Pieces(player.getPlayerType()); }
	public Set<Piece> getPieces(PlayerType playerType) { return new HashSet<Piece>(this.getPieceID2Pieces(playerType).values()); }
	public Set<Piece> getPieces(Player player) { return new HashSet<Piece>(this.getPieceID2Pieces(player).values()); }
	public Set<Piece> getPieces(Player player, PieceType pieceType)
	{
		Set<Piece> pieces = new HashSet<Piece>(Board.Constants.NUMPIECESPERTEAM);
		for(Piece piece : this.getPieces(player))
		{
			if(piece.getType().equals(pieceType))
			{
				pieces.add(piece);
			}
		}

		return pieces;
	}
	public int getNumberOfAlivePieces(Player player, PieceType pieceType) { return this.getPieces(player, pieceType).size(); }
	public Set<Integer> getUnitIDs(PlayerType playerType) { return new HashSet<Integer>(this.getPieceID2UnitIDs(playerType).values()); }
	public Set<Integer> getUnitIDs(Player player) { return this.getUnitIDs(player.getPlayerType()); }
	public Set<Integer> getUnitIDs(PlayerType playerType, PieceType pieceType)
	{
		Map<Integer, Piece> pieceID2Pieces = this.getPieceID2Pieces(playerType);
		Map<Integer, Integer> pieceID2UnitIDs = this.getPieceID2UnitIDs(playerType);
		Set<Integer> unitIDs = new HashSet<Integer>(pieceID2Pieces.size());

		for(Map.Entry<Integer, Piece> pieceInfo : pieceID2Pieces.entrySet())
		{
			if(pieceInfo.getValue().getType().equals(pieceType))
			{
				unitIDs.add(pieceID2UnitIDs.get(pieceInfo.getKey()));
			}
		}
		return unitIDs;
	}
	public Set<Integer> getUnitIDs(Player player, PieceType pieceType) { return this.getUnitIDs(player.getPlayerType(), pieceType); }
	private int getPointsEarned(PlayerType playerType)
	{
		int points = 0;

		switch(playerType)
		{
		case BLACK:
			points = this.blackPointsEarned;
			break;
		case WHITE:
			points = this.whitePointsEarned;
			break;
		default:
			System.err.println("Board.getPointsEarned [ERROR]: unknown player type=" + playerType);
			System.exit(-1);
			break;
		}

		return points;
	}
	public int getPointsEarned(Player player) { return this.getPointsEarned(player.getPlayerType()); }
	private void earnPoints(PlayerType playerType, int amount)
	{
		switch(playerType)
		{
		case BLACK:
			this.blackPointsEarned += amount;
			break;
		case WHITE:
			this.whitePointsEarned += amount;
			break;
		default:
			System.err.println("Board.getPointsEarned [ERROR]: unknown player type=" + playerType);
			System.exit(-1);
			break;
		}
	}
	private void earnPoints(Player player, int amount) { this.earnPoints(player.getPlayerType(), amount); }

	private Map<PieceType, Set<Piece> > getPieceType2Pieces(PlayerType playerType)
	{
		Map<PieceType, Set<Piece> > pieceType2Pieces = new HashMap<PieceType, Set<Piece> >(PieceType.values().length);
		for(Piece piece : this.getPieces(playerType))
		{
			if(!pieceType2Pieces.containsKey(piece.getType()))
			{
				pieceType2Pieces.put(piece.getType(), new HashSet<Piece>(Board.Constants.NUMPIECESPERTEAM));
			}
			pieceType2Pieces.get(piece.getType()).add(piece);
		}
		return pieceType2Pieces;
	}
	private Map<PieceType, Set<Piece> > getPieceType2Pieces(Player player) { return this.getPieceType2Pieces(player.getPlayerType()); }
	private Map<Integer, Integer> getPieceID2UnitIDs(PlayerType playerType)
	{
		Map<Integer, Integer> result = null;
		switch(playerType)
		{
		case BLACK:
			result = this.blackPieceID2UnitIDs;
			break;
		case WHITE:
			result = this.whitePieceID2UnitIDs;
			break;
		default:
			System.err.println("Board.getPieceID2UnitIDs [ERROR]: invalid playerType=" + playerType);
			System.exit(-1);
			break;
		}
		return result;
	}
	private Map<Integer, Integer> getPieceID2UnitIDs(Player player) { return this.getPieceID2UnitIDs(player.getPlayerType()); }
	public Integer getUnitID(Player player, int pieceID) { return this.getPieceID2UnitIDs(player).get(pieceID); }
	public Piece getPiece(PlayerType playerType, int pieceID)
	{
		return this.getPieceID2Pieces(playerType).get(pieceID);
	}
	public Piece getPiece(Player player, int pieceID) { return this.getPiece(player.getPlayerType(), pieceID); }
	public Piece getPieceAtPosition(Coordinate position)
	{
		for(PlayerType playerType : PlayerType.values())
		{
			for(Map.Entry<Integer, Coordinate> piecePositions : this.getPieceID2Coordinate(playerType).entrySet())
			{
				if(position.equals(piecePositions.getValue()))
				{
					Piece piece = this.getPiece(playerType, piecePositions.getKey());
					if(piece == null)
					{
						System.err.println("Board.getPieceAtPosition [ERROR]: found piece=" + piece + " at position=" + position
								+ " for player=" + playerType);
					}
					return piece;
				}
			}
		}

		return null;
	}

	public boolean isPositionOccupied(Coordinate position)
	{
		for(PlayerType playerType : PlayerType.values())
		{
			for(Map.Entry<Integer, Coordinate> piecePositions : this.getPieceID2Coordinate(playerType).entrySet())
			{
				if(position.equals(piecePositions.getValue()))
				{
					return true;
				}
			}
		}
		return false;
	}
	public boolean isPositionAvailable(Coordinate position) { return !this.isPositionOccupied(position); }
	public boolean doesPieceAlreadyExist(Player player, int pieceID) { return this.getPieceID2Pieces(player).containsKey(pieceID); }

	public void updatePiecePosition(Player player, int pieceID, Coordinate newPosition)
	{
		// make sure this position isn't already taken
		boolean foundAnotherPieceAtSamePosition = false;
		for(PlayerType playerType : PlayerType.values())
		{
			for(Map.Entry<Integer, Coordinate> piecePosition : this.getPieceID2Coordinate(playerType).entrySet())
			{
				if(piecePosition.getValue().equals(newPosition) && piecePosition.getKey() != pieceID)
				{
					foundAnotherPieceAtSamePosition = true;
				}
			}
		}
		if(foundAnotherPieceAtSamePosition)
		{
			System.err.println("Board.updatePiecePosition: [ERROR] piece already exists at position=" + newPosition
					+ " existing pieceID=" + pieceID + " for player=" + player);
		}

		this.getPieceID2Coordinate(player).remove(pieceID);
		this.getPieceID2Coordinate(player).put(pieceID, newPosition);
	}
	public void updatePiecePosition(Piece piece, Coordinate newPosition)
	{
		this.updatePiecePosition(piece.getPlayer(), piece.getPieceID(), newPosition);
	}
	public void updateUnitIDForPiece(Player player, int pieceID, int unitID)
	{
		this.getPieceID2UnitIDs(player).remove(pieceID);
		this.getPieceID2UnitIDs(player).put(pieceID, unitID);
	}
	public void updateUnitIDForPiece(Piece piece, int unitID)
	{
		this.updateUnitIDForPiece(piece.getPlayer(), piece.getPieceID(), unitID);
	}
	public void addNewPiece(Piece newPiece, Coordinate position)
	{
		if(this.isPositionOccupied(position))
		{
			System.err.println("Board.addNewPiece [ERROR]: piece already exists at position="
					+ position + "...cannot create a new piece there");
			System.exit(-1);
		} else if(this.doesPieceAlreadyExist(newPiece.getPlayer(), newPiece.getPieceID()))
		{
			System.err.println("Board.addNewPiece [ERROR]: piece with id=" + newPiece.getPieceID()
					+ " already exists for player=" + newPiece.getPlayer() + "...cannot create a new piece there");
			System.exit(-1);
		}

		this.updatePiecePosition(newPiece, position);
		this.getPieceID2Pieces(newPiece.getPlayer()).put(newPiece.getPieceID(), newPiece);
		this.getPieceType2Pieces(newPiece.getPlayer()).get(newPiece.getType()).add(newPiece);
	}
	public void removePiece(Player player, int pieceIDToRemove)
	{
		this.getPieceID2Coordinate(player).remove(pieceIDToRemove);
		this.getPieceID2Pieces(player).remove(pieceIDToRemove);
		this.getPieceID2UnitIDs(player).remove(pieceIDToRemove);
	}
	public int getPawnStartingRowIdx(Player player)
	{
		int startingRowIdx;
		if(player.getPlayerType() == PlayerType.WHITE)
		{
			startingRowIdx = Board.Constants.WHITESTARTINGPAWNROWIDX;
		} else
		{
			startingRowIdx = Board.Constants.BLACKSTARTINGPAWNROWIDX;
		}
		return startingRowIdx;
	}

	public boolean isInbounds(Coordinate position)
	{
		return position.getXPosition() >= 1 && position.getXPosition() <= Board.Constants.NROWS &&
				position.getYPosition() >= 1 && position.getYPosition() <= Board.Constants.NCOLS;
	}

	public Board copy()
	{
		return new Board(this.getPieceID2Pieces(PlayerType.BLACK), this.getPieceID2UnitIDs(PlayerType.BLACK), this.getPieceID2Coordinate(PlayerType.BLACK),
				this.getPieceID2Pieces(PlayerType.WHITE), this.getPieceID2UnitIDs(PlayerType.WHITE), this.getPieceID2Coordinate(PlayerType.WHITE),
				this.getPointsEarned(PlayerType.BLACK), this.getPointsEarned(PlayerType.WHITE));
	}

	public Board applyMove(Move move)
	{
		Board board = null;

		switch(move.getType())
		{
		case MOVEMENTMOVE:
			board = Board.MovementHandler.handleMovementMove(this, (MovementMove)move);
			break;
		case CAPTUREMOVE:
			board = Board.MovementHandler.handleCaptureMove(this, (CaptureMove)move);
			break;
		case CASTLEMOVE:
			board = Board.MovementHandler.handleCastleMove(this, (CastleMove)move);
			break;
		case PROMOTEPAWNMOVE:
			board = Board.MovementHandler.handlePromotePawnMove(this, (PromotePawnMove)move);
			break;
		case ENPASSANTMOVE:
//			board = Board.MovementHandler.handleEnPassantMove(this, (EnPassantMove)move);
			System.err.println("Board.applyAction [ERROR]: unsupported action=" + move.toString() + " with type=" + move.getType().toString());
			System.exit(-1);
			break;
		default:
			System.err.println("Board.applyAction [ERROR]: unknown action=" + move.toString() + " with type=" + move.getType().toString());
			System.exit(-1);
			break;
		}

		return board;
	}

	private static Triple<Map<Integer, Piece>, Map<Integer, Integer>, Map<Integer, Coordinate> > getInitialPieces(Player player, StateView state)
	{
		// System.out.println("Board.getInitialPieces [INFO]: initializing pieces for player=" + player);
		Map<Integer, Coordinate> pieceID2Positions = new HashMap<Integer, Coordinate>(Board.Constants.NUMPIECESPERTEAM);
		Map<Integer, Piece> pieceID2Pieces = new HashMap<Integer, Piece>(Board.Constants.NUMPIECESPERTEAM);
		Map<Integer, Integer> pieceID2UnitIDs = new HashMap<Integer, Integer>(Board.Constants.NUMPIECESPERTEAM);
		int pieceID = 0;

		for(Integer unitID : state.getUnitIds(player.getPlayerID()))
		{
			Unit.UnitView unitView = state.getUnit(unitID);

			PieceType pieceType = PieceType.valueOf(unitView.getTemplateView().getName().toUpperCase());
			Piece piece = Piece.makePiece(pieceID, player, pieceType);

			pieceID2Positions.put(pieceID, new Coordinate(unitView.getXPosition(), unitView.getYPosition()));
			pieceID2Pieces.put(pieceID, piece);
			pieceID2UnitIDs.put(pieceID, unitID);
			pieceID += 1;

			// System.out.println("Board.getInitialPieces [INFO]: initializing piece=" + piece + " for player=" + player);
		}

		// System.out.println("Board.getInitialPieces [INFO]: initialized pieces for player=" + player);
		// System.out.println();
		return new Triple<>(pieceID2Pieces, pieceID2UnitIDs, pieceID2Positions);
	}

	public static Board getInitialBoard(Player blackPlayer, Player whitePlayer, StateView state)
	{
		// System.out.println("Board.getInitialBoard [INFO]: initializing board");
		Triple<Map<Integer, Piece>, Map<Integer, Integer>, Map<Integer, Coordinate> > blackPieceInfo = Board.getInitialPieces(blackPlayer, state);
		Triple<Map<Integer, Piece>, Map<Integer, Integer>, Map<Integer, Coordinate> > whitePieceInfo = Board.getInitialPieces(whitePlayer, state);

		// System.out.println("Board.getInitialBoard [INFO]: initialized board");
		System.out.println();
		return new Board(blackPieceInfo.getFirst(), blackPieceInfo.getSecond(), blackPieceInfo.getThird(),
				whitePieceInfo.getFirst(), whitePieceInfo.getSecond(), whitePieceInfo.getThird());
	}

}