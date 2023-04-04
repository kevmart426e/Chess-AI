package hw2.chess.agents;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.environment.model.history.History.HistoryView;
import edu.cwru.sepia.environment.model.state.State.StateView;
import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.planning.Planner;
import hw2.chess.game.player.Player;
import hw2.chess.game.player.PlayerType;
import hw2.chess.streaming.Streamer;

public class RandomActionAgent extends ChessAgent
{

	private static final long serialVersionUID = -8325987205183244708L;
	private final int maxNumMoves = 100;

	private final long maxPlaytimeInMS;
	private final PlayerType playerType;
	private Player myPlayer;
	private int numMovesPlayed;

	public RandomActionAgent(int playerID, String[] args)
	{
		super(playerID);
		long maxPlaytimeInMS = 0;
		String playerTypeString = null;
		String filePath = null;

		if(args.length == 3)
		{
			playerTypeString = args[1];
			maxPlaytimeInMS = Long.parseLong(args[2]) * 1000;
		} else if(args.length == 4)
		{
			playerTypeString = args[1];
			maxPlaytimeInMS = Long.parseLong(args[2]) * 1000;
			filePath = args[3];
		} else
		{
			System.err.println("RandomActionAgent.RandomActionAgent [ERROR]: not enough arguments. Must specify player type, total playing time (in seconds), and (optionally) a filepath");
			System.exit(-1);
		}

		this.playerType = PlayerType.valueOf(playerTypeString);
		this.maxPlaytimeInMS = maxPlaytimeInMS;
		this.myPlayer = null;
		this.numMovesPlayed = 0;
		this.setFilePath(filePath);

		System.out.println("Constructed RandomActionAgent(teamColor=" + this.getPlayerType() + ", timeLimit(ms)=" + this.getMaxPlaytimeInMS() + ")");
	}

	public long getMaxPlaytimeInMS() { return this.maxPlaytimeInMS; }
	private int getMaxNumMoves() { return this.maxNumMoves; }
	private boolean outOfMoves() { return this.numMovesPlayed > this.getMaxNumMoves(); }
	private void recordMove() { this.numMovesPlayed += 1; }

	@Override
	public PlayerType getPlayerType() { return this.playerType; }

	@Override
	protected Player getPlayer() { return this.myPlayer; }

	@Override
	protected Move getChessMove(StateView state)
	{
		Game game = Planner.getPlanner().getGame(state, this.maxPlaytimeInMS);
		List<Move> allMoves = game.getAllMoves(this.getPlayer());

//		System.out.println("number of moves available=" + allMoves.size());
//		for(Move move: allMoves)
//		{
//			System.out.println("move=" + move + " with piece=" + Planner.getPlanner().getGame().getPiece(move.getActorPlayer(), move.getActorPieceID()));
//		}

		// choose randomly amongst the available knight moves
		int index = (int)(Math.random() * allMoves.size());
		Move move = allMoves.get(index);

		Streamer.getStreamer(this.getFilePath()).streamMove(move, Planner.getPlanner().getGame());
		return move;
	}

	@Override
	public Map<Integer, Action> initialStep(StateView state, HistoryView history)
	{
		Game game = Planner.getPlanner().getGame(state, this.maxPlaytimeInMS);
		game.registerPlayer(this.getPlayerNumber(), this.getPlayerType(), state);
		this.myPlayer = game.getPlayer(this.getPlayerType());

		Streamer.getStreamer(this.getFilePath());
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

		if(Planner.getPlanner().isMyTurn(this.getPlayerType())) // only allow the white player to move for now
		{
			if(this.outOfMoves() || Planner.getPlanner().isGameOver())
			{
				actions = this.killMyPieces(state);
			}
			else
			{
				// System.out.println("RandomActionAgent.middleStep [INFO] game is not over!");
				if(Planner.getPlanner().canSubmitMove())
				{
					Move move = this.getChessMove(state);
					// System.out.println("RandomActionAgent.middleStep [INFO] selected move=" + move);
		
					// System.out.println("RandomActionAgent.middleStep [INFO] getPlanner().canSubmitMove()=" + Planner.getPlanner().canSubmitMove());
					if(Planner.getPlanner().canSubmitMove())
					{
						Planner.getPlanner().submitMove(move, Planner.getPlanner().getGame());
						this.recordMove();
					}
				}

				Action action = Planner.getPlanner().getAction(this.getPlayer(), state);
				// System.out.println("RandomActionAgent.middleStep [INFO] current action=" + action);
				if(action != null)
				{
					// do something
					actions.put(action.getUnitId(), action);
				}
			}
		}
		return actions;
	}

	@Override
	public void savePlayerData(OutputStream history)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void terminalStep(StateView state, HistoryView history)
	{
		// System.exit(0);
	}

}
