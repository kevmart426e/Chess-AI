package hw2.chess.game.planning.command;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.piece.Castleable;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Coordinate;

/**
 * This class is a first order logic (STRIPS) action representing a movement along a simple path. The path this action
 * represents MUST exist and does NOT involve moving any other pieces.
 * @author andrew
 *
 */
public class MovementCommand extends Command
{
	private final Coordinate finalPosition;

	protected MovementCommand(Player player, int pieceID, Coordinate finalPosition)
	{
		super(player, pieceID, CommandType.MOVEMENTCOMMAND);
		this.finalPosition = finalPosition;
	}

	public Coordinate getFinalPosition() { return this.finalPosition; }

	@Override
	public boolean preconditionsMet(StateView state, Game game)
	{
		return true;
	}

	@Override
	public Game applyPostconditions(StateView state, Game game)
	{
		// update the game with the new location of the unit
		Game newGame = game.applyMove(Move.createMovementMove(this.getPlayer(), this.getPieceID(),
				this.getFinalPosition()));

		// if the actor piece is a rook or a king, we needs to disqualify that piece from castling
		Piece actorPiece = this.getActorPiece(newGame);
		if(actorPiece.getType() == PieceType.KING || actorPiece.getType() == PieceType.ROOK)
		{
			if(((Castleable)actorPiece).canCastle())
			{
				// disqualify the piece...we need to make a fresh copy of the piece so that this change is not propagated backwards up the tree...only downwards
				Piece newActorPiece = Piece.makePiece(this.getPieceID(), this.getPlayer(), actorPiece.getType());
				Coordinate position = this.getFinalPosition();
				int unitID = this.getUnitID(newGame, this.getPlayer(), this.getPieceID());

				((Castleable)newActorPiece).disqualify();

				newGame.getBoard().removePiece(this.getPlayer(), this.getPieceID());
				newGame.getBoard().addNewPiece(newActorPiece, position);
				newGame.getBoard().updateUnitIDForPiece(newActorPiece, unitID);
			}
		}

		return newGame;
	}

	@Override
	public Action getAction(Game game)
	{
		return Action.createCompoundMove(this.getActorPieceUnitID(game),
				this.getFinalPosition().getXPosition(),
				this.getFinalPosition().getYPosition());
	}

	@Override
	public boolean isResolved(StateView state, Game game)
	{
		// lookup the piece...this action is resolved IFF that piece is at the target location
		if(state.getUnit(game.getUnitID(this.getPlayer(), this.getPieceID())) == null)
		{
			System.err.println("MovementCommand.isResolved [ERROR]: piece=" + this.getActorPiece(game) + " does not exist");
			System.exit(-1);
		}
		UnitView unitView = state.getUnit(this.getActorPieceUnitID(game));
		return new Coordinate(unitView.getXPosition(), unitView.getYPosition()).equals(this.getFinalPosition());
	}

	@Override
	public String toString() { return "MovementCommand(pieceID=" + this.getPieceID() + ", dst=" + this.getFinalPosition() + ")"; }

}
