package NextLevel;

import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPAgentMoveController;
import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPGameKnowledge;
import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPGameKnowledgeExplorer;
import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPState;
import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPStateHandler;
import NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner.TPOLMCTSMoveController;
import NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner.TPOLMCTSMovePlanner;
import NextLevel.twoPlayer.TPWinScoreStateEvaluator;
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
    private FBTPGameKnowledgeExplorer gameKnowledgeExplorer;
    private FBTPGameKnowledge gameKnowledge;
    private FBTPAgentMoveController agentMoveController;
	private FBTPStateHandler stateHandler;
	private StateEvaluatorTeacher stateEvaluatorTeacher;
    private TPWinScoreStateEvaluator stateEvaluator;
    private TPOLMCTSMoveController moveController;
    
    // Algorithm parameters
    
    private final int remainingLimit = 12;
	private final int rolloutDepth = 10;
	private final double uctConstant = Math.sqrt(2);
	
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
		agentMoveController = new FBTPAgentMoveController(gameKnowledge);
		gameKnowledgeExplorer = new FBTPGameKnowledgeExplorer(gameKnowledge, agentMoveController);
		
		stateHandler = new FBTPStateHandler();
		stateEvaluator = new TPWinScoreStateEvaluator(gameKnowledge, stateHandler);
		stateEvaluatorTeacher = new StateEvaluatorTeacher(stateEvaluator, gameKnowledge);
		
		moveController = new TPOLMCTSMoveController(stateEvaluator, gameKnowledge);
		moveController.setParameters(uctConstant);
		movePlanner = new TPOLMCTSMovePlanner(stateEvaluator, gameKnowledge, gameKnowledgeExplorer, agentMoveController, moveController);
		
		// Learning
		
		gameKnowledgeExplorer.learn(stateObs, playerID, elapsedTimer, timeForLearningDuringInitialization);
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
		gameKnowledgeExplorer.learn(stateObs, playerID, elapsedTimer, timeForLearningDuringMove);
		stateEvaluatorTeacher.updateEvaluator();

		FBTPState state = stateHandler.prepareState(stateObs);
		
		movePlanner.setParameters(remainingLimit, rolloutDepth);
		Types.ACTIONS action = movePlanner.chooseAction(state, elapsedTimer, timeForChoosingMove);
		
		return action;
	}
}
