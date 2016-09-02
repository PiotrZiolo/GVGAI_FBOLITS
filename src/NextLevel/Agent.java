package NextLevel;

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
import NextLevel.moduleTP.BasicTPState;
import NextLevel.moduleTP.SimpleTPStateEvaluator;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPGameKnowledgeExplorer;
import NextLevel.moduleTP.TPStateHandler;
import NextLevel.treeSearchPlanners.moduleTP.TPOLITSPlanner.TPOLITSMovePlanner;
import NextLevel.treeSearchPlanners.moduleTP.TPOLMCTSPlanner.TPOLMCTSMovePlanner;
import NextLevel.utils.LogHandler;
import NextLevel.utils.PerformanceMonitor;
import NextLevel.utils.StatePrinter;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractMultiPlayer
{
	// Parameters

	private int playerID;
	private int oppID;
	private int numOfPlayers;

	private int timeForLearningDuringInitialization = 0;
	private int timeForLearningDuringMove = 0;
	private int timeForChoosingMove = 30;

	// Objects structure

	private TPOLMCTSMovePlanner movePlanner;
	private FBTPGameKnowledgeExplorer gameKnowledgeExplorer;
	private FBTPGameKnowledge gameKnowledge;
	private FBTPAgentMoveController agentMoveController;
	private TPGameMechanicsController gameMechanicsController;
	private TPStateHandler stateHandler;
	private FBTPStateEvaluatorTeacher stateEvaluatorTeacher;
	private FBTPStateEvaluator stateEvaluator;
	private FBTPGameStateTracker gameStateTracker;

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
		LogHandler.clearLog();
		PerformanceMonitor.clearLog();
		
		this.playerID = playerID;
		this.oppID = 1 - playerID;
		this.numOfPlayers = stateObs.getNoPlayers();

		gameKnowledge = new FBTPGameKnowledge();
		gameMechanicsController = new TPGameMechanicsController(gameKnowledge);
		agentMoveController = new FBTPAgentMoveController(gameKnowledge, gameMechanicsController);
		//agentMoveController.setParameters(false, approachingSpriteMovesLimit);
		gameStateTracker = new FBTPGameStateTracker(gameMechanicsController, gameKnowledge);
		gameKnowledgeExplorer = new FBTPGameKnowledgeExplorer(gameKnowledge, agentMoveController,
				gameMechanicsController, gameStateTracker);

		// Learning

		// make one advance, because many objects appear after one turn
		stateObs.advance(new Types.ACTIONS[]{Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL});
		
		gameStateTracker.runTracker(stateObs);
		gameKnowledgeExplorer.initialLearn(stateObs, playerID, elapsedTimer, timeForLearningDuringInitialization);
		
		stateHandler = new FBTPStateHandler();
		stateEvaluator = new FBTPStateEvaluator(gameKnowledge);
		stateEvaluatorTeacher = new FBTPStateEvaluatorTeacher(stateEvaluator, gameKnowledge);
		stateEvaluatorTeacher.initializeEvaluator(stateObs);
		
		movePlanner = new TPOLMCTSMovePlanner(stateEvaluator, gameKnowledge, gameKnowledgeExplorer,
				agentMoveController);
		movePlanner.initialize(stateObs);
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
		LogHandler.writeLog("Turn: " + stateObs.getGameTick(), "FBTPGameStateTracker.searchForNewPOIs", 1);
		
		gameStateTracker.runTracker(stateObs);
		gameKnowledgeExplorer.successiveLearn(stateObs, playerID, elapsedTimer, timeForLearningDuringMove);
		stateEvaluatorTeacher.updateEvaluator();

		BasicTPState state = stateHandler.prepareState(stateObs);

		Types.ACTIONS action = movePlanner.chooseAction(state, elapsedTimer, timeForChoosingMove);

		return action;
	}
	
	private void setXXXParameters()
	{
		// make a method for every object that needs parameters initialization
		// use separate setter for every parameter in every class
	}
}
