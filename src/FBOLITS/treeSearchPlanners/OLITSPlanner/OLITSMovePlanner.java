package FBOLITS.treeSearchPlanners.OLITSPlanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;

import FBOLITS.PointOfInterest;
import FBOLITS.PointOfInterest.POITYPE;
import FBOLITS.moduleFB.FBGameKnowledge;
import FBOLITS.moduleFB.FBGameKnowledgeExplorer;
import FBOLITS.moduleFB.FBGameStateTracker;
import FBOLITS.moduleFB.FBStateEvaluator;
import FBOLITS.moduleFB.MechanicsController.FBAgentMoveController;
import FBOLITS.StateEvaluator;
import FBOLITS.mechanicsController.GameMechanicsController;
import FBOLITS.treeSearchPlanners.TreeNode;
import FBOLITS.treeSearchPlanners.TreeSearchMovePlanner;
import FBOLITS.utils.AuxUtils;
import FBOLITS.utils.Pair;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

public class OLITSMovePlanner extends TreeSearchMovePlanner
{
	// Real types of fields
	// protected StateEvaluator stateEvaluator;
	// protected FBGameKnowledge gameKnowledge;
	// protected FBGameKnowledgeExplorer gameKnowledgeExplorer;
	// protected FBAgentMoveController agentMoveController;
	// protected OLITSTreeNode rootNode;
	// protected StateObservation rootStateObs;
	// protected FBGameStateTracker gameStateTracker;
	// protected GameMechanicsController gameMechanicsController;

	protected long initialRemainingTime;

	// protected ArrayList<PointOfInterest> pois;
	protected ArrayList<OLITSTreeNode> nodesNearPOIs;
	protected OLITSTreeNode goalNode;
	protected int goalNodeIndex;
	protected OLITSTreeNode shootingScenarioNode;

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

	protected int moveChoiceRemainingLimit = 3; // in ms
	protected int poiExplorationRemainingLimit = 3; // in ms
	protected double shotScenarioImportance = 0.01;
	protected int poiCloseDistanceThreshold = 3; // expressed in the number of moves to the POI
	protected double timePercentageForMoveChoiceInActionMode = 0.75;
	protected double timePercentageForMoveChoiceInActionModeAfterAction = 0.6;
	// protected double timePercentageForMoveChoiceInApproachMode = 0.1;
	protected long timeLimitForDeterministicPathSearch = 1; // in ms
	protected long timeLimitForNonDeterministicPathSearch = 2; // in ms
	protected long timeLimitForOneAdvance = 1; // in ms;
	protected int minimumNumOfPOIExplorations = 3;
	protected double goalPOIHandicap = 1.5;
	protected double uctConstantInExplore = 5;
	protected double uctConstant = Math.sqrt(2);
	protected double[] bounds = new double[] { -Double.MAX_VALUE, Double.MAX_VALUE };
	protected int numOfRolloutsInExplore = 10;
	protected int shotTestingWaitingTurns = 10;
	protected int maxExploreRolloutDepth = 10;
	protected int oletsDepth = 30;
	// protected double pureVsHeuristicDrivenOLETSproportion = 0.5;
	protected double tabooBias = 0.5;
	// Number of past positions and orientations that are kept in memory for the exploration bias
	protected int memoryLength = 15;

	public OLITSMovePlanner(StateEvaluator stateEvaluator, FBGameKnowledge gameKnowledge,
			FBGameKnowledgeExplorer gameKnowledgeExplorer, FBAgentMoveController agentMoveController,
			GameMechanicsController gameMechanicsController, FBGameStateTracker gameStateTracker)
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

	@Override
	public void initialize(StateObservation stateObs)
	{
		this.rootStateObs = stateObs;
		initializePOINodes();
	}

	private void initializePOINodes()
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.initializePOINodes", 3);

		FBStateEvaluator fbStateEvaluator = (FBStateEvaluator) this.stateEvaluator;
		FBGameKnowledge fbGameKnowledge = (FBGameKnowledge) this.gameKnowledge;

		// initialize list of POIs from gameStateTracker + optionally add regular grid on the map
		nodesNearPOIs = new ArrayList<OLITSTreeNode>();

		ArrayList<Observation> observations = new ArrayList<Observation>();
		for (PointOfInterest poi : gameStateTracker.getPOIs())
		{
			observations.add(poi.observation);
		}

		fbStateEvaluator.initializePlayerInfluenceMap(rootStateObs);
		HashMap<Integer, Double> spriteEvaluations = fbStateEvaluator.evaluateSprites(observations);
		// double highestImportance = 0;

		for (PointOfInterest poi : gameStateTracker.getPOIs())
		{
			OLITSTreeNode node = new OLITSTreeNode(gameKnowledge.getNumOfPlayerActions(), poi);

			if (poi.poiType == POITYPE.SPRITE)
			{
				node.totalValue = spriteEvaluations.get(poi.observation.obsID);
				node.numVisits = 1;

				// if (node.totalValue > highestImportance)
				// highestImportance = node.totalValue;
				/*
				 * LogHandler.writeLog("POI id: " + poi.observation.obsID + ", type: " + poi.observation.itype
				 * + ", category: " + poi.observation.category + ", position: " + poi.position + ", value: "
				 * + node.totalValue, "TPOLITSMovePlanner.initializePOINodes", 0);
				 */
			}

			nodesNearPOIs.add(node);
		}

