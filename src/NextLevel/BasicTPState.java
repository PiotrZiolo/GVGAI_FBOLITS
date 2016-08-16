package NextLevel;

import NextLevel.State;
import core.game.StateObservationMulti;

public class BasicTPState extends State
{
	protected StateObservationMulti stateObs;

	public BasicTPState()
	{
		
	}
	
	public BasicTPState(StateObservationMulti stateObs)
	{
		this.stateObs = stateObs;
	}

	public StateObservationMulti getStateObservation()
	{
		return stateObs;
	}
}
