package FBOLITS.treeSearchPlanners;

public class TreeNode
{
	public TreeNode parent;
	public TreeNode[] children;

	public int depth;
	public double totalValue;
	public int numVisits;

	public TreeNode()
	{
		this.parent = null;
		this.children = null;
		this.depth = 0;
		this.totalValue = 0.0;
		this.numVisits = 0;
	}
	
	public TreeNode(int numOfActions)
	{
		this.parent = null;
		this.children = new TreeNode[numOfActions];
		this.depth = 0;
		this.totalValue = 0.0;
		this.numVisits = 0;
	}
	
	public TreeNode(TreeNode parent, int numOfActions)
	{
		this.parent = parent;
		this.children = new TreeNode[numOfActions];
		if (parent != null)
			depth = parent.depth + 1;
		else
			depth = 0;
		this.totalValue = 0.0;
		this.numVisits = 0;
		children = new TreeNode[numOfActions];
	}
	
	public boolean isNotFullyExpanded()
	{
		for (TreeNode child : children)
		{
			if (child == null)
			{
				return true;
			}
		}
		return false;
	}
}
