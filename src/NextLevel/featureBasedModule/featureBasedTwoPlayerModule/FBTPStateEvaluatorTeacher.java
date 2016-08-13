package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import NextLevel.GameKnowledge;
import NextLevel.StateEvaluator;
import NextLevel.StateEvaluatorTeacher;

public class FBTPStateEvaluatorTeacher extends StateEvaluatorTeacher
{
	private FBTPStateEvaluator stateEvaluator;
	private FBTPGameKnowledge gameKnowledge;
	
	public FBTPStateEvaluatorTeacher(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge)
	{
		this.stateEvaluator = (FBTPStateEvaluator)stateEvaluator;
		this.gameKnowledge = (FBTPGameKnowledge)gameKnowledge;
	}
	
	public void initializeEvaluator()
	{
		
	}

	public void updateEvaluator()
	{
		
	}
}
