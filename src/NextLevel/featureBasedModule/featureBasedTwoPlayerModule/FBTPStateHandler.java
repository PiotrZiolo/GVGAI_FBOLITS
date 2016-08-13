package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import NextLevel.State;
import NextLevel.StateHandler;
import core.game.StateObservation;
import core.game.StateObservationMulti;

public class FBTPStateHandler extends StateHandler
{
	public void prepareState(State state, StateObservation stateObs)
	{
		state = new FBTPState((StateObservationMulti) stateObs);
	}
}
