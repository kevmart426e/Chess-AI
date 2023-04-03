package hw2.chess.game.move;

import edu.cwru.sepia.util.Direction;
import hw2.chess.game.Board;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Coordinate;

public class CastleMove extends Move
{

	private final int rookPieceID;
	private Coordinate finalKingPosition;
	private Coordinate finalRookPosition;

	protected CastleMove(Player player, int kingPieceID, int rookPieceID)
	{
		super(player, kingPieceID, MoveType.CASTLEMOVE);
		this.rookPieceID = rookPieceID;
		this.finalKingPosition = null;
		this.finalRookPosition = null;
	}

	public Player getKingPlayer() { return this.getActorPlayer(); }
	public int getKingPieceID() { return this.getActorPieceID(); }
	public Player getRookPlayer() { return this.getActorPlayer(); }
	public int getRookPieceID() { return this.rookPieceID; }

	public Coordinate getFinalKingPosition() { return this.finalKingPosition; }
	public Coordinate getFinalRookPosition() { return this.finalRookPosition; }

	public void makeFinalPositions(Board board)
	{
		// only 2 options for castling
		// king always moves two spaces over
		Coordinate kingPosition = board.getPiecePosition(this.getKingPlayer(), this.getKingPieceID());
		Coordinate rookPosition = board.getPiecePosition(this.getRookPlayer(), this.getRookPieceID());

		Direction kingDirection = kingPosition.getDirectionTo(rookPosition);
		this.finalKingPosition = kingPosition.getNeighbor(kingDirection, 2);

		Direction rookDirection = rookPosition.getDirectionTo(kingPosition);
		this.finalRookPosition = this.finalKingPosition.getNeighbor(rookDirection);
	}

	@Override
	public boolean isResolved(Board board)
	{
		Coordinate kingPosition = board.getPiecePosition(this.getKingPlayer(), this.getKingPieceID());
		Coordinate rookPosition = board.getPiecePosition(this.getRookPlayer(), this.getRookPieceID());

		return (this.getFinalKingPosition() == null || kingPosition.equals(this.getFinalKingPosition()))
				&& ((this.getFinalRookPosition() == null || rookPosition.equals(this.getFinalRookPosition())));
	}

	@Override
	public String toString()
	{
		return "CastleAction(kingPieceID=" + this.getKingPieceID() + ", rookPieceID=" + this.getRookPieceID()
			+ ", player=" + this.getKingPlayer() + ")";
	}

}
