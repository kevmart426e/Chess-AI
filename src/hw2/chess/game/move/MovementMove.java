package hw2.chess.game.move;

import hw2.chess.game.Board;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Coordinate;

public class MovementMove extends Move
{

	private final Coordinate tgtPosition;

	protected MovementMove(Player actorPlayer, int actorPieceID, Coordinate tgtPosition)
	{
		super(actorPlayer, actorPieceID, MoveType.MOVEMENTMOVE);
		this.tgtPosition = tgtPosition;
	}

	public Coordinate getTargetPosition() { return this.tgtPosition; }

	@Override
	public boolean isResolved(Board board)
	{
		return board.getPiecePosition(this.getActorPlayer(), this.getActorPieceID()).equals(this.getTargetPosition());
	}

	@Override
	public String toString()
	{
		return "MovementAction(movingPieceID=" + this.getActorPieceID() + ", player=" + this.getActorPlayer()
			+ ", destination=" + this.getTargetPosition() + ")";
	}

}
