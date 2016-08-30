package NextLevel;

import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.moduleFB.moduleFBTP.FBTPGameKnowledge;
import NextLevel.moduleFB.moduleFBTP.FBTPGameKnowledgeExplorer;
import NextLevel.moduleFB.moduleFBTP.FBTPGameStateTracker;
import NextLevel.moduleFB.moduleFBTP.FBTPState;
import NextLevel.moduleFB.moduleFBTP.FBTPStateHandler;
import NextLevel.moduleFB.moduleFBTP.MechanicsController.FBTPAgentMoveController;
import NextLevel.moduleTP.BasicTPState;
import NextLevel.moduleTP.SimpleTPStateEvaluator;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPGameKnowledgeExplorer;
import NextLevel.moduleTP.TPStateHandler;
import NextLevel.treeSearchPlanners.moduleTP.TPOLMCTSPlanner.TPOLMCTSMovePlanner;
import NextLevel.utils.LogHandler;
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

	private int timeForLearningDuringInitialization;
	private int timeForLearningDuringMove;
	private int timeForChoosingMove;

	// Objects structure

	private TPOLMCTSMovePlanner movePlanner;
	private TPGameKnowledgeExplorer gameKnowledgeExplorer;
	private FBTPGameKnowledge gameKnowledge;
	private AgentMoveController agentMoveController;
	private TPGameMechanicsController gameMechanicsController;
	private TPStateHandler stateHandler;
	private StateEvaluatorTeacher stateEvaluatorTeacher;
	private SimpleTPStateEvaluator stateEvaluator;
	private GameStateTracker gameStateTracker;

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
		this.playerID = playerID;
		this.oppID = 1 - playerID;
		this.numOfPlayers = stateObs.getNoPlayers();

		gameKnowledge = new FBTPGameKnowledge();
		gameMechanicsController = new TPGameMechanicsController(gameKnowledge);
		agentMoveController = new AgentMoveController(gameKnowledge, gameMechanicsController);
		//agentMoveController.setParameters(false, approachingSpriteMovesLimit);
		gameStateTracker = new GameStateTracker(gameMechanicsController, gameKnowledge);
		gameKnowledgeExplorer = new TPGameKnowledgeExplorer(gameKnowledge, agentMoveController,
				gameMechanicsController);

		stateHandler = new FBTPStateHandler();
		stateEvaluator = new SimpleTPStateEvaluator(gameKnowledge, stateHandler);
		stateEvaluatorTeacher = new StateEvaluatorTeacher(stateEvaluator, gameKnowledge);

		movePlanner = new TPOLMCTSMovePlanner(stateEvaluator, gameKnowledge, gameKnowledgeExplorer,
				agentMoveController);

		// Learning

		gameStateTracker.runTracker(stateObs);
		gameKnowledgeExplorer.initialLearn(stateObs, playerID, elapsedTimer, timeForLearningDuringInitialization);
		stateEvaluatorTeacher.initializeEvaluator();
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
		gameStateTracker.runTracker(stateObs);
		gameKnowledgeExplorer.successiveLearn(stateObs, playerID, elapsedTimer, timeForLearningDuringMove);
		stateEvaluatorTeacher.updateEvaluator();

		BasicTPState state = stateHandler.prepareState(stateObs);

		movePlanner.setParameters(remainingLimit, rolloutDepth, uctConstant);
		Types.ACTIONS action = movePlanner.chooseAction(state, elapsedTimer, timeForChoosingMove);

		return action;
	}
}
