package NextLevel.treeSearchPlanners.moduleTP.TPOLITSPlanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import NextLevel.PointOfInterest;
import NextLevel.PointOfInterest.POITYPE;
import NextLevel.State;
import NextLevel.moduleFB.moduleFBTP.FBTPGameKnowledge;
import NextLevel.moduleFB.moduleFBTP.FBTPGameStateTracker;
import NextLevel.moduleFB.moduleFBTP.FBTPState;
import NextLevel.moduleFB.moduleFBTP.FBTPStateEvaluator;
import NextLevel.moduleFB.moduleFBTP.MechanicsController.FBTPAgentMoveController;
import NextLevel.StateEvaluator;
import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.mechanicsController.GameMechanicsController;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPGameKnowledgeExplorer;
import NextLevel.treeSearchPlanners.TreeNode;
import NextLevel.treeSearchPlanners.TreeSearchMovePlanner;
import NextLevel.utils.AuxUtils;
import NextLevel.utils.LogHandler;
import NextLevel.utils.Pair;
import core.game.Observation;
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
	// protected GameMechanicsController gameMechanicsController;

	protected long initialRemainingTime;

	// protected ArrayList<PointOfInterest> pois;
	protected ArrayList<TPOLITSTreeNode> nodesNearPOIs;
	protected TPOLITSTreeNode goalNode;
	protected int goalNodeIndex;

	protected long totalTimeForChoosingMove;
	// mode: "approach", "action", "shooting"
	// - approach goal POI
	// - choose the best action when close to goal POI
	// - shoot and search for other action in the next move
	protected TREESEARCHMODE mode;
	protected TREESEARCHMODE previousMode;
	protected long timeLimitForPOIExploration;
	protected long timeLimitForTreeSearch;
	protected int numTurnsGoalPOINotChanged = 0;

	// OLETS variables
	// Array of past avatar positions. This is used to give a bias towards exploration of new board locations.
	protected Vector2d[] pastAvatarPositions;
	// Array of past avatar orientations. This is used to give a bias towards exploration of new board locations.
	protected Vector2d[] pastAvatarOrientations;
	// Index used to know where to write the next location/orientation.
	protected int memoryIndex;

	// protected OLETSMODE oletsMode = OLETSMODE.PURE;

	// Algorithm parameters

	protected int moveChoiceRemainingLimit = 6; // in ms
	protected int poiExplorationRemainingLimit = 3; // in ms
	protected double shotScenarioImportance = 0.01;
	protected int poiCloseDistanceThreshold = 3; // expressed in the number of moves to the POI
	protected double timePercentageForMoveChoiceInActionMode = 0.9;
	protected double timePercentageForMoveChoiceInActionModeAfterAction = 0.3;
	// protected double timePercentageForMoveChoiceInApproachMode = 0.1;
	protected long timeLimitForNonDeterministicPathSearch = 2; // in ms
	protected double goalPOIHandicap = 1.5;
	protected double uctConstant = Math.sqrt(2);
	protected double[] bounds = new double[] { -Double.MAX_VALUE, Double.MAX_VALUE };
	protected int numOfRolloutsInExplore = 3;
	protected int shotTestingWaitingTurns = 20;
	protected int maxExploreRolloutDepth = 6;
	protected int oletsDepth = 100;
	// protected double pureVsHeuristicDrivenOLETSproportion = 0.5;
	protected double tabooBias = 0.5;
	// Number of past positions and orientations that are kept in memory for the exploration bias
	protected int memoryLength = 15;

	public TPOLITSMovePlanner(StateEvaluator stateEvaluator, TPGameKnowledge gameKnowledge,
			TPGameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController,
			GameMechanicsController gameMechanicsController, FBTPGameStateTracker gameStateTracker)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
		this.gameKnowledgeExplorer = gameKnowledgeExplorer;
		this.agentMoveController = agentMoveController;
		this.gameMechanicsController = gameMechanicsController;
		this.gameStateTracker = gameStateTracker;

		this.randomGenerator = new Random();

		this.pastAvatarPositions = new Vector2d[memoryLength];
		this.pastAvatarOrientations = new Vector2d[memoryLength];
		this.memoryIndex = 0;

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

		ArrayList<Observation> observations = new ArrayList<Observation>();
		for (PointOfInterest poi : gameStateTracker.getPOIs())
		{
			observations.add(poi.observation);
		}

		HashMap<Integer, Double> spriteEvaluations = fbtpStateEvaluator.evaluateSprites(observations);
		double highestImportance = 0;

		for (PointOfInterest poi : gameStateTracker.getPOIs())
		{
			TPOLITSTreeNode node = new TPOLITSTreeNode(gameKnowledge.getNumOfPlayerActions(), poi);

			if (poi.poiType == POITYPE.SPRITE)
			{
				node.totalValue = spriteEvaluations.get(poi.observation.obsID);
				node.numVisits = 1;

				if (node.totalValue > highestImportance)
					highestImportance = node.totalValue;
			}

			nodesNearPOIs.add(node);
		}

		if (fbtpGameKnowledge.isShootingAllowed())
		{
			PointOfInterest shootingScenario = new PointOfInterest();
			shootingScenario.poiType = POITYPE.SHOT;
			TPOLITSTreeNode node = new TPOLITSTreeNode(gameKnowledge.getNumOfPlayerActions(), shootingScenario);
			node.totalValue = highestImportance + shotScenarioImportance;
			node.numVisits = 1;

			nodesNearPOIs.add(node);
		}
	}

	public ACTIONS chooseAction(State state, ElapsedCpuTimer elapsedTimer, long totalTimeForChoosingMove)
	{
		this.rootState = (FBTPState) state;
		this.rootStateObs = rootState.getStateObservation();
		this.mainElapsedTimer = elapsedTimer;
		this.initialRemainingTime = this.mainElapsedTimer.remainingTimeMillis();
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

		numTurnsGoalPOINotChanged++;

		return getOLITSAction();
	}

	protected void initialize()
	{
		this.previousMode = this.mode;

		if (this.goalNode == null)
		{
			pastAvatarPositions[memoryIndex] = this.rootStateObs.getAvatarPosition();
			pastAvatarOrientations[memoryIndex] = this.rootStateObs.getAvatarOrientation();
			if (memoryIndex < memoryLength - 1)
			{
				memoryIndex += 1;
			}
			else
			{
				memoryIndex = 0;
			}
		}
	}

	private void updatePOIList()
	{
		FBTPStateEvaluator fbtpStateEvaluator = (FBTPStateEvaluator) this.stateEvaluator;

		ArrayList<Observation> observations = new ArrayList<Observation>();
		for (PointOfInterest poi : gameStateTracker.getPOIs())
		{
			observations.add(poi.observation);
		}

		HashMap<Integer, Double> spriteEvaluations = fbtpStateEvaluator.evaluateSprites(observations);

		for (PointOfInterest poi : gameStateTracker.getNewPOIs())
		{
			TPOLITSTreeNode node = new TPOLITSTreeNode(gameKnowledge.getNumOfPlayerActions(), poi);

			if (poi.poiType == POITYPE.SPRITE)
			{
				node.totalValue = spriteEvaluations.get(poi.observation.obsID);
				node.numVisits = 1;
			}

			nodesNearPOIs.add(node);
		}

		for (PointOfInterest poi : gameStateTracker.getRemovedPOIs())
		{
			for (int index = 0; index < this.nodesNearPOIs.size(); index++)
			{
				if (this.nodesNearPOIs.get(index).poi.observation.obsID == poi.observation.obsID)
				{
					if (this.goalNode.poi.observation.obsID == poi.observation.obsID)
					{
						this.goalNode = null;
					}
					this.nodesNearPOIs.remove(index);
					break;
				}
			}
		}
	}

	private void setGoalPOIIfItDoesntExist()
	{
		if (this.goalNode == null)
		{
			double bestValue = -Double.MAX_VALUE;

			for (int index = 0; index < this.nodesNearPOIs.size(); index++)
			{
				TPOLITSTreeNode poiNode = this.nodesNearPOIs.get(index);
				double challengerValue = poiNode.getValue();
				if (challengerValue + (randomGenerator.nextDouble() - 0.5) * this.epsilon > bestValue)
				{
					bestValue = challengerValue;
					this.goalNode = poiNode;
					this.goalNodeIndex = index;
					numTurnsGoalPOINotChanged = 0;
				}
			}
		}
	}

	private void chooseMode()
	{
		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;

		Vector2d avatarPosition = rootStateObsMulti.getAvatarPosition(gameKnowledge.getPlayerID());
		Vector2d poiPosition = this.goalNode.poi.observation.position;
		boolean isAvatarCloseToGoalPOI = fbtpAgentMoveController.arePositionsWithinGivenMoveRange(rootStateObsMulti,
				avatarPosition, poiPosition, this.poiCloseDistanceThreshold, fbtpGameKnowledge.getPlayerID());

		TREESEARCHMODE previousMode = this.mode;

		if (isAvatarCloseToGoalPOI)
			this.mode = TREESEARCHMODE.ACTION;
		else
			this.mode = TREESEARCHMODE.APPROACH;

		// set time limits
		long timeAlreadyUsed = this.initialRemainingTime - this.mainElapsedTimer.remainingTimeMillis();
		long remainingTimeForChoosingMove = this.totalTimeForChoosingMove - timeAlreadyUsed;

		LogHandler.writeLog("Time already used for move planner initialization: " + timeAlreadyUsed,
				"TPOLITSMovePlanner.chooseMode", 1);

		if (this.mode == TREESEARCHMODE.APPROACH)
		{
			if (fbtpGameKnowledge.isGameDeterministic())
				this.timeLimitForTreeSearch = 1;
			else
				this.timeLimitForTreeSearch = timeLimitForNonDeterministicPathSearch;
		}
		else
		{
			if (previousMode == TREESEARCHMODE.ACTION)
				this.timeLimitForTreeSearch = (int) (remainingTimeForChoosingMove
						* this.timePercentageForMoveChoiceInActionModeAfterAction);
			else
				this.timeLimitForTreeSearch = (int) (remainingTimeForChoosingMove
						* this.timePercentageForMoveChoiceInActionMode);
		}

		this.timeLimitForPOIExploration = remainingTimeForChoosingMove - this.timeLimitForTreeSearch;
	}

	private void initializeTreeForExploration()
	{
		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;

		for (TPOLITSTreeNode node : nodesNearPOIs)
		{
			if (this.gameMechanicsController.isMovingAction(
					rootStateObsMulti.getAvatarLastAction(gameKnowledge.getPlayerID())) && node != this.goalNode)
			{
				node.path = null;
			}

			Vector2d avatarPosition = rootStateObsMulti.getAvatarPosition(gameKnowledge.getPlayerID());
			Vector2d poiPosition = node.poi.observation.position;
			if (node.poi.poiType == POITYPE.SHOT)
			{
				node.poiApproached = false;
				node.stateObs = rootStateObsMulti;
			}
			else if (fbtpAgentMoveController.arePositionsWithinGivenMoveRange(rootStateObsMulti, avatarPosition,
					poiPosition, this.poiCloseDistanceThreshold, this.gameKnowledge.getPlayerID()))
			{
				node.poiApproached = true;
				node.path = null;
				node.stateObs = rootStateObsMulti;
			}
			else if (node.poi.positionChangedFromPreviousTurn)
			{
				Pair<StateObservationMulti, ArrayList<Types.ACTIONS>> approachInfo = null;

				if (node.poi.poiType == POITYPE.SPRITE)
				{
					int maxDistance = 2;
					int timeLimit = 1;
					approachInfo = fbtpAgentMoveController.approachSprite(node.stateObs,
							this.gameKnowledge.getPlayerID(), node.poi.observation, maxDistance, timeLimit);
				}
				else if (node.poi.poiType == POITYPE.POSITION)
				{
					int maxDistance = 2;
					int timeLimit = 1;
					approachInfo = fbtpAgentMoveController.approachPosition(node.stateObs,
							this.gameKnowledge.getPlayerID(), node.poi.position, maxDistance, timeLimit);
				}

				node.stateObs = approachInfo.first();
				node.path = null; // One could consider merging paths to reduce the number of invocations of path finding, however this may lead to very non-optimal paths
			}
			else
				node.poiApproached = false;
		}
	}

	private void explorePOIs()
	{
		double avgTimeTaken = 0;
		double acumTimeTaken = 0;
		long initialRemaining = mainElapsedTimer.remainingTimeMillis();
		long remaining = this.timeLimitForPOIExploration;
		int numIters = 0;

		while (remaining > 2 * avgTimeTaken && remaining > poiExplorationRemainingLimit)
		{
			ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

			TPOLITSTreeNode selected = choosePOIToExplore(numIters);

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
		Types.ACTIONS oppAction = fbtpAgentMoveController.getGreedyAction(shotStateObs, fbtpGameKnowledge.getOppID());

		Types.ACTIONS[] actions = new Types.ACTIONS[2];
		actions[fbtpGameKnowledge.getPlayerID()] = playerAction;
		actions[fbtpGameKnowledge.getOppID()] = oppAction;

		shotStateObs.advance(actions);

		int turn = 0;
		while (!shotStateObs.isGameOver() && turn < this.shotTestingWaitingTurns)
		{
			int safeTurns = Math.min(3, this.shotTestingWaitingTurns - turn);

			playerAction = fbtpAgentMoveController.getNonDyingAction(shotStateObs, fbtpGameKnowledge.getPlayerID(),
					safeTurns);
			oppAction = fbtpAgentMoveController.getGreedyAction(shotStateObs, fbtpGameKnowledge.getOppID());
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
			int maxDistance = 2;
			int timeLimit = 1;
			approachInfo = fbtpAgentMoveController.approachSprite(selected.stateObs, this.gameKnowledge.getPlayerID(),
					selected.poi.observation, maxDistance, timeLimit);
		}
		else if (selected.poi.poiType == POITYPE.POSITION)
		{
			int maxDistance = 2;
			int timeLimit = 1;
			approachInfo = fbtpAgentMoveController.approachPosition(selected.stateObs, this.gameKnowledge.getPlayerID(),
					selected.poi.position, maxDistance, timeLimit);
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
				Types.ACTIONS randomAction = rolloutState.getAvailableActions()
						.get(this.randomGenerator.nextInt(rolloutState.getAvailableActions().size()));

				/*
				 * Types.ACTIONS randomAction = null;
				 * double randomValue = -1;
				 * 
				 * for (Types.ACTIONS action : rolloutState.getAvailableActions()) {
				 * double x = this.randomGenerator.nextDouble();
				 * if (x > randomValue) {
				 * randomAction = action;
				 * randomValue = x;
				 * }
				 * }
				 */

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

	private void updateGoalPOI()
	{
		// if there was a large change in score of the best POI, change goal POI
		double bestValue = goalNode.getValue();

		for (int index = 0; index < this.nodesNearPOIs.size(); index++)
		{
			TPOLITSTreeNode poiNode = this.nodesNearPOIs.get(index);
			double challengerValue = poiNode.getValue();
			if (challengerValue > bestValue * goalPOIHandicap)
			{
				bestValue = challengerValue;
				this.goalNode = poiNode;
				this.goalNodeIndex = index;
				numTurnsGoalPOINotChanged = 0;
			}
		}
	}

	private void initializeTreeForTreeSearch()
	{
		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;

		if (this.mode == TREESEARCHMODE.ACTION)
		{
			if (this.previousMode == TREESEARCHMODE.ACTION && this.numTurnsGoalPOINotChanged > 0)
			{
				// shift the tree near goalPOI by the made move
				Types.ACTIONS lastAction = rootStateObsMulti.getAvatarLastAction(fbtpGameKnowledge.getPlayerID());
				TPOLITSTreeNode childChosenInLastMove = (TPOLITSTreeNode) this.goalNode.children[AuxUtils
						.actionToIndex(rootStateObsMulti, fbtpGameKnowledge.getPlayerID(), lastAction)];

				childChosenInLastMove.poi = goalNode.poi;
				childChosenInLastMove.stateObs = rootStateObsMulti;
				goalNode = childChosenInLastMove;
				this.nodesNearPOIs.set(this.goalNodeIndex, childChosenInLastMove);
				this.goalNode.parent = null;
				this.goalNode.refreshTree(0);
				this.rootNode = goalNode;
			}
		}
	}

	protected void searchTree()
	{
		if (this.mode == TREESEARCHMODE.ACTION)
		{
			searchTreeAction();
		}
		else if (this.mode == TREESEARCHMODE.APPROACH)
		{
			searchTreeApproach();
		}
		else if (this.mode == TREESEARCHMODE.SHOT)
		{
			searchTreeShot();
		}
	}

	protected void searchTreeAction()
	{
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
					"TreeSearchMovePlanner.searchTreeAction", 0);
			avgTimeTaken = acumTimeTaken / numIters;
			remaining = mainElapsedTimer.remainingTimeMillis();
		}
	}

	protected void searchTreeApproach()
	{
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;

		if (this.numTurnsGoalPOINotChanged > 0 && this.goalNode.path != null)
		{
			if (!fbtpGameKnowledge.isGameDeterministic())
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[fbtpGameKnowledge.getPlayerID()] = this.goalNode.path.get(0);
				actions[fbtpGameKnowledge.getOppID()] = fbtpAgentMoveController.getGreedyAction(rootStateObsMulti,
						fbtpGameKnowledge.getOppID());

				StateObservationMulti simulationStateObs = goalNode.stateObs.copy();
				simulationStateObs.advance(actions);

				if (simulationStateObs.isGameOver())
				{
					Pair<StateObservationMulti, ArrayList<Types.ACTIONS>> approachInfo = null;

					if (goalNode.poi.poiType == POITYPE.SPRITE)
					{
						int maxDistance = 2;
						long timeLimit = this.timeLimitForTreeSearch - 1;
						approachInfo = fbtpAgentMoveController.approachSprite(rootStateObsMulti,
								this.gameKnowledge.getPlayerID(), goalNode.poi.observation, maxDistance, timeLimit);
					}
					else if (goalNode.poi.poiType == POITYPE.POSITION)
					{
						int maxDistance = 2;
						long timeLimit = this.timeLimitForTreeSearch - 1;
						approachInfo = fbtpAgentMoveController.approachPosition(rootStateObsMulti,
								this.gameKnowledge.getPlayerID(), goalNode.poi.position, maxDistance, timeLimit);
					}

					if (approachInfo != null)
					{
						goalNode.stateObs = approachInfo.first();
						goalNode.path = approachInfo.second();
					}
				}
			}
		}
		else

		{
			Pair<StateObservationMulti, ArrayList<Types.ACTIONS>> approachInfo = null;

			if (goalNode.poi.poiType == POITYPE.SPRITE)
			{
				int maxDistance = 2;
				long timeLimit = this.timeLimitForTreeSearch - 1;
				approachInfo = fbtpAgentMoveController.approachSprite(rootStateObsMulti,
						this.gameKnowledge.getPlayerID(), goalNode.poi.observation, maxDistance, timeLimit);
			}
			else if (goalNode.poi.poiType == POITYPE.POSITION)
			{
				int maxDistance = 2;
				long timeLimit = this.timeLimitForTreeSearch - 1;
				approachInfo = fbtpAgentMoveController.approachPosition(rootStateObsMulti,
						this.gameKnowledge.getPlayerID(), goalNode.poi.position, maxDistance, timeLimit);
			}

			if (approachInfo != null)
			{
				goalNode.stateObs = approachInfo.first();
				goalNode.path = approachInfo.second();
			}
		}
	}

	protected void searchTreeShot()
	{
		// nothing needs to be done
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
		return (stateObs.isGameOver() || currentNode.depth > this.oletsDepth);
	}

	protected double rollOut(TreeNode selectedNode, StateObservation stateObs)
	{
		// no rollout needed in OLETS

		return stateEvaluator.evaluateState(stateObs);
	}

	protected boolean isRolloutFinished(StateObservation rollerState, int depth)
	{
		return true;
	}

	protected void backUp(TreeNode node, double delta)
	{
		TPOLITSTreeNode n = (TPOLITSTreeNode) node;
		int backUpDepth = 0;
		while (n != null)
		{
			n.numVisits++;
			n.nbGenerated++;
			n.totalValue += delta;
			if (backUpDepth > 0)
			{
				double bestExpectimax = -Double.MAX_VALUE;
				double bestAdjustedExpectimax = -Double.MAX_VALUE;
				for (int i = 0; i < n.children.length; i++)
				{
					TPOLITSTreeNode child = (TPOLITSTreeNode) (TPOLITSTreeNode) n.children[i];
					if (n.children[i] != null)
					{
						if (child.expectimax > bestExpectimax)
						{
							bestExpectimax = child.expectimax;
						}
						if (child.adjEmax > bestAdjustedExpectimax)
						{
							bestAdjustedExpectimax = child.adjEmax;
						}
					}
				}

				n.expectimax = bestExpectimax;
				n.childrenMaxAdjEmax = bestAdjustedExpectimax;
				n.adjEmax = (((float) n.nbExitsHere) / n.numVisits) * (n.totalValueOnExit / n.nbExitsHere)
						+ (1.0 - (((float) n.nbExitsHere) / n.numVisits)) * n.childrenMaxAdjEmax;
			}
			else
			{
				n.nbExitsHere += 1;
				n.totalValueOnExit += delta;

				n.adjEmax = (((float) n.nbExitsHere) / n.numVisits) * (n.totalValueOnExit / n.nbExitsHere)
						+ (1.0 - (((float) n.nbExitsHere) / n.numVisits)) * n.childrenMaxAdjEmax;
				n.expectimax = n.totalValue / n.numVisits;
			}

			n = (TPOLITSTreeNode) n.parent;
			backUpDepth += 1;
		}
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
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		TPOLITSTreeNode tpolmctsNode = (TPOLITSTreeNode) node;

		int bestAction = 0;
		double bestValue = -1;

		for (int i = 0; i < tpolmctsNode.children.length; i++)
		{
			if (tpolmctsNode.children[i] == null)
			{
				double x = this.randomGenerator.nextDouble();
				if (x > bestValue)
				{
					bestAction = i;
					bestValue = x;
				}
			}
		}

		Types.ACTIONS[] actions = new Types.ACTIONS[2];
		actions[fbtpGameKnowledge.getPlayerID()] = stateObsMulti.getAvailableActions(fbtpGameKnowledge.getPlayerID())
				.get(bestAction);
		actions[fbtpGameKnowledge.getOppID()] = fbtpAgentMoveController.getGreedyAction(stateObsMulti,
				fbtpGameKnowledge.getOppID());

		stateObsMulti.advance(actions);

		double tabooBias = 0.0;
		int i = 0;
		boolean stateFound = false;
		while ((!stateFound) && (i < this.memoryLength) && (this.pastAvatarPositions[i] != null))
		{
			if (this.pastAvatarPositions[i].equals(stateObsMulti.getAvatarPosition()))
			{
				tabooBias += this.tabooBias;
				stateFound = true;
			}
			i++;
		}

		TPOLITSTreeNode child = new TPOLITSTreeNode(tpolmctsNode, gameKnowledge.getNumOfPlayerActions(),
				tpolmctsNode.depth + 1, bestAction, tabooBias);
		tpolmctsNode.children[bestAction] = child;

		return child;
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
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		TPOLITSTreeNode tpolmctsNode = (TPOLITSTreeNode) node;

		TPOLITSTreeNode selected = null;
		double bestValue = -Double.MAX_VALUE;

		// pick the best Q.
		for (TPOLITSTreeNode child : (TPOLITSTreeNode[]) tpolmctsNode.children)
		{
			double score = Utils.noise(getNodeAdjustedEmaxScore(child), this.epsilon,
					this.randomGenerator.nextDouble());
			if (score > bestValue)
			{
				selected = child;
				bestValue = score;
			}
		}

		Types.ACTIONS[] actions = new Types.ACTIONS[2];
		actions[fbtpGameKnowledge.getPlayerID()] = stateObsMulti.getAvailableActions(gameKnowledge.getPlayerID())
				.get(selected.actionIndex);
		actions[fbtpGameKnowledge.getOppID()] = fbtpAgentMoveController.getGreedyAction(stateObsMulti,
				fbtpGameKnowledge.getOppID());

		stateObsMulti.advance(actions);

		double tabooBias = 0.0;
		if (selected.nbGenerated == 0)
		{
			tabooBias = 0.0;
			int i = 0;
			boolean stateFound = false;
			while ((!stateFound) && (i < memoryLength) && (this.pastAvatarPositions[i] != null))
			{
				if (this.pastAvatarPositions[i].equals(stateObsMulti.getAvatarPosition()))
				{
					tabooBias += this.tabooBias;
					stateFound = true;
				}
				i++;
			}
			selected.tabooBias = tabooBias;
		}

		return selected;
	}

	/**
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Actions for both players.
	 */
	public void moveInRollout(StateObservation stateObs)
	{
		// not needed in OLETS
	}

	private ACTIONS getOLITSAction()
	{
		Types.ACTIONS action = null;

		if (this.mode == TREESEARCHMODE.ACTION)
		{
			action = getMostVisitedAction();
		}
		else if (this.mode == TREESEARCHMODE.APPROACH)
		{
			if (this.goalNode.path != null && this.goalNode.path.size() > 0)
			{
				action = this.goalNode.path.get(0);
				this.goalNode.path.remove(0);
			}
			else
			{
				action = Types.ACTIONS.ACTION_NIL;
			}
		}
		else if (this.mode == TREESEARCHMODE.SHOT)
		{
			action = Types.ACTIONS.ACTION_USE;
		}

		return action;
	}

	/**
	 * Computes the weighted expectimax of a node, minus a location bias to increase the value of nodes in locations that
	 * have not been visited often in the past
	 * 
	 * @return the weighted expectimax with location bias
	 */
	public double getNodeAdjustedEmaxScore(TPOLITSTreeNode node)
	{
		return (node.adjEmax
				+ this.uctConstant * Math.sqrt(Math.log(node.parent.numVisits + 1) / (node.numVisits + this.epsilon))
				- node.tabooBias);
	}

	public void setMoveChoiceRemainingLimit(int moveChoiceRemainingLimit)
	{
		this.moveChoiceRemainingLimit = moveChoiceRemainingLimit;
	}

	public void setPoiExplorationRemainingLimit(int poiExplorationRemainingLimit)
	{
		this.poiExplorationRemainingLimit = poiExplorationRemainingLimit;
	}

	public void setShotScenarioImportance(double shotScenarioImportance)
	{
		this.shotScenarioImportance = shotScenarioImportance;
	}

	public void setPoiCloseDistanceThreshold(int poiCloseDistanceThreshold)
	{
		this.poiCloseDistanceThreshold = poiCloseDistanceThreshold;
	}

	public void setTimePercentageForMoveChoiceInActionMode(double timePercentageForMoveChoiceInActionMode)
	{
		this.timePercentageForMoveChoiceInActionMode = timePercentageForMoveChoiceInActionMode;
	}

	public void setTimePercentageForMoveChoiceInActionModeAfterAction(
			double timePercentageForMoveChoiceInActionModeAfterAction)
	{
		this.timePercentageForMoveChoiceInActionModeAfterAction = timePercentageForMoveChoiceInActionModeAfterAction;
	}

	public void setTimeLimitForNonDeterministicPathSearch(long timeLimitForNonDeterministicPathSearch)
	{
		this.timeLimitForNonDeterministicPathSearch = timeLimitForNonDeterministicPathSearch;
	}

	public void setGoalPOIHandicap(double goalPOIHandicap)
	{
		this.goalPOIHandicap = goalPOIHandicap;
	}

	public void setUctConstant(double uctConstant)
	{
		this.uctConstant = uctConstant;
	}

	public void setBounds(double[] bounds)
	{
		this.bounds = bounds;
	}

	public void setNumOfRolloutsInExplore(int numOfRolloutsInExplore)
	{
		this.numOfRolloutsInExplore = numOfRolloutsInExplore;
	}

	public void setShotTestingWaitingTurns(int shotTestingWaitingTurns)
	{
		this.shotTestingWaitingTurns = shotTestingWaitingTurns;
	}

	public void setMaxExploreRolloutDepth(int maxExploreRolloutDepth)
	{
		this.maxExploreRolloutDepth = maxExploreRolloutDepth;
	}

	public void setOletsDepth(int oletsDepth)
	{
		this.oletsDepth = oletsDepth;
	}

	public void setTabooBias(double tabooBias)
	{
		this.tabooBias = tabooBias;
	}

	public void setMemoryLength(int memoryLength)
	{
		this.memoryLength = memoryLength;
	}

	public static enum TREESEARCHMODE
	{
		APPROACH, ACTION, SHOT
	}

	public static enum OLETSMODE
	{
		PURE, HEURISTIC, RANDOM
	}
}
