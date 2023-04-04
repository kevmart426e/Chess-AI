package hw2.agents.moveorder;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import hw2.chess.search.DFSTreeNode;
import hw2.chess.search.DFSTreeNodeType;

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
		//return DefaultMoveOrderer.order(nodes);
		return orderingMoves(nodes);
	}
	
	public static List<DFSTreeNode> orderingMoves(List<DFSTreeNode> children) {
		Collections.sort(children, new Comparator<DFSTreeNode>()
		{
			public int compare(DFSTreeNode node1, DFSTreeNode node2) {
		        double diff = node1.getMaxPlayerUtilityValue() - node2.getMaxPlayerUtilityValue();
		        if (diff < 0) {
		            return -1;
		        } else if (diff > 0) {
		            return 1;
		        } else {
		            return 0;
		        }
		    }
		}); 
        return children;
	}

}
