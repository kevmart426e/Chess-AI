package hw2.chess.game.move;

import hw2.chess.game.Board;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.player.Player;

public class PromotePawnMove extends Move {

	private final PieceType promotedType;

	protected PromotePawnMove(Player player, int pawnToPromotePieceID, PieceType promotedType)
	{
		super(player, pawnToPromotePieceID, MoveType.PROMOTEPAWNMOVE);
		this.promotedType = promotedType;
	}

	public Player getPawnPlayer() { return this.getActorPlayer(); }
	public int getPawnPieceID() { return this.getActorPieceID(); }
	public PieceType getPromotedPieceType() { return this.promotedType; }

	@Override
	public boolean isResolved(Board board)
	{
		// this is true when the current pawn is NO LONGER on the board and a new piece
		// (with the requested PieceType) exists with the same unit ID and player
		Piece piece = board.getPiece(this.getPawnPlayer(), this.getPawnPieceID());
		boolean isPawnNoLongerPresent = (piece == null) || (piece.getType() != PieceType.PAWN);

		boolean isNewPiecePresent = piece != null && piece.getType() == this.getPromotedPieceType();

		return isPawnNoLongerPresent && isNewPiecePresent;
	}

	@Override
	public String toString()
	{
		return "PromotePawnAction(pawnPieceID=" + this.getPawnPieceID() + ", player= " + this.getPawnPlayer()
			+ ", promotedPieceType=" + this.getPromotedPieceType() + ")";
	}

}
