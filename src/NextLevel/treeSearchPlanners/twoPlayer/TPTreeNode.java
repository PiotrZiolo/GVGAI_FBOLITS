package NextLevel.treeSearchPlanners.twoPlayer;

public class TPTreeNode
{
	public TPTreeNode parent;
	public TPTreeNode[] children;

	public int depth;
	public double totalValue;
	public int numVisits;

	public boolean isNotFullyExpanded()
	{
		for (TPTreeNode child : children)
		{
			if (child == null)
			{
				return true;
			}
		}
		return false;
	}
}
