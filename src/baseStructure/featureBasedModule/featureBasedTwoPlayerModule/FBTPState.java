package baseStructure.featureBasedModule.featureBasedTwoPlayerModule;

import baseStructure.twoPlayer.BasicTPState;
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
