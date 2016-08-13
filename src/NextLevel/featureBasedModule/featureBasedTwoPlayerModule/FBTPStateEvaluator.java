package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import baseStructure.GameKnowledge;
import baseStructure.State;
import baseStructure.StateEvaluator;

public class FBTPStateEvaluator extends StateEvaluator
{
	private FBTPGameKnowledge gameKnowledge;
	
	public FBTPStateEvaluator(GameKnowledge gameKnowledge)
	{
		this.gameKnowledge = (FBTPGameKnowledge)gameKnowledge;
	}
	
	public double evaluate(State state)
	{
		FBTPState fbtpstate = (FBTPState) state;
		
		return 0;
	}
}
