package hw2.chess.game.planning.command;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.State.StateView;
import hw2.chess.game.Game;
import hw2.chess.game.piece.Pawn;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.player.Player;
import hw2.chess.utils.Coordinate;

/**
 * This is a medium level action. It sits between a Move and a SEPIA Action. A Planner converts a move to a sequence of commands, which themselves are converted into an action.
 * The reason a move doesn't convert directly into an action is because we want to capture postconditions & preconditions, which Command objects serve to do.
 * For instance, a PromotePawnCommand's postcondition will be to identify the new piece that is created and get it's unitID so future commands can manipulate that piece.
 * @author andrew
 *
 */
public abstract class Command
{

	public enum CommandType
	{

		MOVEMENTCOMMAND,
		KILLCOMMAND,
		DISCOVERPIECECOMMAND,
		PROMOTEPAWNCOMMAND,

	}

	private final Player player;
	private final int pieceID;
	private final CommandType type;

	protected Command(Player player, int pieceID, CommandType type)
	{
		this.player = player;
		this.pieceID = pieceID;
		this.type = type;
	}

	public int getPieceID() { return this.pieceID; }
	public CommandType getType() { return this.type; }
	public Player getPlayer() { return this.player; }
	public int getPlayerID() { return this.getPlayer().getPlayerID(); }
	public boolean hasActions()
	{
		boolean hasActions = false;
		switch(this.getType())
		{
		case DISCOVERPIECECOMMAND:
			hasActions = false;
			break;
		default:
			hasActions = true;
			break;
		}
		return hasActions;
	}

	protected Piece getPiece(Game game, Player player, Integer pieceID) { return game.getBoard().getPiece(player, pieceID); }
	protected Integer getUnitID(Game game, Player player, Integer pieceID) { return game.getUnitID(player, pieceID); }
	public Piece getActorPiece(Game game) { return this.getPiece(game, this.getPlayer(), this.getPieceID()); }
	public Integer getActorPieceUnitID(Game game) { return this.getUnitID(game, this.getPlayer(), this.getPieceID()); }

	public abstract boolean preconditionsMet(StateView state, Game game);
	public abstract Game applyPostconditions(StateView state, Game game);
	public abstract Action getAction(Game game);
	public abstract boolean isResolved(StateView state, Game game);


	public static Command createMovementCommand(Player player, int pieceID, Coordinate finalDestination)
	{
		return new MovementCommand(player, pieceID, finalDestination);
	}
	public static Command createMovementCommand(Piece piece, Coordinate finalDestination)
	{
		return Command.createMovementCommand(piece.getPlayer(), piece.getPieceID(), finalDestination);
	}

	public static Command createKillCommand(Player attkPlayer, int attkPieceID, Player tgtPlayer, int tgtPieceID)
	{
		return new KillCommand(attkPlayer, attkPieceID, tgtPlayer, tgtPieceID);
	}
	public static Command createKillCommand(Piece attkPiece, Piece tgtPiece)
	{
		return Command.createKillCommand(attkPiece.getPlayer(), attkPiece.getPieceID(), tgtPiece.getPlayer(), tgtPiece.getPieceID());
	}

	public static Command createPromotePawnCommand(Player player, int pawnToPromotePieceID, PieceType promotionType, int promotionTemplateID)
	{
		return new PromotePawnCommand(player, pawnToPromotePieceID, promotionType, promotionTemplateID);
	}
	public static Command createPromotePawnCommand(Pawn piece, PieceType promotionType, int promotionTemplateID)
	{
		return Command.createPromotePawnCommand(piece.getPlayer(), piece.getPieceID(), promotionType, promotionTemplateID);
	}

	public static Command createdDiscoverPieceCommand(Player player, int pieceIDOfPieceToDiscover, PieceType promotionType)
	{
		return new DiscoverPieceCommand(player, pieceIDOfPieceToDiscover, promotionType);
	}
}
