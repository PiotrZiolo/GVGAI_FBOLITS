package NextLevel.treeSearchPlanners.moduleTP.TPOLITSPlanner;

import java.util.ArrayList;
import java.util.Random;

import NextLevel.BasicState;
import NextLevel.GameStateTracker;
import NextLevel.PointOfInterest;
import NextLevel.PointOfInterest.POITYPE;
import NextLevel.State;
import NextLevel.moduleFB.moduleFBTP.FBTPGameKnowledge;
import NextLevel.moduleFB.moduleFBTP.FBTPState;
import NextLevel.moduleFB.moduleFBTP.FBTPStateEvaluator;
import NextLevel.moduleFB.moduleFBTP.MechanicsController.FBTPAgentMoveController;
import NextLevel.StateEvaluator;
import NextLevel.StateHandler;
import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.mechanicsController.GameMechanicsController;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPGameKnowledgeExplorer;
import NextLevel.moduleTP.TPGameStateTracker;
import NextLevel.moduleTP.TPStateHandler;
import NextLevel.treeSearchPlanners.TreeNode;
import NextLevel.treeSearchPlanners.TreeSearchMovePlanner;
import NextLevel.treeSearchPlanners.moduleTP.TPOLMCTSPlanner.TPOLMCTSTreeNode;
import NextLevel.utils.LogHandler;
import NextLevel.utils.Pair;
import controllers.singlePlayer.olets.Agent;
import controllers.singlePlayer.olets.SingleMCTSPlayer;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

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

	// protected ArrayList<PointOfInterest> pois;
	protected ArrayList<TPOLITSTreeNode> nodesNearPOIs;
	protected TPOLITSTreeNode goalNode;

	protected int totalTimeForChoosingMove;
	// mode: "approach", "action", "shooting"
	// - approach goal POI
	// - choose the best action when close to goal POI
	// - shoot and search for other action in the next move
	protected TREESEARCHMODE mode;
	protected int timeLimitForPOIExploration;
	protected int timeLimitForMoveChoice;

	// Algorithm parameters

	protected int moveChoiceRemainingLimit = 6; // in ms
	protected int poiExplorationRemainingLimit = 3; // in ms
	protected double shotScenarioImportance = 100;
	protected int poiCloseDistanceThreshold = 3; // expressed in the number of moves to the POI
	protected double timePercentageForMoveChoiceInActionMode = 0.9;
	protected double timePercentageForMoveChoiceInActionModeAfterAction = 0.3;
	// protected double timePercentageForMoveChoiceInApproachMode = 0.1;
	protected int timeLimitForNonDeterministicPathSearch = 2; // in ms
	protected double goalPOIHandicap = 1.5;
	protected double uctConstant = Math.sqrt(2);
	protected static double[] bounds = new double[] { -Double.MAX_VALUE, Double.MAX_VALUE };
	protected int numOfRolloutsInExplore = 3;
	protected int shotTestingWaitingTurns = 20;
	protected int maxExploreRolloutDepth = 6;

	public TPOLITSMovePlanner(StateEvaluator stateEvaluator, TPGameKnowledge gameKnowledge,
			TPGameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController,
			TPGameStateTracker gameStateTracker)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
		this.gameKnowledgeExplorer = gameKnowledgeExplorer;
		this.agentMoveController = agentMoveController;
		this.gameStateTracker = gameStateTracker;

		this.randomGenerator = new Random();

		this.mode = TREESEARCHMODE.APPROACH;
		this.goalNode = null;
		initializePOINodes();
	}

	private void initializePOINodes()
	{
		FBTPStateEvaluator fbtpStateEvaluator = (FBTPStateEvaluator) this.stateEvaluator;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		// initialize list of POIs from gameStateTracker + optionally add regular grid on the map
		nodesNearPOIs = new ArrayList<TPOLITSTreeNode>();

		for (PointOfInterest poi : gameStateTracker.getPOIs())
		{
			TPOLITSTreeNode node = new TPOLITSTreeNode(gameKnowledge.getNumOfPlayerActions(), poi);

			if (poi.poiType == POITYPE.SPRITE)
			{
				node.totalValue = fbtpStateEvaluator.evaluateSprite(poi.observation);
				node.numVisits = 1;
			}

			nodesNearPOIs.add(node);
		}

		if (fbtpGameKnowledge.isShootingAllowed())
		{
			PointOfInterest shootingScenario = new PointOfInterest();
			shootingScenario.poiType = POITYPE.SHOT;
			TPOLITSTreeNode node = new TPOLITSTreeNode(gameKnowledge.getNumOfPlayerActions(), shootingScenario);
			node.totalValue = shotScenarioImportance;
			node.numVisits = 1;

			nodesNearPOIs.add(node);
		}
	}

	public void setParameters(int remainingLimit)
	{
		this.moveChoiceRemainingLimit = remainingLimit;
	}

	public ACTIONS chooseAction(State state, ElapsedCpuTimer elapsedTimer, int totalTimeForChoosingMove)
	{
		this.rootState = (FBTPState) state;
		this.rootStateObs = rootState.getStateObservation();
		this.mainElapsedTimer = elapsedTimer;
		this.totalTimeForChoosingMove = totalTimeForChoosingMove;

		initialize();

		updatePOIList();

		setGoalPOIIfItDoesntExist();

		chooseMode();

		initializeTreeForExploration();

		explorePOIs();

		updateGoalPOI();

		initializeTreeForTreeSearch();

		searchTree();

		return getOLITSAction();
	}

	protected void initialize()
	{
		// for clarity this method is divided into several more specialistic ones
	}

	private void updatePOIList()
	{
		// update POI list from gameStateTracker
		// set goalPOI to null if it ceased to exist

	}

	private void setGoalPOIIfItDoesntExist()
	{
		if (this.goalNode == null)
		{
			double bestValue = -Double.MAX_VALUE;

			for (TPOLITSTreeNode poiNode : this.nodesNearPOIs)
			{
				double challengerValue = poiNode.getValue();
				if (challengerValue + (randomGenerator.nextDouble() - 0.5) * this.epsilon > bestValue)
				{
					bestValue = challengerValue;
					this.goalNode = poiNode;
				}
			}
		}
	}

	private void chooseMode()
	{
		// check if close to POI and choose time limits for explore and rollout (small time for approach, large for action, according for exploring)
		// choose mode (approach or action) according to whether avatar is close to POI
		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;

		Vector2d avatarPosition = rootStateObsMulti.getAvatarPosition(gameKnowledge.getPlayerID());
		Vector2d poiPosition = this.goalNode.poi.observation.position;
		boolean isAvatarCloseToGoalPOI = fbtpAgentMoveController.arePositionsWithinGivenMoveRange(avatarPosition,
				poiPosition, this.poiCloseDistanceThreshold, fbtpGameKnowledge.getPlayerID());

		TREESEARCHMODE previousMode = this.mode;

		if (isAvatarCloseToGoalPOI)
			this.mode = TREESEARCHMODE.ACTION;
		else
			this.mode = TREESEARCHMODE.APPROACH;

		// set time limits
		// correction needed to take into account the time used by initialization!!!
		if (this.mode == TREESEARCHMODE.APPROACH)
		{
			if (fbtpGameKnowledge.isGameDeterministic())
				this.timeLimitForMoveChoice = 1;
			else
				this.timeLimitForMoveChoice = timeLimitForNonDeterministicPathSearch;
		}
		else
		{
			if (previousMode == TREESEARCHMODE.ACTION)
				this.timeLimitForMoveChoice = (int) (this.totalTimeForChoosingMove
						* this.timePercentageForMoveChoiceInActionModeAfterAction);
			else
				this.timeLimitForMoveChoice = (int) (this.totalTimeForChoosingMove
						* this.timePercentageForMoveChoiceInActionMode);
		}

		this.timeLimitForPOIExploration = this.totalTimeForChoosingMove - this.timeLimitForMoveChoice;
	}

	private void initializeTreeForExploration()
	{
		// advance the states in nodesNearPOIs by one step toward POIs if POI moved,
		// set underlying tree to null children for such trees, but retain their root statistics

		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;

		// check avatar proximity to all POIs
		for (TPOLITSTreeNode node : nodesNearPOIs)
		{
			Vector2d avatarPosition = rootStateObsMulti.getAvatarPosition(gameKnowledge.getPlayerID());
			Vector2d poiPosition = node.poi.observation.position;
			if (node.poi.poiType == POITYPE.SHOT)
			{
				node.poiApproached = false;
				node.path = null;
				node.stateObs = rootStateObsMulti;
			}
			else if (fbtpAgentMoveController.arePositionsWithinGivenMoveRange(avatarPosition, poiPosition,
					this.poiCloseDistanceThreshold, this.gameKnowledge.getPlayerID()))
			{
				node.poiApproached = true;
				node.path = null;
				node.stateObs = rootStateObsMulti;
			}
			else
				node.poiApproached = false;
		}
	}

	private void explorePOIs()
	{
		// explore POIs:
		// - approach to new POIs
		// - shooting scenario (use and wait trying not to die)
		// - rollouts from nodesNearPOIs

		double avgTimeTaken = 0;
		double acumTimeTaken = 0;
		long initialRemaining = mainElapsedTimer.remainingTimeMillis();
		long remaining = this.timeLimitForPOIExploration;
		int numIters = 0;

		while (remaining > 2 * avgTimeTaken && remaining > poiExplorationRemainingLimit)
		{
			ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

			TPOLITSTreeNode selected = choosePOIToExplore(numIters);

			// approach, test shooting or rollout according to whether there is a path and it's close, it's a shooting POI, else rollout
			explorePOI(selected);

			numIters++;
			acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
			LogHandler.writeLog(
					elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")",
					"TPOLITSMovePlanner.explorePOIs", 0);
			avgTimeTaken = acumTimeTaken / numIters;
			remaining = this.timeLimitForPOIExploration - (initialRemaining - mainElapsedTimer.remainingTimeMillis());
		}
	}

	private void explorePOI(TPOLITSTreeNode selected)
	{		
		if (selected.poi.poiType == POITYPE.SHOT)
		{
			exploreShot(selected);
		}
		else if (selected.path == null && !selected.poiApproached)
		{
			exploreApproach(selected);		
		}
		else
		{
			exploreRollOut(selected);
		}
	}

	private void exploreShot(TPOLITSTreeNode selected)
	{
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		
		StateObservationMulti shotStateObs = selected.stateObs.copy();
		Types.ACTIONS playerAction = Types.ACTIONS.ACTION_USE;
		Types.ACTIONS oppAction = fbtpAgentMoveController.getGreedyAction(fbtpGameKnowledge.getOppID());
		
		Types.ACTIONS[] actions = new Types.ACTIONS[2];
		actions[fbtpGameKnowledge.getPlayerID()] = playerAction;
		actions[fbtpGameKnowledge.getOppID()] = oppAction;
		
		shotStateObs.advance(actions);
		
		int turn = 0;
		while (!shotStateObs.isGameOver() && turn < this.shotTestingWaitingTurns)
		{
			int safeTurns = Math.min(3, this.shotTestingWaitingTurns - turn);
			
			playerAction = fbtpAgentMoveController.getNonDyingAction(fbtpGameKnowledge.getPlayerID(), safeTurns);
			oppAction = fbtpAgentMoveController.getGreedyAction(fbtpGameKnowledge.getOppID());
			actions = new Types.ACTIONS[2];
			actions[fbtpGameKnowledge.getPlayerID()] = playerAction;
			actions[fbtpGameKnowledge.getOppID()] = oppAction;
			
			shotStateObs.advance(actions);
			
			turn++;
		}
		
		selected.totalValue += stateEvaluator.evaluateState(shotStateObs); 
		selected.numVisits++;
	}
	
	private void exploreApproach(TPOLITSTreeNode selected)
	{
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;
		Pair<StateObservationMulti, ArrayList<Types.ACTIONS>> approachInfo = null;
		
		if (selected.poi.poiType == POITYPE.SPRITE)
		{
			int minDistance = 2;
			int timeLimit = 1;
			approachInfo = fbtpAgentMoveController.approachSprite(selected.stateObs, this.gameKnowledge.getPlayerID(), selected.poi.observation, minDistance, timeLimit);
		}
		else if (selected.poi.poiType == POITYPE.POSITION)
		{
			// optionally todo
			int minDistance = 2;
			int timeLimit = 1;
			approachInfo = fbtpAgentMoveController.approachPosition(selected.stateObs, this.gameKnowledge.getPlayerID(), selected.poi.observation, minDistance, timeLimit);
		}
		
		if (approachInfo != null)
		{
			selected.stateObs = approachInfo.first();
			selected.path = approachInfo.second();
		}
	}
	
	private void exploreRollOut(TPOLITSTreeNode selected)
	{
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;

		for (int rolloutNr = 0; rolloutNr < this.numOfRolloutsInExplore; rolloutNr++)
		{
			StateObservationMulti rolloutState = selected.stateObs.copy();
	        int depth = 0;

	        while (!rolloutState.isGameOver() && depth < this.maxExploreRolloutDepth)
	        {
	        	Types.ACTIONS randomAction = null;
		        double randomValue = -1;
	        
		        for (Types.ACTIONS action : rolloutState.getAvailableActions()) {
		            double x = this.randomGenerator.nextDouble();
		            if (x > randomValue) {
		            	randomAction = action;
		                randomValue = x;
		            }
		        }
		        
		        Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[fbtpGameKnowledge.getPlayerID()] = randomAction;
				actions[fbtpGameKnowledge.getOppID()] = Types.ACTIONS.ACTION_NIL;
				
		        rolloutState.advance(actions);
		        
		        depth++;
	        }
	        
	        selected.totalValue += stateEvaluator.evaluateState(rolloutState);
	        selected.numVisits++;
		}
	}

	private TPOLITSTreeNode choosePOIToExplore(int numExplorations)
	{
		TPOLITSTreeNode selected = null;
		double bestValue = -Double.MAX_VALUE;

		for (TPOLITSTreeNode node : this.nodesNearPOIs)
		{
			double nodeValue = node.getValue();

			nodeValue = Utils.normalise(nodeValue, this.bounds[0], this.bounds[1]);

			double uctValue = nodeValue
					+ this.uctConstant * Math.sqrt(Math.log(numExplorations + 1) / (node.numVisits + this.epsilon));

			uctValue = Utils.noise(uctValue, this.epsilon, this.randomGenerator.nextDouble()); // break ties randomly

			if (uctValue > bestValue)
			{
				selected = node;
				bestValue = uctValue;
			}
		}

		return selected;
	}

	private void updateGoalPOI()
	{
		// if there was a large change in score of the best POI, change goal POI
		double bestValue = goalNode.getValue();

		for (TPOLITSTreeNode poiNode : this.nodesNearPOIs)
		{
			double challengerValue = poiNode.getValue();
			if (challengerValue > bestValue * goalPOIHandicap)
			{
				bestValue = challengerValue;
				this.goalNode = poiNode;
			}
		}
	}

	private void initializeTreeForTreeSearch()
	{
		if (this.mode == TREESEARCHMODE.ACTION)
		{

		}
	}

	protected void searchTree()
	{
		// in the approach mode use AgentMoveController to choose an action
		// in the action mode do OLETS (OLETS to make it possible to look for long sequences of moves)

		double avgTimeTaken = 0;
		double acumTimeTaken = 0;
		long remaining = mainElapsedTimer.remainingTimeMillis();
		int numIters = 0;

		while (remaining > 2 * avgTimeTaken && remaining > moveChoiceRemainingLimit)
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
		while (node != null)
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

	public static enum TREESEARCHMODE
	{
		APPROACH, ACTION, SHOT
	}
}
