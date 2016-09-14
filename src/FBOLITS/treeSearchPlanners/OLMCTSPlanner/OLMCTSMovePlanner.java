package FBOLITS.treeSearchPlanners.OLMCTSPlanner;

import java.util.Random;

import FBOLITS.GameKnowledge;
import FBOLITS.GameKnowledgeExplorer;
import FBOLITS.StateEvaluator;
import FBOLITS.mechanicsController.AgentMoveController;
import FBOLITS.treeSearchPlanners.TreeNode;
import FBOLITS.treeSearchPlanners.TreeSearchMovePlanner;
import core.game.StateObservation;
import tools.Utils;

public class OLMCTSMovePlanner extends TreeSearchMovePlanner
{
	// Real types of fields
	// protected StateEvaluator stateEvaluator;
	// protected FBGameKnowledge gameKnowledge;
	// protected FBGameKnowledgeExplorer gameKnowledgeExplorer; 
	// protected AgentMoveController agentMoveController;
	// protected OLMCTSTreeNode rootNode;
	// protected StateObservationMulti rootStateObs;
	
	// Algorithm parameters
	
	protected int remainingLimit = 12;
	protected int rolloutDepth = 10;
	private double uctConstant = Math.sqrt(2);
	private static double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};
	
	public OLMCTSMovePlanner(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge,
			GameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
		this.gameKnowledgeExplorer = gameKnowledgeExplorer;
		this.agentMoveController = agentMoveController;
		
		this.randomGenerator = new Random();
	}
	
	public void setParameters(int remainingLimit, int rolloutDepth, double uctConstant)
	{
		this.remainingLimit = remainingLimit;
		this.rolloutDepth = rolloutDepth;
		this.uctConstant = uctConstant;
	}
	
	@Override
	protected void initializeForTreeSearch()
	{
		this.rootNode = new OLMCTSTreeNode(gameKnowledge.getNumOfPlayerActions());
	}
	
	@Override
	protected boolean isTreePolicyFinished(TreeNode currentNode, StateObservation stateObs, boolean expand)
	{
		return (stateObs.isGameOver() || expand || currentNode.depth >= rolloutDepth);
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
	@Override
	protected OLMCTSTreeNode expandNode(TreeNode node, StateObservation stateObs)
	{
		OLMCTSTreeNode tpolmctsNode = (OLMCTSTreeNode) node;
		
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

		stateObs.advance(stateObs.getAvailableActions().get(bestAction));

		OLMCTSTreeNode chosenChildNode = new OLMCTSTreeNode(tpolmctsNode, stateObs.getAvailableActions().size(), bestAction);
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
	@Override
	protected OLMCTSTreeNode exploitNode(TreeNode node, StateObservation stateObs)
	{
		OLMCTSTreeNode tpolmctsNode = (OLMCTSTreeNode) node;
		
		OLMCTSTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        
        for (OLMCTSTreeNode child : (OLMCTSTreeNode[])tpolmctsNode.children)
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

        stateObs.advance(stateObs.getAvailableActions().get(selected.actionLeadingToThisNode));

        return selected;
	}
		
	@Override
	protected boolean isRolloutFinished(StateObservation rollerState, int depth)
	{
		if (depth >= rolloutDepth) //rollout end condition.
			return true;
		
		if (rollerState.isGameOver()) // end of game
			return true;

		return false;
	}
	
	/**
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Actions for both players.
	 */
	@Override
	protected void moveInRollout(StateObservation stateObs)
	{
		stateObs.advance(agentMoveController.getRandomAction(stateObs));
	}
	
	protected void updateNode(TreeNode node, double delta)
	{
		node.numVisits++;
        node.totalValue += delta;
	}
}
