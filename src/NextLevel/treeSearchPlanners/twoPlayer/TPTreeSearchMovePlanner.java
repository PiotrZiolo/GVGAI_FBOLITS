package NextLevel.treeSearchPlanners.twoPlayer;

import java.util.Random;

import NextLevel.GameKnowledge;
import NextLevel.GameKnowledgeExplorer;
import NextLevel.MovePlanner;
import NextLevel.State;
import NextLevel.StateEvaluator;
import NextLevel.moveController.AgentMoveController;
import NextLevel.twoPlayer.BasicTPState;
import NextLevel.twoPlayer.TPGameKnowledge;
import NextLevel.utils.LogHandler;
import core.game.StateObservationMulti;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Utils;

public class TPTreeSearchMovePlanner extends MovePlanner
{
	protected TPGameKnowledge gameKnowledge;
	protected TPTreeNode rootNode;
	protected BasicTPState rootState;
	protected StateObservationMulti rootStateObs;
	protected ElapsedCpuTimer mainElapsedTimer;
	protected TPTreeSearchMoveController tpTreeSearchMoveController;
	protected Random randomGenerator;
	
	protected double epsilon = 1e-6;

	// Algorithm parameters

	protected int remainingLimit;
	/* protected int rolloutDepth; */

	public TPTreeSearchMovePlanner()
	{

	}
	
	public TPTreeSearchMovePlanner(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge,
			GameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = (TPGameKnowledge) gameKnowledge;
		this.gameKnowledgeExplorer = gameKnowledgeExplorer;
		this.agentMoveController = agentMoveController;
		
		this.randomGenerator = new Random();
		
		this.tpTreeSearchMoveController = new TPTreeSearchMoveController(stateEvaluator, gameKnowledge, randomGenerator);
	}
	
	public void setParameters(int remainingLimit)
	{
		this.remainingLimit = remainingLimit;
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
		rootNode = new TPTreeNode();
	}

	protected void searchTree()
	{
		double avgTimeTaken = 0;
		double acumTimeTaken = 0;
		long remaining = mainElapsedTimer.remainingTimeMillis();
		int numIters = 0;

		while (remaining > 2 * avgTimeTaken && remaining > remainingLimit)
		{
			StateObservationMulti stateObs = rootStateObs.copy();

			ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
			TPTreeNode selectedNode = applyTreePolicy(stateObs);
			double delta = rollOut(selectedNode, stateObs);
			backUp(selectedNode, delta);

			numIters++;
			acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
			LogHandler.writeLog(
					elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")",
					"TreeSearchMovePlanner.searchTree", 1);
			avgTimeTaken = acumTimeTaken / numIters;
			remaining = mainElapsedTimer.remainingTimeMillis();
		}
	}

	protected TPTreeNode applyTreePolicy(StateObservationMulti stateObs)
	{
		TPTreeNode currentNode = rootNode;
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

	protected boolean isTreePolicyFinished(TPTreeNode currentNode, StateObservationMulti stateObs, boolean expand)
	{
		return (stateObs.isGameOver() || !expand /* || currentNode.depth >= rolloutDepth */);
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
		/*
		 * if (depth >= rolloutDepth) //rollout end condition.
		 * return true;
		 */

		if (rollerState.isGameOver()) // end of game
			return true;

		return false;
	}

	protected void backUp(TPTreeNode node, double delta)
	{
        while(node != null)
        {
            updateNode(node, delta);
            node = node.parent;
        }
	}
	
	protected void updateNode(TPTreeNode node, double delta)
	{
		// To be overriden in subclasses
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
