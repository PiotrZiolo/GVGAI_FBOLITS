package baseStructure.treeSearchPlanners.OLETSPlanner;

import baseStructure.GameKnowledge;
import baseStructure.GameKnowledgeExplorer;
import baseStructure.State;
import baseStructure.StateEvaluator;
import baseStructure.moveController.AgentMoveController;
import baseStructure.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner.TPTreeSearchMovePlanner;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class TPOLETSPlanner extends TPTreeSearchMovePlanner
{
	public TPOLETSPlanner(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge,
			GameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController)
	{
		super(stateEvaluator, gameKnowledge, gameKnowledgeExplorer, agentMoveController);
	}
	
	public ACTIONS chooseAction(State state, ElapsedCpuTimer elapsedTimer, int timeForChoosingMove)
	{

		return Types.ACTIONS.ACTION_LEFT;
	}
}
