package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import NextLevel.GameKnowledge;
import NextLevel.State;
import NextLevel.StateEvaluator;
import core.game.StateObservation;
import core.game.StateObservationMulti;

public class FBTPStateEvaluator extends StateEvaluator
{
	// Real types of fields
	// protected FBTPGameKnowledge gameKnowledge;
	// protected FBTPStateHandler stateHandler;
	
	public FBTPStateEvaluator(FBTPGameKnowledge gameKnowledge, FBTPStateHandler stateHandler)
	{
		this.gameKnowledge = gameKnowledge;
		this.stateHandler = stateHandler;
	}
	
	public double evaluateState(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		FBTPStateHandler fbtpStateHandler = (FBTPStateHandler) this.stateHandler;
		FBTPState fbtpstate = fbtpStateHandler.prepareState(stateObsMulti);
		
		return 0;
	}
}
