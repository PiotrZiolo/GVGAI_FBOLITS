package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import NextLevel.twoPlayer.BasicTPState;
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
	
	public void setStateObs(StateObservationMulti stateObs)
	{
		this.stateObs = stateObs;
	}

	public StateObservationMulti getStateObservation()
	{
		return stateObs;
	}
}
