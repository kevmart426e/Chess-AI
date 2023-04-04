package hw2.chess.game.piece;

import java.util.List;

import hw2.chess.game.Board;
import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Coordinate;

public abstract class Piece extends Object
{

	private final int pieceID;
//	private Integer unitID;				// this will need to be set for pawn promotion
	private final Player player;

	private final PieceType type;

//	public Piece(int pieceID, Integer unitID, Player player, PieceType type, Coordinate pos)
	public Piece(int pieceID, Player player, PieceType type)
	{
		this.pieceID = pieceID;
//		this.unitID = unitID;
		this.player = player;
		this.type = type;
	}

//	public void setUnitID(int newUnitID) { this.unitID = newUnitID; }

	public final int getPieceID() { return this.pieceID; }
//	public final Integer getUnitID() { return this.unitID; }
	public final Player getPlayer() { return this.player; }
	public PieceType getType() { return this.type; }

	public boolean isEnemyPiece(Piece otherPiece)
	{
		if(otherPiece == null)
		{
			System.err.println("Piece.isEnemyPiece [ERROR]: otherPiece == null!");
		}
		return !this.getPlayer().equals(otherPiece.getPlayer());
	}

	public abstract List<Move> getAllCaptureMoves(Game game);
	public abstract List<Move> getAllMoves(Game game);

	public static int getPointValue(PieceType pieceType)
	{
		int value = 0;
		switch(pieceType)
		{
		case PAWN:
			value = 1;
			break;
		case BISHOP:
			value = 3;
			break;
		case KNIGHT:
			value = 3;
			break;
		case ROOK:
			value = 5;
			break;
		case KING:
			value = 100;
			break;
		case QUEEN:
			value = 9;
			break;
		default:
			System.err.print("Piece.getPointValue [ERROR]: unknown piece type=" + pieceType);
			System.exit(-1);
			break;
		}
		return value;
	}

	@Override
	public String toString()
	{
//		return "Piece(type=" + this.getType() + ", pieceID=" + this.getPieceID() + ", unitID=" + (this.getUnitID() != null ? this.getUnitID() : "?")
//			+ ", player=" + this.getPlayer() + ", pos=" + this.getCurrentPosition() + ", alive=" + this.isAlive() + ")";
		return "Piece(type=" + this.getType() + ", pieceID=" + this.getPieceID()
				+ ", player=" + this.getPlayer() + ")";
	}

	@Override
	public boolean equals(Object other)
	{
		boolean isEqual = false;

		if(other instanceof Piece)
		{
			Piece otherPiece = (Piece)other;
			return this.getType() == otherPiece.getType() && this.getPieceID() == otherPiece.getPieceID()
					&& this.getPlayer().equals(otherPiece.getPlayer());
		}

		return isEqual;
	}

	public Piece copy()
	{
//		return Piece.makePiece(this.getPieceID(), this.getUnitID(), this.getPlayer(), this.getType(), this.getCurrentPosition());
		return Piece.makePiece(this.getPieceID(), this.getPlayer(), this.getType());
	}

	public Coordinate getCurrentPosition(Board board)
	{
		return board.getPiecePosition(this.getPlayer(), this.getPieceID());
	}

	public String getAlgebraicSymbol()
	{
		return Piece.getAlgebraicSymbol(this.getType());
	}

	public static String getAlgebraicSymbol(PieceType pieceType)
	{
		switch(pieceType)
		{
		case PAWN:
			return "P";
		case BISHOP:
			return "B";
		case KNIGHT:
			return "N";
		case ROOK:
			return "R";
		case QUEEN:
			return "Q";
		default:
			return "K";
		}
	}

//	public static Piece makePiece(int pieceID, Integer unitID, Player player, PieceType type, Coordinate pos)
	public static Piece makePiece(int pieceID, Player player, PieceType type)
	{
		Piece piece = null;
		if(type == PieceType.PAWN)
		{
//			piece = new Pawn(pieceID, unitID, player, pos);
			piece = new Pawn(pieceID, player);
		} else if(type == PieceType.BISHOP)
		{
			// piece = new Bishop(pieceID, unitID, player, pos);
			piece = new Bishop(pieceID, player);
		} else if(type == PieceType.KNIGHT)
		{
			// piece = new Knight(pieceID, unitID, player, pos);
			piece = new Knight(pieceID, player);
		} else if(type == PieceType.ROOK)
		{
			// piece = new Rook(pieceID, unitID, player, pos);
			piece = new Rook(pieceID, player);
		} else if(type == PieceType.QUEEN)
		{
			// piece = new Queen(pieceID, unitID, player, pos);
			piece = new Queen(pieceID, player);
		} else if(type == PieceType.KING)
		{
			// piece = new King(pieceID, unitID, player, pos);
			piece = new King(pieceID, player);
		}
		return piece;
	}
}
