package baseStructure.twoPlayer;

import baseStructure.BasicState;
import core.game.StateObservationMulti;

public class BasicTPState extends BasicState
{
	// Real types of fields
	// protected StateObservationMulti stateObs;

	public BasicTPState()
	{
		
	}
	
	public BasicTPState(StateObservationMulti stateObs)
	{
		this.stateObs = stateObs;
	}
	
	public StateObservationMulti getStateObservation()
	{
		return (StateObservationMulti)stateObs;
	}
}
