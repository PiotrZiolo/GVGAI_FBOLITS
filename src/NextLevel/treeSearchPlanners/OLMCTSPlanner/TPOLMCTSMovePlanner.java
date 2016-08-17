package NextLevel.treeSearchPlanners.OLMCTSPlanner;

import java.util.Random;

import NextLevel.GameKnowledge;
import NextLevel.GameKnowledgeExplorer;
import NextLevel.State;
import NextLevel.StateEvaluator;
import NextLevel.moveController.AgentMoveController;
import NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner.TPTreeNode;
import NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner.TPTreeSearchMoveController;
import NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner.TPTreeSearchMovePlanner;
import NextLevel.utils.LogHandler;
import core.game.StateObservationMulti;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class TPOLMCTSMovePlanner extends TPTreeSearchMovePlanner
{
	protected TPOLMCTSTreeNode rootNode;
	protected TPOLMCTSMoveController tpTreeSearchMoveController;
	
	// Algorithm parameters
	
	protected int remainingLimit;
	protected int rolloutDepth;
	protected double uctConstant;
	
	public TPOLMCTSMovePlanner(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge,
			GameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController)
	{
		super(stateEvaluator, gameKnowledge, gameKnowledgeExplorer, agentMoveController);
		
		tpTreeSearchMoveController = new TPOLMCTSMoveController(stateEvaluator, gameKnowledge, randomGenerator);
	}
	
	public void setParameters(int remainingLimit, int rolloutDepth, double uctConstant)
	{
		this.remainingLimit = remainingLimit;
		this.rolloutDepth = rolloutDepth;
		this.uctConstant = uctConstant;
		tpTreeSearchMoveController.setParameters(uctConstant);
	}
	
	public ACTIONS chooseAction(State state, ElapsedCpuTimer elapsedTimer, int timeForChoosingMove)
	{
		return super.chooseAction(state, elapsedTimer, timeForChoosingMove);
	}
	
	protected void initialize()
	{
		rootNode = new TPOLMCTSTreeNode(gameKnowledge.getNumOfPlayerActions());
	}
	
	protected boolean isTreePolicyFinished(TPTreeNode currentNode, StateObservationMulti stateObs, boolean expand)
	{
		return (!stateObs.isGameOver() && !expand && currentNode.depth < rolloutDepth);
	}
	
	protected boolean isRolloutFinished(StateObservationMulti rollerState, int depth)
	{
		if (depth >= rolloutDepth) //rollout end condition.
			return true;
		
		if (rollerState.isGameOver()) // end of game
			return true;

		return false;
	}
	
	protected void updateNode(TPTreeNode node, double delta)
	{
		node.numVisits++;
        node.totalValue += delta;
	}
}
