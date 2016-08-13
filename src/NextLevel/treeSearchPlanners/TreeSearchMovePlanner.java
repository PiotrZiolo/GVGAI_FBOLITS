package NextLevel.treeSearchPlanners;

import NextLevel.GameKnowledge;
import NextLevel.GameKnowledgeExplorer;
import NextLevel.MovePlanner;
import NextLevel.State;
import NextLevel.StateEvaluator;
import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPState;
import NextLevel.moveController.AgentMoveController;
import NextLevel.utils.LogHandler;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class TreeSearchMovePlanner extends MovePlanner
{
	private TreePolicyMoveEvaluator treePolicyMoveEvaluator;
	
	public TreeSearchMovePlanner(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge,
			GameKnowledgeExplorer gameKnowledgeExplorer, AgentMoveController agentMoveController)
	{
		super(stateEvaluator, gameKnowledge, gameKnowledgeExplorer, agentMoveController);
		
		treePolicyMoveEvaluator = new TreePolicyMoveEvaluator(stateEvaluator, gameKnowledge);
	}
	
	public ACTIONS chooseAction(State state, ElapsedCpuTimer elapsedTimer, int timeForChoosingMove)
	{
		
		return Types.ACTIONS.ACTION_NIL;
	}
}
