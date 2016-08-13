package baseStructure.featureBasedModule.featureBasedTwoPlayerModule;

import baseStructure.State;
import core.game.StateObservationMulti;

public class FBTPState extends State
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
