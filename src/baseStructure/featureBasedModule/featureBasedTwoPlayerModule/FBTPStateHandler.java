package baseStructure.featureBasedModule.featureBasedTwoPlayerModule;

import baseStructure.StateHandler;
import core.game.StateObservation;
import core.game.StateObservationMulti;

public class FBTPStateHandler extends StateHandler
{
	public FBTPState prepareState(StateObservation stateObs)
	{
		return new FBTPState((StateObservationMulti) stateObs);
	}
}
