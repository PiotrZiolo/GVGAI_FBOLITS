package NextLevel;

import NextLevel.moveController.AgentMoveController;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class MovePlanner
{
	protected StateHandler stateHandler;
	protected StateEvaluator stateEvaluator;
	protected GameKnowledge gameKnowledge;
	protected GameKnowledgeExplorer gameKnowledgeExplorer; 
	protected AgentMoveController agentMoveController;
	
	public MovePlanner()
	{
		
	}
	
	public MovePlanner(StateHandler stateHandler, StateEvaluator stateEvaluator, GameKnowledge gameKnowledge,
			GameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController)
	{
		this.stateHandler = stateHandler;
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
		this.gameKnowledgeExplorer = gameKnowledgeExplorer;
		this.agentMoveController = agentMoveController;
	}

	public ACTIONS chooseAction(State state, ElapsedCpuTimer elapsedTimer, int timeForChoosingMove)
	{
		// To be overridden in subclasses
		
		return Types.ACTIONS.ACTION_NIL;
	}
	
	public void setParameters()
	{
		
	}
}
