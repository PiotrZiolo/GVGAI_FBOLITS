package NextLevel.treeSearchPlanners;

import java.util.Random;

import NextLevel.BasicState;
import NextLevel.GameKnowledge;
import NextLevel.GameKnowledgeExplorer;
import NextLevel.GameStateTracker;
import NextLevel.MovePlanner;
import NextLevel.State;
import NextLevel.StateEvaluator;
import NextLevel.StateHandler;
import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.utils.LogHandler;
import core.game.StateObservation;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Utils;

public class TreeSearchMovePlanner extends MovePlanner
{
	// Real types of fields
	// protected StateEvaluator stateEvaluator;
	// protected GameKnowledge gameKnowledge;
	// protected GameKnowledgeExplorer gameKnowledgeExplorer; 
	// protected AgentMoveController agentMoveController;
	// protected GameStateTracker gameStateTracker;
	
	protected TreeNode rootNode;
	protected BasicState rootState;
	protected StateObservation rootStateObs;
	
	protected ElapsedCpuTimer mainElapsedTimer;
	protected Random randomGenerator;
	
	protected double epsilon = 1e-6;

	// Algorithm parameters

	protected int remainingLimit;
	/* protected int rolloutDepth; */

	public TreeSearchMovePlanner()
	{

	}
	
	public TreeSearchMovePlanner(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge,
			GameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
		this.gameKnowledgeExplorer = gameKnowledgeExplorer;
		this.agentMoveController = agentMoveController;
		
		this.randomGenerator = new Random();
	}
	
	public void setParameters(int remainingLimit)
	{
		this.remainingLimit = remainingLimit;
	}

	public ACTIONS chooseAction(State state, ElapsedCpuTimer elapsedTimer, int timeForChoosingMove)
	{
		this.rootState = (BasicState) state;
		this.rootStateObs = rootState.getStateObservation();
		this.mainElapsedTimer = elapsedTimer;

		initialize();

		searchTree();

		return getMostVisitedAction();
	}

	protected void initialize()
	{
		rootNode = new TreeNode();
	}

	protected void searchTree()
	{
		double avgTimeTaken = 0;
		double acumTimeTaken = 0;
		long remaining = mainElapsedTimer.remainingTimeMillis();
		int numIters = 0;

		while (remaining > 2 * avgTimeTaken && remaining > remainingLimit)
		{
			StateObservation stateObs = rootStateObs.copy();

			ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
			TreeNode selectedNode = applyTreePolicy(stateObs);
			double delta = rollOut(selectedNode, stateObs);
			backUp(selectedNode, delta);

			numIters++;
			acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
			LogHandler.writeLog(
					elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")",
					"TreeSearchMovePlanner.searchTree", 0);
			avgTimeTaken = acumTimeTaken / numIters;
			remaining = mainElapsedTimer.remainingTimeMillis();
		}
	}

	protected TreeNode applyTreePolicy(StateObservation stateObs)
	{
		TreeNode currentNode = rootNode;
		boolean expand = false;

		while (!isTreePolicyFinished(currentNode, stateObs, expand))
		{
			if (currentNode.isNotFullyExpanded())
			{
				currentNode = expandNode(currentNode, stateObs);
				expand = true;
			}
			else
			{
				currentNode = exploitNode(currentNode, stateObs);
				expand = false;
			}
		}

		return currentNode;
	}

	protected boolean isTreePolicyFinished(TreeNode currentNode, StateObservation stateObs, boolean expand)
	{
		return (stateObs.isGameOver() || expand /* || currentNode.depth >= rolloutDepth */);
	}

	protected double rollOut(TreeNode selectedNode, StateObservation stateObs)
	{
		int thisDepth = selectedNode.depth;

		while (!isRolloutFinished(stateObs, thisDepth))
		{
			moveInRollout(stateObs);
			LogHandler.writeLog("State game tick: " + stateObs.getGameTick(), "TreeSearchMoveController.rollOut", 0);
			thisDepth++;
		}

		double delta = stateEvaluator.evaluateState(stateObs);

		return delta;
	}

	protected boolean isRolloutFinished(StateObservation rollerState, int depth)
	{
		/*
		 * if (depth >= rolloutDepth) //rollout end condition.
		 * return true;
		 */

		if (rollerState.isGameOver()) // end of game
			return true;

		return false;
	}

	protected void backUp(TreeNode node, double delta)
	{
        while(node != null)
        {
            updateNode(node, delta);
            node = node.parent;
        }
	}
	
	protected void updateNode(TreeNode node, double delta)
	{
		// To be overriden in subclasses
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
	public TreeNode expandNode(TreeNode node, StateObservation stateObs)
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
	public TreeNode exploitNode(TreeNode node, StateObservation stateObs)
	{
		// To be overridden in subclasses
		return null;
	}

	/**
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Actions for both players.
	 */
	public void moveInRollout(StateObservation stateObs)
	{
		// To be overridden in subclasses
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

		return (allEqual) ? getBestAction() : gameKnowledge.getPlayerActions().get(selected);
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

		return gameKnowledge.getPlayerActions().get(selected);
	}
}
