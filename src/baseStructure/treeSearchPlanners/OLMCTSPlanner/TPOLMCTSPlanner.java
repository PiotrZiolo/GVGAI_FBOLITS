package baseStructure.treeSearchPlanners.OLMCTSPlanner;

import baseStructure.GameKnowledge;
import baseStructure.GameKnowledgeExplorer;
import baseStructure.State;
import baseStructure.StateEvaluator;
import baseStructure.featureBasedModule.featureBasedTwoPlayerModule.FBTPState;
import baseStructure.moveController.AgentMoveController;
import baseStructure.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner.TPTreeSearchMovePlanner;
import baseStructure.utils.LogHandler;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class TPOLMCTSPlanner extends TPTreeSearchMovePlanner
{
	public TPOLMCTSPlanner(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge,
			GameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController)
	{
		super(stateEvaluator, gameKnowledge, gameKnowledgeExplorer, agentMoveController);
	}
	
	public ACTIONS chooseAction(State state, ElapsedCpuTimer elapsedTimer, int timeForChoosingMove)
	{

		return Types.ACTIONS.ACTION_LEFT;
	}
}
