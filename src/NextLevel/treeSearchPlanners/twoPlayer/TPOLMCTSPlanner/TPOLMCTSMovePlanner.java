package NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner;

import java.util.Random;

import NextLevel.GameKnowledge;
import NextLevel.GameKnowledgeExplorer;
import NextLevel.State;
import NextLevel.StateEvaluator;
import NextLevel.moveController.AgentMoveController;
import NextLevel.treeSearchPlanners.twoPlayer.TPTreeNode;
import NextLevel.treeSearchPlanners.twoPlayer.TPTreeSearchMovePlanner;
import NextLevel.twoPlayer.BasicTPState;
import NextLevel.twoPlayer.TPGameKnowledge;
import NextLevel.utils.LogHandler;
import core.game.StateObservationMulti;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Utils;

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
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = (TPGameKnowledge) gameKnowledge;
		this.gameKnowledgeExplorer = gameKnowledgeExplorer;
		this.agentMoveController = agentMoveController;
		
		this.randomGenerator = new Random();
		
		this.tpTreeSearchMoveController = new TPOLMCTSMoveController(stateEvaluator, gameKnowledge, randomGenerator);
		
		if (this.gameKnowledge == null)
			LogHandler.writeLog("GameKnowledge null", "TPOLMCTSMovePlanner.creator", 1);
		else
			LogHandler.writeLog("GameKnowledge not null", "TPOLMCTSMovePlanner.creator", 1);
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
		this.rootState = (BasicTPState) state;
		this.rootStateObs = rootState.getStateObservation();
		this.mainElapsedTimer = elapsedTimer;

		initialize();

		searchTree();

		return getMostVisitedAction();
	}
	
	protected void initialize()
	{
		this.rootNode = new TPOLMCTSTreeNode(gameKnowledge.getNumOfPlayerActions());
	}
	
	protected TPOLMCTSTreeNode applyTreePolicy(StateObservationMulti stateObs)
	{
		TPOLMCTSTreeNode currentNode = rootNode;
		boolean expand = false;

		while (isTreePolicyFinished(currentNode, stateObs, expand))
		{
			if (currentNode.isNotFullyExpanded())
			{
				currentNode = tpTreeSearchMoveController.expandNode(currentNode, stateObs);
				expand = true;
			}
			else
			{
				currentNode = tpTreeSearchMoveController.exploitNode(currentNode, stateObs);
				expand = false;
			}
		}

		return currentNode;
	}
	
	protected boolean isTreePolicyFinished(TPOLMCTSTreeNode currentNode, StateObservationMulti stateObs, boolean expand)
	{
		return (!stateObs.isGameOver() && !expand && currentNode.depth < rolloutDepth);
	}
	
	protected double rollOut(TPTreeNode selectedNode, StateObservationMulti stateObs)
	{
		int thisDepth = selectedNode.depth;

		while (!isRolloutFinished(stateObs, thisDepth))
		{
			stateObs.advance(tpTreeSearchMoveController.chooseMovesInRollout(stateObs));
			thisDepth++;
		}

		double delta = stateEvaluator.evaluate(stateObs);

		return delta;
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
	
	protected Types.ACTIONS getMostVisitedAction()
	{
		int selected = -1;
		double bestValue = -Double.MAX_VALUE;
		boolean allEqual = true;
		double first = -1;

		for (int i = 0; i < rootNode.children.length; i++)
		{

			if (rootNode.children[i] != null)
			{
				if (first == -1)
					first = rootNode.children[i].numVisits;
				else if (first != rootNode.children[i].numVisits)
				{
					allEqual = false;
				}

				double childValue = rootNode.children[i].numVisits;
				childValue = Utils.noise(childValue, this.epsilon, this.randomGenerator.nextDouble()); // break ties randomly
				if (childValue > bestValue)
				{
					bestValue = childValue;
					selected = i;
				}
			}
		}

		if (selected == -1)
		{
			selected = 0;
		}

		return (allEqual) ? getBestAction()
				: rootStateObs.getAvailableActions(gameKnowledge.getPlayerID()).get(selected);
	}
	
	protected Types.ACTIONS getBestAction()
	{
		int selected = -1;
		double bestValue = -Double.MAX_VALUE;

		for (int i = 0; i < rootNode.children.length; i++)
		{

			if (rootNode.children[i] != null)
			{

				double childValue = rootNode.children[i].totalValue / (rootNode.children[i].numVisits + this.epsilon);

				childValue = Utils.noise(childValue, this.epsilon, this.randomGenerator.nextDouble()); // break ties randomly
				if (childValue > bestValue)
				{
					bestValue = childValue;
					selected = i;
				}
			}
		}

		if (selected == -1)
		{
			selected = 0;
		}

		return rootStateObs.getAvailableActions(gameKnowledge.getPlayerID()).get(selected);
	}
}
