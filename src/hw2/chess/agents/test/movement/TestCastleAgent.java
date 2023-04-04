package hw2.chess.agents.test.movement;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import hw2.chess.agents.ChessAgent;
import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.move.MoveType;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.planning.Planner;
import hw2.chess.game.player.Player;
import hw2.chess.game.player.PlayerType;

public class TestCastleAgent extends ChessAgent {

	private static final long serialVersionUID = -329240707297539858L;
	private static final int timePlayerHasInMS = Integer.MAX_VALUE;

	private final PlayerType playerType;
	private boolean hasCastled;
	private Player myPlayer;

	public TestCastleAgent(int playerID, String[] args)
	{
		super(playerID);
		String playerTypeString = null;
		if(args.length == 2)
		{
			playerTypeString = args[1];
		} else
		{
			System.err.println("TestCastleAgent.TestCastleAgent [ERROR]: not enough arguments. Must specify player type");
			System.exit(-1);
		}

		this.playerType = PlayerType.valueOf(playerTypeString);
		this.hasCastled = false;
		this.myPlayer = null;

		System.out.println("Constructed TestCastleAgent(teamColor=" + this.getPlayerType() + ")");
	}

	private synchronized boolean hasCastled() { return this.hasCastled; }
	private synchronized void recordHasCastled() { this.hasCastled = true; }

	@Override
	protected Player getPlayer() { return this.myPlayer; }

	@Override
	protected Move getChessMove(StateView state)
	{
		Game game = Planner.getPlanner().getGame(state, TestCastleAgent.timePlayerHasInMS);
		List<Move> kingMoves = game.getAllMovesForPieceType(this.getPlayer(), PieceType.KING);

		List<Move> castleMoves = new LinkedList<Move>();
		for(Move move : kingMoves)
		{
			if(move.getType().equals(MoveType.CASTLEMOVE))
			{
				castleMoves.add(move);
			}
		}

		System.out.println("number of moves available for castling=" + castleMoves.size());
		for(Move move: castleMoves)
		{
			System.out.println("castle move=" + move);
		}

		// choose randomly amongst the available knight moves
		int index = (int)(Math.random() * castleMoves.size());
		return castleMoves.get(index);
	}

	@Override
	protected PlayerType getPlayerType()
	{
		return this.playerType;
	}

	@Override
	public Map<Integer, Action> initialStep(StateView state, HistoryView history)
	{
		Game game = Planner.getPlanner().getGame(state, TestCastleAgent.timePlayerHasInMS);
		game.registerPlayer(this.getPlayerNumber(), this.getPlayerType(), state);
		this.myPlayer = game.getPlayer(this.getPlayerType());
		return null;
	}

	@Override
	public void loadPlayerData(InputStream stream)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Integer, Action> middleStep(StateView state, HistoryView history)
	{
		Map<Integer, Action> actions = new HashMap<Integer, Action>();

		Action action = Planner.getPlanner().getAction(this.getPlayer(), state);
		System.out.println("TestCastleAgent.middleStep [INFO] current action=" + action);
		
		if(action != null)
		{
			// do something
			actions.put(action.getUnitId(), action);
		} else if(Planner.getPlanner().canSubmitMove()) // only allow the white player to move for now
		{
			if(!this.hasCastled() && this.getPlayerType() == PlayerType.WHITE)
			{
				Move move = this.getChessMove(state);
				System.out.println("TestCastleAgent.middleStep [INFO] selected move=" + move);
	
				System.out.println("TestCastleAgent.middleStep [INFO] getPlanner().canSubmitMove()=" + Planner.getPlanner().canSubmitMove());
				if(Planner.getPlanner().canSubmitMove())
				{
					Planner.getPlanner().submitMove(move, Planner.getPlanner().getGame());
				}
	
				this.recordHasCastled();
			} else if(this.getPlayerType() == PlayerType.WHITE)
			{
				// System.out.println("TestKnightMovementAgent.middleStep [INFO] playerType=" + this.getPlayerType() + " numTimesKnightHasMoved=" + this.getTimesKnightHasMoved());
				// kill pieces to end the game
//				for(Piece piece : Planner.getPlanner().getGame().getBoard().getPieces(this.getPlayer()))
//				{
//					actions.put(piece.getUnitID(), Action.createPrimitiveAttack(piece.getUnitID(), piece.getUnitID()));
//				}
			}
		}
		return actions;
	}

	@Override
	public void savePlayerData(OutputStream stream)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void terminalStep(StateView state, HistoryView history)
	{
		// TODO Auto-generated method stub

	}

}
