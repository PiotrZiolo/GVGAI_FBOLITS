package NextLevel.treeSearchPlanners.OLMCTSPlanner;

import NextLevel.GameKnowledge;
import NextLevel.GameKnowledgeExplorer;
import NextLevel.State;
import NextLevel.StateEvaluator;
import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPState;
import NextLevel.moveController.AgentMoveController;
import NextLevel.treeSearchPlanners.twoPlayer.TPOLMCTSPlanner.TPTreeSearchMovePlanner;
import NextLevel.utils.LogHandler;
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
