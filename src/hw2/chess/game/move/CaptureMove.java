package hw2.chess.game.move;

import hw2.chess.game.Board;
import hw2.chess.game.player.Player;

public class CaptureMove extends Move
{

	private final Player tgtPlayer;
	private final int tgtPieceID;

	protected CaptureMove(Player attkPlayer, int attkPieceID, Player tgtPlayer, int tgtPieceID) {
		super(attkPlayer, attkPieceID, MoveType.CAPTUREMOVE);
		this.tgtPlayer = tgtPlayer;
		this.tgtPieceID = tgtPieceID;
	}

	// for enpassant
	protected CaptureMove(Player attkPlayer, int attkPieceID, Player tgtPlayer, int tgtPieceID, MoveType moveType) {
		super(attkPlayer, attkPieceID, moveType);
		this.tgtPlayer = tgtPlayer;
		this.tgtPieceID = tgtPieceID;
	}

	public Player getAttackingPlayer() { return this.getActorPlayer(); }
	public int getAttackingPieceID() { return this.getActorPieceID(); }
	public Player getTargetPlayer() { return this.tgtPlayer; }
	public int getTargetPieceID() { return this.tgtPieceID; }

	@Override
	public boolean isResolved(Board board)
	{
		return board.getPiecePosition(this.getAttackingPlayer(), this.getAttackingPieceID())
				.equals(board.getPiecePosition(this.getTargetPlayer(), this.getTargetPieceID()));
	}

	@Override
	public String toString()
	{
		return "CaptureMove(attkPieceID=" + this.getAttackingPieceID() + ", attkPlayer=" + this.getAttackingPlayer()
			+ ", tgtPieceID=" + this.getTargetPieceID() + ", tgtPlayer=" + this.getTargetPlayer() + ")";
	}

}
