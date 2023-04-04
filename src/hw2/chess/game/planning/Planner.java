package hw2.chess.game.planning;

import java.util.List;
import java.util.Stack;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.state.State.StateView;
import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.move.MoveType;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.planning.command.Command;
import hw2.chess.game.player.Player;
import hw2.chess.game.player.PlayerType;
import hw2.chess.utils.TypeConverter;

public class Planner extends BasePlanner
{

	private Move currentMove;
	private Game currentGame;
	private Stack<Command> currentPlan;

	private static Planner singletonInstance = null;

	public Planner()
	{
		this.currentMove = null;
		this.currentGame = null;
		this.currentPlan = null;
	}

	private void setGame(Game game) { this.currentGame = game; }

	public Move getCurrentMove() { return this.currentMove; }
	public synchronized Game getGame() { return this.currentGame; }
	public synchronized Game getGame(StateView state, long timeForEachPlayerInMS)
	{
		if(this.getGame() == null)
		{
			this.setGame(Game.makeNewGame(state, timeForEachPlayerInMS));
		}
		return this.getGame();
	}
	public synchronized void registerPlayer(int playerID, PlayerType type, StateView state)
	{
		this.getGame().registerPlayer(playerID, type, state);
	}
	public synchronized boolean isGameOver()
	{
		boolean over = this.getGame().isInCheckmate() || this.getGame().isInStalemate() || this.getGame().outOfTime();
		if(over)
		{
			this.setGame(null);
		}
		return over;
	}

	public boolean canSubmitMove() { return this.getCurrentMove() == null; }
	public synchronized final Stack<Command> getCurrentPlan() { return this.currentPlan; }
	public synchronized final boolean isMyTurn(PlayerType playerType)
	{
		return this.getGame() != null && this.getGame().getCurrentPlayer().getPlayerType().equals(playerType);
	}

	private void resolveMove()
	{
		this.currentMove = null;
		this.currentPlan = null;
	}

	public synchronized void submitMove(Move move, Game game)
	{
		if(this.canSubmitMove())
		{
			this.currentMove = move;
			this.currentGame = game;

			this.currentPlan = TypeConverter.listToStack(this.makeCommands(move, game));
		}
	}

	public synchronized Action getAction(Player player, StateView state)
	{
		Action action = null;

		if(this.isMyTurn(player.getPlayerType()))
		{
			// System.out.println("Planner.getAction [INFO]: currentPlan=" + this.getCurrentPlan());
			// can only get actions if there is commands
			if(this.getCurrentPlan() != null)
			{
				// resolve the command if we can OR if the current command has no actions we can generate from it
				if(this.getCurrentPlan().peek().isResolved(state, this.getGame()))
				{
					this.setGame(this.getCurrentPlan().pop().applyPostconditions(state, this.getGame()));
					this.getGame().setCurrentPlayer(player); // still want the current player to be us!
				}

				// if the list is now empty...resolve the move!
				while(!this.getCurrentPlan().isEmpty() && !this.getCurrentPlan().peek().hasActions())
				{
					// System.out.println("Planner.getAction [INFO]: currentPlan=" + this.getCurrentPlan());
					this.setGame(this.getCurrentPlan().pop().applyPostconditions(state, this.getGame()));
					this.getGame().setCurrentPlayer(player); // still want the current player to be us!
				}

				if(this.getCurrentPlan().isEmpty())
				{
					this.resolveMove();
					this.getGame().setCurrentPlayer(this.getGame().getOtherPlayer(player));
				} else
				{
					action = this.getCurrentPlan().peek().getAction(this.getGame());
				}
			}
		}

		return action;
	}

	@Override
	protected List<Command> makeCommands(Move move, Game game)
	{
		BasePlanner planner = null;

		// castle moves, promotion moves, & moves where the KNIGHT is moving are tricky...handle those separately
		if(move.getType() == MoveType.CASTLEMOVE || move.getType() == MoveType.PROMOTEPAWNMOVE  ||
			game.getPiece(move.getActorPlayer(), move.getActorPieceID()).getType() == PieceType.KNIGHT)
		{
			planner = new ComplexPlanner();
		} else
		{
			// any other piece/type of move is simple...get path to target (one is guaranteed to exist)
			// and either kill the target or just move there
			planner = new SimplePlanner();
		}
		return planner.makeCommands(move, game);
	}

	public static synchronized Planner getPlanner()
	{
		if(Planner.singletonInstance == null)
		{
			synchronized(Planner.class)
			{
				if(Planner.singletonInstance == null)
				{
					Planner.singletonInstance = new Planner();
				}
			}
		}
		return Planner.singletonInstance;
	}
}
