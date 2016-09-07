package NextLevel;

import java.util.ArrayList;

import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.moduleFB.moduleFBTP.BasicFBTPGameKnowledgeExplorer;
import NextLevel.moduleFB.moduleFBTP.FBTPGameKnowledge;
import NextLevel.moduleFB.moduleFBTP.FBTPGameKnowledgeExplorer;
import NextLevel.moduleFB.moduleFBTP.FBTPGameStateTracker;
import NextLevel.moduleFB.moduleFBTP.FBTPState;
import NextLevel.moduleFB.moduleFBTP.FBTPStateEvaluator;
import NextLevel.moduleFB.moduleFBTP.FBTPStateEvaluatorTeacher;
import NextLevel.moduleFB.moduleFBTP.FBTPStateHandler;
import NextLevel.moduleFB.moduleFBTP.InfluenceMap;
import NextLevel.moduleFB.moduleFBTP.MechanicsController.FBTPAgentMoveController;
import NextLevel.moduleFB.moduleFBTP.MechanicsController.FBTPPathFinder;
import NextLevel.moduleTP.BasicTPState;
import NextLevel.moduleTP.SimpleTPStateEvaluator;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPGameKnowledgeExplorer;
import NextLevel.moduleTP.TPStateHandler;
import NextLevel.treeSearchPlanners.moduleTP.TPOLITSPlanner.TPOLITSMovePlanner;
import NextLevel.treeSearchPlanners.moduleTP.TPOLMCTSPlanner.TPOLMCTSMovePlanner;
import NextLevel.utils.LogHandler;
import NextLevel.utils.Pair;
import NextLevel.utils.PerformanceMonitor;
import NextLevel.utils.StatePrinter;
import core.game.Observation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class Agent extends AbstractMultiPlayer
{
	// Parameters

	private int playerID;
	private int oppID;
	private int numOfPlayers;

	private int timeForLearningDuringInitialization = 600;
	private int timeForLearningDuringMove = 100;
	private int timeForChoosingMove = 30;

	// Objects structure

	private TPOLITSMovePlanner movePlanner;
	private FBTPGameKnowledgeExplorer gameKnowledgeExplorer;
	private FBTPGameKnowledge gameKnowledge;
	private FBTPAgentMoveController agentMoveController;
	private TPGameMechanicsController gameMechanicsController;
	private TPStateHandler stateHandler;
	private FBTPStateEvaluatorTeacher stateEvaluatorTeacher;
	private FBTPStateEvaluator stateEvaluator;
	private FBTPGameStateTracker gameStateTracker;
	
	// private FBTPPathFinder fbtpPathFinder;
	// private ArrayList<Types.ACTIONS> path;
	// private int moveNumber = 0;

	// Algorithm parameters

	private final int remainingLimit = 12;
	private final int rolloutDepth = 10;
	private final double uctConstant = Math.sqrt(2);
	private int approachingSpriteMovesLimit = 100;

	/**
	 * Initializes all variables for the agent
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 * @param playerID
	 *            ID if this agent
	 */
	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID)
	{
		PerformanceMonitor.clearLog();
		/*
		LogHandler.clearLog();

		LogHandler.writeLog("Speed: " + stateObs.getAvatarSpeed(playerID), "Agent.creator", 1);
		LogHandler.writeLog("Block size: " + stateObs.getBlockSize(), "Agent.creator", 1);
		LogHandler.writeLog("Health points: " + stateObs.getAvatarHealthPoints(playerID), "Agent.creator", 0);
		LogHandler.writeLog("World dimensions: " + stateObs.getWorldDimension(), "Agent.creator", 1);
		LogHandler.writeLog("World dimensions: " + stateObs.getObservationGrid().length * stateObs.getBlockSize() + ", "
				+ stateObs.getObservationGrid()[0].length * stateObs.getBlockSize(), "Agent.creator", 0);
		LogHandler.writeLog("Avatar position: " + stateObs.getAvatarPosition(playerID), "Agent.creator", 3);
		*/

		this.playerID = playerID;
		this.oppID = 1 - playerID;
		this.numOfPlayers = stateObs.getNoPlayers();

		gameKnowledge = new FBTPGameKnowledge();
		gameMechanicsController = new TPGameMechanicsController(gameKnowledge);
		agentMoveController = new FBTPAgentMoveController(gameKnowledge, gameMechanicsController);
		// agentMoveController.setParameters(false, approachingSpriteMovesLimit);
		gameStateTracker = new FBTPGameStateTracker(gameMechanicsController, gameKnowledge);
		gameKnowledgeExplorer = new FBTPGameKnowledgeExplorer(gameKnowledge, agentMoveController,
				gameMechanicsController, gameStateTracker);

		// Learning

		// make one advance, because many objects appear after one turn
		stateObs.advance(new Types.ACTIONS[] { Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL });

		gameKnowledgeExplorer.learnBasics(stateObs, playerID);
		gameStateTracker.runTracker(stateObs);
		gameKnowledgeExplorer.initialLearn(stateObs, elapsedTimer, timeForLearningDuringInitialization);

		stateHandler = new FBTPStateHandler();
		stateEvaluator = new FBTPStateEvaluator(gameKnowledge, gameMechanicsController);
		stateEvaluatorTeacher = new FBTPStateEvaluatorTeacher(stateEvaluator, gameKnowledge);
		stateEvaluatorTeacher.initializeEvaluator(stateObs);

		movePlanner = new TPOLITSMovePlanner(stateEvaluator, gameKnowledge, gameKnowledgeExplorer, agentMoveController,
				gameMechanicsController, gameStateTracker);
		movePlanner.initialize(stateObs);

		/*
		 * fbtpPathFinder = new FBTPPathFinder(gameKnowledge, agentMoveController, gameMechanicsController);
		 * Vector2d goal = new Vector2d(690, 150);
		 * Pair<StateObservation, ArrayList<Types.ACTIONS>> pathInfo
		 * = fbtpPathFinder.findPathToAreaNearPosition(goal,
		 * stateObs, new ElapsedCpuTimer(), 2000);
		 * if (pathInfo != null)
		 * path = pathInfo.second();
		 * LogHandler.writeLog("Goal position: " + goal.x + ", " + goal.y + ", " + ((pathInfo != null) ? "Path found" : "Path not found"), "Agent.creator", 3);
		 */
	}

	/**
	 * Picks an action using chosen algorithm. This function is called every
	 * game step to request an action from the player.
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 * @return An action for the current state
	 */
	public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer)
	{
		//LogHandler.writeLog("Turn: " + stateObs.getGameTick(), "Agent.act", 3);
	
		//Types.ACTIONS action = Types.ACTIONS.ACTION_NIL;
		
		gameKnowledgeExplorer.learnBasics(stateObs, playerID);
		gameStateTracker.runTracker(stateObs);
		gameKnowledgeExplorer.successiveLearn(stateObs, elapsedTimer, timeForLearningDuringMove);
		stateEvaluatorTeacher.updateEvaluator();
		
		BasicTPState state = stateHandler.prepareState(stateObs);
		
		//LogHandler.writeLog("From avatar sprite type: " + gameKnowledge.getFromAvatarSpriteType(), "Agent.act", 3);
		
		//LogHandler.writeLog("State evaluation: " + stateEvaluator.evaluateState(stateObs), "Agent.act", 3);
		//stateObs.advance(new Types.ACTIONS[]{Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL});
		//LogHandler.writeLog("State evaluation: " + stateEvaluator.evaluateState(stateObs), "Agent.act", 3);
		
		/*
		Types.ACTIONS[] actions = new Types.ACTIONS[2];
		Types.ACTIONS action = agentMoveController.getRandomNonDyingAction(stateObs,
				gameKnowledge.getPlayerID());
		actions[gameKnowledge.getPlayerID()] = ((action != null) ? action : Types.ACTIONS.ACTION_NIL);
		actions[gameKnowledge.getOppID()] = agentMoveController.getGreedyAction(stateObs,
				gameKnowledge.getOppID());
		
		LogHandler.writeLog("Avatar non dying action: " + actions[gameKnowledge.getPlayerID()]
				+ " Opponent greedy action: " + actions[gameKnowledge.getOppID()], "Agent.act", 3);
		*/
		 
		Types.ACTIONS action = movePlanner.chooseAction(state, elapsedTimer, timeForChoosingMove);
		
		/*
		 * for (int x = 0; x < (int) (stateObs.getWorldDimension().getWidth() / stateObs.getBlockSize()); x++)
		 * {
		 * for (int y = 0; y < (int) (stateObs.getWorldDimension().getHeight() / stateObs.getBlockSize()); y++)
		 * {
		 * int xpx = x * stateObs.getBlockSize();
		 * int ypx = y * stateObs.getBlockSize();
		 * Pair<StateObservation, ArrayList<Types.ACTIONS>> pathInfo
		 * = fbtpPathFinder.findPathToAreaNearPosition(new Vector2d(xpx, ypx),
		 * stateObs, elapsedTimer, 20);
		 * LogHandler.writeLog("Goal position: " + xpx + ", " + ypx + ", " + ((pathInfo != null) ? "Path found" : "Path not found"), "Agent.creator", 3);
		 * }
		 * }
		 */
		/*
		Types.ACTIONS action = Types.ACTIONS.ACTION_NIL;
		
		 * if (path != null && moveNumber < path.size())
		 * {
		 * action = path.get(moveNumber);
		 * LogHandler.writeLog("Avatar position: " + stateObs.getAvatarPosition(playerID) + ", action: " + action, "Agent.act", 3);
		 * }
		 * else
		 * {
		 * action = Types.ACTIONS.ACTION_NIL;
		 * }
		 * moveNumber++;
		 */
		/*
		int iters = 0;
		long initialRemaining = elapsedTimer.remainingTimeMillis();
		while (elapsedTimer.remainingTimeMillis() > 5)
		{
			//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
			//performanceMonitor.startNanoMeasure("Start", "TPOLITSMovePlanner.initializePOINodes", 3);
			stateObs.advance(new Types.ACTIONS[] { Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL });
			//performanceMonitor.finishNanoMeasure("Finish", "TPOLITSMovePlanner.initializePOINodes", 3);
			iters++;
		}
		long finalRemaining = elapsedTimer.remainingTimeMillis();
		LogHandler.writeLog("Iters: " + iters + ", avg time: " + (finalRemaining / iters), "Agent.act", 3);
		*/

		return action;
	}

	private void setXXXParameters()
	{
		// make a method for every object that needs parameters initialization
		// use separate setter for every parameter in every class
	}
}
