package NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner;

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
public class TPOLMCTSMoveController extends TreeSearchMoveController
{
	// Real types of fields
	// protected StateEvaluator stateEvaluator;
	// protected TPGameKnowledge gameKnowledge;
	
	private double uctConstant;
	private static double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};

	public TPOLMCTSMoveController(StateEvaluator stateEvaluator, TPGameKnowledge gameKnowledge)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
	}
	
	public void setParameters(double uctConstant)
	{
		this.uctConstant = uctConstant;
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
	public TPOLMCTSTreeNode expandNode(TreeNode node, StateObservation stateObs)
	{
		TPOLMCTSTreeNode tpolmctsNode = (TPOLMCTSTreeNode) node;
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		TPGameKnowledge tpGameKnowledge = (TPGameKnowledge) this.gameKnowledge;
		
		int bestAction = 0;
		double bestValue = -1;

		for (int i = 0; i < tpolmctsNode.children.length; i++)
		{
			double x = randomGenerator.nextDouble();
			if (x > bestValue && tpolmctsNode.children[i] == null)
			{
				bestAction = i;
				bestValue = x;
			}
		}

		// Advance the state

		// need to provide actions for all players to advance the forward model
		Types.ACTIONS[] acts = new Types.ACTIONS[tpGameKnowledge.getNumOfPlayers()];

		// set this agent's action
		acts[tpGameKnowledge.getPlayerID()] = stateObsMulti.getAvailableActions(tpGameKnowledge.getPlayerID()).get(bestAction);

		// get actions available to the opponent and assume they will do a random action
		acts[tpGameKnowledge.getOppID()] = getRandomAction(stateObsMulti, tpGameKnowledge.getOppID(), Types.ACTIONS.ACTION_NIL, false);

		stateObsMulti.advance(acts);

		TPOLMCTSTreeNode chosenChildNode = new TPOLMCTSTreeNode(tpolmctsNode, tpGameKnowledge.getNumOfPlayerActions(), bestAction);
		tpolmctsNode.children[bestAction] = chosenChildNode;
		return chosenChildNode;
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
	public TPOLMCTSTreeNode exploitNode(TreeNode node, StateObservation stateObs)
	{
		TPOLMCTSTreeNode tpolmctsNode = (TPOLMCTSTreeNode) node;
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		TPGameKnowledge tpGameKnowledge = (TPGameKnowledge) this.gameKnowledge;
		
		TPOLMCTSTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        
        for (TPOLMCTSTreeNode child : (TPOLMCTSTreeNode[])tpolmctsNode.children)
        {
            double childValue = child.totalValue / (child.numVisits + this.epsilon);

            childValue = Utils.normalise(childValue, bounds[0], bounds[1]);

            double uctValue = childValue +
            		uctConstant * Math.sqrt(Math.log(tpolmctsNode.numVisits + 1) / (child.numVisits + this.epsilon));

            uctValue = Utils.noise(uctValue, this.epsilon, this.randomGenerator.nextDouble());     //break ties randomly

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }
        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + tpolmctsNode.children.length + " " +
            + bounds[0] + " " + bounds[1]);
        }

        //Roll the state:

        //need to provide actions for all players to advance the forward model
        Types.ACTIONS[] acts = new Types.ACTIONS[tpGameKnowledge.getNumOfPlayers()];
        
        //set this agent's action
        acts[tpGameKnowledge.getPlayerID()] = stateObsMulti.getAvailableActions(tpGameKnowledge.getPlayerID()).get(selected.actionLeadingToThisNode);

        //get actions available to the opponent and assume they will do a random action
        //ArrayList<Types.ACTIONS> oppActions = state.getAvailableActions(Agent.oppID);
        //acts[Agent.oppID] = oppActions.get(m_rnd.nextInt(oppActions.size()));
        acts[tpGameKnowledge.getOppID()] = getRandomAction(stateObsMulti, tpGameKnowledge.getOppID(), Types.ACTIONS.ACTION_NIL, false);

        stateObsMulti.advance(acts);

        return selected;
	}

	/**
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Actions for both players.
	 */
	public Types.ACTIONS[] chooseMovesInRollout(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		TPGameKnowledge tpGameKnowledge = (TPGameKnowledge) this.gameKnowledge;
		
		Types.ACTIONS[] acts = new Types.ACTIONS[tpGameKnowledge.getNumOfPlayers()];
		for (int i = 0; i < tpGameKnowledge.getNumOfPlayers(); i++)
		{
			acts[i] = getRandomAction(stateObsMulti, i, Types.ACTIONS.ACTION_NIL, false);
		}
		
		LogHandler.writeLog(acts[0] + " " + acts[1], "TPOLMCTSMoveController.chooseMovesInRollout", 0);

		return acts;
	}

	/**
	 * Returns a random action that avoids being killed or a purely random action.
	 * 
	 * @param stateObs
	 *            current state
	 * @param playerID
	 */
	private Types.ACTIONS getRandomAction(StateObservationMulti stateObs, int playerID, Types.ACTIONS oppmove,
			boolean notLosingAction)
	{
		if (notLosingAction)
		{
			int numOfPlayers = gameKnowledge.getNumOfPlayers();
			int oppID = 1 - playerID;
			ArrayList<Types.ACTIONS> availableActions = stateObs.getAvailableActions(playerID);
			java.util.Collections.shuffle(availableActions);

			// Look for the opponent actions that would not kill him.
			for (Types.ACTIONS action : availableActions)
			{
				Types.ACTIONS[] acts = new Types.ACTIONS[numOfPlayers];
				acts[oppID] = oppmove;
				acts[playerID] = action;

				StateObservationMulti stateCopy = stateObs.copy();
				stateCopy.advance(acts);

				if (stateCopy.getMultiGameWinner()[playerID] != Types.WINNER.PLAYER_LOSES)
					return action;
			}
			return Types.ACTIONS.ACTION_NIL;
		}
		else
		{
			ArrayList<Types.ACTIONS> availableActions = stateObs.getAvailableActions(playerID);
			return availableActions.get(new Random().nextInt(availableActions.size()));
		}
	}
}
