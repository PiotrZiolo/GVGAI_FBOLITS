package baseStructure.treeSearchPlanners;

import java.util.Random;

import baseStructure.GameKnowledge;
import baseStructure.StateEvaluator;
import core.game.StateObservation;

/**
 * This class is responsible for all choices of child nodes during the tree search. 
 * By assumption StateEvaluator should only be used in this class, not in the planner. 
 *
 */
public class TreeSearchMoveController
{
	protected StateEvaluator stateEvaluator;
	protected GameKnowledge gameKnowledge;
	protected Random randomGenerator;

	protected double epsilon = 1e-6;

	public TreeSearchMoveController()
	{

	}
	
	public TreeSearchMoveController(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
	}
	
	public void setRandomGenerator(Random randomGenerator)
	{
		this.randomGenerator = randomGenerator;
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
	public TreeNode expandNode(TreeNode node, StateObservation stateObs)
	{
		// To be overriden in subclasses
		return null;
	}

	/**
	 * Chooses the next node from the children of node, when all children have already been explored at least once. Advances stateObs accordingly.
	 * 
	 * @param node
	 *            Node to be expanded.
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Chosen child node.
	 */
	public TreeNode exploitNode(TreeNode node, StateObservation stateObs)
	{
		// To be overridden in subclasses
		return null;
	}

	/**
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Actions for both players.
	 */
	public void moveInRollout(StateObservation stateObs)
	{
		// To be overridden in subclasses
	}
}
