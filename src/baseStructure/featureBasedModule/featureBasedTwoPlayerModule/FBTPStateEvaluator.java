package baseStructure.featureBasedModule.featureBasedTwoPlayerModule;

import baseStructure.GameKnowledge;
import baseStructure.State;
import baseStructure.StateEvaluator;
import core.game.StateObservationMulti;

public class FBTPStateEvaluator extends StateEvaluator
{
	private FBTPGameKnowledge gameKnowledge;
	
	public FBTPStateEvaluator(GameKnowledge gameKnowledge)
	{
		this.gameKnowledge = (FBTPGameKnowledge)gameKnowledge;
	}
	
	public double evaluate(StateObservationMulti stateObs)
	{
		FBTPState fbtpstate = new FBTPState(stateObs);
		
		return 0;
	}
}
