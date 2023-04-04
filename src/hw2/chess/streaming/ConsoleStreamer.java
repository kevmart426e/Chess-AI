package hw2.chess.streaming;

import hw2.chess.game.Game;
import hw2.chess.game.move.Move;

public class ConsoleStreamer extends Streamer
{

	public ConsoleStreamer(String filePath)
	{
		super(filePath);
	}

	@Override
	public void createStream()
	{
	}

	@Override
	public void streamMove(Move move, Game game)
	{
		String moveString = this.getAlgebraicMoveString(move, game);
		System.out.println("ConsoleStreamer.streamMove: move=" + moveString);
	}

	@Override
	public void closeStream()
	{	
	} 

}
