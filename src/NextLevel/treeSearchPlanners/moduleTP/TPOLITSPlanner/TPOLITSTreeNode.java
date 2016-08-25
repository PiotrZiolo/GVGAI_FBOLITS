package NextLevel.treeSearchPlanners.moduleTP.TPOLITSPlanner;

import java.util.ArrayList;

import NextLevel.PointOfInterest;
import NextLevel.treeSearchPlanners.TreeNode;
import core.game.StateObservationMulti;
import ontology.Types;

public class TPOLITSTreeNode extends TreeNode
{
	public StateObservationMulti stateObs;
	public PointOfInterest poi;
	public boolean poiApproached;
	public boolean rebuildTree;
	
	public ArrayList<Types.ACTIONS> path;
	
	public TPOLITSTreeNode(int numOfActions)
	{
		this.parent = null;
		this.totalValue = 0.0;
		this.numVisits = 0;
		this.depth = 0;
		this.children = new TPOLITSTreeNode[numOfActions];
		this.poiApproached = false;
		this.rebuildTree = true;
		this.path = null;
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
		this.path = null;
	}
	
	public TPOLITSTreeNode(int numOfActions, PointOfInterest poi)
	{
		this.parent = null;
		this.totalValue = 0.0;
		this.numVisits = 0;
		this.depth = 0;
		this.children = new TPOLITSTreeNode[numOfActions];
		this.poi = poi;
		this.poiApproached = false;
		this.rebuildTree = true;
		this.path = null;
	}
	
	public double getValue()
	{
		return this.totalValue / this.numVisits;
	}
}