		// LogHandler.writeLog("Highest importance: " + highestImportance, "TPOLITSMovePlanner.initializePOINodes", 3);

		if (fbGameKnowledge.isShootingAllowed())
		{
			PointOfInterest shootingScenario = new PointOfInterest(POITYPE.SHOT,
					rootStateObs.getAvatarPosition());
			shootingScenario.poiType = POITYPE.SHOT;
			OLITSTreeNode node = new OLITSTreeNode(gameKnowledge.getNumOfPlayerActions(), shootingScenario);
			node.totalValue = 0; // highestImportance + shotScenarioImportance;
			node.numVisits = 1;

			this.shootingScenarioNode = node;
			nodesNearPOIs.add(node);

			/*
			 * LogHandler.writeLog("Shot POI> position: " + node.poi.position + ", value: " + node.totalValue,
			 * "TPOLITSMovePlanner.initializePOINodes", 0);
			 */
		}

		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.initializePOINodes", 3);
	}

	@Override
	public Types.ACTIONS chooseAction(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, long timeForChoosingMove)
	{
		this.rootStateObs = stateObs;
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
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.initializeForChooseAction", 3);
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
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.initializeForChooseAction", 3);
	}

	protected void updatePOIList()
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.updatePOIList", 3);
		FBStateEvaluator fbStateEvaluator = (FBStateEvaluator) this.stateEvaluator;
		FBGameKnowledge fbGameKnowledge = (FBGameKnowledge) this.gameKnowledge;

		// Add new POIs, remove POIs which dissappeared

		for (PointOfInterest poi : gameStateTracker.getNewPOIs())
		{
			OLITSTreeNode node = new OLITSTreeNode(gameKnowledge.getNumOfPlayerActions(), poi);
			nodesNearPOIs.add(node);
			node.totalValue = 0;
			node.numVisits = 1;
		}

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

		// Evaluate POIs

		Collections.sort(nodesNearPOIs, nodesComparator);

		fbStateEvaluator.initializePlayerInfluenceMap(rootStateObs);

		double avgTimeTaken = 0;
		double acumTimeTaken = 0;
		long initialRemaining = mainElapsedTimer.remainingTimeMillis();
		long timeForInitialization = (int) Math.max(3, ((double) this.totalTimeForChoosingMove) / 4);
		long remaining = timeForInitialization;
		int numIters = 0;

		while (remaining > 2 * avgTimeTaken && numIters < nodesNearPOIs.size())
		{
			ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

			OLITSTreeNode node = nodesNearPOIs.get(numIters);
			if (node.poi.poiType == POITYPE.SPRITE)
			{
				node.totalValue = fbStateEvaluator.evaluateSprite(node.poi.observation);
				node.numVisits = 1;
				/*
				 * LogHandler.writeLog(
				 * "POI id: " + node.poi.observation.obsID + ", category: " + node.poi.observation.category + ", type: "
				 * + node.poi.observation.itype + ", position: " + node.poi.position
				 * + ", distance: " + node.poi.observation.sqDist + ", value: " + node.totalValue,
				 * "TPOLITSMovePlanner.updatePOIList", 3);
				 */
			}

			numIters++;
			acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
			avgTimeTaken = acumTimeTaken / numIters;
			remaining = timeForInitialization - (initialRemaining - mainElapsedTimer.remainingTimeMillis());
		}

		/*
		 * // Evaluate new POIs
		 * 
		 * ArrayList<Observation> newObservations = new ArrayList<Observation>();
		 * for (PointOfInterest poi : gameStateTracker.getNewPOIs())
		 * {
		 * newObservations.add(poi.observation);
		 * }
		 * 
		 * HashMap<Integer, Double> spriteEvaluations = fbStateEvaluator.evaluateSprites(newObservations);
		 * 
		 * // Add new POIs to nodesNearPOIs with their initial evaluation
		 * 
		 * for (PointOfInterest poi : gameStateTracker.getNewPOIs())
		 * {
		 * TPOLITSTreeNode node = new TPOLITSTreeNode(gameKnowledge.getNumOfPlayerActions(), poi);
		 * 
		 * if (poi.poiType == POITYPE.SPRITE)
		 * {
		 * node.totalValue = spriteEvaluations.get(poi.observation.obsID);
		 * node.numVisits = 1;
		 * 
		 * LogHandler.writeLog(
		 * "POI id: " + poi.observation.obsID + ", category: " + poi.observation.category + ", type: "
		 * + poi.observation.itype + ", position: " + poi.position + ", value: " + node.totalValue,
		 * "TPOLITSMovePlanner.updatePOIList", 0);
		 * }
		 * 
		 * nodesNearPOIs.add(node);
		 * }
		 * 
		 * for (PointOfInterest poi : gameStateTracker.getRemovedPOIs())
		 * {
		 * for (int index = 0; index < this.nodesNearPOIs.size(); index++)
		 * {
		 * if (this.nodesNearPOIs.get(index).poi.poiType == POITYPE.SPRITE
		 * && this.nodesNearPOIs.get(index).poi.observation.obsID == poi.observation.obsID)
		 * {
		 * if (this.goalNode != null && this.goalNode.poi.poiType == POITYPE.SPRITE
		 * && this.goalNode.poi.observation.obsID == poi.observation.obsID)
		 * {
		 * this.goalNode = null;
		 * }
		 * this.nodesNearPOIs.remove(index);
		 * break;
		 * }
		 * }
		 * }
		 */

		if (shootingScenarioNode != null)
		{
			shootingScenarioNode.poi.position = rootStateObs.getAvatarPosition();
		}

		/*
		 * LogHandler.writeLog("Number of POIs: " + this.nodesNearPOIs.size(), "TPOLITSMovePlanner.updatePOIList", 0);
		 */
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.updatePOIList", 3);
	}

	protected void setGoalPOI()
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.setGoalPOI", 3);
		double bestValue = -Double.MAX_VALUE;

		for (int index = 0; index < this.nodesNearPOIs.size(); index++)
		{
			OLITSTreeNode poiNode = this.nodesNearPOIs.get(index);
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

		/*
		 * LogHandler
		 * .writeLog(
		 * "Goal node type: " + goalNode.poi.poiType + ", position: " + goalNode.poi.position + ", value: "
		 * + goalNode.getValue() + ((goalNode.poi.observation != null)
		 * ? ", POI type: " + goalNode.poi.observation.itype : ""),
		 * "TPOLITSMovePlanner.setGoalPOI", 3);
		 */
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.setGoalPOI", 3);
	}

	protected void chooseMode()
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.chooseMode", 3);
		FBGameKnowledge fbGameKnowledge = (FBGameKnowledge) this.gameKnowledge;
		FBAgentMoveController fbtpAgentMoveController = (FBAgentMoveController) this.agentMoveController;

		Vector2d poiPosition = this.goalNode.poi.position;
		boolean isAvatarCloseToGoalPOI = fbtpAgentMoveController.isPositionWithinGivenMoveRange(rootStateObs,
				poiPosition, this.poiCloseDistanceThreshold);

		/*
		 * LogHandler.writeLog("Player position: " + rootStateObs.getAvatarPosition(fbGameKnowledge.getPlayerID())
		 * + " Goal POI position: " + goalNode.poi.position + " Close: "
		 * + ((isAvatarCloseToGoalPOI) ? "yes" : "no"), "TPOLITSMovePlanner.chooseMode", 3);
		 */

		TREESEARCHMODE previousMode = this.mode;

		if (isAvatarCloseToGoalPOI)
			this.mode = TREESEARCHMODE.ACTION;
		else
			this.mode = TREESEARCHMODE.APPROACH;

		// set time limits
		long timeAlreadyUsed = this.initialRemainingTime - this.mainElapsedTimer.remainingTimeMillis();
		long remainingTimeForChoosingMove = this.totalTimeForChoosingMove - timeAlreadyUsed;

		/*
		 * LogHandler.writeLog("Time used for move planner initialization: " + timeAlreadyUsed,
		 * "TPOLITSMovePlanner.chooseMode", 3);
		 */

		if (remainingTimeForChoosingMove > 0)
		{
			if (this.mode == TREESEARCHMODE.APPROACH)
			{
				if (fbGameKnowledge.isDeterministicGame())
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

		/*
		LogHandler.writeLog("Time for POI exploration: " + timeLimitForPOIExploration, "TPOLITSMovePlanner.chooseMode",
				3);
		LogHandler.writeLog("Time for single POI exploration: " + timeLimitForSinglePOIExplore,
				"TPOLITSMovePlanner.chooseMode", 3);
		LogHandler.writeLog("Time for tree search: " + timeLimitForTreeSearch, "TPOLITSMovePlanner.chooseMode", 3);

		LogHandler.writeLog("Chosen mode: " + this.mode, "TPOLITSMovePlanner.chooseMode", 3);
		performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.chooseMode", 3);
		*/
	}

	protected void initializeForExploration()
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.initializeForExploration", 3);
		FBAgentMoveController fbtpAgentMoveController = (FBAgentMoveController) this.agentMoveController;

		for (OLITSTreeNode node : nodesNearPOIs)
		{
			if (!(node == goalNode && this.previousMode == TREESEARCHMODE.APPROACH
					&& this.numTurnsGoalPOINotChanged > 0))
			{
				node.path = null;
			}

			Vector2d poiPosition = node.poi.position;
			if (node.poi.poiType == POITYPE.SHOT)
			{
				node.totalValue = 0;
				node.poiApproached = false;
				node.stateObs = rootStateObs;
				/*
				 * LogHandler.writeLog("Shot scenario> POI type: " + node.poi.poiType + ", position: " + node.poi.position,
				 * "TPOLITSMovePlanner.initializeForExploration", 3);
				 */
			}
			else if (fbtpAgentMoveController.isPositionWithinGivenMoveRange(rootStateObs, poiPosition,
					this.poiCloseDistanceThreshold))
			{
				node.poiApproached = true;
				node.path = null;
				node.stateObs = rootStateObs;
				/*
				 * LogHandler.writeLog(
				 * "POI approached scenario> POI type: " + node.poi.poiType + ", position: " + node.poi.position,
				 * "TPOLITSMovePlanner.initializeForExploration", 3);
				 */
			}
			/*
			 * else if (node.poi.positionChangedFromPreviousTurn)
			 * {
			 * node.poiApproached = false;
			 * 
			 * if (node.stateObs != null) // if a stateObs close to POI has already been found earlier, move this state towards POI
			 * {
			 * Pair<StateObservation, ArrayList<Types.ACTIONS>> approachInfo = null;
			 * 
			 * if (node.poi.poiType == POITYPE.SPRITE)
			 * {
			 * int maxDistance = 2;
			 * int timeLimit = 20;
			 * approachInfo = fbtpAgentMoveController.approachSprite(node.stateObs,
			 * this.gameKnowledge.getPlayerID(), node.poi.observation, maxDistance, timeLimit);
			 * }
			 * else if (node.poi.poiType == POITYPE.POSITION)
			 * {
			 * int maxDistance = 2;
			 * int timeLimit = 20;
			 * approachInfo = fbtpAgentMoveController.reachPosition(node.stateObs,
			 * this.gameKnowledge.getPlayerID(), node.poi.position, maxDistance, timeLimit);
			 * }
			 * 
			 * if (approachInfo != null)
			 * node.stateObs = approachInfo.first();
			 * node.path = null; // One could consider merging paths to reduce the number of invocations of path finding, however this may lead to very non-optimal paths
			 * 
			 * LogHandler.writeLog(
			 * "POI not approached scenario>" + " short approach successful: "
			 * + ((approachInfo != null) ? "yes" : "no") + ", POI type: " + node.poi.poiType
			 * + ", position: " + node.poi.position,
			 * "TPOLITSMovePlanner.initializeForExploration", 3);
			 * }
			 * else
			 * {
			 * LogHandler.writeLog("POI not approached scenario> no stateObs, POI type: " + node.poi.poiType
			 * + ", position: " + node.poi.position, "TPOLITSMovePlanner.initializeForExploration", 3);
			 * }
			 * }
			 */
			else
			{
				node.poiApproached = false;
				node.stateObs = null;
			}
		}
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.initializeForExploration", 3);
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

			OLITSTreeNode selected = choosePOIToExplore(numIters);

			explorePOI(selected);

			numIters++;
			acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
			avgTimeTaken = acumTimeTaken / numIters;
			remaining = this.timeLimitForPOIExploration - (initialRemaining - mainElapsedTimer.remainingTimeMillis());
			/*
			 * LogHandler
			 * .writeLog(
			 * "Iteration time: " + elapsedTimerIteration.elapsedMillis() + " | cumulative time taken: "
			 * + acumTimeTaken + " | remaining: " + remaining + ")",
			 * "TPOLITSMovePlanner.explorePOIs", 3);
			 */
		}
	}

	protected OLITSTreeNode choosePOIToExplore(int numExplorations)
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.choosePOIToExplore", 3);
		FBGameKnowledge fbGameKnowledge = (FBGameKnowledge) this.gameKnowledge;
		OLITSTreeNode selected = null;
		double bestValue = -Double.MAX_VALUE;

		if (fbGameKnowledge.isShootingAllowed() && numExplorations == 0
				&& !gameMechanicsController.isFromAvatarSpriteOnTheMap(rootStateObs))
		{
			selected = shootingScenarioNode;
			bestValue = 0;
		}
		else
		{
			for (OLITSTreeNode node : this.nodesNearPOIs)
			{
				if (node.poi.poiType != POITYPE.SHOT && !(node == goalNode
						&& this.previousMode == TREESEARCHMODE.APPROACH && this.numTurnsGoalPOINotChanged > 0))
				// we exclude shot scenario and an approach goal
				{
					double nodeValue = node.getValue();
					// nodeValue = Utils.normalise(nodeValue, this.bounds[0], this.bounds[1]);
					double uctValue = nodeValue
							+ this.uctConstantInExplore * Math.sqrt(rootStateObs.getGameTick() / node.numVisits);

					uctValue = Utils.noise(uctValue, this.epsilon, this.randomGenerator.nextDouble()); // break ties randomly

					if (uctValue > bestValue)
					{
						selected = node;
						bestValue = uctValue;
					}
				}
			}
		}

		/*
		 * LogHandler.writeLog(
		 * "Explored POI: " + selected.poi.poiType + ", position: " + selected.poi.position + ", uctValue: "
		 * + bestValue + ", value: " + selected.getValue() + ", visits: " + selected.numVisits,
		 * "TPOLITSMovePlanner.choosePOIToExplore", 3);
		 */
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.choosePOIToExplore", 3);
		return selected;
	}

	protected void explorePOI(OLITSTreeNode selected)
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

	protected void exploreShot(OLITSTreeNode selected)
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.exploreShot", 3);
		StateObservation shotStateObs = selected.stateObs.copy();

		shotStateObs.advance(Types.ACTIONS.ACTION_USE);

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
			// fbAgentMoveController.getRandomNonDyingAction(shotStateObs, fbGameKnowledge.getPlayerID(), safeTurns);

			shotStateObs.advance(Types.ACTIONS.ACTION_NIL);

			numIters++;
			acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
			avgTimeTaken = acumTimeTaken / numIters;
			remaining = this.timeLimitForSinglePOIExplore - (initialRemaining - mainElapsedTimer.remainingTimeMillis());
		}
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.exploreShot", 3);
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.exploreShot", 3);
		double score = stateEvaluator.evaluateState(shotStateObs);
		selected.totalValue += score;
		selected.numVisits = 1;
		/*
		 * LogHandler.writeLog(
		 * "Exploring shot> turns: " + numIters + ", score: " + score + ", value: " + selected.getValue(),
		 * "TPOLITSMovePlanner.exploreShot", 3);
		 */
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.exploreShot", 3);
	}

	protected void exploreApproach(OLITSTreeNode selected)
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.exploreApproach", 3);
		FBAgentMoveController fbtpAgentMoveController = (FBAgentMoveController) this.agentMoveController;
		Pair<StateObservation, ArrayList<Types.ACTIONS>> approachInfo = null;

		if (selected.poi.poiType == POITYPE.SPRITE)
		{
			int maxDistance = 2;
			long timeLimit = timeLimitForSinglePOIExplore;
			approachInfo = fbtpAgentMoveController.approachSprite(rootStateObs,
					selected.poi.observation, maxDistance, timeLimit);
		}
		else if (selected.poi.poiType == POITYPE.POSITION)
		{
			int maxDistance = 2;
			long timeLimit = timeLimitForSinglePOIExplore;
			approachInfo = fbtpAgentMoveController.reachPosition(rootStateObs,
					selected.poi.position, maxDistance, timeLimit);
		}

		if (approachInfo != null)
		{
			selected.stateObs = approachInfo.first();
			selected.path = approachInfo.second();
			// selected.poiApproached = true;
		}

		/*
		 * LogHandler.writeLog(
		 * "Exploring approach> " + ((approachInfo != null) ? "Path length: " + approachInfo.second().size() : "no path found"),
		 * "TPOLITSMovePlanner.exploreApproach", 3);
		 */
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.exploreApproach", 3);
	}

	protected void exploreRollOut(OLITSTreeNode selected)
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.exploreRollOut", 3);
		FBAgentMoveController fbtpAgentMoveController = (FBAgentMoveController) this.agentMoveController;
		FBGameKnowledge fbGameKnowledge = (FBGameKnowledge) this.gameKnowledge;

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

			StateObservation rolloutState = selected.stateObs.copy();
			int numIters = 0;

			while (remaining > 2 * avgTimeTaken && remaining > timeLimitForOneAdvance && !rolloutState.isGameOver()
					&& numIters < this.maxExploreRolloutDepth)
			{
				ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

				// Types.ACTIONS randomAction = rolloutState.getAvailableActions().get(this.randomGenerator.nextInt(rolloutState.getAvailableActions().size()));

				Types.ACTIONS action = fbtpAgentMoveController.getRandomNonDyingAction(rolloutState);
				action = ((action != null) ? action : Types.ACTIONS.ACTION_NIL);

				rolloutState.advance(action);

				numIters++;
				acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
				avgTimeTaken = acumTimeTaken / numIters;
				remaining = this.timeLimitForSinglePOIExplore
						- (initialRemaining - mainElapsedTimer.remainingTimeMillis());
			}

			double score = stateEvaluator.evaluateState(rolloutState);
			selected.totalValue += score;
			selected.numVisits++;

			numLargeIters++;
			acumLargeTimeTaken += (elapsedTimerLargeIteration.elapsedMillis());
			avgLargeTimeTaken = acumLargeTimeTaken / numLargeIters;
			remaining = this.timeLimitForSinglePOIExplore - (initialRemaining - mainElapsedTimer.remainingTimeMillis());

			/*
			 * LogHandler.writeLog(
			 * "Exploring rollout> rollout: " + numLargeIters + ", final depth: " + numIters + ", score: " + score,
			 * "TPOLITSMovePlanner.exploreRollOut", 3);
			 */
		}
		/*
		 * LogHandler.writeLog("Exploring rollout> total value: " + selected.totalValue + ", value: " + selected.getValue()
		 * + ", visits: " + selected.numVisits, "TPOLITSMovePlanner.exploreRollOut", 3);
		 */

		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.exploreRollOut", 3);
	}

	protected void updateGoalPOI()
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.updateGoalPOI", 3);
		FBAgentMoveController fbtpAgentMoveController = (FBAgentMoveController) this.agentMoveController;
		// if there was a large change in score of the best POI, change goal POI
		double bestValue = goalNode.getValue();

		for (int index = 0; index < this.nodesNearPOIs.size(); index++)
		{
			OLITSTreeNode poiNode = this.nodesNearPOIs.get(index);
			double challengerValue = poiNode.getValue();
			if (challengerValue > bestValue * goalPOIHandicap)
			{
				bestValue = challengerValue;
				this.goalNode = poiNode;
				this.goalNodeIndex = index;
				numTurnsGoalPOINotChanged = 0;
			}
		}

		if (numTurnsGoalPOINotChanged == 0)
		{
			if (fbtpAgentMoveController.isPositionWithinGivenMoveRange(rootStateObs, goalNode.poi.position,
					this.poiCloseDistanceThreshold))
				this.mode = TREESEARCHMODE.ACTION;
			else
				this.mode = TREESEARCHMODE.APPROACH;
		}

		if (goalNode.poi.poiType == POITYPE.SHOT)
		{
			this.mode = TREESEARCHMODE.SHOT;
		}

		/*
		 * LogHandler
		 * .writeLog(
		 * "Goal node type: " + goalNode.poi.poiType + ", position: " + goalNode.poi.position + ", value: "
		 * + goalNode.getValue() + ", final mode: "
		 * + this.mode + ((goalNode.poi.observation != null)
		 * ? ", POI type: " + goalNode.poi.observation.itype : ""),
		 * "TPOLITSMovePlanner.updateGoalPOI", 3);
		 */
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.updateGoalPOI", 3);
	}

	protected void initializeForTreeSearch()
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.initializeForTreeSearch", 3);
		if (this.mode == TREESEARCHMODE.ACTION)
		{
			if (this.previousMode == TREESEARCHMODE.ACTION && this.numTurnsGoalPOINotChanged > 0)
			{
				// shift the tree near goalPOI by the made move
				Types.ACTIONS lastAction = rootStateObs.getAvatarLastAction();
				OLITSTreeNode childChosenInLastMove = (OLITSTreeNode) this.goalNode.children[AuxUtils
						.actionToIndex(rootStateObs, lastAction)];

				childChosenInLastMove.poi = goalNode.poi;
				childChosenInLastMove.stateObs = rootStateObs;
				goalNode = childChosenInLastMove;
				this.nodesNearPOIs.set(this.goalNodeIndex, childChosenInLastMove);
				this.goalNode.parent = null;
				this.goalNode.refreshTree(0);
			}
			this.rootNode = goalNode;
		}
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.initializeForTreeSearch", 3);
	}

	@Override
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
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.searchTreeAction", 3);
		double avgTimeTaken = 0;
		double acumTimeTaken = 0;
		long initialRemaining = mainElapsedTimer.remainingTimeMillis();
		long remaining = timeLimitForTreeSearch;
		int numIters = 0;

		// LogHandler.writeLog("Remaining time: " + remaining, "TPOLITSMovePlanner.searchTreeAction", 3);

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
			remaining = timeLimitForTreeSearch - (initialRemaining - mainElapsedTimer.remainingTimeMillis());

			/*
			 * TPOLITSTreeNode tpolitsRootNode = (TPOLITSTreeNode) this.rootNode;
			 * LogHandler
			 * .writeLog(
			 * "Root node total value: " + tpolitsRootNode.totalValue + " Value: "
			 * + tpolitsRootNode.getValue() + " Visits: " + tpolitsRootNode.numVisits,
			 * "TPOLITSMovePlanner.searchTreeAction", 3);
			 * 
			 * for (int i = 0; i < tpolitsRootNode.children.length; i++)
			 * {
			 * TPOLITSTreeNode child = (TPOLITSTreeNode) tpolitsRootNode.children[i];
			 * 
			 * if (child != null)
			 * {
			 * LogHandler.writeLog(
			 * "Child: " + i + " actionIndex: " + child.actionIndex
			 * + " action: " + AuxUtils.indexToAction((StateObservation)rootStateObs, gameKnowledge.getPlayerID(), i)
			 * + " total value: " + child.totalValue + " value: " + child.getValue()
			 * + " visits: " + child.numVisits,
			 * "TPOLITSMovePlanner.searchTreeAction", 3);
			 * }
			 * else
			 * {
			 * LogHandler.writeLog(
			 * "Null child " + i,
			 * "TPOLITSMovePlanner.searchTreeAction", 3);
			 * }
			 * }
			 */

			/*
			 * LogHandler.writeLog("Iteration time: " + elapsedTimerIteration.elapsedMillis() + " | cumulative time: "
			 * + acumTimeTaken + " | remaining: " + remaining, "TPOLITSMovePlanner.searchTreeAction", 3);
			 */
		}
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.searchTreeAction", 3);
	}

	protected void searchTreeApproach()
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.searchTreeApproach", 3);
		FBAgentMoveController fbtpAgentMoveController = (FBAgentMoveController) this.agentMoveController;
		FBGameKnowledge fbGameKnowledge = (FBGameKnowledge) this.gameKnowledge;

		boolean searchForANewPath = false;
		boolean foundNewPath = false;

		if (this.numTurnsGoalPOINotChanged > 0 && this.goalNode.path != null)
		{
			if (!fbGameKnowledge.isDeterministicGame())
			{
				StateObservation simulationStateObs = goalNode.stateObs.copy();
				simulationStateObs.advance(this.goalNode.path.get(0));

				if (simulationStateObs.isGameOver())
				{
					Pair<StateObservation, ArrayList<Types.ACTIONS>> approachInfo = null;
					searchForANewPath = true;

					if (goalNode.poi.poiType == POITYPE.SPRITE)
					{
						int maxDistance = 2;
						long timeLimit = this.timeLimitForTreeSearch - 1;
						approachInfo = fbtpAgentMoveController.approachSprite(rootStateObs, goalNode.poi.observation, maxDistance, timeLimit);
					}
					else if (goalNode.poi.poiType == POITYPE.POSITION)
					{
						int maxDistance = 2;
						long timeLimit = this.timeLimitForTreeSearch - 1;
						approachInfo = fbtpAgentMoveController.reachPosition(rootStateObs, goalNode.poi.position, maxDistance, timeLimit);
					}

					if (approachInfo != null)
					{
						goalNode.stateObs = approachInfo.first();
						goalNode.path = approachInfo.second();
						foundNewPath = true;
					}
					else
					{
						goalNode.stateObs = null;
						goalNode.path = null;
					}
				}
			}
		}
		else
		{
			if (this.goalNode.path == null)
			{
				Pair<StateObservation, ArrayList<Types.ACTIONS>> approachInfo = null;
				searchForANewPath = true;

				if (goalNode.poi.poiType == POITYPE.SPRITE)
				{
					int maxDistance = 2;
					long timeLimit = this.timeLimitForTreeSearch - 1;
					approachInfo = fbtpAgentMoveController.approachSprite(rootStateObs, goalNode.poi.observation, maxDistance, timeLimit);
				}
				else if (goalNode.poi.poiType == POITYPE.POSITION)
				{
					int maxDistance = 2;
					long timeLimit = this.timeLimitForTreeSearch - 1;
					approachInfo = fbtpAgentMoveController.reachPosition(rootStateObs, goalNode.poi.position, maxDistance, timeLimit);
				}

				if (approachInfo != null)
				{
					goalNode.stateObs = approachInfo.first();
					goalNode.path = approachInfo.second();
					foundNewPath = true;
				}
				else
				{
					goalNode.stateObs = null;
					goalNode.path = null;
				}
			}
		}

		/*
		 * LogHandler.writeLog("Searching for a new path: " + searchForANewPath + ", found a new path: " + foundNewPath,
		 * "TPOLITSMovePlanner.searchTreeApproach", 3);
		 */
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.searchTreeApproach", 3);
	}

	protected void searchTreeShot()
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.searchTreeShot", 3);
		// nothing needs to be done
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.searchTreeShot", 3);
	}

	@Override
	protected TreeNode applyTreePolicy(StateObservation stateObs)
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.applyTreePolicy", 3);
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

		/*
		 * LogHandler.writeLog(
		 * "Tree policy depth: " + currentNode.depth + ", is game over state: " + stateObs.isGameOver(),
		 * "TPOLITSMovePlanner.applyTreePolicy", 3);
		 */

		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.applyTreePolicy", 3);
		return currentNode;
	}

	@Override
	protected boolean isTreePolicyFinished(TreeNode currentNode, StateObservation stateObs, boolean expand)
	{
		return (stateObs.isGameOver() || currentNode.depth > this.oletsDepth);
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
	@Override
	protected OLITSTreeNode expandNode(TreeNode node, StateObservation stateObs)
	{
		FBAgentMoveController fbtpAgentMoveController = (FBAgentMoveController) this.agentMoveController;
		OLITSTreeNode tpolmctsNode = (OLITSTreeNode) node;

		Types.ACTIONS action = fbtpAgentMoveController.getRandomNonDyingAction(stateObs);
		action = ((action != null) ? action : Types.ACTIONS.ACTION_NIL);

		int bestAction = gameMechanicsController.actToIndex(
				stateObs.getAvailableActions(), action);

		/*
		 * LogHandler.writeLog("Available actions: " + stateObs.getAvailableActions(fbGameKnowledge.getPlayerID())
		 * + "Action: " + actions[fbGameKnowledge.getPlayerID()] + " Best action: " + bestAction, "TPOLITSMovePlanner.expandNode", 0);
		 */

		stateObs.advance(action);

		double tabooBias = 0.0;
		int i = 0;
		boolean stateFound = false;
		while ((!stateFound) && (i < this.memoryLength) && (this.pastAvatarPositions[i] != null))
		{
			if (this.pastAvatarPositions[i].equals(stateObs.getAvatarPosition()))
			{
				tabooBias += this.tabooBias;
				stateFound = true;
			}
			i++;
		}

		OLITSTreeNode child = new OLITSTreeNode(tpolmctsNode, gameKnowledge.getNumOfPlayerActions(),
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
	@Override
	protected OLITSTreeNode exploitNode(TreeNode node, StateObservation stateObs)
	{
		FBAgentMoveController fbtpAgentMoveController = (FBAgentMoveController) this.agentMoveController;
		FBGameKnowledge fbGameKnowledge = (FBGameKnowledge) this.gameKnowledge;
		OLITSTreeNode tpolmctsNode = (OLITSTreeNode) node;

		OLITSTreeNode selected = null;
		double bestValue = -Double.MAX_VALUE;

		// pick the best Q.
		for (OLITSTreeNode child : (OLITSTreeNode[]) tpolmctsNode.children)
		{
			double score = Utils.noise(getNodeAdjustedEmaxScore(child), this.epsilon,
					this.randomGenerator.nextDouble());
			if (score > bestValue)
			{
				selected = child;
				bestValue = score;
			}
			/*
			 * LogHandler.writeLog("Child: " + child.actionIndex + " AdjEmaxScore: " + getNodeAdjustedEmaxScore(child)
			 * + " Best value: " + bestValue + " Child value: " + child.getValue(), "TPOLITSMovePlanner.exploitNode", 0);
			 */
		}

		stateObs.advance(stateObs.getAvailableActions()
				.get(selected.actionIndex));

		double tabooBias = 0.0;
		if (selected.nbGenerated == 0)
		{
			tabooBias = 0.0;
			int i = 0;
			boolean stateFound = false;
			while ((!stateFound) && (i < memoryLength) && (this.pastAvatarPositions[i] != null))
			{
				if (this.pastAvatarPositions[i].equals(stateObs.getAvatarPosition()))
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

	@Override
	protected double rollOut(TreeNode selectedNode, StateObservation stateObs)
	{
		// no rollout needed in OLETS

		double score = stateEvaluator.evaluateState(stateObs);

		// LogHandler.writeLog("Tree policy final state score: " + score, "TPOLITSMovePlanner.rollOut", 3);

		return score;
	}

	@Override
	protected boolean isRolloutFinished(StateObservation rollerState, int depth)
	{
		return true;
	}
	
	/**
	 * @param stateObs
	 *            State observation connected with this node.
	 * @return Actions for both players.
	 */
	@Override
	protected void moveInRollout(StateObservation stateObs)
	{
		// not needed in OLETS
	}

	@Override
	protected void backUp(TreeNode node, double delta)
	{
		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.backUp", 3);
		OLITSTreeNode n = (OLITSTreeNode) node;
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
					OLITSTreeNode child = (OLITSTreeNode) (OLITSTreeNode) n.children[i];
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

			n = (OLITSTreeNode) n.parent;
			backUpDepth += 1;
		}
		//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.backUp", 3);
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
				this.goalNode.path = null;
				action = getRandomFBGreedyAction((StateObservation) this.rootStateObs,
						gameKnowledge.getPlayerID());
			}
		}
		else if (this.mode == TREESEARCHMODE.SHOT)
		{
			action = Types.ACTIONS.ACTION_USE;
		}

		// LogHandler.writeLog("Action: " + action, "TPOLITSMovePlanner.getOLITSAction", 3);

		return action;
	}

	/**
	 * Computes the weighted expectimax of a node, minus a location bias to increase the value of nodes in locations that
	 * have not been visited often in the past
	 * 
	 * @return the weighted expectimax with location bias
	 */
	protected double getNodeAdjustedEmaxScore(OLITSTreeNode node)
	{
		return (node.adjEmax
				+ this.uctConstant * Math.sqrt(Math.log(node.parent.numVisits + 1) / (node.numVisits + this.epsilon))
				- node.tabooBias);
	}

	public ACTIONS getRandomFBGreedyAction(StateObservation stateObs, int playerID)
	{
		FBStateEvaluator fbStateEvaluator = (FBStateEvaluator) this.stateEvaluator;
		ArrayList<Types.ACTIONS> availableActions = new ArrayList<Types.ACTIONS>(
				stateObs.getAvailableActions());
		Collections.shuffle(availableActions);
		StateObservation nextState;
		double bestScore = -10000000.0;
		Types.ACTIONS bestAction = Types.ACTIONS.ACTION_USE;
		for (Types.ACTIONS action : availableActions)
		{
			nextState = stateObs.copy();
			nextState.advance(action);
			double score = fbStateEvaluator.evaluateState(nextState);
			if (score > bestScore)
			{
				bestScore = score;
				bestAction = action;
			}
		}
		return bestAction;
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

	private Comparator<OLITSTreeNode> nodesComparator = new Comparator<OLITSTreeNode>()
	{
		public int compare(OLITSTreeNode a, OLITSTreeNode b)
		{
			if (a.poi.poiType == POITYPE.SHOT)
				return -1;
			if (b.poi.poiType == POITYPE.SHOT)
				return 1;
			if (a.poi.observation.sqDist > b.poi.observation.sqDist)
				return 1;
			if (a.poi.observation.sqDist == b.poi.observation.sqDist)
				return 0;
			return -1;
		}
	};
}
