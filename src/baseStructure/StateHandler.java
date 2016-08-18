package baseStructure;

import core.game.StateObservation;

public class StateHandler
{
	public State prepareState(StateObservation stateObs)
	{
		return new State();
		// To be overridden in subclasses
	}
}
