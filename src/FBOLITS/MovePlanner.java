package FBOLITS;

import FBOLITS.mechanicsController.AgentMoveController;
import FBOLITS.mechanicsController.GameMechanicsController;
import core.game.StateObservation;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class MovePlanner
{
	protected StateEvaluator stateEvaluator;
	protected GameKnowledge gameKnowledge;
	protected GameKnowledgeExplorer gameKnowledgeExplorer;
	protected AgentMoveController agentMoveController;
	protected GameMechanicsController gameMechanicsController;
	protected GameStateTracker gameStateTracker;

	public MovePlanner()
	{

	}
	
	public MovePlanner(StateEvaluator stateEvaluator)
	{
		this.stateEvaluator = stateEvaluator;
	}
	
	public MovePlanner(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge,
			GameKnowledgeExplorer gameKnowledgeExplorer)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
		this.gameKnowledgeExplorer = gameKnowledgeExplorer;
	}

	public MovePlanner(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge,
			GameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController, 
			GameMechanicsController gameMechanicsController, GameStateTracker gameStateTracker)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
		this.gameKnowledgeExplorer = gameKnowledgeExplorer;
		this.agentMoveController = agentMoveController;
		this.gameMechanicsController = gameMechanicsController;
		this.gameStateTracker = gameStateTracker;
	}

	public void setGameStateTracker(GameStateTracker gameStateTracker)
	{
		this.gameStateTracker = gameStateTracker;
	}

	public void setGameMechanicsController(GameMechanicsController gameMechanicsController)
	{
		this.gameMechanicsController = gameMechanicsController;
	}
	
	public void initialize(StateObservation stateObs)
	{
		
	}

	public ACTIONS chooseAction(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, long timeForChoosingMove)
	{
		// To be overridden in subclasses

		return Types.ACTIONS.ACTION_NIL;
	}

	public void setParameters()
	{

	}
}
