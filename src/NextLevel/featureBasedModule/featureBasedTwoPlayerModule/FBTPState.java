package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import NextLevel.twoPlayer.BasicTPState;
import core.game.StateObservation;
import core.game.StateObservationMulti;

public class FBTPState extends BasicTPState
{
	// Real types of fields
	// protected StateObservationMulti stateObs;

	public FBTPState()
	{
		
	}
	
	public FBTPState(StateObservationMulti stateObs)
	{
		this.stateObs = stateObs;
	}
}
