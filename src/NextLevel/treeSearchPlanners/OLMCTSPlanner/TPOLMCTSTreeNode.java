package NextLevel.treeSearchPlanners.OLMCTSPlanner;

import NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner.TPTreeNode;
import core.game.StateObservationMulti;

public class TPOLMCTSTreeNode extends TPTreeNode
{
	public TPOLMCTSTreeNode parent;
	public TPOLMCTSTreeNode[] children;

	public int actionLeadingToThisNode;

	public TPOLMCTSTreeNode(int numOfActions)
	{
		this.parent = parent;
		this.totalValue = 0.0;
		this.numVisits = 0;
		this.actionLeadingToThisNode = actionLeadingToThisNode;
		if (parent != null)
			depth = parent.depth + 1;
		else
			depth = 0;
		children = new TPOLMCTSTreeNode[numOfActions];
	}
	
	public TPOLMCTSTreeNode(TPOLMCTSTreeNode parent, int actionLeadingToThisNode, int numOfActions)
	{
		this.parent = parent;
		this.totalValue = 0.0;
		this.numVisits = 0;
		this.actionLeadingToThisNode = actionLeadingToThisNode;
		if (parent != null)
			depth = parent.depth + 1;
		else
			depth = 0;
		children = new TPOLMCTSTreeNode[numOfActions];
	}
}
