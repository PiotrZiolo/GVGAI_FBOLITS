package NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner;

import NextLevel.treeSearchPlanners.twoPlayer.TPTreeNode;

public class TPOLMCTSTreeNode extends TPTreeNode
{
	public int actionLeadingToThisNode;

	public TPOLMCTSTreeNode(int numOfActions)
	{
		this.parent = null;
		this.totalValue = 0.0;
		this.numVisits = 0;
		this.actionLeadingToThisNode = -1;
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
