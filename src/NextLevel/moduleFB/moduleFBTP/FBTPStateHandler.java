package NextLevel.moduleFB.moduleFBTP;

import NextLevel.moduleTP.TPStateHandler;
import core.game.StateObservation;
import core.game.StateObservationMulti;

public class FBTPStateHandler extends TPStateHandler
{
	public FBTPState prepareState(StateObservation stateObs)
	{
		return new FBTPState((StateObservationMulti) stateObs);
	}
}
