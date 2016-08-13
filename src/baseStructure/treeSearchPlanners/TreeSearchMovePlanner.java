package baseStructure.treeSearchPlanners;

import baseStructure.GameKnowledge;
import baseStructure.GameKnowledgeExplorer;
import baseStructure.MovePlanner;
import baseStructure.State;
import baseStructure.StateEvaluator;
import baseStructure.featureBasedModule.featureBasedTwoPlayerModule.FBTPState;
import baseStructure.moveController.AgentMoveController;
import baseStructure.utils.LogHandler;
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
