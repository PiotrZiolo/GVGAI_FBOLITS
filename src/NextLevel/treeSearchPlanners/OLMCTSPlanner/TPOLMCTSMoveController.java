package NextLevel.treeSearchPlanners.OLMCTSPlanner;

import java.util.ArrayList;
import java.util.Random;

import NextLevel.GameKnowledge;
import NextLevel.StateEvaluator;
import NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner.TPTreeSearchMoveController;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.Utils;

/**
 * This class is responsible for all choices of child nodes during the tree search.
 * By assumption StateEvaluator should only be used in this class, not in the planner.
 *
 */
public class TPOLMCTSMoveController extends TPTreeSearchMoveController
{
	private StateEvaluator stateEvaluator;
	
	private double uctConstant;
	private static double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};

	public TPOLMCTSMoveController(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge, Random randomGenerator)
	{
		super(stateEvaluator, gameKnowledge, randomGenerator);
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
	public TPOLMCTSTreeNode expandNode(TPOLMCTSTreeNode node, StateObservationMulti stateObs)
	{
		int bestAction = 0;
		double bestValue = -1;

		for (int i = 0; i < node.children.length; i++)
		{
			double x = randomGenerator.nextDouble();
			if (x > bestValue && node.children[i] == null)
			{
				bestAction = i;
				bestValue = x;
			}
		}

		// Advance the state

		// need to provide actions for all players to advance the forward model
		Types.ACTIONS[] acts = new Types.ACTIONS[gameKnowledge.getNumOfPlayers()];

		// set this agent's action
		acts[gameKnowledge.getPlayerID()] = stateObs.getAvailableActions(gameKnowledge.getPlayerID()).get(bestAction);

		// get actions available to the opponent and assume they will do a random action
		acts[gameKnowledge.getOppID()] = getRandomAction(stateObs, gameKnowledge.getOppID(), Types.ACTIONS.ACTION_NIL, false);

		stateObs.advance(acts);

		TPOLMCTSTreeNode chosenChildNode = new TPOLMCTSTreeNode(node, bestAction, gameKnowledge.getNumOfPlayerActions());
		node.children[bestAction] = chosenChildNode;
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
	public TPOLMCTSTreeNode exploitNode(TPOLMCTSTreeNode node, StateObservationMulti stateObs)
	{
		TPOLMCTSTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        
        for (TPOLMCTSTreeNode child : node.children)
        {
            double childValue = child.totalValue / (child.numVisits + this.epsilon);

            childValue = Utils.normalise(childValue, bounds[0], bounds[1]);

            double uctValue = childValue +
            		uctConstant * Math.sqrt(Math.log(node.numVisits + 1) / (child.numVisits + this.epsilon));

            uctValue = Utils.noise(uctValue, this.epsilon, this.randomGenerator.nextDouble());     //break ties randomly

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }
        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + node.children.length + " " +
            + bounds[0] + " " + bounds[1]);
        }

        //Roll the state:

        //need to provide actions for all players to advance the forward model
        Types.ACTIONS[] acts = new Types.ACTIONS[gameKnowledge.getNumOfPlayers()];
        
        //set this agent's action
        acts[gameKnowledge.getPlayerID()] = stateObs.getAvailableActions(gameKnowledge.getPlayerID()).get(selected.actionLeadingToThisNode);

        //get actions available to the opponent and assume they will do a random action
        //ArrayList<Types.ACTIONS> oppActions = state.getAvailableActions(Agent.oppID);
        //acts[Agent.oppID] = oppActions.get(m_rnd.nextInt(oppActions.size()));
        acts[gameKnowledge.getOppID()] = getRandomAction(stateObs, gameKnowledge.getOppID(), Types.ACTIONS.ACTION_NIL, false);

        stateObs.advance(acts);

        return selected;
	}

	/**
	 * @param node
	 *            Node to be rolled out.
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Actions for both players.
	 */
	public Types.ACTIONS[] chooseMovesInRollout(TPOLMCTSTreeNode node, StateObservationMulti stateObs)
	{
		Types.ACTIONS[] acts = new Types.ACTIONS[gameKnowledge.getNumOfPlayers()];
		for (int i = 0; i < gameKnowledge.getNumOfPlayers(); i++)
		{
			acts[i] = getRandomAction(stateObs, i, Types.ACTIONS.ACTION_NIL, false);
		}

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
