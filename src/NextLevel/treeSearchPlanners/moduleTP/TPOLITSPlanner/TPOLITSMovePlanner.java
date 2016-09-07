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
import NextLevel.utils.PerformanceMonitor;
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
	protected TPOLITSTreeNode shootingScenarioNode;

	protected long totalTimeForChoosingMove;
	// mode: "approach", "action", "shooting"
	// - approach goal POI
	// - choose the best action when close to goal POI
	// - shoot and search for other action in the next move
	protected TREESEARCHMODE mode = TREESEARCHMODE.NOMODE;
	protected TREESEARCHMODE previousMode = TREESEARCHMODE.NOMODE;
	protected long timeLimitForPOIExploration;
	protected long timeLimitForSinglePOIExplore;
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

	protected int moveChoiceRemainingLimit = 5; // in ms
	protected int poiExplorationRemainingLimit = 5; // in ms
	protected double shotScenarioImportance = 0.01;
	protected int poiCloseDistanceThreshold = 3; // expressed in the number of moves to the POI
	protected double timePercentageForMoveChoiceInActionMode = 0.75;
	protected double timePercentageForMoveChoiceInActionModeAfterAction = 0.3;
	// protected double timePercentageForMoveChoiceInApproachMode = 0.1;
	protected long timeLimitForDeterministicPathSearch = 100; // in ms
	protected long timeLimitForNonDeterministicPathSearch = 200; // in ms
	protected long timeLimitForOneAdvance = 1; // in ms;
	protected int minimumNumOfPOIExplorations = 3;
	protected double goalPOIHandicap = 1.5;
	protected double uctConstantInExplore = 5;
	protected double uctConstant = Math.sqrt(2);
	protected double[] bounds = new double[] { -Double.MAX_VALUE, Double.MAX_VALUE };
	protected int numOfRolloutsInExplore = 10;
	protected int shotTestingWaitingTurns = 20;
	protected int maxExploreRolloutDepth = 10;
	protected int oletsDepth = 50;
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
		this.shootingScenarioNode = null;
	}

	public void initialize(StateObservation stateObs)
	{
		this.rootStateObs = stateObs;
		initializePOINodes();
	}

	private void initializePOINodes()
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.initializePOINodes", 3);

		FBTPStateEvaluator fbtpStateEvaluator = (FBTPStateEvaluator) this.stateEvaluator;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;

		// initialize list of POIs from gameStateTracker + optionally add regular grid on the map
		nodesNearPOIs = new ArrayList<TPOLITSTreeNode>();

		ArrayList<Observation> observations = new ArrayList<Observation>();
		for (PointOfInterest poi : gameStateTracker.getPOIs())
		{
			observations.add(poi.observation);
		}

		HashMap<Integer, Double> spriteEvaluations = fbtpStateEvaluator.evaluateSprites(rootStateObsMulti,
				observations);
		// double highestImportance = 0;

		for (PointOfInterest poi : gameStateTracker.getPOIs())
		{
			TPOLITSTreeNode node = new TPOLITSTreeNode(gameKnowledge.getNumOfPlayerActions(), poi);

			if (poi.poiType == POITYPE.SPRITE)
			{
				node.totalValue = spriteEvaluations.get(poi.observation.obsID);
				node.numVisits = 1;

				// if (node.totalValue > highestImportance)
				// highestImportance = node.totalValue;

				LogHandler.writeLog("POI id: " + poi.observation.obsID + ", type: " + poi.observation.itype
						+ ", category: " + poi.observation.category + ", position: " + poi.position + ", value: "
						+ node.totalValue, "TPOLITSMovePlanner.initializePOINodes", 3);
			}

			nodesNearPOIs.add(node);
		}

		// LogHandler.writeLog("Highest importance: " + highestImportance, "TPOLITSMovePlanner.initializePOINodes", 3);

		if (fbtpGameKnowledge.isShootingAllowed())
		{
			PointOfInterest shootingScenario = new PointOfInterest(POITYPE.SHOT,
					rootStateObsMulti.getAvatarPosition(fbtpGameKnowledge.getPlayerID()));
			shootingScenario.poiType = POITYPE.SHOT;
			TPOLITSTreeNode node = new TPOLITSTreeNode(gameKnowledge.getNumOfPlayerActions(), shootingScenario);
			node.totalValue = 0; // highestImportance + shotScenarioImportance;
			node.numVisits = 1;

			this.shootingScenarioNode = node;
			nodesNearPOIs.add(node);

			LogHandler.writeLog("Shot POI> position: " + node.poi.position + ", value: " + node.totalValue,
					"TPOLITSMovePlanner.initializePOINodes", 3);
		}

		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.initializePOINodes", 3);
	}

	public ACTIONS chooseAction(State state, ElapsedCpuTimer elapsedTimer, long timeForChoosingMove)
	{
		this.rootState = (FBTPState) state;
		this.rootStateObs = rootState.getStateObservation();
		this.mainElapsedTimer = elapsedTimer;
		this.initialRemainingTime = this.mainElapsedTimer.remainingTimeMillis();
		this.totalTimeForChoosingMove = timeForChoosingMove;

		initializeForChooseAction();

		updatePOIList();

		setGoalPOI();

		chooseMode();

		initializeForExploration();

		explorePOIs();

		updateGoalPOI();

		initializeForTreeSearch();

		searchTree();

		numTurnsGoalPOINotChanged++;

		return getOLITSAction();
	}

	protected void initializeForChooseAction()
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.initializeForChooseAction", 3);
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
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.initializeForChooseAction", 3);
	}

	protected void updatePOIList()
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.updatePOIList", 3);
		FBTPStateEvaluator fbtpStateEvaluator = (FBTPStateEvaluator) this.stateEvaluator;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;

		// Evaluate new POIs

		ArrayList<Observation> newObservations = new ArrayList<Observation>();
		for (PointOfInterest poi : gameStateTracker.getNewPOIs())
		{
			newObservations.add(poi.observation);
		}

		HashMap<Integer, Double> spriteEvaluations = fbtpStateEvaluator.evaluateSprites(rootStateObsMulti,
				newObservations);

		// Add new POIs to nodesNearPOIs with their initial evaluation

		for (PointOfInterest poi : gameStateTracker.getNewPOIs())
		{
			TPOLITSTreeNode node = new TPOLITSTreeNode(gameKnowledge.getNumOfPlayerActions(), poi);

			if (poi.poiType == POITYPE.SPRITE)
			{
				node.totalValue = spriteEvaluations.get(poi.observation.obsID);
				node.numVisits = 1;

				LogHandler.writeLog(
						"POI id: " + poi.observation.obsID + ", category: " + poi.observation.category + ", type: "
								+ poi.observation.itype + ", position: " + poi.position + ", value: " + node.totalValue,
						"TPOLITSMovePlanner.updatePOIList", 3);
			}

			nodesNearPOIs.add(node);
		}

		// TODO Consider making an adjustment in the value of all POIs according to their current evaluation.
		// However, this evaluation should already be taken into account as existing POI can only be added for evaluation
		// in GKE by the move planner.

		for (PointOfInterest poi : gameStateTracker.getRemovedPOIs())
		{
			for (int index = 0; index < this.nodesNearPOIs.size(); index++)
			{
				if (this.nodesNearPOIs.get(index).poi.poiType == POITYPE.SPRITE
						&& this.nodesNearPOIs.get(index).poi.observation.obsID == poi.observation.obsID)
				{
					if (this.goalNode != null && this.goalNode.poi.poiType == POITYPE.SPRITE
							&& this.goalNode.poi.observation.obsID == poi.observation.obsID)
					{
						this.goalNode = null;
					}
					this.nodesNearPOIs.remove(index);
					break;
				}
			}
		}

		if (shootingScenarioNode != null)
		{
			shootingScenarioNode.poi.position = rootStateObsMulti.getAvatarPosition(fbtpGameKnowledge.getPlayerID());
		}

		LogHandler.writeLog("Number of POIs: " + this.nodesNearPOIs.size(), "TPOLITSMovePlanner.updatePOIList", 3);
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.updatePOIList", 3);
	}

	protected void setGoalPOI()
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.setGoalPOI", 3);
		double bestValue = -Double.MAX_VALUE;

		for (int index = 0; index < this.nodesNearPOIs.size(); index++)
		{
			TPOLITSTreeNode poiNode = this.nodesNearPOIs.get(index);
			if (poiNode.poi.poiType != POITYPE.SHOT)
			{
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

		LogHandler
				.writeLog(
						"Goal node type: " + goalNode.poi.poiType + ", position: " + goalNode.poi.position + ", value: "
								+ goalNode.getValue() + ((goalNode.poi.observation != null)
										? ", POI type: " + goalNode.poi.observation.itype : ""),
						"TPOLITSMovePlanner.setGoalPOI", 3);
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.setGoalPOI", 3);
	}

	protected void chooseMode()
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.chooseMode", 3);
		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;

		Vector2d poiPosition = this.goalNode.poi.position;
		boolean isAvatarCloseToGoalPOI = fbtpAgentMoveController.isPositionWithinGivenMoveRange(rootStateObsMulti,
				poiPosition, this.poiCloseDistanceThreshold, fbtpGameKnowledge.getPlayerID());

		LogHandler.writeLog("Player position: " + rootStateObsMulti.getAvatarPosition(fbtpGameKnowledge.getPlayerID())
				+ " Goal POI position: " + goalNode.poi.position + " Close: "
				+ ((isAvatarCloseToGoalPOI) ? "yes" : "no"), "TPOLITSMovePlanner.chooseMode", 3);

		TREESEARCHMODE previousMode = this.mode;

		if (isAvatarCloseToGoalPOI)
			this.mode = TREESEARCHMODE.ACTION;
		else
			this.mode = TREESEARCHMODE.APPROACH;

		// set time limits
		long timeAlreadyUsed = this.initialRemainingTime - this.mainElapsedTimer.remainingTimeMillis();
		long remainingTimeForChoosingMove = this.totalTimeForChoosingMove - timeAlreadyUsed;

		LogHandler.writeLog("Time used for move planner initialization: " + timeAlreadyUsed,
				"TPOLITSMovePlanner.chooseMode", 3);

		if (remainingTimeForChoosingMove > 0)
		{
			if (this.mode == TREESEARCHMODE.APPROACH)
			{
				if (fbtpGameKnowledge.isDeterministicGame())
					this.timeLimitForTreeSearch = timeLimitForDeterministicPathSearch;
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
			this.timeLimitForSinglePOIExplore = (int) ((double) timeLimitForPOIExploration
					/ minimumNumOfPOIExplorations);

			if (timeLimitForSinglePOIExplore == 0)
			{
				this.timeLimitForTreeSearch = remainingTimeForChoosingMove;
				this.timeLimitForPOIExploration = 0;
			}
		}
		else
		{
			this.timeLimitForTreeSearch = 0;
			this.timeLimitForPOIExploration = 0;
		}

		LogHandler.writeLog("Time for POI exploration: " + timeLimitForPOIExploration, "TPOLITSMovePlanner.chooseMode",
				3);
		LogHandler.writeLog("Time for single POI exploration: " + timeLimitForSinglePOIExplore,
				"TPOLITSMovePlanner.chooseMode", 3);
		LogHandler.writeLog("Time for tree search: " + timeLimitForTreeSearch, "TPOLITSMovePlanner.chooseMode", 3);

		LogHandler.writeLog("Chosen mode: " + this.mode, "TPOLITSMovePlanner.chooseMode", 3);
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.chooseMode", 3);
	}

	protected void initializeForExploration()
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.initializeForExploration", 3);
		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;

		for (TPOLITSTreeNode node : nodesNearPOIs)
		{
			if (this.gameMechanicsController.isMovingAction(
					rootStateObsMulti.getAvatarLastAction(gameKnowledge.getPlayerID())) && node != this.goalNode)
			{
				node.path = null;
			}

			Vector2d poiPosition = node.poi.position;
			if (node.poi.poiType == POITYPE.SHOT)
			{
				node.totalValue = 0;
				node.poiApproached = false;
				node.stateObs = rootStateObsMulti;
				LogHandler.writeLog("Shot scenario> POI type: " + node.poi.poiType + ", position: " + node.poi.position,
						"TPOLITSMovePlanner.initializeForExploration", 3);
			}
			else if (fbtpAgentMoveController.isPositionWithinGivenMoveRange(rootStateObsMulti, poiPosition,
					this.poiCloseDistanceThreshold, this.gameKnowledge.getPlayerID()))
			{
				node.poiApproached = true;
				node.path = null;
				node.stateObs = rootStateObsMulti;
				LogHandler.writeLog(
						"POI approached scenario> POI type: " + node.poi.poiType + ", position: " + node.poi.position,
						"TPOLITSMovePlanner.initializeForExploration", 3);
			}
			else if (node.poi.positionChangedFromPreviousTurn)
			{
				node.poiApproached = false;

				if (node.stateObs != null) // if a stateObs close to POI has already been found earlier, move this state towards POI
				{
					Pair<StateObservationMulti, ArrayList<Types.ACTIONS>> approachInfo = null;

					if (node.poi.poiType == POITYPE.SPRITE)
					{
						int maxDistance = 2;
						int timeLimit = 20;
						approachInfo = fbtpAgentMoveController.approachSprite(node.stateObs,
								this.gameKnowledge.getPlayerID(), node.poi.observation, maxDistance, timeLimit);
					}
					else if (node.poi.poiType == POITYPE.POSITION)
					{
						int maxDistance = 2;
						int timeLimit = 20;
						approachInfo = fbtpAgentMoveController.reachPosition(node.stateObs,
								this.gameKnowledge.getPlayerID(), node.poi.position, maxDistance, timeLimit);
					}

					if (approachInfo != null)
						node.stateObs = approachInfo.first();
					node.path = null; // One could consider merging paths to reduce the number of invocations of path finding, however this may lead to very non-optimal paths

					LogHandler.writeLog(
							"POI not approached scenario>" + " short approach successful: "
									+ ((approachInfo != null) ? "yes" : "no") + ", POI type: " + node.poi.poiType
									+ ", position: " + node.poi.position,
							"TPOLITSMovePlanner.initializeForExploration", 3);
				}
				else
				{
					LogHandler.writeLog("POI not approached scenario> no stateObs, POI type: " + node.poi.poiType
							+ ", position: " + node.poi.position, "TPOLITSMovePlanner.initializeForExploration", 3);
				}
			}
			else
				node.poiApproached = false;
		}
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.initializeForExploration", 3);
	}

	protected void explorePOIs()
	{
		double avgTimeTaken = 0;
		double acumTimeTaken = 0;
		long initialRemaining = mainElapsedTimer.remainingTimeMillis();
		long remaining = this.timeLimitForPOIExploration;
		int numIters = 0;

		while (remaining > 2 * avgTimeTaken && remaining > timeLimitForSinglePOIExplore)
		{
			ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

			TPOLITSTreeNode selected = choosePOIToExplore(numIters);

			explorePOI(selected);

			numIters++;
			acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
			avgTimeTaken = acumTimeTaken / numIters;
			remaining = this.timeLimitForPOIExploration - (initialRemaining - mainElapsedTimer.remainingTimeMillis());
			LogHandler
					.writeLog(
							"Iteration time: " + elapsedTimerIteration.elapsedMillis() + " | cumulative time taken: "
									+ acumTimeTaken + " | remaining: " + remaining + ")",
							"TPOLITSMovePlanner.explorePOIs", 3);
		}
	}

	protected TPOLITSTreeNode choosePOIToExplore(int numExplorations)
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.choosePOIToExplore", 3);
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;
		TPOLITSTreeNode selected = null;
		double bestValue = -Double.MAX_VALUE;

		if (fbtpGameKnowledge.isShootingAllowed() && numExplorations == 0
				&& !gameMechanicsController.isFromAvatarSpriteOnTheMap(rootStateObsMulti))
		{
			selected = shootingScenarioNode;
			bestValue = 0;
		}
		else
		{
			for (TPOLITSTreeNode node : this.nodesNearPOIs)
			{
				if (node.poi.poiType != POITYPE.SHOT)
				{
					double nodeValue = node.getValue();
					// nodeValue = Utils.normalise(nodeValue, this.bounds[0], this.bounds[1]);
					double uctValue = nodeValue + this.uctConstantInExplore * Math.sqrt(rootStateObsMulti.getGameTick() / node.numVisits);

					uctValue = Utils.noise(uctValue, this.epsilon, this.randomGenerator.nextDouble()); // break ties randomly

					if (uctValue > bestValue)
					{
						selected = node;
						bestValue = uctValue;
					}
				}
			}
		}

		LogHandler.writeLog(
				"Explored POI: " + selected.poi.poiType + ", position: " + selected.poi.position + ", uctValue: "
						+ bestValue + ", value: " + selected.getValue() + ", visits: " + selected.numVisits,
				"TPOLITSMovePlanner.choosePOIToExplore", 3);

		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.choosePOIToExplore", 3);
		return selected;
	}

	protected void explorePOI(TPOLITSTreeNode selected)
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

	protected void exploreShot(TPOLITSTreeNode selected)
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.exploreShot", 3);
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;

		StateObservationMulti shotStateObs = selected.stateObs.copy();
		Types.ACTIONS playerAction = Types.ACTIONS.ACTION_USE;
		Types.ACTIONS oppAction = Types.ACTIONS.ACTION_NIL; // fbtpAgentMoveController.getRandomAction(shotStateObs, fbtpGameKnowledge.getOppID());
		Types.ACTIONS[] actions = new Types.ACTIONS[2];
		actions[fbtpGameKnowledge.getPlayerID()] = playerAction;
		actions[fbtpGameKnowledge.getOppID()] = oppAction;

		shotStateObs.advance(actions);

		double avgTimeTaken = 0;
		double acumTimeTaken = 0;
		long initialRemaining = mainElapsedTimer.remainingTimeMillis();
		long remaining = this.timeLimitForSinglePOIExplore;
		int numIters = 0;

		while (remaining > 2 * avgTimeTaken && remaining > poiExplorationRemainingLimit && !shotStateObs.isGameOver()
				&& numIters < this.shotTestingWaitingTurns)
		{
			ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
			// int safeTurns = Math.min(3, this.shotTestingWaitingTurns - turn);

			playerAction = Types.ACTIONS.ACTION_NIL; // fbtpAgentMoveController.getRandomNonDyingAction(shotStateObs, fbtpGameKnowledge.getPlayerID(), safeTurns);
			oppAction = Types.ACTIONS.ACTION_NIL; // fbtpAgentMoveController.getRandomAction(shotStateObs, fbtpGameKnowledge.getOppID());
			actions = new Types.ACTIONS[2];
			actions[fbtpGameKnowledge.getPlayerID()] = playerAction;
			actions[fbtpGameKnowledge.getOppID()] = oppAction;

			shotStateObs.advance(actions);

			numIters++;
			acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
			avgTimeTaken = acumTimeTaken / numIters;
			remaining = this.timeLimitForPOIExploration - (initialRemaining - mainElapsedTimer.remainingTimeMillis());
		}

		double score = stateEvaluator.evaluateState(shotStateObs);
		selected.totalValue += score;
		selected.numVisits = 1;
		LogHandler.writeLog(
				"Exploring shot> turns: " + numIters + ", score: " + score + ", value: " + selected.getValue(),
				"TPOLITSMovePlanner.exploreShot", 3);
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.exploreShot", 3);
	}

	protected void exploreApproach(TPOLITSTreeNode selected)
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.exploreApproach", 3);
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;
		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;
		Pair<StateObservationMulti, ArrayList<Types.ACTIONS>> approachInfo = null;

		if (selected.poi.poiType == POITYPE.SPRITE)
		{
			int maxDistance = 2;
			long timeLimit = timeLimitForSinglePOIExplore;
			approachInfo = fbtpAgentMoveController.approachSprite(rootStateObsMulti, this.gameKnowledge.getPlayerID(),
					selected.poi.observation, maxDistance, timeLimit);
		}
		else if (selected.poi.poiType == POITYPE.POSITION)
		{
			int maxDistance = 2;
			long timeLimit = timeLimitForSinglePOIExplore;
			approachInfo = fbtpAgentMoveController.reachPosition(rootStateObsMulti, this.gameKnowledge.getPlayerID(),
					selected.poi.position, maxDistance, timeLimit);
		}

		if (approachInfo != null)
		{
			selected.stateObs = approachInfo.first();
			selected.path = approachInfo.second();
		}

		LogHandler.writeLog(
				"Exploring approach> " + ((approachInfo != null) ? "Path length: " + approachInfo.second().size() : "no path found"),
				"TPOLITSMovePlanner.exploreApproach", 3);
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.exploreApproach", 3);
	}

	protected void exploreRollOut(TPOLITSTreeNode selected)
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.exploreRollOut", 3);
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;

		double avgLargeTimeTaken = 0;
		double acumLargeTimeTaken = 0;
		long initialRemaining = mainElapsedTimer.remainingTimeMillis();
		long remaining = this.timeLimitForSinglePOIExplore;
		int numLargeIters = 0;

		while (remaining > 2 * avgLargeTimeTaken && remaining > poiExplorationRemainingLimit
				&& numLargeIters < this.numOfRolloutsInExplore)
		{
			ElapsedCpuTimer elapsedTimerLargeIteration = new ElapsedCpuTimer();
			double avgTimeTaken = 0;
			double acumTimeTaken = 0;

			StateObservationMulti rolloutState = selected.stateObs.copy();
			int numIters = 0;

			while (remaining > 2 * avgTimeTaken && remaining > timeLimitForOneAdvance && !rolloutState.isGameOver()
					&& numIters < this.maxExploreRolloutDepth)
			{
				ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

				//Types.ACTIONS randomAction = rolloutState.getAvailableActions().get(this.randomGenerator.nextInt(rolloutState.getAvailableActions().size()));

				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				Types.ACTIONS action = fbtpAgentMoveController.getRandomNonDyingAction(rolloutState,
						fbtpGameKnowledge.getPlayerID());
				actions[fbtpGameKnowledge.getPlayerID()] = ((action != null) ? action : Types.ACTIONS.ACTION_NIL);
				actions[fbtpGameKnowledge.getOppID()] = fbtpAgentMoveController.getRandomAction(rolloutState, 
						fbtpGameKnowledge.getOppID());

				rolloutState.advance(actions);

				numIters++;
				acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
				avgTimeTaken = acumTimeTaken / numIters;
				remaining = this.timeLimitForPOIExploration
						- (initialRemaining - mainElapsedTimer.remainingTimeMillis());
			}

			double score = stateEvaluator.evaluateState(rolloutState);
			selected.totalValue += score;
			selected.numVisits++;

			numLargeIters++;
			acumLargeTimeTaken += (elapsedTimerLargeIteration.elapsedMillis());
			avgLargeTimeTaken = acumLargeTimeTaken / numLargeIters;
			remaining = this.timeLimitForPOIExploration - (initialRemaining - mainElapsedTimer.remainingTimeMillis());

			LogHandler.writeLog(
					"Exploring rollout> rollout: " + numLargeIters + ", final depth: " + numIters + ", score: " + score,
					"TPOLITSMovePlanner.exploreRollOut", 3);
		}
		LogHandler.writeLog("Exploring rollout> total value: " + selected.totalValue + ", value: " + selected.getValue()
				+ ", visits: " + selected.numVisits, "TPOLITSMovePlanner.exploreRollOut", 3);

		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.exploreRollOut", 3);
	}

	protected void updateGoalPOI()
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.updateGoalPOI", 3);
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

		if (goalNode.poi.poiType == POITYPE.SHOT)
		{
			this.mode = TREESEARCHMODE.SHOT;
		}

		LogHandler
				.writeLog(
						"Goal node type: " + goalNode.poi.poiType + ", position: " + goalNode.poi.position + ", value: "
								+ goalNode.getValue() + ", final mode: "
								+ this.mode + ((goalNode.poi.observation != null)
										? ", POI type: " + goalNode.poi.observation.itype : ""),
						"TPOLITSMovePlanner.updateGoalPOI", 3);
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.updateGoalPOI", 3);
	}

	protected void initializeForTreeSearch()
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.initializeForTreeSearch", 3);
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
			}
			this.rootNode = goalNode;
		}
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.initializeForTreeSearch", 3);
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
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.searchTreeAction", 3);
		double avgTimeTaken = 0;
		double acumTimeTaken = 0;
		long remaining = mainElapsedTimer.remainingTimeMillis();
		int numIters = 0;

		LogHandler.writeLog("Remaining time: " + remaining, "TPOLITSMovePlanner.searchTreeAction", 3);

		while (remaining > 2 * avgTimeTaken && remaining > moveChoiceRemainingLimit)
		{
			StateObservation stateObs = rootStateObs.copy();

			ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
			TreeNode selectedNode = applyTreePolicy(stateObs);
			double delta = rollOut(selectedNode, stateObs);
			backUp(selectedNode, delta);

			numIters++;
			acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
			avgTimeTaken = acumTimeTaken / numIters;
			remaining = mainElapsedTimer.remainingTimeMillis();

			TPOLITSTreeNode tpolitsRootNode = (TPOLITSTreeNode) this.rootNode;
			LogHandler
					.writeLog(
							"Root node total value: " + tpolitsRootNode.totalValue + " Value: "
									+ tpolitsRootNode.getValue() + " Visits: " + tpolitsRootNode.numVisits,
							"TPOLITSMovePlanner.searchTreeAction", 3);
			for (int i = 0; i < tpolitsRootNode.children.length; i++)
			{
				TPOLITSTreeNode child =  (TPOLITSTreeNode) tpolitsRootNode.children[i];
				if (child != null)
				{
					LogHandler.writeLog(
							"Child: " + i + " actionIndex: " + child.actionIndex
							+ " total value: " + child.totalValue + " value: " + child.getValue()
							+ " visits: " + child.numVisits,
							"TPOLITSMovePlanner.searchTreeAction", 3);
				}
				else
				{
					LogHandler.writeLog(
							"Null child " + i,
							"TPOLITSMovePlanner.searchTreeAction", 3);
				}
			}

			LogHandler.writeLog("Iteration time: " + elapsedTimerIteration.elapsedMillis() + " | cumulative time: "
					+ acumTimeTaken + " | remaining: " + remaining, "TPOLITSMovePlanner.searchTreeAction", 3);
		}
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.searchTreeAction", 3);
	}

	protected void searchTreeApproach()
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.searchTreeApproach", 3);
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		StateObservationMulti rootStateObsMulti = (StateObservationMulti) this.rootStateObs;

		boolean searchForANewPath = false;
		boolean foundNewPath = false;

		if (this.numTurnsGoalPOINotChanged > 0 && this.goalNode.path != null)
		{
			if (!fbtpGameKnowledge.isDeterministicGame())
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
					searchForANewPath = true;

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
						approachInfo = fbtpAgentMoveController.reachPosition(rootStateObsMulti,
								this.gameKnowledge.getPlayerID(), goalNode.poi.position, maxDistance, timeLimit);
					}

					if (approachInfo != null)
					{
						goalNode.stateObs = approachInfo.first();
						goalNode.path = approachInfo.second();
						foundNewPath = true;
					}
				}
			}
		}
		else
		{
			Pair<StateObservationMulti, ArrayList<Types.ACTIONS>> approachInfo = null;
			searchForANewPath = true;

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
				approachInfo = fbtpAgentMoveController.reachPosition(rootStateObsMulti,
						this.gameKnowledge.getPlayerID(), goalNode.poi.position, maxDistance, timeLimit);
			}

			if (approachInfo != null)
			{
				goalNode.stateObs = approachInfo.first();
				goalNode.path = approachInfo.second();
				foundNewPath = true;
			}
		}

		LogHandler.writeLog("Searching for a new path: " + searchForANewPath + ", found a new path: " + foundNewPath,
				"TPOLITSMovePlanner.searchTreeApproach", 3);
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.searchTreeApproach", 3);
	}

	protected void searchTreeShot()
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.searchTreeShot", 3);
		// nothing needs to be done
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.searchTreeShot", 3);
	}

	protected TreeNode applyTreePolicy(StateObservation stateObs)
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.applyTreePolicy", 3);
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

		LogHandler.writeLog(
				"Tree policy depth: " + currentNode.depth + ", is game over state: " + stateObs.isGameOver(),
				"TPOLITSMovePlanner.applyTreePolicy", 3);

		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.applyTreePolicy", 3);
		return currentNode;
	}

	protected boolean isTreePolicyFinished(TreeNode currentNode, StateObservation stateObs, boolean expand)
	{
		return (stateObs.isGameOver() || currentNode.depth > this.oletsDepth);
	}

	protected double rollOut(TreeNode selectedNode, StateObservation stateObs)
	{
		// no rollout needed in OLETS

		double score = stateEvaluator.evaluateState(stateObs);

		LogHandler.writeLog("Tree policy final state score: " + score, "TPOLITSMovePlanner.rollOut", 3);

		return score;
	}

	protected boolean isRolloutFinished(StateObservation rollerState, int depth)
	{
		return true;
	}

	protected void backUp(TreeNode node, double delta)
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.backUp", 3);
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
							bestExpectimax = child.expectimax;
						if (child.adjEmax > bestAdjustedExpectimax)
							bestAdjustedExpectimax = child.adjEmax;
					}
				}

				n.expectimax = bestExpectimax;
				n.childrenMaxAdjEmax = bestAdjustedExpectimax;
				if (n.nbExitsHere > 0)
				{
					n.adjEmax = (((float) n.nbExitsHere) / n.numVisits) * (n.totalValueOnExit / n.nbExitsHere)
							+ (1.0 - (((float) n.nbExitsHere) / n.numVisits)) * n.childrenMaxAdjEmax;
				}
				else
					n.adjEmax = n.childrenMaxAdjEmax;
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
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.backUp", 3);
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
	protected TPOLITSTreeNode expandNode(TreeNode node, StateObservation stateObs)
	{
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		TPOLITSTreeNode tpolmctsNode = (TPOLITSTreeNode) node;

		Types.ACTIONS[] actions = new Types.ACTIONS[2];
		Types.ACTIONS action = fbtpAgentMoveController.getRandomNonDyingAction(stateObsMulti,
				fbtpGameKnowledge.getPlayerID());
		actions[fbtpGameKnowledge.getPlayerID()] = ((action != null) ? action : Types.ACTIONS.ACTION_NIL);
		actions[fbtpGameKnowledge.getOppID()] = fbtpAgentMoveController.getGreedyAction(stateObsMulti,
				fbtpGameKnowledge.getOppID());

		int bestAction = gameMechanicsController.actToIndex(stateObsMulti.getAvailableActions(fbtpGameKnowledge.getPlayerID()), 
				actions[fbtpGameKnowledge.getPlayerID()]);
		
		LogHandler.writeLog("Available actions: " + stateObsMulti.getAvailableActions(fbtpGameKnowledge.getPlayerID())
			+ "Action: " + actions[fbtpGameKnowledge.getPlayerID()] + " Best action: " + bestAction, "TPOLITSMovePlanner.expandNode", 0);
		
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
	protected TPOLITSTreeNode exploitNode(TreeNode node, StateObservation stateObs)
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
			LogHandler.writeLog("Child: " + child.actionIndex + " AdjEmaxScore: " + getNodeAdjustedEmaxScore(child)
					+ " Best value: " + bestValue + " Child value: " + child.getValue(), "TPOLITSMovePlanner.exploitNode", 0);
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
	protected void moveInRollout(StateObservation stateObs)
	{
		// not needed in OLETS
	}

	protected ACTIONS getOLITSAction()
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

		LogHandler.writeLog("Action: " + action, "TPOLITSMovePlanner.getOLITSAction", 3);

		return action;
	}

	/**
	 * Computes the weighted expectimax of a node, minus a location bias to increase the value of nodes in locations that
	 * have not been visited often in the past
	 * 
	 * @return the weighted expectimax with location bias
	 */
	protected double getNodeAdjustedEmaxScore(TPOLITSTreeNode node)
	{
		return (node.adjEmax
				+ this.uctConstant * Math.sqrt(Math.log(node.parent.numVisits + 1) / (node.numVisits + this.epsilon))
				- node.tabooBias);
	}

	public void setMoveChoiceRemainingLimit(int moveChoiceRemainingLimit)
	{
		this.moveChoiceRemainingLimit = moveChoiceRemainingLimit;
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

	protected static enum TREESEARCHMODE
	{
		APPROACH, ACTION, SHOT, NOMODE
	}

	protected static enum OLETSMODE
	{
		PURE, HEURISTIC, RANDOM
	}
}
