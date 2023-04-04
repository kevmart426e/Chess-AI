package hw2.chess.game.planning;


import java.util.List;
import java.util.Stack;

import hw2.chess.game.Game;
import hw2.chess.game.move.Move;
import hw2.chess.game.planning.command.Command;
import hw2.chess.utils.TypeConverter;

public abstract class BasePlanner extends Object
{

	protected abstract List<Command> makeCommands(Move move, Game game);

	protected Stack<Command> makePlan(Move move, Game game)
	{
		List<Command> commands = this.makeCommands(move, game);

		for(Command command: commands)
		{
			System.out.println("BasePlanner.makePlan [INFO] cmd=" + command);
		}

		return TypeConverter.listToStack(commands);
	}

	protected void failAndExit(String msg)
	{
		System.err.println(msg);
		System.exit(-1);
	}

}
