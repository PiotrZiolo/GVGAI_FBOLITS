package NextLevel.treeSearchPlanners.twoPlayer.TPOLITSPlanner;

import NextLevel.treeSearchPlanners.TreeNode;

public class TPOLITSTreeNode extends TreeNode
{
	public TPOLITSTreeNode(int numOfActions)
	{
		this.parent = null;
		this.totalValue = 0.0;
		this.numVisits = 0;
		this.depth = 0;
		this.children = new TPOLITSTreeNode[numOfActions];
	}
	
	public TPOLITSTreeNode(TPOLITSTreeNode parent, int numOfActions, int actionLeadingToThisNode)
	{
		this.parent = parent;
		this.totalValue = 0.0;
		this.numVisits = 0;
		if (parent != null)
			this.depth = parent.depth + 1;
		else
			this.depth = 0;
		this.children = new TPOLITSTreeNode[numOfActions];
	}
}
