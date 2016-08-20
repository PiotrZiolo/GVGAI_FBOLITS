package NextLevel.treeSearchPlanners.twoPlayer.TPOLITSPlanner;

import java.util.ArrayList;
import java.util.Random;

import NextLevel.StateEvaluator;
import NextLevel.treeSearchPlanners.TreeNode;
import NextLevel.treeSearchPlanners.TreeSearchMoveController;
import NextLevel.twoPlayer.TPGameKnowledge;
import NextLevel.utils.LogHandler;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.Utils;

/**
 * This class is responsible for all choices of child nodes during the tree search.
 * By assumption StateEvaluator should only be used in this class, not in the planner.
 *
 */
public class TPOLITSMoveController extends TreeSearchMoveController
{
	// Real types of fields
	// protected StateEvaluator stateEvaluator;
	// protected TPGameKnowledge gameKnowledge;
	
	public TPOLITSMoveController(StateEvaluator stateEvaluator, TPGameKnowledge gameKnowledge)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
	}
	
	public void setParameters()
	{
		
	}

	/**
	 * Chooses the next node from the children of node, when there are still unexplored children. Advances stateObs accordingly.
	 * 
	 * @param node
	 *            Node to be expanded.
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Chosen child node.
	 */
	public TPOLITSTreeNode expandNode(TreeNode node, StateObservation stateObs)
	{
		TPOLITSTreeNode tpolmctsNode = (TPOLITSTreeNode) node;
		
		
		return tpolmctsNode;
	}

	/**
	 * Chooses the next node from the children of node, when all children have already been explored at least once. 
	 * Advances stateObs accordingly.
	 * 
	 * @param node
	 *            Node to be expanded.
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Chosen child node.
	 */
	public TPOLITSTreeNode exploitNode(TreeNode node, StateObservation stateObs)
	{
		TPOLITSTreeNode tpolmctsNode = (TPOLITSTreeNode) node;
		
		
		return tpolmctsNode;
	}

	/**
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Actions for both players.
	 */
	public void moveInRollout(StateObservation stateObs)
	{
		
	}
}
