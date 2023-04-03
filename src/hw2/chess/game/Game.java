package hw2.chess.game;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.cwru.sepia.environment.model.state.State.StateView;
import hw2.chess.game.move.Move;
import hw2.chess.game.move.MoveType;
import hw2.chess.game.move.CaptureMove;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.player.Player;
import hw2.chess.game.player.PlayerType;
import hw2.chess.utils.Coordinate;


public class Game extends Object
{

	private Player blackPlayer;
	private Player whitePlayer;
	private Player currentPlayer;

	private long whiteTimeLeftInMS;
	private long blackTimeLeftInMS;

	private Board board;

	private Map<Player, Map<PieceType, Integer> > pieceTypeToTemplateIDs;

	private Game(StateView state, long whiteTimeLeftInMS, long blackTimeLeftInMS)
	{
		this.blackPlayer = null;
		this.whitePlayer = null;
		this.board = null; // Board.getInitialBoard(this.getBlackPlayer(), this.getWhitePlayer(), state);
		this.currentPlayer = this.getWhitePlayer();

		this.whiteTimeLeftInMS = whiteTimeLeftInMS;
		this.blackTimeLeftInMS = blackTimeLeftInMS;

		this.pieceTypeToTemplateIDs = new HashMap<Player, Map<PieceType, Integer> >(2);
	}

	private Game(Player blackPlayer, Player whitePlayer, Board board, Player currentPlayer,
			long whiteTimeLeftInMS, long blackTimeLeftInMS, Map<Player, Map<PieceType, Integer> > pieceTypeToTemplateIDs)
	{
		this.blackPlayer = blackPlayer;
		this.whitePlayer = whitePlayer;
		this.board = board;
		this.currentPlayer = currentPlayer;

		this.whiteTimeLeftInMS = whiteTimeLeftInMS;
		this.blackTimeLeftInMS = blackTimeLeftInMS;
		this.pieceTypeToTemplateIDs = pieceTypeToTemplateIDs;
	}

	public Player getWhitePlayer() { return this.whitePlayer; }
	public Player getBlackPlayer() { return this.blackPlayer; }
	public Player getPlayer(PlayerType type)
	{
		Player player = null;
		if(type == PlayerType.BLACK)
		{
			player = this.getBlackPlayer();
		} else
		{
			player = this.getWhitePlayer();
		}
		return player;
	}
	public Board getBoard() { return this.board; }
	public final Player getCurrentPlayer() { return this.currentPlayer; }
	private Map<Player, Map<PieceType, Integer> > getPieceType2TemplateIDs() { return this.pieceTypeToTemplateIDs; }
	public synchronized Map<PieceType, Integer> getPieceType2TemplateIDs(Player player)
	{
		return this.getPieceType2TemplateIDs().get(player);
	}
	public Integer getUnitID(Player player, int pieceID) { return this.getBoard().getUnitID(player, pieceID); }

	public synchronized double getPointsEarned(Player player)
	{
		return this.getBoard().getPointsEarned(player);
	}

	public void setBoard(Board board) { this.board = board; }
	public void setCurrentPlayer(Player player) { this.currentPlayer = player; }

	public synchronized void registerPlayer(int playerID, PlayerType type, StateView state)
	{
		// System.out.println("Game.registerPlayer [INFO]: registering player=" + playerID + " to team=" + type);

		// System.out.println("Game.registerPlayer [INFO]: getting townhallID for player=" + playerID + " to team=" + type);

		// System.out.println("Game.registerPlayer [INFO]: found townhall with ID=" + townhallID + " for player=" + playerID);

		if(type == PlayerType.BLACK)
		{
			if(this.getBlackPlayer() == null)
			{
				this.blackPlayer = new Player(playerID, type);
			} else
			{
				System.err.println("Game.registerPlayer [ERROR]: multiple attempts to register player controlling black pieces");
				System.exit(-1);
			}

			// populate the templateIDs
			this.pieceTypeToTemplateIDs.put(this.getBlackPlayer(), new HashMap<PieceType, Integer>(Board.Constants.NUMPIECETYPES));
			for(Integer templateID : state.getTemplateIds(this.getBlackPlayer().getPlayerID())) // both sides should have the same template IDs
			{
				PieceType pieceType = PieceType.valueOf(state.getTemplate(templateID).getName().toUpperCase());

				// System.out.println("Game.registerPlayer [INFO]: found pieceType=" + pieceType + " for player=" + this.getBlackPlayer());

				this.getPieceType2TemplateIDs(this.getBlackPlayer()).put(pieceType, templateID);
			}
		}
			
		if(type == PlayerType.WHITE)
		{
			if(this.getWhitePlayer() == null)
			{
				this.whitePlayer = new Player(playerID, type);
			} else
			{
				System.err.println("Game.registerPlayer [ERROR]: multiple attempts to register player controlling white pieces");
				System.exit(-1);
			}

			// populate the templateIDs
			this.pieceTypeToTemplateIDs.put(this.getWhitePlayer(), new HashMap<PieceType, Integer>(Board.Constants.NUMPIECETYPES));
			for(Integer templateID : state.getTemplateIds(this.getWhitePlayer().getPlayerID())) // both sides should have the same template IDs
			{
				PieceType pieceType = PieceType.valueOf(state.getTemplate(templateID).getName().toUpperCase());

				// System.out.println("Game.registerPlayer [INFO]: found pieceType=" + pieceType + " for player=" + this.getWhitePlayer());

				this.getPieceType2TemplateIDs(this.getWhitePlayer()).put(pieceType, templateID);
			}
		}

		if(this.getWhitePlayer() != null && this.getBlackPlayer() != null)
		{
			// can intialize the board
			this.setBoard(Board.getInitialBoard(this.getBlackPlayer(), this.getWhitePlayer(), state));
			this.setCurrentPlayer(this.getWhitePlayer());
		}
	}

