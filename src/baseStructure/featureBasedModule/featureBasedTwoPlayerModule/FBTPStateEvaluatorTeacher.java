package baseStructure.featureBasedModule.featureBasedTwoPlayerModule;

import baseStructure.GameKnowledge;
import baseStructure.StateEvaluator;
import baseStructure.StateEvaluatorTeacher;

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
