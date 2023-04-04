package hw2.chess.agents.test.pawnpromotion;

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
import hw2.chess.game.move.PromotePawnMove;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.planning.Planner;
import hw2.chess.game.player.Player;
import hw2.chess.game.player.PlayerType;

public class TestPromoteToKnight extends ChessAgent {

	private static final long serialVersionUID = -329240707297539858L;
	private static final int timePlayerHasInMS = Integer.MAX_VALUE;

	private final PlayerType playerType;
	private boolean hasPawnBeenPromoted;
	private Player myPlayer;

	public TestPromoteToKnight(int playerID, String[] args)
	{
		super(playerID);
		String playerTypeString = null;
		if(args.length == 2)
		{
			playerTypeString = args[1];
		} else
		{
			System.err.println("TestPromoteToKnight.TestPromoteToKnight [ERROR]: not enough arguments. Must specify player type");
			System.exit(-1);
		}

		this.playerType = PlayerType.valueOf(playerTypeString);
		this.hasPawnBeenPromoted = false;
		this.myPlayer = null;

		System.out.println("Constructed TestPromoteToKnight(teamColor=" + this.getPlayerType() + ")");
	}

	private boolean hasPawnBeenPromoted() { return this.hasPawnBeenPromoted; }
	private void recordPawnPromoted() { this.hasPawnBeenPromoted = true; }

	@Override
	protected Player getPlayer() { return this.myPlayer; }

	@Override
	protected Move getChessMove(StateView state)
	{
		Game game = Planner.getPlanner().getGame(state, TestPromoteToKnight.timePlayerHasInMS);
		List<Move> pawnMoves = game.getAllMovesForPieceType(this.getPlayer(), PieceType.PAWN);

		System.out.println("total num moves for pawns=" + pawnMoves.size());

		List<Move> pawnPromotionMoves = new LinkedList<Move>();
		for(Move move: pawnMoves)
		{
			if(move.getType().equals(MoveType.PROMOTEPAWNMOVE))
			{
				pawnPromotionMoves.add(move);
			}
		}

		Move pawnPromotionToKnightMove = null;
		for(Move move : pawnPromotionMoves)
		{
			if(((PromotePawnMove)move).getPromotedPieceType().equals(PieceType.KNIGHT))
			{
				pawnPromotionToKnightMove = move;
			}
		}

		System.out.println("number of moves available for pawn promotion=" + pawnPromotionMoves.size());
		for(Move move: pawnPromotionMoves)
		{
			System.out.println("pawn promotion move=" + move);
		}

		return pawnPromotionToKnightMove;
	}

	@Override
	protected PlayerType getPlayerType()
	{
		return this.playerType;
	}

	@Override
	public Map<Integer, Action> initialStep(StateView state, HistoryView history)
	{
		Game game = Planner.getPlanner().getGame(state, TestPromoteToKnight.timePlayerHasInMS);
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
		if(action != null)
		{
			// do something
			System.out.println("TestPromoteToKnight.middleStep [INFO] current action=" + action);
			actions.put(action.getUnitId(), action);
		} else if(Planner.getPlanner().canSubmitMove()) // only allow the white player to move for now
		{
			if(!this.hasPawnBeenPromoted() && this.getPlayerType() == PlayerType.WHITE)
			{
				Move move = this.getChessMove(state);
				System.out.println("TestPromoteToKnight.middleStep [INFO] selected move=" + move);
	
				System.out.println("TestPromoteToKnight.middleStep [INFO] getPlanner().canSubmitMove()=" + Planner.getPlanner().canSubmitMove());
				if(Planner.getPlanner().canSubmitMove())
				{
					Planner.getPlanner().submitMove(move, Planner.getPlanner().getGame());
				}
	
				this.recordPawnPromoted();
			} else if(this.getPlayerType() == PlayerType.WHITE)
			{
				// System.out.println("TestPromoteToKnight.middleStep [INFO] playerType=" + this.getPlayerType() + " numTimesKnightHasMoved=" + this.getTimesKnightHasMoved());
				// kill pieces to end the game
//				for(Piece piece : Planner.getPlanner().getGame().getBoard().getPieces(this.getPlayer()))
//				{
//					actions.put(Planner.getPlanner().getGame().getUnitID(piece.getPlayer(), piece.getPieceID()),
//							Action.createPrimitiveAttack(Planner.getPlanner().getGame().getUnitID(piece.getPlayer(), piece.getPieceID()),
//									Planner.getPlanner().getGame().getUnitID(piece.getPlayer(), piece.getPieceID())));
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
