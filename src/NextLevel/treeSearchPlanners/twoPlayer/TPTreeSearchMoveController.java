package NextLevel.treeSearchPlanners.twoPlayer;

import java.util.Random;

import NextLevel.GameKnowledge;
import NextLevel.StateEvaluator;
import NextLevel.twoPlayer.TPGameKnowledge;
import core.game.StateObservationMulti;
import ontology.Types;

/**
 * This class is responsible for all choices of child nodes during the tree search. 
 * By assumption StateEvaluator should only be used in this class, not in the planner. 
 *
 */
public class TPTreeSearchMoveController
{
	protected StateEvaluator stateEvaluator;
	protected TPGameKnowledge gameKnowledge;
	protected Random randomGenerator;
	
	protected double epsilon = 1e-6;

	public TPTreeSearchMoveController(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge, Random randomGenerator)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = (TPGameKnowledge)gameKnowledge;
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
	public TPTreeNode expandNode(TPTreeNode node, StateObservationMulti stateObs)
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
	public TPTreeNode exploitNode(TPTreeNode node, StateObservationMulti stateObs)
	{
		// To be overridden in subclasses
		return null;
	}

	/**
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Actions for both players.
	 */
	public Types.ACTIONS[] chooseMovesInRollout(StateObservationMulti stateObs)
	{
		Types.ACTIONS[] acts = new Types.ACTIONS[gameKnowledge.getNumOfPlayers()];

		return acts;
	}
}
