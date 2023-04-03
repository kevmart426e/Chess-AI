package hw2.chess.utils;

import java.util.List;

import edu.cwru.sepia.util.Direction;

public class Coordinate extends Object
{

	private final int xPosition;
	private final int yPosition;

	public Coordinate(int xPosition, int yPosition)
	{
		this.xPosition = xPosition;
		this.yPosition = yPosition;
	}

	public int getXPosition() { return this.xPosition; }
	public int getYPosition() { return this.yPosition; }

	public String toString()
	{
		return "(" + this.getXPosition() + ", " + this.getYPosition() + ")";
	}

	public boolean equals(Object other)
	{
		boolean isEqual = false;
		if(other instanceof Coordinate)
		{
			isEqual = this.getXPosition() == ((Coordinate)other).getXPosition() &&
					this.getYPosition() == ((Coordinate)other).getYPosition();
		}
		return isEqual;
	}

	public int hashCode()
	{
		return this.getXPosition() * this.getYPosition() + this.getYPosition() * this.getYPosition() * this.getXPosition();
	}

	public Coordinate getNeighbor(Direction direction, int numRepetitions)
	{
		Coordinate newCoordinate = null;
		if(direction == Direction.EAST)
		{
			newCoordinate = new Coordinate(this.getXPosition() + numRepetitions, this.getYPosition());
		} else if(direction == Direction.WEST)
		{
			newCoordinate = new Coordinate(this.getXPosition() - numRepetitions, this.getYPosition());
		} else if(direction == Direction.NORTH)
		{
			newCoordinate = new Coordinate(this.getXPosition(), this.getYPosition() - numRepetitions);
		} else if(direction == Direction.SOUTH)
		{
			newCoordinate = new Coordinate(this.getXPosition(), this.getYPosition() + numRepetitions);
		} else if(direction == Direction.NORTHEAST)
		{
			newCoordinate = new Coordinate(this.getXPosition() + numRepetitions, this.getYPosition() - numRepetitions);
		} else if(direction == Direction.NORTHWEST)
		{
			newCoordinate = new Coordinate(this.getXPosition() - numRepetitions, this.getYPosition() - numRepetitions);
		} else if(direction == Direction.SOUTHEAST)
		{
			newCoordinate = new Coordinate(this.getXPosition() + numRepetitions, this.getYPosition() + numRepetitions);
		} else
		{
			// SOUTHWEST
			newCoordinate = new Coordinate(this.getXPosition() - numRepetitions, this.getYPosition() + numRepetitions);
		}
		return newCoordinate;
	}

	public Coordinate getNeighbor(Direction direction)
	{
		return this.getNeighbor(direction, 1);
	}

	public Coordinate getNeighbor(List<Direction> directions)
	{
		Coordinate newPosition = this;
		for(Direction direction: directions)
		{
			newPosition = newPosition.getNeighbor(direction);
		}
		return newPosition;
	}

	public Direction getDirectionTo(Coordinate other)
	{
		Direction directionTo = null;

		if(this.getYPosition() == other.getYPosition())
		{
			// on the same col
			if(this.getXPosition() > other.getXPosition())
			{
				directionTo = Direction.WEST;
			} else if(this.getXPosition() < other.getXPosition())
			{
				directionTo = Direction.EAST;
			}
		} else if(this.getXPosition() == other.getXPosition())
		{
			// on the same row
			if(this.getYPosition() > other.getYPosition())
			{
				directionTo = Direction.NORTH;
			} else if(this.getYPosition() < other.getYPosition())
			{
				directionTo = Direction.SOUTH;
			}
		} else if(Distance.xDist(this, other) == Distance.yDist(this, other))
		{
			// on the same diagonal
			if(this.getXPosition() < other.getXPosition() && this.getYPosition() < other.getYPosition())
			{
				directionTo = Direction.SOUTHEAST;
			} else if(this.getXPosition() < other.getXPosition() && this.getYPosition() > other.getYPosition())
			{
				directionTo = Direction.NORTHEAST;
			} else if(this.getXPosition() > other.getXPosition() && this.getYPosition() < other.getYPosition())
			{
				directionTo = Direction.SOUTHWEST;
			} else if(this.getXPosition() > other.getXPosition() && this.getYPosition() > other.getYPosition())
			{
				directionTo = Direction.NORTHWEST;
			}
		}

		return directionTo;
	}

	public Coordinate copy()
	{
		return new Coordinate(this.getXPosition(), this.getYPosition());
	}
}
