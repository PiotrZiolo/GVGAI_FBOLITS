package baseStructure;

import core.game.StateObservation;

public class BasicState extends State
{
	protected StateObservation stateObs;

	public BasicState()
	{
		
	}
	
	public BasicState(StateObservation stateObs)
	{
		this.stateObs = stateObs;
	}

	public StateObservation getStateObservation()
	{
		return stateObs;
	}
}
