package hw2.agents.heuristics;

import java.util.List;
import java.util.Set;

import hw2.chess.game.Board;
import hw2.chess.game.Game;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.game.planning.Planner;
import hw2.chess.game.player.Player;
import hw2.chess.game.player.PlayerType;
import hw2.chess.search.DFSTreeNode;
import hw2.chess.search.DFSTreeNodeType;
import hw2.chess.utils.Coordinate;
import hw2.chess.game.move.CaptureMove;
import hw2.chess.game.move.Move;
import hw2.chess.game.move.MoveType;
import hw2.chess.game.move.PromotePawnMove;


public class CustomHeuristics
{
	// Weights for chess pieces
	public static final int PAWN_WEIGHT = 1;
	public static final int KNIGHT_WEIGHT = 3;
	public static final int BISHOP_WEIGHT = 3;
	public static final int ROOK_WEIGHT = 5;
	public static final int QUEEN_WEIGHT = 9;

	/**
	 * TODO: implement me! The heuristics that I wrote are useful, but not very good for a good chessbot.
	 * Please use this class to add your heuristics here! I recommend taking a look at the ones I provided for you
	 * in DefaultHeuristics.java (which is in the same directory as this file)
	 */
	public static double getHeuristicValue(DFSTreeNode node)
	{
		// please replace this!
//		double testMaterial = BoardHeuristics.calculateMaterialAdvantage(node);
//		double testAttackingVal = newOffensiveHeuristics.getValueOfAttackingPieces(node);
//		
//		return DefaultHeuristics.getHeuristicValue(node) + testMaterial + testAttackingVal;	
//		return DefaultHeuristics.getMaxPlayerHeuristicValue(node);
		double OffensiveValue = getNewOffensiveHeuristicValue(node);
		double DefensiveValue = getNewDefensiveHeuristics(node);
		double BoardpositionValue = getBoardHeuristicValue(node);
		return Math.max((OffensiveValue + DefensiveValue + BoardpositionValue), 0);
	}
	
	
	public static class BoardHeuristics extends Object {
		//Start of BoardHeuristic Class
		
		public static double calculateMaterialAdvantage(DFSTreeNode node) {
			Player maxPlayer = DefaultHeuristics.getMaxPlayer(node);
			Player minPlayer = DefaultHeuristics.getMinPlayer(node);
			Game game = node.getGame();
			return Math.max(getPieceAdvantage(maxPlayer,game) - getPieceAdvantage(minPlayer, game),0);
		}
		
		
		
		public static double getPieceAdvantage(Player player, Game game) {
			//Returns the calculation for calculateMaterialAdvantage method
			int pieceScore = 0;
			for(PieceType pieceType : PieceType.values())
			{
				pieceScore += (game.getNumberOfAlivePieces(player, pieceType)) * Piece.getPointValue(pieceType);
			}
			return pieceScore;
		}
		
		
		public static double calculateMobilityScore(Game game, Player player) {
			//Returns the mobility score
			double mobilityVal = 0;
			for(Piece piece : game.getBoard().getPieces(player)) {
				mobilityVal += game.getAllMovesForPiece(player, piece).size();
			}
			
			return mobilityVal;
		}
		
		public static double getMobilityScore(DFSTreeNode node) {
			// Gets the mobility score which is the max mobility minus the min mobility score
			Game game = node.getGame();
			Player maxPlayer = DefaultHeuristics.getMaxPlayer(node);
			Player minPlayer = DefaultHeuristics.getMinPlayer(node);
			return Math.max((calculateMobilityScore(game, maxPlayer) - calculateMobilityScore(game, minPlayer)), 0);
		}
		
		public static double getPointsEarnedScore(DFSTreeNode node) {
			Game game = node.getGame();
			Player maxPlayer = DefaultHeuristics.getMaxPlayer(node);
			Player minPlayer = DefaultHeuristics.getMinPlayer(node);
			return Math.max((game.getBoard().getPointsEarned(maxPlayer) - game.getBoard().getPointsEarned(minPlayer)), 0);
		}
		
		
		public static boolean isInCenter(Board board, Piece piece) {
			Coordinate points = board.getPiecePosition(piece);
			int X = points.getXPosition();
			int Y = points.getYPosition();
			return ((X == 3 || X == 4) && (Y == 3 || Y == 4) );	
		}
		
		public static double getcenterBoardScore(DFSTreeNode node) {
			double val = 0;
			Game game = node.getGame();
			Player maxPlayer = DefaultHeuristics.getMaxPlayer(node);
			for(Piece piece : game.getBoard().getPieces(maxPlayer)) {
				if (isInCenter(game.getBoard(), piece)) {
					val += 1;
				}
			}
			return val;
		}
		

	} //End of BoardHeuristic Class
	
	
	
