package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import NextLevel.GameKnowledge;
import NextLevel.State;
import NextLevel.StateEvaluator;

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
