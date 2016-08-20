package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import NextLevel.StateHandler;
import NextLevel.twoPlayer.TPStateHandler;
import core.game.StateObservation;
import core.game.StateObservationMulti;

public class FBTPStateHandler extends TPStateHandler
{
	public FBTPState prepareState(StateObservation stateObs)
	{
		return new FBTPState((StateObservationMulti) stateObs);
	}
}
