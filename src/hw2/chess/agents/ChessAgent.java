package hw2.chess.agents;

import java.util.HashMap;
import java.util.Map;

import edu.cwru.sepia.action.Action;
import edu.cwru.sepia.agent.Agent;
import edu.cwru.sepia.environment.model.state.State.StateView;
import hw2.chess.game.move.Move;
import hw2.chess.game.player.Player;
import hw2.chess.game.player.PlayerType;

public abstract class ChessAgent extends Agent {

	private static final long serialVersionUID = -9057756971234802221L;

	private String filePath;

	public ChessAgent(int playerID)
	{
		super(playerID);
		this.filePath = null;
	}

	protected abstract Move getChessMove(StateView state);

	protected abstract PlayerType getPlayerType();
	protected abstract Player getPlayer();

	protected void setFilePath(String filePath) { this.filePath = filePath; }
	protected String getFilePath() { return this.filePath; }

	protected Map<Integer, Action> killMyPieces(StateView state)
	{
		Map<Integer, Action> actions = new HashMap<Integer, Action>();
		for(Integer unitID : state.getUnitIds(this.getPlayerNumber()))
		{
			actions.put(unitID, Action.createPrimitiveAttack(unitID, unitID));
		}
		return actions;
	}

}
