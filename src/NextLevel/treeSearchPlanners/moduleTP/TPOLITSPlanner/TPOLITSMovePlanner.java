package NextLevel.treeSearchPlanners.moduleTP.TPOLITSPlanner;

import java.util.ArrayList;
import java.util.Random;

import NextLevel.BasicState;
import NextLevel.GameStateTracker;
import NextLevel.PointOfInterest;
import NextLevel.State;
import NextLevel.moduleFB.moduleFBTP.FBTPState;
import NextLevel.StateEvaluator;
import NextLevel.StateHandler;
import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPGameKnowledgeExplorer;
import NextLevel.moduleTP.TPGameStateTracker;
import NextLevel.moduleTP.TPStateHandler;
import NextLevel.treeSearchPlanners.TreeNode;
import NextLevel.treeSearchPlanners.TreeSearchMovePlanner;
import NextLevel.utils.LogHandler;
import core.game.StateObservation;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class TPOLITSMovePlanner extends TreeSearchMovePlanner
{
	// Real types of fields
	// protected StateEvaluator stateEvaluator;
	// protected TPGameKnowledge gameKnowledge;
	// protected TPGameKnowledgeExplorer gameKnowledgeExplorer; 
	// protected AgentMoveController agentMoveController;
	// protected TPOLITSTreeNode rootNode;
	// protected BasicTPState rootState;
	// protected StateObservationMulti rootStateObs;
	// protected TPGameStateTracker gameStateTracker;
	
	protected ArrayList<PointOfInterest> pois;
	protected ArrayList<TPOLITSTreeNode> nodesNearPOIs;
	protected PointOfInterest goalPOI;
	
	// mode: "approach", "action", "shooting" 
	// - approach goal POI
	// - choose the best action when close to goal POI
	// - shoot and search for other action in the next move
	protected String mode; 
	
	// Algorithm parameters
	
	protected int remainingLimit;
	
	public TPOLITSMovePlanner(StateEvaluator stateEvaluator, TPGameKnowledge gameKnowledge,
			TPGameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController, TPGameStateTracker gameStateTracker)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
		this.gameKnowledgeExplorer = gameKnowledgeExplorer;
		this.agentMoveController = agentMoveController;
		this.gameStateTracker = gameStateTracker;
		
		this.randomGenerator = new Random();
		
		initializePOIList();
	}
	
	private void initializePOIList()
	{
		// initialize list of POIs from gameStateTracker + add regular grid on the map (optional)
		
	}

	public void setParameters(int remainingLimit)
	{
		this.remainingLimit = remainingLimit;
	}
	
	public ACTIONS chooseAction(State state, ElapsedCpuTimer elapsedTimer, int timeForChoosingMove)
	{
		this.rootState = (FBTPState) state;
		this.rootStateObs = rootState.getStateObservation();
		this.mainElapsedTimer = elapsedTimer;
		
		initialize();
		
		explorePOIs();
		
		updateGoalPOI();

		searchTree();
		
		return getOLITSAction();
	}
	
	protected void initialize()
	{
		updatePOIList();
		
		chooseMode();
		
		// initialize the tree according to mode
	}
	
	private void updatePOIList()
	{
		// update POI list from gameStateTracker
		
	}
	
	private void chooseMode()
	{
		// check if close to POI and choose time limits for explore and rollout (small time for approach, large for action, according for exploring)
		// choose mode (approach or action) according to whether avatar is close to POI
	}

	private void explorePOIs()
	{
		// explore POIs:
		// - approach to new POIs
		// - shooting scenario (use and wait trying not to die)
		// - rollouts from nodesNearPOIs
		
	}
	
	private void updateGoalPOI()
	{
		// if there was a large change in score of the best POI, change goal POI
		
	}
	
	protected void searchTree()
	{		
		// in the approach mode use AgentMoveController to choose an action
		// in the action mode do OLETS (OLETS to make it possible to look for long sequences of moves)
		
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
	public TPOLITSTreeNode expandNode(TreeNode node, StateObservation stateObs)
	{
		TPOLITSTreeNode tpolmctsNode = (TPOLITSTreeNode) node;
		
		
		return tpolmctsNode;
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
	public TPOLITSTreeNode exploitNode(TreeNode node, StateObservation stateObs)
	{
		TPOLITSTreeNode tpolmctsNode = (TPOLITSTreeNode) node;
		
		
		return tpolmctsNode;
	}

	/**
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Actions for both players.
	 */
	public void moveInRollout(StateObservation stateObs)
	{
		
	}
	
	private ACTIONS getOLITSAction()
	{
		// return the right action to be done according to mode
		// in approach the next move from the path to goal POI
		// in 
		return null;
	}
}
