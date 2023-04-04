package hw2.chess.game.planning.command;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Distance;

public class KillCommand extends Command
{
	private final Player tgtPlayer;
	private final int tgtPieceID;

	protected KillCommand(Player attkPlayer, int attkPieceID, Player tgtPlayer, Integer tgtPieceID)
	{
		super(attkPlayer, attkPieceID, CommandType.KILLCOMMAND);
		this.tgtPlayer = tgtPlayer;
		this.tgtPieceID = tgtPieceID;
	}

	public Player getTargetPlayer() { return this.tgtPlayer; }
	public int getTargetPieceID() { return this.tgtPieceID; }
	public Player getAttackingPlayer() { return this.getPlayer(); }
	public int getAttackingPieceID() { return this.getPieceID(); }

	public Piece getAttackingPiece(Game game) { return this.getPiece(game, this.getAttackingPlayer(), this.getAttackingPieceID()); }
	public Piece getTargetPiece(Game game) { return this.getPiece(game, this.getTargetPlayer(), this.getTargetPieceID()); }
	public Integer getAttackingPieceUnitID(Game game) { return this.getUnitID(game, this.getAttackingPlayer(), this.getAttackingPieceID()); }
	public Integer getTargetPieceUnitID(Game game) { return this.getUnitID(game, this.getTargetPlayer(), this.getTargetPieceID()); }

	@Override
	public boolean preconditionsMet(StateView state, Game game)
	{
		boolean preconditionsMet = false;
		switch(this.getAttackingPiece(game).getType())
		{
		case KNIGHT:
			// knights can kill from anywhere...just makes life easier
			preconditionsMet = true;
		default:
			// other pieces must be next to opponent
			preconditionsMet = Distance.lInfDist(game.getCurrentPosition(this.getAttackingPiece(game)), game.getCurrentPosition(this.getTargetPiece(game))) == 1;
		}
		// true if the piece is adjacent to the tgtPiece
		if(preconditionsMet)
		{
			
		}
		return preconditionsMet;
	}

	@Override
	public Game applyPostconditions(StateView state, Game game)
	{
		// remove the tgtPiece from the game
		// System.out.println("KillCommand.applyPostconditions [INFO]: init");
		Game newGame = game.applyMove(Move.createCaptureMove(this.getAttackingPiece(game), this.getTargetPiece(game)));
		// System.out.println("KillCommand.applyPostconditions [INFO]: exit");
		return newGame;
	}

	@Override
	public Action getAction(Game game)
	{
		return Action.createPrimitiveAttack(this.getActorPieceUnitID(game), this.getTargetPieceUnitID(game));
	}

	@Override
	public boolean isResolved(StateView state, Game game)
	{
		// this action is resolved when the target piece does not exist anymore
		UnitView unitView = state.getUnit(this.getTargetPieceUnitID(game));
		// System.out.println("KillCommand.isResolved [INFO] unit=" + unitView + " isResolved=" + (unitView == null));
		return unitView == null;
	}

	@Override
	public String toString() { return "KillComand(attkPieceID=" + this.getAttackingPieceID() + ", tgtPieceID=" + this.getTargetPieceID() + ")"; }

}
