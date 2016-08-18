package baseStructure;

import baseStructure.moveController.AgentMoveController;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class MovePlanner
{
	protected StateEvaluator stateEvaluator;
	protected GameKnowledge gameKnowledge;
	protected GameKnowledgeExplorer gameKnowledgeExplorer; 
	protected AgentMoveController agentMoveController;
	
	public MovePlanner()
	{
		
	}
	
	public MovePlanner(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge,
			GameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController)
	{
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
