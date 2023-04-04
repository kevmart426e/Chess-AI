package hw2.chess.game.player;

public class Player extends Object
{

	private final int playerID;
	private final PlayerType playerType;

	public Player(int playerID, PlayerType playerType)
	{
		this.playerID = playerID;
		this.playerType = playerType;
	}

	public int getPlayerID() { return this.playerID; }
	public PlayerType getPlayerType() { return this.playerType; }

	@Override
	public boolean equals(Object other)
	{
		boolean isEqual = false;

		if(other instanceof Player)
		{
			isEqual = ((Player)other).getPlayerID() == this.getPlayerID();
		}

		return isEqual;
	}

	@Override
	public int hashCode()
	{
		return this.getPlayerID();
	}

	@Override
	public String toString()
	{
		return "Player(type=" + this.getPlayerType() + ", id=" + this.getPlayerID() + ")";
	}

	public String getAlgebraicSymbol()
	{
		if(this.getPlayerType().equals(PlayerType.BLACK))
		{
			return "B";
		}
		return "W";
	}

}
