package FBOLITS;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import FBOLITS.mechanicsController.AgentMoveController;
import FBOLITS.mechanicsController.GameMechanicsController;
import FBOLITS.moduleFB.FBGameKnowledge;
import FBOLITS.moduleFB.FBGameKnowledgeExplorer;
import FBOLITS.moduleFB.FBGameStateTracker;
import FBOLITS.moduleFB.FBStateEvaluator;
import FBOLITS.moduleFB.FBStateEvaluatorTeacher;
import FBOLITS.moduleFB.MechanicsController.FBAgentMoveController;
import FBOLITS.moduleFB.MechanicsController.FBGameMechanicsController;
import FBOLITS.treeSearchPlanners.OLITSPlanner.OLITSMovePlanner;
import FBOLITS.treeSearchPlanners.OLMCTSPlanner.OLMCTSMovePlanner;
import FBOLITS.utils.PerformanceMonitor;
import FBOLITS.utils.StatePrinter;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractPlayer
{
	// Parameters

	private int playerID;

	private int timeForLearningDuringInitialization = 600;
	private int timeForLearningDuringMove = 10;
	private int timeForChoosingMove = 25;

	// Objects structure

	private OLMCTSMovePlanner movePlanner;
	private FBGameKnowledgeExplorer gameKnowledgeExplorer;
	private FBGameKnowledge gameKnowledge;
	private FBAgentMoveController agentMoveController;
	private FBGameMechanicsController gameMechanicsController;
	private FBStateEvaluatorTeacher stateEvaluatorTeacher;
	private FBStateEvaluator stateEvaluator;
	private FBGameStateTracker gameStateTracker;
	
	// private FBTPPathFinder fbtpPathFinder;
	// private ArrayList<Types.ACTIONS> path;
	// private int moveNumber = 0;

	// Algorithm parameters

	/**
	 * Initializes all variables for the agent
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 */
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start creator", Agent.class, 3);
		gameKnowledge = new FBGameKnowledge();
		gameMechanicsController = new FBGameMechanicsController(gameKnowledge);
		agentMoveController = new FBAgentMoveController(gameKnowledge, gameMechanicsController);
		gameStateTracker = new FBGameStateTracker(gameMechanicsController, gameKnowledge);
		gameKnowledgeExplorer = new FBGameKnowledgeExplorer(gameKnowledge, gameMechanicsController, 
				agentMoveController, gameStateTracker);

		// Learning

		// make one advance, because many objects appear after one turn
		stateObs.advance(Types.ACTIONS.ACTION_NIL);

		gameKnowledgeExplorer.learnStaticBasics(stateObs, playerID);
		gameKnowledgeExplorer.learnDynamicBasics(stateObs);
		gameStateTracker.runTracker(stateObs);
		gameKnowledgeExplorer.initialLearn(stateObs, elapsedTimer, timeForLearningDuringInitialization);

		stateEvaluator = new FBStateEvaluator(gameKnowledge, gameMechanicsController);
		stateEvaluatorTeacher = new FBStateEvaluatorTeacher(stateEvaluator, gameKnowledge);
		stateEvaluatorTeacher.initializeEvaluator(stateObs);

		movePlanner = new OLMCTSMovePlanner(stateEvaluator, gameKnowledge, gameKnowledgeExplorer, agentMoveController);
		movePlanner.initialize(stateObs);
		
		//StatePrinter sp = new StatePrinter();
		//sp.printGameState(stateObs, 0, true);
		
		performanceMonitor.finishNanoMeasure("Finish creator", Agent.class, 3);
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
	public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer)
	{
		PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		performanceMonitor.startNanoMeasure("Start act", Agent.class, 3);
		gameKnowledgeExplorer.learnDynamicBasics(stateObs);
		gameStateTracker.runTracker(stateObs);
		gameKnowledgeExplorer.successiveLearn(stateObs, elapsedTimer, timeForLearningDuringMove);
		stateEvaluatorTeacher.updateEvaluator(stateObs);
		
		Types.ACTIONS action = movePlanner.chooseAction(stateObs, elapsedTimer, timeForChoosingMove);

		performanceMonitor.finishNanoMeasure("Finish act", Agent.class, 3);
		return action;
	}

	private void setXXXParameters()
	{
		// make a method for every object that needs parameters initialization
		// use separate setter for every parameter in every class
	}
}
