package NextLevel.treeSearchPlanners.moduleTP.TPOLITSPlanner;

import java.util.Random;

import NextLevel.StateEvaluator;
import NextLevel.StateHandler;
import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPGameKnowledgeExplorer;
import NextLevel.moduleTP.TPStateHandler;
import NextLevel.treeSearchPlanners.TreeNode;
import NextLevel.treeSearchPlanners.TreeSearchMovePlanner;
import core.game.StateObservation;

public class TPOLITSMovePlanner extends TreeSearchMovePlanner
{
	// Real types of fields
	// protected StateEvaluator stateEvaluator;
	// protected TPGameKnowledge gameKnowledge;
	// protected TPGameKnowledgeExplorer gameKnowledgeExplorer; 
	// protected AgentMoveController agentMoveController;
	// protected TPOLMCTSTreeNode rootNode;
	// protected BasicTPState rootState;
	// protected StateObservationMulti rootStateObs;
	// protected TPOLMCTSMoveController treeSearchMoveController; 
	
	// Algorithm parameters
	
	protected int remainingLimit;
	
	public TPOLITSMovePlanner(StateEvaluator stateEvaluator, TPGameKnowledge gameKnowledge,
			TPGameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController, 
			TPOLITSMoveController treeSearchMoveController)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
		this.gameKnowledgeExplorer = gameKnowledgeExplorer;
		this.agentMoveController = agentMoveController;
		this.treeSearchMoveController = treeSearchMoveController;
		
		this.randomGenerator = new Random();
		this.treeSearchMoveController.setRandomGenerator(randomGenerator);
	}
	
	public void setParameters(int remainingLimit)
	{
		this.remainingLimit = remainingLimit;
	}
	
	protected void initialize()
	{
		this.rootNode = new TPOLITSTreeNode(gameKnowledge.getNumOfPlayerActions());
	}
	
	protected boolean isTreePolicyFinished(TreeNode currentNode, StateObservation stateObs, boolean expand)
	{
		return (stateObs.isGameOver() || expand);
	}
		
	protected boolean isRolloutFinished(StateObservation rollerState, int depth)
	{
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
