package hw2.chess.game.move;

import hw2.chess.game.Board;
import hw2.chess.game.piece.King;
import hw2.chess.game.piece.Pawn;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.piece.Rook;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Coordinate;

public abstract class Move extends Object
{

	private final Player actorPlayer;
	private final int actorPieceID;
	private final MoveType type;

	protected Move(Player actorPlayer, int actorPieceID, MoveType type)
	{
		this.actorPlayer = actorPlayer;
		this.actorPieceID = actorPieceID;
		this.type = type;
	}

	public Player getActorPlayer() { return actorPlayer; }
	public int getActorPieceID() { return this.actorPieceID; }
	public MoveType getType() { return this.type; }

	public abstract boolean isResolved(Board board);


	public static Move createMovementMove(Player actorPlayer, int actorPieceID, Coordinate tgtPosition)
	{
		return new MovementMove(actorPlayer, actorPieceID, tgtPosition);
	}
	public static Move createMovementMove(Piece piece, Coordinate tgtPosition)
	{
		return Move.createMovementMove(piece.getPlayer(), piece.getPieceID(), tgtPosition);
	}

	public static Move createCaptureMove(Player attkPlayer, int attkPieceID, Player tgtPlayer, int tgtPieceID)
	{
		return new CaptureMove(attkPlayer, attkPieceID, tgtPlayer, tgtPieceID);
	}
	public static Move createCaptureMove(Piece attkPiece, Piece tgtPiece)
	{
		// System.out.println("Move.createCaptureMove [INFO] attkPiece=" + attkPiece + " tgtPiece=" + tgtPiece);
		return Move.createCaptureMove(attkPiece.getPlayer(), attkPiece.getPieceID(), tgtPiece.getPlayer(), tgtPiece.getPieceID());
	}

	public static Move createCastleMove(Player player, int kingPieceID, int rookPieceID)
	{
		return new CastleMove(player, kingPieceID, rookPieceID);
	}
	public static Move createCastleMove(King kingPiece, Rook rookPiece)
	{
		return Move.createCastleMove(kingPiece.getPlayer(), kingPiece.getPieceID(), rookPiece.getPieceID());
	}

	public static Move createPromotePawnMove(Player player, int pawnToPromotePieceID, PieceType promotedType)
	{
		return new PromotePawnMove(player, pawnToPromotePieceID, promotedType);
	}
	public static Move createPromotePawnMove(Pawn pawn, PieceType promotedType)
	{
		return Move.createPromotePawnMove(pawn.getPlayer(), pawn.getPieceID(), promotedType);
	}
}
