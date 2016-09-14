package FBOLITS.treeSearchPlanners.OLMCTSPlanner;

import FBOLITS.treeSearchPlanners.TreeNode;

public class OLMCTSTreeNode extends TreeNode
{
	public int actionLeadingToThisNode;

	public OLMCTSTreeNode(int numOfActions)
	{
		this.parent = null;
		this.totalValue = 0.0;
		this.numVisits = 0;
		this.actionLeadingToThisNode = -1;
		this.depth = 0;
		this.children = new OLMCTSTreeNode[numOfActions];
	}
	
	public OLMCTSTreeNode(OLMCTSTreeNode parent, int numOfActions, int actionLeadingToThisNode)
	{
		this.parent = parent;
		this.totalValue = 0.0;
		this.numVisits = 0;
		this.actionLeadingToThisNode = actionLeadingToThisNode;
		if (parent != null)
			this.depth = parent.depth + 1;
		else
			this.depth = 0;
		this.children = new OLMCTSTreeNode[numOfActions];
	}
}
