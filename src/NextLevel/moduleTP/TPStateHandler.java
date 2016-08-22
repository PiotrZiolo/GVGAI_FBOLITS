package NextLevel.moduleTP;

import NextLevel.StateHandler;
import core.game.StateObservation;
import core.game.StateObservationMulti;

public class TPStateHandler extends StateHandler
{
	public BasicTPState prepareState(StateObservation stateObs)
	{
		return new BasicTPState((StateObservationMulti) stateObs);
	}
}
