package hw2.chess.utils;

public class Distance extends Object
{

	public static int lInfDist(Coordinate a, Coordinate b)
	{
		return Math.max(Distance.xDist(a, b), Distance.yDist(a, b));
	}

	public static int l1Dist(int a, int b)
	{
		return Math.abs(a - b);
	}

	public static int l1Dist(Coordinate a, Coordinate b)
	{
		return Math.abs(a.getXPosition() - b.getXPosition()) + Math.abs(a.getYPosition() - b.getYPosition());
	}

	public static int xDist(Coordinate a, Coordinate b)
	{
		return Distance.l1Dist(a.getXPosition(), b.getXPosition());
	}

	public static int yDist(Coordinate a, Coordinate b)
	{
		return Distance.l1Dist(a.getYPosition(), b.getYPosition());
	}

}
