package NextLevel;

import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPGameKnowledge;
import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPGameKnowledgeExplorer;
import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPState;
import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPStateEvaluator;
import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPStateEvaluatorTeacher;
import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPStateHandler;
import NextLevel.moveController.AgentMoveController;
import NextLevel.treeSearchPlanners.OLMCTSPlanner.OLMCTSPlanner;
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
    
	private MovePlanner movePlanner;
    private GameKnowledgeExplorer gameKnowledgeExplorer;
    private GameKnowledge gameKnowledge;
    private AgentMoveController agentMoveController;
	private StateHandler stateHandler;
	private StateEvaluatorTeacher stateEvaluatorTeacher;
    private StateEvaluator stateEvaluator;
    
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
		agentMoveController = new AgentMoveController(gameKnowledge);
		gameKnowledgeExplorer = new FBTPGameKnowledgeExplorer(stateObs, gameKnowledge, agentMoveController, playerID);
		
		stateEvaluator = new FBTPStateEvaluator(gameKnowledge);
		stateEvaluatorTeacher = new FBTPStateEvaluatorTeacher(stateEvaluator, gameKnowledge);
		
		stateHandler = new FBTPStateHandler();
		
		movePlanner = new OLMCTSPlanner(stateEvaluator, gameKnowledge, gameKnowledgeExplorer, agentMoveController);
		
		// Learning
		
		gameKnowledgeExplorer.learn(elapsedTimer, timeForLearningDuringInitialization);
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
		gameKnowledgeExplorer.learn(elapsedTimer, timeForLearningDuringMove);
		stateEvaluatorTeacher.updateEvaluator();

		FBTPState state = new FBTPState();
		stateHandler.prepareState(state, stateObs);
		
		Types.ACTIONS action = movePlanner.chooseAction(state, elapsedTimer, timeForChoosingMove);
		
		return action;
	}
}
