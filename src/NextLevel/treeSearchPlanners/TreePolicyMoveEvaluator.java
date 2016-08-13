package NextLevel.treeSearchPlanners;

import NextLevel.GameKnowledge;
import NextLevel.StateEvaluator;

public class TreePolicyMoveEvaluator
{
	private StateEvaluator stateEvaluator;
	private GameKnowledge gameKnowledge;

	public TreePolicyMoveEvaluator(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
	}
}