	public synchronized long getTimeLeftInMS(Player player)
	{
		long timeLeftInMS;
		if(player.equals(this.getBlackPlayer()))
		{
			timeLeftInMS = this.blackTimeLeftInMS;
		} else
		{
			timeLeftInMS = this.whiteTimeLeftInMS;
		}
		return timeLeftInMS;
	}

	public synchronized void removeTimeFromPlayer(Player p, long timeToRemoveInMS)
	{
		if(p.equals(this.getBlackPlayer()))
		{
			this.blackTimeLeftInMS -= timeToRemoveInMS;
		} else
		{
			this.whiteTimeLeftInMS -= timeToRemoveInMS;
		}
	}

	public Player getOtherPlayer(Player player)
	{
		Player otherPlayer = this.getBlackPlayer();

		if(player.equals(this.getBlackPlayer()))
		{
			otherPlayer = this.getWhitePlayer();
		}
		return otherPlayer;
	}

	public Player getOtherPlayer()
	{
		return this.getOtherPlayer(this.getCurrentPlayer());
	}

	public List<Move> getAllMoves(Player player)
	{
		List<Move> allMoves = new LinkedList<Move>();

		// boolean isPlayerInCheck = this.isInCheck(player);

		Set<Piece> playerPieces = this.getBoard().getPieces(player);
		// System.out.println("Game.getAllMoves [INFO]: board.getPieceType2Pieces(player)=" + playerPieces);
		if(playerPieces != null)
		{
			for(Piece piece : playerPieces)
			{
				// System.out.println("Game.getAllMoves [INFO]: getting moves for piece=" + piece);
//				if(isPlayerInCheck)
//				{
//					// if the player is in check, the only allowable moves are moves which move the player out of check
//					for(Move move: piece.getAllMoves(this))
//					{
//						if(!this.applyMove(move).isInCheck(player))
//						{
//							allMoves.add(move);
//						}
//					}
//				} else
//				{
//					// if the player is not in check, cannot play a move that will put them in check
//					for(Move move : piece.getAllMoves(this))
//					{
//						if(!this.applyMove(move).isInCheck(player))
//						{
//							allMoves.add(move);
//						}
//					}
//				}

				// player can only make moves where they are not in check after the move resolves
				for(Move move : piece.getAllMoves(this))
				{
					if(!this.applyMove(move).isInCheck(player))
					{
						// System.out.println("Game.getAllMoves [INFO]: viable move=" + move);
						allMoves.add(move);
					}
				}
				// System.out.println("Game.getAllMoves [INFO]: got moves for piece=" + piece);
			}
		}

		return allMoves;
	}

	public List<Move> getAllMovesForPiece(Player player, Piece piece)
	{
		return piece.getAllMoves(this);
	}

	public List<Move> getAllMovesForPieceType(Player player, PieceType pieceType)
	{
		// System.out.println("Game.getAllMovesForPieceType [INFO]: player=" + player);
		List<Move> allMoves = new LinkedList<Move>();

		Set<Piece> playerPieces = this.getBoard().getPieces(player, pieceType);
		// System.out.println("Game.getAllMovesForPieceType [INFO]: board.getPieceType2Pieces(player)=" + playerPieces);
		if(playerPieces != null)
		{
			for(Piece piece : playerPieces)
			{
				// System.out.println("Game.getAllMovesForPieceType [INFO]: getting moves for piece=" + piece);
				allMoves.addAll(piece.getAllMoves(this));
			}
		}

		return allMoves;
	}

