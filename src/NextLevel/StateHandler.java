package NextLevel;

import core.game.StateObservation;
import core.game.StateObservationMulti;

public class StateHandler
{
	public State prepareState(StateObservation stateObs)
	{
		return new State();
		// To be overridden in subclasses
	}
}
