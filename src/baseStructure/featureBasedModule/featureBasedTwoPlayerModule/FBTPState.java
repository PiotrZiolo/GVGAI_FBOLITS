package baseStructure.featureBasedModule.featureBasedTwoPlayerModule;

import baseStructure.BasicTPState;
import core.game.StateObservationMulti;

public class FBTPState extends BasicTPState
{
	private StateObservationMulti stateObs;

	public FBTPState()
	{
		
	}
	
	public FBTPState(StateObservationMulti stateObs)
	{
		this.stateObs = stateObs;
	}

	public StateObservationMulti getStateObservation()
	{
		return stateObs;
	}
}
