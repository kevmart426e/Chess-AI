package hw2.chess.game.planning.command;

import java.util.Set;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.State.StateView;
import edu.cwru.sepia.environment.model.state.Unit.UnitView;
import hw2.chess.game.Game;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Coordinate;

public class DiscoverPieceCommand extends Command
{

	private final PieceType newPieceType;

	protected DiscoverPieceCommand(Player player, int pieceIDOfPieceToDiscover, PieceType newPieceType)
	{
		super(player, pieceIDOfPieceToDiscover, CommandType.DISCOVERPIECECOMMAND);
		this.newPieceType = newPieceType;
	}
	public final PieceType getNewPieceType() { return this.newPieceType; }

	@Override
	public boolean preconditionsMet(StateView state, Game game)
	{
		// return true IFF there is an extra piece in the state for that player
		Set<Integer> playerUnitIDs = game.getUnitIDs(this.getPlayer(), this.getNewPieceType());

		boolean newPieceInStateButNotInGame = false;
		for(Integer unitID : state.getUnitIds(this.getPlayerID()))
		{
			if(!playerUnitIDs.contains(unitID) && state.getUnit(unitID).getTemplateView().getName()
					.toUpperCase().equals(this.getNewPieceType().toString()))
			{
				if(playerUnitIDs.contains(unitID))
				{
					newPieceInStateButNotInGame = true;
				}
			}
		}

		return newPieceInStateButNotInGame;
	}

	@Override
	public Game applyPostconditions(StateView state, Game game)
	{
		Set<Integer> playerUnitIDs = game.getUnitIDs(this.getPlayer(), this.getNewPieceType());
		// System.out.println("DiscoverPieceCommand.applyPostconditions [INFO]: playerUnitIDs=" + playerUnitIDs);

		Integer newUnitID = null;
		for(Integer unitID : state.getUnitIds(this.getPlayerID()))
		{
			// System.out.println("DiscoverPieceCommand.applyPostconditions [INFO]: unitID from state=" + unitID + ", type from state="
			// 		+ state.getUnit(unitID).getTemplateView().getName());
			if(!playerUnitIDs.contains(unitID) && state.getUnit(unitID).getTemplateView().getName()
					.toUpperCase().equals(this.getNewPieceType().toString()))
			{
				newUnitID = unitID;
			}
		}

		// System.out.println("DiscoverPieceCommand.applyPostconditions [INFO]: newUnitID=" + newUnitID);

		// newUnitID will never be null because we found the new unitID in the preconditions
		Piece newPiece = Piece.makePiece(this.getPieceID(), this.getPlayer(), this.getNewPieceType());
		UnitView unitView = state.getUnit(newUnitID);
		Game newGame = game.copy();
		newGame.getBoard().removePiece(this.getPlayer(), this.getPieceID());
		newGame.getBoard().addNewPiece(newPiece, new Coordinate(unitView.getXPosition(), unitView.getYPosition()));
		newGame.getBoard().updateUnitIDForPiece(newPiece, newUnitID);
		// System.out.println("DiscoverPieceCommand.applyPostconditions [INFO]: exit");
		return newGame;
	}

	@Override
	public Action getAction(Game game)
	{
		return null; // nothing to do!
	}

	@Override
	public boolean isResolved(StateView state, Game game)
	{
		// if the new piece is in the game, then we're good!
		return true;
	}

	@Override
	public String toString() { return "DiscoverPieceCommand(pieceID=" + this.getPieceID() + " newPieceType=" + this.getNewPieceType() + ")"; }

}
