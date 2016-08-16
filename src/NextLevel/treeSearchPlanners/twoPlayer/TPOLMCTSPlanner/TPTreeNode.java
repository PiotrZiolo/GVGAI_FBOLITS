package NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner;

public class TPTreeNode
{
	public TPTreeNode parent;
	public TPTreeNode[] children;

	public int depth;
	public double totalValue;
	public int numVisits;

	public boolean isNotFullyExpanded()
	{
		for (TPTreeNode tn : children)
		{
			if (tn == null)
			{
				return true;
			}
		}
		return false;
	}
}
