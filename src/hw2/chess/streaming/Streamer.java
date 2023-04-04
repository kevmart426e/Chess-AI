package hw2.chess.streaming;

import hw2.chess.game.Game;
import hw2.chess.game.move.CaptureMove;
import hw2.chess.game.move.CastleMove;
import hw2.chess.game.move.Move;
import hw2.chess.game.move.MovementMove;
import hw2.chess.game.move.PromotePawnMove;
import hw2.chess.game.piece.Piece;
import hw2.chess.utils.Coordinate;
import hw2.chess.utils.Distance;

public abstract class Streamer extends Object
{
	private final String filePath;

	private static Streamer singletonInstance = null;

	protected Streamer(String filePath)
	{
		this.filePath = filePath;
	}

	public final String getFilePath() { return this.filePath; }

	public abstract void createStream();
	public abstract void streamMove(Move move, Game game);
	public abstract void closeStream();

	protected String getAlgebraicMoveString(Move move, Game game)
	{
		String algebraicMove = null;

		// get the piece
		Piece actorPiece = game.getPiece(move.getActorPlayer(), move.getActorPieceID());
		Coordinate actorPosition = null;
		Coordinate tgtPosition = null;
		switch(move.getType())
		{
		case MOVEMENTMOVE:
			MovementMove movementMove = (MovementMove)move;
			actorPosition = game.getCurrentPosition(actorPiece);
			tgtPosition = movementMove.getTargetPosition();
			algebraicMove = move.getActorPlayer().getAlgebraicSymbol() + actorPiece.getAlgebraicSymbol()
				+ actorPosition.getXPosition() + actorPosition.getYPosition() + "->" + tgtPosition.getXPosition() + tgtPosition.getYPosition();
			break;
		case CAPTUREMOVE:
			CaptureMove captureMove = (CaptureMove)move;
			actorPosition = game.getCurrentPosition(actorPiece);
			tgtPosition = game.getCurrentPosition(captureMove.getTargetPlayer(), captureMove.getTargetPieceID());
//			System.out.println("ChessAgent.getAlgebraicMoveString [INFO] move=" + captureMove + " actorPiece=" + actorPiece
//					+ " tgtPosition=" + tgtPosition);
			algebraicMove = captureMove.getActorPlayer().getAlgebraicSymbol() + actorPiece.getAlgebraicSymbol() + "x"
					+ actorPosition.getXPosition() + actorPosition.getYPosition() + "->" + tgtPosition.getXPosition() + tgtPosition.getYPosition();
			break;
		case CASTLEMOVE:
			CastleMove castleMove = (CastleMove)move;
			actorPosition = game.getCurrentPosition(actorPiece);
			tgtPosition = game.getCurrentPosition(castleMove.getRookPlayer(), castleMove.getRookPieceID());

			int xDist = Distance.xDist(actorPosition, tgtPosition);
			// if 4 -> queen side castle
			if(xDist == 4)
			{
				algebraicMove = move.getActorPlayer().getAlgebraicSymbol() + "O-O-O";
			} else
			{
				algebraicMove = move.getActorPlayer().getAlgebraicSymbol() + "O-O";
			}
			break;
		case PROMOTEPAWNMOVE:
			PromotePawnMove promoteMove = (PromotePawnMove)move;
			tgtPosition = game.getCurrentPosition(promoteMove.getPawnPlayer(), promoteMove.getPawnPieceID());
			algebraicMove = move.getActorPlayer().getAlgebraicSymbol() + Piece.getAlgebraicSymbol(promoteMove.getPromotedPieceType()) + "=" +
					tgtPosition.getXPosition() + tgtPosition.getYPosition();
			break;
		default:
			System.err.println("ChessAgent.recordAlgebraicMove [ERROR]: unsupported move type=" + move.getType());
			System.exit(-1);
			break;
		}

		return algebraicMove;
	}

	public static synchronized Streamer getStreamer(String filePath)
	{
		if(Streamer.singletonInstance == null)
		{
			synchronized(ConsoleStreamer.class)
			{
				if(Streamer.singletonInstance == null)
				{
					Streamer.singletonInstance = new ConsoleStreamer(filePath);
				}
			}
		}
		return Streamer.singletonInstance;
	}
}
