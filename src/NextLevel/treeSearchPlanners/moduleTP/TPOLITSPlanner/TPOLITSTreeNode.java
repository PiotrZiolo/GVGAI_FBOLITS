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
	public boolean stateObsNearPOI;

	public ArrayList<Types.ACTIONS> path;

	// OLETS fields

	/**
	 * The expectimax value of a node (i.e. we back up the max value from its children nodes, plus the instant values
	 * observed when a simulation exited in this node, weighted proportionally to the number of exits
	 */
	public double expectimax;
	/**
	 * Action index of the action that was chosen immediately before landing in this node. Should be null for a root node
	 */
	public int actionIndex;
	/**
	 * Number of times a simulation passed through this node by calling the model (i.e. the advance method)
	 */
	public int nbGenerated;
	/**
	 * The bias given to a node according to the avatar location - a negative score bias is given if the location has
	 * been visited many times before
	 */
	public double tabooBias;
	/**
	 * Number of simulations that passed through this node AND ended here (game over or simulation over)
	 */
	public int nbExitsHere;
	/**
	 * Cumulated value of simulations that have exited in this node
	 */
	public double totalValueOnExit;
	/**
	 * The maximum expectimax value of this node's children
	 */
	public double childrenMaxAdjEmax;
	/**
	 * Like expectimax, but adjusted using the observed ratio of exits vs no exits in the node
	 */
	public double adjEmax;
	
	public TPOLITSTreeNode(int numOfActions)
	{
		this.parent = null;
		this.totalValue = 0.0;
		this.numVisits = 0;
		this.depth = 0;
		this.children = new TPOLITSTreeNode[numOfActions];
		this.poiApproached = false;
		this.rebuildTree = true;
		this.stateObsNearPOI = false;
		this.path = null;

		this.expectimax = 0.0;
		this.nbGenerated = 0;
		this.nbExitsHere = 0;
		this.totalValueOnExit = 0.0;
		this.childrenMaxAdjEmax = 0.0;
		this.adjEmax = 0.0;
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
		this.stateObsNearPOI = false;
		this.path = null;

		this.expectimax = 0.0;
		this.nbGenerated = 0;
		this.nbExitsHere = 0;
		this.totalValueOnExit = 0.0;
		this.childrenMaxAdjEmax = 0.0;
		this.adjEmax = 0.0;
	}
	
	public TPOLITSTreeNode(TPOLITSTreeNode parent, int numOfActions, int depth, int actionIndex, double tabooBias) {
        this.parent = parent;
        this.children = new TPOLITSTreeNode[numOfActions];
        this.stateObsNearPOI = false;
		this.path = null;
        this.totalValue = 0.0;
        this.numVisits = 0;
        this.expectimax = 0.0;
        this.nbGenerated = 0;
        this.depth = depth;
        this.actionIndex = actionIndex;
        this.tabooBias = tabooBias;
        this.nbExitsHere = 0;
        this.totalValueOnExit = 0.0;
        this.childrenMaxAdjEmax = 0.0;
        this.adjEmax = 0.0;

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
		this.stateObsNearPOI = false;
		this.path = null;

		this.expectimax = 0.0;
		this.nbGenerated = 0;
		this.nbExitsHere = 0;
		this.totalValueOnExit = 0.0;
		this.childrenMaxAdjEmax = 0.0;
		this.adjEmax = 0.0;
	}

	public double getValue()
	{
		return this.totalValue / this.numVisits;
	}
	
	/**
     * Updates nodes attributes in a tree; mostly used to reset number of simulations to 1 to reduce the weight of past
     * simulations when salvaging a tree branch from one time step to the next
     */
    public void refreshTree(int depth) 
    {
    	this.depth = depth;
        this.totalValue = this.totalValue / this.numVisits;
        this.totalValueOnExit = this.totalValueOnExit / this.numVisits;
        this.numVisits = 1;
        this.expectimax = this.totalValue;
        this.adjEmax = this.totalValue;
        this.nbExitsHere = 1;
        this.nbGenerated = 0;

        for (TPOLITSTreeNode child : (TPOLITSTreeNode[])this.children) {
            if (!(child == null)) {
                child.refreshTree(this.depth + 1);
            }
        }
    }
}
