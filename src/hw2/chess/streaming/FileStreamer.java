package hw2.chess.streaming;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import hw2.chess.game.Game;
import hw2.chess.game.move.Move;

public class FileStreamer extends Streamer
{

	public FileStreamer(String filePath)
	{
		super(filePath);
	}

	@Override
	public void createStream()
	{
		if(this.getFilePath() != null)
		{
			File file = new File(this.getFilePath());
			if(file.exists())
			{
				file.delete();
			}
			try
			{
				file.createNewFile();
			} catch(IOException e)
			{
				System.err.println("ChessAgent.writeNewMove [ERROR]: unable to create file=" + this.getFilePath());
				e.printStackTrace();
			}
		}

	}

	@Override
	public void streamMove(Move move, Game game)
	{
		if(this.getFilePath() != null)
		{
			String moveString = this.getAlgebraicMoveString(move, game);
			try
			{
				FileWriter writer = new FileWriter(this.getFilePath(), true);
				writer.write(moveString + "\n");
				writer.close();

			} catch(IOException e)
			{
				System.err.println("ChessAgent.writeNewMove [ERROR]: unable to write to file=" + this.getFilePath());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void closeStream()
	{
	}

}