	public static class newOffensiveHeuristics extends Object {
		//Class for calculating offensive Heuristic weight
		
		
		public static double getNumberOfPiecesPlayerIsThreateningScore(Game currentGame, Player player)
		// helper for getValueOfAttackingPieces method, returns the number of attacking pieces value
		{
			int attackScore = 0;
			for(Piece piece : currentGame.getBoard().getPieces(player))
			{
				attackScore += piece.getAllCaptureMoves(currentGame).size() * captureMoveScore(currentGame.getBoard(), piece, piece.getAllCaptureMoves(currentGame));
			}
			return attackScore;
		}
		
		
		public static double captureMoveScore(Board board, Piece currentPiece, List<Move> pieceMoves) {
			/* Takes current board being simulated on, chess piece, and simulated moves, and return the score based on how 
			 * valuable the pieces that can be captured are
			 */
			double score = 0; // return value
			int simulatedPlayersPieceID = currentPiece.getPieceID();
			for(Move move : pieceMoves)
			{
				Player simulatedPlayer = move.getActorPlayer();
				Piece pieceThatWasCaptured = board.getPieceAtPosition(board.getPiecePosition(simulatedPlayer, simulatedPlayersPieceID));
				score += Piece.getPointValue(pieceThatWasCaptured.getType()); //Piece.getPointValue(currentPiece.getType());
			}
			return score;
		}
		
		
		public static double getValueOfAttackingPieces(DFSTreeNode node)
		// returns the calculation for number of pieces that can be threaten
		{
			Game currentGame = node.getGame();
			Player player = DefaultHeuristics.getMaxPlayer(node);
			Player enemyPlayer = DefaultHeuristics.getMinPlayer(node);	
			return Math.max((getNumberOfPiecesPlayerIsThreateningScore(currentGame, player) 
					- getNumberOfPiecesPlayerIsThreateningScore(currentGame, enemyPlayer)), 0);
		}
		
		
		public static boolean isPieceForkable(Piece piece, Game game) {
			// return True if piece has more than one option to attack pieces
			return piece.getAllCaptureMoves(game).size() > 1;
		}
		
		public static double pieceForkableScore(Board board, Piece currentPiece, List<Move> pieceMoves) {
			// return fork value, where one piece attacks two or more of the opponent's pieces at the same time 
			double score = 0; 
			int simulatedPlayersPieceID = currentPiece.getPieceID();
			for(Move move : pieceMoves)
			{
				Player simulatedPlayer = move.getActorPlayer();
				Piece pieceThatWasCaptured = board.getPieceAtPosition(board.getPiecePosition(simulatedPlayer, simulatedPlayersPieceID));
				if (pieceThatWasCaptured.getType() != currentPiece.getType()) {
					score += Piece.getPointValue(pieceThatWasCaptured.getType());
				}
			}
			return score;
		}
		
		public static double getValueOfPieceFork(DFSTreeNode node) {
			double score = 0;
			Board board = node.getGame().getBoard();
			for(Piece piece : board.getPieces(DefaultHeuristics.getMaxPlayer(node))) {
				if (isPieceForkable(piece, node.getGame())) {
					score += pieceForkableScore(board, piece, piece.getAllCaptureMoves(node.getGame()));
				}
			}
			return score;
		}
		
		
	} // End of newOffensiveHeuristics class
	
	
	
