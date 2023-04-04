package hw2.agents.moveorder;

import java.util.LinkedList;
import java.util.List;

import hw2.chess.game.move.Move;
import hw2.chess.game.piece.Piece;
import hw2.chess.game.piece.PieceType;
import hw2.chess.search.DFSTreeNode;

public class CustomMoveOrderer
{

	/**
	 * TODO: implement me!
	 * This method should perform move ordering. Remember, move ordering is how alpha-beta pruning gets part of its power from.
	 * You want to see nodes which are beneficial FIRST so you can prune as much as possible during the search (i.e. be faster)
	 * @param nodes. The nodes to order (these are children of a DFSTreeNode) that we are about to consider in the search.
	 * @return The ordered nodes.
	 */
	public static List<DFSTreeNode> order(List<DFSTreeNode> nodes)
	{
		// please replace this!

		List<DFSTreeNode> captureNodes = new LinkedList<DFSTreeNode>();
		List<DFSTreeNode> promotePawnNodes = new LinkedList<DFSTreeNode>();
		List<DFSTreeNode> pawnNodes = new LinkedList<DFSTreeNode>();
		List<DFSTreeNode> movementNodes = new LinkedList<DFSTreeNode>();
		List<DFSTreeNode> otherNodes = new LinkedList<DFSTreeNode>();
		List<DFSTreeNode> castleNodes = new LinkedList<DFSTreeNode>();
		for (DFSTreeNode node : nodes) {
			Move move = node.getMove();
			if (move != null) {
				switch (move.getType()) {
					case CASTLEMOVE:
						castleNodes.add(node);
						break;
					case CAPTUREMOVE:
						captureNodes.add(node);
						break;
					case PROMOTEPAWNMOVE:
						promotePawnNodes.add(node);
						break;
					case MOVEMENTMOVE:
						int pieceID = move.getActorPieceID();
						Piece piece = node.getGame().getBoard().getPiece(move.getActorPlayer(),pieceID);
						if (piece.getType() == PieceType.PAWN) {
							pawnNodes.add(node);
						} else {
							movementNodes.add(node);
						}
						break;
					default:
						otherNodes.add(node);
						break;
				}
			} else {
				otherNodes.add(node);
			}
		}
		captureNodes.addAll(promotePawnNodes);
		captureNodes.addAll(pawnNodes);
		captureNodes.addAll(movementNodes);
		captureNodes.addAll(otherNodes);
		castleNodes.addAll(captureNodes);
		return castleNodes;
	}

}
