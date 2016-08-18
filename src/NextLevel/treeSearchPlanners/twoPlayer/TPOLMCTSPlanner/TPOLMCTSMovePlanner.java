package NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner;

import java.util.Random;

import NextLevel.StateEvaluator;
import NextLevel.moveController.AgentMoveController;
import NextLevel.treeSearchPlanners.TreeNode;
import NextLevel.treeSearchPlanners.TreeSearchMovePlanner;
import NextLevel.twoPlayer.TPGameKnowledge;
import NextLevel.twoPlayer.TPGameKnowledgeExplorer;
import core.game.StateObservation;

public class TPOLMCTSMovePlanner extends TreeSearchMovePlanner
{
	// Real types of fields
	// protected StateEvaluator stateEvaluator;
	// protected TPGameKnowledge gameKnowledge;
	// protected TPGameKnowledgeExplorer gameKnowledgeExplorer; 
	// protected AgentMoveController agentMoveController;
	// protected TPOLMCTSTreeNode rootNode;
	// protected BasicTPState rootState;
	// protected StateObservationMulti rootStateObs;
	// protected TPOLMCTSMoveController tpTreeSearchMoveController; 
	
	// Algorithm parameters
	
	protected int remainingLimit;
	protected int rolloutDepth;
	
	public TPOLMCTSMovePlanner(StateEvaluator stateEvaluator, TPGameKnowledge gameKnowledge,
			TPGameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController, 
			TPOLMCTSMoveController treeSearchMoveController)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
		this.gameKnowledgeExplorer = gameKnowledgeExplorer;
		this.agentMoveController = agentMoveController;
		this.treeSearchMoveController = treeSearchMoveController;
		
		this.randomGenerator = new Random();
		this.treeSearchMoveController.setRandomGenerator(randomGenerator);
	}
	
	public void setParameters(int remainingLimit, int rolloutDepth)
	{
		this.remainingLimit = remainingLimit;
		this.rolloutDepth = rolloutDepth;
	}
	
	protected void initialize()
	{
		this.rootNode = new TPOLMCTSTreeNode(gameKnowledge.getNumOfPlayerActions());
	}
	
	protected boolean isTreePolicyFinished(TreeNode currentNode, StateObservation stateObs, boolean expand)
	{
		return (stateObs.isGameOver() || expand || currentNode.depth >= rolloutDepth);
	}
		
	protected boolean isRolloutFinished(StateObservation rollerState, int depth)
	{
		if (depth >= rolloutDepth) //rollout end condition.
			return true;
		
		if (rollerState.isGameOver()) // end of game
			return true;

		return false;
	}
	
	protected void updateNode(TreeNode node, double delta)
	{
		node.numVisits++;
        node.totalValue += delta;
	}
}