	public List<Move> getAllCaptureMoves(Player player, List<Move> allPlayerMoves)
	{
		List<Move> allCaptureMoves = new LinkedList<Move>();

		if(allPlayerMoves == null)
		{
			allPlayerMoves = this.getAllMoves(player);
		}

		for(Move move : allPlayerMoves)
		{
			if(move.getType() == MoveType.CAPTUREMOVE)
			{
				allCaptureMoves.add(move);
			}
		}

		return allCaptureMoves;
	}

	public List<Move> getAllCaptureMoves(Player player)
	{
		return this.getAllCaptureMoves(player, this.getAllMoves(player));
	}

	public Coordinate getCurrentPosition(Player player, int pieceID) { return this.getBoard().getPiecePosition(player, pieceID); }
	public Coordinate getCurrentPosition(Piece piece) { return this.getCurrentPosition(piece.getPlayer(), piece.getPieceID()); }
	public Piece getPiece(Player player, int pieceID) { return this.getBoard().getPiece(player, pieceID); }
	public int getNumberOfAlivePieces(Player player, PieceType pieceType) { return this.getBoard().getNumberOfAlivePieces(player, pieceType); }
	public Set<Integer> getUnitIDs(Player player, PieceType pieceType) { return this.getBoard().getUnitIDs(player, pieceType); }

	public synchronized boolean isInCheck(Player player)
	{

		Piece ourKing = this.getBoard().getPieces(player, PieceType.KING).iterator().next(); // will always have a piece

		// get all CaptureMoves from the opposite player
//		for(Move move : this.getAllCaptureMoves(this.getOtherPlayer(player)))
//		{
//			Coordinate tgtPosition = this.getCurrentPosition(((CaptureMove)move).getTargetPlayer(),
//					((CaptureMove)move).getTargetPieceID());
//			if(tgtPosition.equals(ourKing))
//			{
//				return true;
//			}
//		}

		// see if any enemy piece can capture our king
		for(Piece enemyPiece : this.getBoard().getPieces(this.getOtherPlayer(player)))
		{
			for(Move captureMove : enemyPiece.getAllCaptureMoves(this))
			{
				if(((CaptureMove)captureMove).getTargetPieceID() == ourKing.getPieceID() &&
						((CaptureMove)captureMove).getTargetPlayer() == player)
				{
					return true;
				}
			}
		}
		return false;
	}

	public synchronized boolean isInCheckmate()
	{
		// is the current player in checkmate?
		boolean checkmated = true;

		// a player is in checkmate IF for every move the current player can make, they are still in check
		for(Move action : this.getAllMoves(this.getCurrentPlayer()))
		{
			checkmated = checkmated && this.applyMove(action).isInCheck(this.getCurrentPlayer());
		}

		return checkmated;
	}

	public synchronized boolean isInStalemate()
	{
		return (this.getAllMoves(this.getCurrentPlayer()).size() == 0 && !this.isInCheck(this.getCurrentPlayer())) ||
				(this.getBoard().getPieces(this.getBlackPlayer()).size() == 1 && this.getBoard().getPieces(this.getWhitePlayer()).size() == 1);
	}

	public synchronized boolean outOfTime()
	{
		return this.blackTimeLeftInMS <= 0 || this.whiteTimeLeftInMS <= 0;
	}

	public boolean isTerminal() { return this.isInCheckmate() || this.isInStalemate() || this.outOfTime(); }

	public Player getNextPlayer()
	{
		Player nextPlayer = this.getBlackPlayer();
		if(this.getCurrentPlayer().equals(this.getBlackPlayer()))
		{
			nextPlayer = this.getWhitePlayer();
		}
		return nextPlayer;
	}

	public Game applyMove(Move action)
	{
		return new Game(this.getBlackPlayer(), this.getWhitePlayer(), this.getBoard().applyMove(action), this.getOtherPlayer(),
				this.getTimeLeftInMS(this.getWhitePlayer()), this.getTimeLeftInMS(this.getWhitePlayer()), this.getPieceType2TemplateIDs());
	}

	public Game copy()
	{
		return new Game(this.getBlackPlayer(), this.getWhitePlayer(), this.getBoard().copy(), this.getCurrentPlayer(),
				this.getTimeLeftInMS(this.getWhitePlayer()), this.getTimeLeftInMS(this.getBlackPlayer()), this.getPieceType2TemplateIDs());
	}

	public static Game makeNewGame(StateView state, long timeForEachPlayerInMS)
	{
		return new Game(state, timeForEachPlayerInMS, timeForEachPlayerInMS);
	}

//	public static synchronized Game getGame(StateView state, double timeForEachPlayerInMS)
//	{
//		if(Game.singletonInstance == null)
//		{
//			synchronized(Game.class)
//			{
//				if(Game.singletonInstance == null)
//				{
//					Game.singletonInstance = new Game(state, timeForEachPlayerInMS, timeForEachPlayerInMS);
//				}
//			}
//		}
//		return Game.singletonInstance;
//	}

}
