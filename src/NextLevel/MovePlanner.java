package NextLevel;

import baseStructure.moveController.AgentMoveController;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class MovePlanner
{
	private StateEvaluator stateEvaluator;
	private GameKnowledge gameKnowledge;
	private GameKnowledgeExplorer gameKnowledgeExplorer; 
	private AgentMoveController agentMoveController;
	
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
}
