package hw2.agents.heuristics;

import hw2.chess.game.Board;
import hw2.chess.game.move.CaptureMove;
import hw2.chess.game.move.Move;
import hw2.chess.game.move.MovementMove;
import hw2.chess.game.piece.*;
import hw2.chess.game.player.Player;
import hw2.chess.game.player.PlayerType;
import hw2.chess.search.DFSTreeNode;
import hw2.chess.utils.Coordinate;

import java.util.*;
import java.util.stream.Collectors;

public class CustomHeuristics
{

	/**
	 * TODO: implement me! The heuristics that I wrote are useful, but not very good for a good chessbot.
	 * Please use this class to add your heuristics here! I recommend taking a look at the ones I provided for you
	 * in DefaultHeuristics.java (which is in the same directory as this file)
	 */
	public static double getHeuristicValue(DFSTreeNode node)
	{

		Player p1 = getMaxPlayer(node);
		//System.out.println(p1);
		Player p2 = getMinPlayer(node);

		double p1Score = getPlayerScore(p1,node);
		double p2Score = getPlayerScore(p2,node);

		return p1Score - p2Score + (Integer.MAX_VALUE / 2);
	}
	private static Player getMaxPlayer(DFSTreeNode node){
		return node.getMaxPlayer();
	}
	private static Player getMinPlayer(DFSTreeNode node){
		return node.getGame().getOtherPlayer(getMaxPlayer(node));
	}
	private static double getPlayerScore(Player player,DFSTreeNode node){
		Set<Piece> pieces = node.getGame().getBoard().getPieces(player);

		double score = 0;

		score += getMaterialValueForPieces(pieces); // Material Score, the basic idea
		score += getPawnStructureScore(player,pieces,node); // Pawn Structure Score
		score += getPiecePositionScore(player,pieces,node); // Center Control Score
		score += getMobilityScore(player,node); // Mobility Score
		return score;
	}
	private static double getPiecePositionScore(Player player, Set<Piece> pieces, DFSTreeNode node){
		double score = 0.0;
		// first lets create a set of pawns based on pieces, for future simplification
		List<Set<Piece>> piecesByType = new ArrayList<>();
		for (PieceType type : PieceType.values()){ // King - Queen - Bishop - Knight - Rook - Pawn
			piecesByType.add(pieces.stream().filter(p -> p.getType().equals(type)).collect(Collectors.toSet()));
		}
		Set<Piece> notPawnsAndNotKing = pieces.stream().filter(p -> !p.getType().equals(PieceType.PAWN) && !p.getType().equals(PieceType.KING)).collect(Collectors.toSet());
		score += getQueenPositionScore(player,piecesByType.get(1),node);
		score += getRookPositionScore(player,piecesByType.get(4),node);
		score += getBishopPositionScore(player,piecesByType.get(2),node);
		score += getKnightPositionScore(player,piecesByType.get(3),node);
		score += getPawnPositionScore(player,piecesByType.get(5),node);
		score += getKingPositionScore(player,piecesByType.get(0).iterator().next(),notPawnsAndNotKing,node);

		return score;
	}
	private static double getPawnStructureScore(Player player, Set<Piece> pieces, DFSTreeNode node){
		double score = 0.0;
		// first lets create a set of pawns based on pieces, for future simplification

		List<Piece> pawns = pieces.stream().filter(p -> p.getType().equals(PieceType.PAWN)).collect(Collectors.toList()); // Praise the Java 8

		// Now let's see if we have any pawns at the same column, if so, deduct points
		Board board = node.getGame().getBoard();
		double doubledPawnsScore = 0;
		double isolatedPawnsScore = 0;
		double defenseScore = 0;
		for (Piece pawn : pawns){
			Coordinate pawnPos = pawn.getCurrentPosition(board);
			int pawnCol = pawnPos.getYPosition();
			int pawnRow = pawnPos.getXPosition();
			int currentColPawns = 0;
			boolean isolated = true;
			int pawnDefenders = 0;
			// check if there is a pawn in the same column, i.e. doubled pawns
			for (Piece otherPawn : pawns){
				Coordinate otherPawnPos = otherPawn.getCurrentPosition(board);
				int otherPawnCol = otherPawnPos.getYPosition();
				int otherPawnRow = otherPawnPos.getXPosition();
				if (pawnCol == otherPawnCol){
					currentColPawns++;
				}
				if ((Math.abs(pawnCol - otherPawnCol)) == 1){
					isolated = false;
				}
				if ((Math.abs(pawnCol - otherPawnCol)) == 1 && (Math.abs(pawnRow - otherPawnRow)) == 1){
					pawnDefenders++;
				}
			}

			isolatedPawnsScore = (isolated) ? isolatedPawnsScore * 1.2 - 0.5 : isolatedPawnsScore;
			doubledPawnsScore = (currentColPawns == 0) ? doubledPawnsScore : -0.5 * Math.pow(1.5,currentColPawns - 1);
			defenseScore += pawnDefenders == 2 ? 3 : pawnDefenders * 1;
		}
		score = doubledPawnsScore + isolatedPawnsScore + defenseScore;
		return score;
	}
	private static double getPawnPositionScore(Player player, Set<Piece> pieces, DFSTreeNode node){
		double[][] control = new double[][]{
				new double[]{0.0, 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, 0.0, 0.0},
				new double[]{0.0, 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, 0.0, 0.0},
				new double[]{0.0, 5.0,  5.0,  5.0,  5.0,  5.0,  5.0,  5.0, 5.0, 0.0},
				new double[]{0.0, 1.0,  1.0,  2.0,  4.0,  4.0,  2.0,  1.0, 1.0, 0.0},
				new double[]{0.0, 0.5,  0.5,  1.0,  2.5,  2.5,  1.0,  0.5, 0.5, 0.0},
				new double[]{0.0, 0.0,  0.0,  0.0,  2.0,  2.0,  0.0,  0.0, 0.0, 0.0},
				new double[]{0.0, 0.5, -0.5, -1.0,  0.0,  0.0, -1.0, -0.5, 0.5, 0.0},
				new double[]{0.0, 0.5,  1.0,  1.0, -2.0, -2.0,  1.0,  1.0, 0.5, 0.0},
				new double[]{0.0, 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, 0.0, 0.0},
				new double[]{0.0, 0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, 0.0, 0.0},
		};
		double score = 0;

		if (player.getPlayerType().equals(PlayerType.BLACK)){
			Collections.reverse(Arrays.asList(control));
		}
		for (Piece p : pieces){
			Coordinate pos = p.getCurrentPosition(node.getGame().getBoard());
			score += control[pos.getYPosition()][pos.getXPosition()] * 10;
		}

		return score;
	}
	private static double getKnightPositionScore(Player player,Set<Piece> pieces, DFSTreeNode node){
		double[][] control = new double[][]{
				new double[]{0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
				new double[]{0.0, -5.0, -4.0, -3.0, -3.0, -3.0, -3.0, -4.0, -5.0,  0.0},
				new double[]{0.0, -4.0, -2.0,  0.0,  0.0,  0.0,  0.0, -2.0, -4.0,  0.0},
				new double[]{0.0, -3.0,  0.0,  1.0,  1.5,  1.5,  1.0,  0.0, -3.0,  0.0},
				new double[]{0.0, -3.0,  0.5,  1.5,  2.0,  2.0,  1.5,  0.5, -3.0,  0.0},
				new double[]{0.0, -3.0,  0.0,  1.5,  2.0,  2.0,  1.5,  0.0, -3.0,  0.0},
				new double[]{0.0, -3.0,  0.5,  1.0,  1.5,  1.5,  1.0,  0.5, -3.0,  0.0},
				new double[]{0.0, -4.0, -2.0,  0.0,  0.5,  0.5,  0.0, -2.0, -4.0,  0.0},
				new double[]{0.0, -5.0, -4.0, -3.0, -3.0, -3.0, -3.0, -4.0, -5.0,  0.0},
				new double[]{0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0}
		};

		double score = 0;

		if (player.getPlayerType().equals(PlayerType.WHITE)){
			Collections.reverse(Arrays.asList(control));
		}
		for (Piece p : pieces){
			Coordinate pos = p.getCurrentPosition(node.getGame().getBoard());
			score += control[pos.getYPosition()][pos.getXPosition()] * 10;
		}

		return score;
	}
	private static double getBishopPositionScore(Player player,Set<Piece> pieces, DFSTreeNode node){
		double[][] control = new double[][]{
				new double[]{0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
				new double[]{0.0, -2.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -2.0,  0.0},
				new double[]{0.0, -1.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -1.0,  0.0},
				new double[]{0.0, -1.0,  0.0,  0.5,  1.0,  1.0,  0.5,  0.0, -1.0,  0.0},
				new double[]{0.0, -1.0,  0.5,  0.5,  1.0,  1.0,  0.5,  0.5, -1.0,  0.0},
				new double[]{0.0, -1.0,  0.0,  1.0,  1.0,  1.0,  1.0,  0.0, -1.0,  0.0},
				new double[]{0.0, -1.0,  1.0,  1.0,  1.0,  1.0,  1.0,  1.0, -1.0,  0.0},
				new double[]{0.0, -1.0,  0.5,  0.0,  0.0,  0.0,  0.0,  0.5, -1.0,  0.0},
				new double[]{0.0, -2.0, -1.0, -1.0, -1.0, -1.0, -1.0, -1.0, -2.0,  0.0},
				new double[]{0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0}
		};

		double score = 0;

		if (player.getPlayerType().equals(PlayerType.WHITE)){
			Collections.reverse(Arrays.asList(control));
		}
		for (Piece p : pieces){
			Coordinate pos = p.getCurrentPosition(node.getGame().getBoard());
			score += control[pos.getYPosition()][pos.getXPosition()] * 10;
		}
		return score;
	}
	private static double getRookPositionScore(Player player,Set<Piece> pieces,DFSTreeNode node){
		double[][] control = new double[][]{
				new double[]{0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
				new double[]{0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
				new double[]{0.0,  0.5,  1.0,  1.0,  1.0,  1.0,  1.0,  1.0,  0.5,  0.0},
				new double[]{0.0, -0.5,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -0.5,  0.0},
				new double[]{0.0, -0.5,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -0.5,  0.0},
				new double[]{0.0, -0.5,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -0.5,  0.0},
				new double[]{0.0, -0.5,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -0.5,  0.0},
				new double[]{0.0, -0.5,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -0.5,  0.0},
				new double[]{0.0,  0.0,  0.0,  0.0,  0.5,  0.5,  0.0,  0.0,  0.0,  0.0},
				new double[]{0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
		};

		double score = 0;

		if (player.getPlayerType().equals(PlayerType.WHITE)){
			Collections.reverse(Arrays.asList(control));
		}
		for (Piece p : pieces){
			Coordinate pos = p.getCurrentPosition(node.getGame().getBoard());
			score += control[pos.getYPosition()][pos.getXPosition()] * 10;
		}
		return score;
	}
	private static double getQueenPositionScore(Player player,Set<Piece> pieces,DFSTreeNode node){
		double[][] control = new double[][]{
				new double[]{0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
				new double[]{0.0, -2.0, -1.0, -1.0, -0.5, -0.5, -1.0, -1.0, -2.0,  0.0},
				new double[]{0.0, -1.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0, -1.0,  0.0},
				new double[]{0.0, -1.0,  0.0,  0.5,  0.5,  0.5,  0.5,  0.0, -1.0,  0.0},
				new double[]{0.0, -0.5,  0.0,  0.5,  0.5,  0.5,  0.5,  0.0, -0.5,  0.0},
				new double[]{0.0,  0.0,  0.0,  0.5,  0.5,  0.5,  0.5,  0.0,  0.0,  0.0},
				new double[]{0.0, -1.0,  0.5,  0.5,  0.5,  0.5,  0.5,  0.0, -1.0,  0.0},
				new double[]{0.0, -1.0,  0.0,  0.5,  0.0,  0.0,  0.0,  0.0, -1.0,  0.0},
				new double[]{0.0, -2.0, -1.0, -1.0, -0.5, -0.5, -1.0, -1.0, -2.0,  0.0},
				new double[]{0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0,  0.0},
		};

		double score = 0;

		if (player.getPlayerType().equals(PlayerType.WHITE)) {
			for (double[] row : control){
				Collections.reverse(Arrays.asList(row));
			}
			Collections.reverse(Arrays.asList(control));
		}
		for (Piece p : pieces){
			Coordinate pos = p.getCurrentPosition(node.getGame().getBoard());
			score += control[pos.getYPosition()][pos.getXPosition()] * 10;
		}
		return score;
	}
	private static double getKingPositionScore(Player player, Piece king, Set<Piece> otherPieces, DFSTreeNode node){
		double[][] control;

		// check if endgame
		int numRooks = 0;
		int numBishops = 0;
		int numKnights = 0;
		int numQueens = 0;
		for (Piece p : otherPieces){
			switch (p.getType()){
				case ROOK:
					numRooks++;
					break;
				case BISHOP:
					numBishops++;
					break;
				case KNIGHT:
					numKnights++;
					break;
				case QUEEN:
					numQueens++;
					break;
			}
		}
		if ((numRooks + numBishops + numKnights + numQueens) <= 3){
			control = new double[][]{
				new double[]{ 0,  0,  0,  0,  0,  0,  0,  0,  0, 0},
				new double[]{ 0, -5, -4, -3, -2, -2, -3, -4, -5, 0},
				new double[]{ 0, -3, -2, -1,  0,  0, -1, -2, -3, 0},
				new double[]{ 0, -3, -1,  2,  3,  3,  2, -1, -3, 0},
				new double[]{ 0, -3, -1,  3,  4,  4,  3, -1, -3, 0},
				new double[]{ 0, -3, -1,  3,  4,  4,  3, -1, -3, 0},
				new double[]{ 0, -3, -1,  2,  3,  3,  2, -1, -3, 0},
				new double[]{ 0, -3, -2,  0,  0,  0,  0, -2, -3, 0},
				new double[]{ 0, -5, -4, -3, -2, -2, -3, -4, -5, 0},
				new double[]{ 0,  0,  0,  0,  0,  0,  0,  0,  0, 0}
			};
		}else {
			control = new double[][]{
					new double[]{ 0,  0,  0,  0,  0,  0,  0,  0,  0, 0},
					new double[]{ 0, -3, -4, -4, -5, -5, -4, -4, -3, 0},
					new double[]{ 0, -3, -4, -4, -5, -5, -4, -4, -3, 0},
					new double[]{ 0, -3, -4, -4, -5, -5, -4, -4, -3, 0},
					new double[]{ 0, -3, -4, -4, -5, -5, -4, -4, -3, 0},
					new double[]{ 0, -2, -3, -3, -4, -4, -3, -3, -2, 0},
					new double[]{ 0, -1, -2, -2, -2, -2, -2, -2, -1, 0},
					new double[]{ 0,  2,  2,  0,  0,  0,  0,  2,  2, 0},
					new double[]{ 0,  2,  3,  1,  0,  0,  1,  3,  2, 0},
					new double[]{ 0,  0,  0,  0,  0,  0,  0,  0,  0, 0}
			};
		}
		double score = 0.0;

		if (player.getPlayerType().equals(PlayerType.WHITE)){
			Collections.reverse(Arrays.asList(control));
		}

		Coordinate pos = king.getCurrentPosition(node.getGame().getBoard());
		score += control[pos.getYPosition()][pos.getXPosition()] * 10;

		return score;
	}
	private static double getMobilityScore(Player player,DFSTreeNode node){
		return (node.getGame().getAllMoves(player).size());
	}

	private static double getMaterialValueForPieces(Set<Piece> pieces){
		double score = 0.0;

		for (Piece piece : pieces){
			switch (piece.getType()){
				case PAWN:
					score += 100;
					break;
				case KNIGHT:
					score += 320;
					break;
				case BISHOP:
					score += 330;
					break;
				case ROOK:
					score += 500;
					break;
				case QUEEN:
					score += 900;
					break;
				case KING:
					score += 20000;
					break;
			}
		}
		return score;
	}
}
