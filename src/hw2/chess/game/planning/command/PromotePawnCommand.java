package hw2.chess.game.planning.command;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.State.StateView;
import hw2.chess.game.Game;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.player.Player;

public class PromotePawnCommand extends Command
{

	private final PieceType promotionType;
	private final int promotionTypeTemplateID;

	protected PromotePawnCommand(Player player, int pawnToPromotePieceID, PieceType promotionType, int promotionTypeTemplateID)
	{
		super(player, pawnToPromotePieceID, CommandType.PROMOTEPAWNCOMMAND);
		this.promotionType = promotionType;
		this.promotionTypeTemplateID = promotionTypeTemplateID;
	}

	public PieceType getPromotionType() { return this.promotionType; }
	public int getPromotionTypeTemplateID() { return this.promotionTypeTemplateID; }

	@Override
	public boolean preconditionsMet(StateView state, Game game)
	{
		// townhall always has enough resources to make new units
		return true;
	}

	@Override
	public Game applyPostconditions(StateView state, Game game)
	{
		// nothing to do!
		return game;
	}

	@Override
	public Action getAction(Game game)
	{
		return Action.createCompoundProduction(this.getActorPieceUnitID(game), this.getPromotionTypeTemplateID());
	}

	/**
	 * This method returns true IFF the piece is dead AND the new piece exists (with the same pieceID)
	 */
	@Override
	public boolean isResolved(StateView state, Game game)
	{
		// System.out.println("PromotePawnCommand.isResolved [INFO]: init");
		// lookup the piece in the game....this action is resolved IFF there is a new piece in the game which has the same
		// template as the one requested

		int oldNumberOfPieces = game.getNumberOfAlivePieces(this.getPlayer(), this.getPromotionType());

		int newNumberOfPieces = 0;
		for(Integer unitID : state.getUnitIds(this.getPlayerID()))
		{
			if(PieceType.valueOf(state.getUnit(unitID).getTemplateView().getName().toUpperCase()) == this.getPromotionType())
			{
				newNumberOfPieces += 1;
			}
		}

		// System.out.println("PromotePawnCommand.isResolved [INFO]: newNumPieces=" + newNumberOfPieces
		// 		+ " oldNumPieces=" + oldNumberOfPieces);

		boolean answer = (newNumberOfPieces - oldNumberOfPieces) == 1;

		// System.out.println("PromotePawnCommand.isResolved [INFO]: isResolved=" + answer);
		return answer;
	}

	@Override
	public String toString() { return "PromotePawnCommand(pawnID=" + this.getPieceID() + ", promotionType=" + this.getPromotionType() + ")"; }

}