	public static class newDefensiveHeuristics extends Object {
		// Does the calculation of Defensive Heuristic weight
		
		
		public static double getKingInDangerScore(Player player, Game currentGame) {
			// returns sum of the value of pieces threating player's king
			double score = 0;
			Piece ourKing = currentGame.getBoard().getPieces(player, PieceType.KING).iterator().next();
			Board board = currentGame.getBoard();
			for(Piece enemyPiece : currentGame.getBoard().getPieces(currentGame.getOtherPlayer(player)))
			{
				for(Move captureMove : enemyPiece.getAllCaptureMoves(currentGame))
				{
					if(((CaptureMove)captureMove).getTargetPieceID() == ourKing.getPieceID() &&
							((CaptureMove)captureMove).getTargetPlayer() == player)
					{
						int pieceThatCaptureKingID = ((CaptureMove)captureMove).getAttackingPieceID();
						Piece pieceThatCaptureKing = board.getPieceAtPosition(board.getPiecePosition(currentGame.getOtherPlayer(player), pieceThatCaptureKingID));
						score += Piece.getPointValue(pieceThatCaptureKing.getType());
					}
				}
			}
			return score;
		}
		
		
		public static double getKingDangerScore(DFSTreeNode node) {
			Player player = node.getMaxPlayer();
			Player enemyPlayer = DefaultHeuristics.getMinPlayer(node);
			Game game = node.getGame();
			return Math.max((getKingInDangerScore(enemyPlayer, game) - getKingInDangerScore(player, game)), 0);
			
		}
		
		
		public static double getRatioOfNumberOfPiecesAttcking(DFSTreeNode node) {
			//return number of max player pieces attacking - number of max player pieces (want a positive ratio)
			return Math.max((DefaultHeuristics.OffensiveHeuristics.getNumberOfPiecesMaxPlayerIsThreatening(node) -
					DefaultHeuristics.DefensiveHeuristics.getNumberOfPiecesThreateningMaxPlayer(node)), 0);
		}
		
		
		
		
		
		
		
//		public static double CastleScore(DFSTreeNode node) {
//			double score = 0;
//			Game currentGame = node.getGame();
//			Player player = currentGame.getCurrentPlayer();
//			Set<Piece> pieces = currentGame.getBoard().getPieces(player, PieceType.ROOK);
//			pieces.addAll(currentGame.getBoard().getPieces(player, PieceType.KING));
//			for(Piece piece : pieces)
//			{
//				for (Move move : currentGame.getAllMovesForPiece(player, piece)) {
//					if (move.getType() == MoveType.CASTLEMOVE) {
//						score += 1;
//					}
//				}
//			}
//			return score;
//		}
//		
//		public static double enemyCastleScore(DFSTreeNode node) {
//			double score = 0;
//			Game currentGame = node.getGame();
//			Player player = currentGame.getOtherPlayer();
//			Set<Piece> pieces = currentGame.getBoard().getPieces(player, PieceType.ROOK);
//			pieces.addAll(currentGame.getBoard().getPieces(player, PieceType.KING));
//			for(Piece piece : pieces)
//			{
//				for (Move move : currentGame.getAllMovesForPiece(player, piece)) {
//					if (move.getType() == MoveType.CASTLEMOVE) {
//						score -= 1;
//					}
//				}
//			}
//			return score;
//		}
//		
//		public static double getCastleScore(DFSTreeNode node) {
//			return Math.max(CastleScore(node) + enemyCastleScore(node), 0);
//			
//		}
		
		
		
	} // End of newDefensiveHeuristics class
	
	
	public static double getNewOffensiveHeuristicValue(DFSTreeNode node) {
		// Get function that calls all methods in NewOffensiveHeuristic Class	
		double damageDealtInThisNode = node.getGame().getBoard().getPointsEarned(DefaultHeuristics.getMaxPlayer(node));
		double damageDealtInOtherNode = node.getGame().getBoard().getPointsEarned(DefaultHeuristics.getMinPlayer(node));

		switch(node.getMove().getType())
		{
		case PROMOTEPAWNMOVE:
			PromotePawnMove promoteMove = (PromotePawnMove)node.getMove();
			damageDealtInThisNode += Piece.getPointValue(promoteMove.getPromotedPieceType());
			break;
		default:
			break;
		}
		
		damageDealtInThisNode = Math.max((damageDealtInThisNode - damageDealtInOtherNode), 0);
		double offenseScore = newOffensiveHeuristics.getValueOfAttackingPieces(node) + newOffensiveHeuristics.getValueOfPieceFork(node);
		return offenseScore + damageDealtInThisNode;
	}
	
	public static double getNewDefensiveHeuristics(DFSTreeNode node) {
		// Get function that calls all methods in NewDefensiveHeuristics Class
		return DefaultHeuristics.DefensiveHeuristics.getClampedPieceValueTotalSurroundingMaxPlayersKing(node) + 
				newDefensiveHeuristics.getKingDangerScore(node) + DefaultHeuristics.DefensiveHeuristics.getNumberOfMaxPlayersAlivePieces(node) + 
				newDefensiveHeuristics.getRatioOfNumberOfPiecesAttcking(node);
	}
	
	public static double getBoardHeuristicValue(DFSTreeNode node) {
		// Get function that calls all methods in BoardHeuristic Class
		return BoardHeuristics.calculateMaterialAdvantage(node) + BoardHeuristics.getMobilityScore(node) +
				DefaultHeuristics.getNonlinearPieceCombinationMaxPlayerHeuristicValue(node) + 
				BoardHeuristics.getPointsEarnedScore(node) + BoardHeuristics.getcenterBoardScore(node);
	}
	
	
	
	
}
