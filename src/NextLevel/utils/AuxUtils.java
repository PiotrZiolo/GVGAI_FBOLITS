package NextLevel.utils;

import core.game.StateObservationMulti;
import ontology.Types;

public class AuxUtils
{
	public static int actionToIndex(StateObservationMulti stateObs, int playerID, Types.ACTIONS action)
	{
		int index = 0;
		
		for ( ; index < stateObs.getAvailableActions().size(); index++)
		{
			if (action == stateObs.getAvailableActions().get(index))
				break;
		}
		
		return index; 
	}
	
	public static Types.ACTIONS indexToAction(StateObservationMulti stateObs, int playerID, int index)
	{
		return stateObs.getAvailableActions(playerID).get(index);
	}
}
