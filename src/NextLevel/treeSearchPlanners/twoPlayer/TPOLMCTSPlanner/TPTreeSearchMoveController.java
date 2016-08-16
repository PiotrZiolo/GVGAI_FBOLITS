package NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner;

import NextLevel.GameKnowledge;
import NextLevel.StateEvaluator;
import core.game.StateObservationMulti;
import ontology.Types;

public class TPTreeSearchMoveController
{
	private StateEvaluator stateEvaluator;
	private GameKnowledge gameKnowledge;

	public TPTreeSearchMoveController(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
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
		return null;
	}

	/**
	 * @param node
	 *            Node to be rolled out.
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Actions for both players.
	 */
	public Types.ACTIONS[] chooseMovesInRollout(TPTreeNode node, StateObservationMulti stateObs)
	{
		Types.ACTIONS[] acts = new Types.ACTIONS[gameKnowledge.getNumOfPlayers()];

		return acts;
	}
}
