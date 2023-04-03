package hw2.chess.search;

import java.util.LinkedList;
import java.util.List;

import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.player.Player;

public class DFSTreeNode extends Object
{

	private static class UtilityConstants extends Object
	{
		private static final double MAXPLAYERLOSS = 0;
		private static final double MAXPLAYERTIE = 15;
		private static final double MAXPLAYERWIN = Double.MAX_VALUE;
	}

	private final Move move;
	private final Game game;
	private final DFSTreeNode parent;
	private final Player maxPlayer;
	private final DFSTreeNodeType nodeType;

	private double maxPlayerUtilityValue;
	private double maxPlayerHeuristicValue;

	private final boolean isTerminal;

	public DFSTreeNode(Move move, Game game, DFSTreeNode parent, Player maxPlayer)
	{
		this.move = move;
		this.game = game;
		this.parent = parent;
		this.maxPlayer = maxPlayer;
		this.nodeType = this.getMaxPlayer().equals(game.getCurrentPlayer()) ? DFSTreeNodeType.MAX : DFSTreeNodeType.MIN;

		this.maxPlayerUtilityValue = 0.0;
		this.maxPlayerHeuristicValue = 0.0;

		boolean isTerminal = false;
		if(this.getGame().isTerminal())
		{
			isTerminal = true;
			// terminal state
			this.calculateUtilityValue();
		}
		this.isTerminal = isTerminal;
	}

	public DFSTreeNode(Game game, Player maxPlayer)
	{
		this(null, game, null, maxPlayer);
	}

	public Move getMove() { return this.move; }
	public Game getGame() { return this.game; }
	public DFSTreeNode getParent() { return this.parent; }
	public Player getMaxPlayer() { return this.maxPlayer; }
	public DFSTreeNodeType getType() { return this.nodeType; }
	public boolean isTerminal() { return this.isTerminal; }

	public double getMaxPlayerUtilityValue() { return this.maxPlayerUtilityValue; }
	public double getMaxPlayerHeuristicValue() { return this.maxPlayerHeuristicValue; }

	public void setMaxPlayerUtilityValue(double val) { this.maxPlayerUtilityValue = val; }
	public void setMaxPlayerHeuristicValue(double val) { this.maxPlayerHeuristicValue = val; }

	private void calculateUtilityValue()
	{
		if(this.getGame().isInCheckmate())
		{
			// who is in check?
			if(this.getGame().isInCheck(this.getMaxPlayer()))
			{
				// loss
				this.setMaxPlayerUtilityValue(DFSTreeNode.UtilityConstants.MAXPLAYERLOSS);
			} else
			{
				// win
				this.setMaxPlayerUtilityValue(DFSTreeNode.UtilityConstants.MAXPLAYERWIN);
			}
		} else if(this.getGame().isInStalemate())
		{
			// tie
			this.setMaxPlayerUtilityValue(DFSTreeNode.UtilityConstants.MAXPLAYERTIE);
		} else
		{
			// out of time...but who?
			if(this.getGame().getTimeLeftInMS(this.getMaxPlayer()) <= 0)
			{
				// loss
				this.setMaxPlayerUtilityValue(DFSTreeNode.UtilityConstants.MAXPLAYERLOSS);
			} else
			{
				// win
				this.setMaxPlayerUtilityValue(DFSTreeNode.UtilityConstants.MAXPLAYERWIN);
			}
		}
	}

	public List<DFSTreeNode> getChildren()
	{
		List<DFSTreeNode> children = new LinkedList<DFSTreeNode>();

		// Map<PieceType, Set<Piece> > playerPieces = this.getGame().getBoard().getPieceType2Pieces(this.getGame().getCurrentPlayer());
		if(!this.isTerminal())
		{
			for(Move move : this.getGame().getAllMoves(this.getGame().getCurrentPlayer()))
			{
				children.add(new DFSTreeNode(move, this.getGame().applyMove(move), this, this.getMaxPlayer()));
			}
		}

		return children;
	}

}
