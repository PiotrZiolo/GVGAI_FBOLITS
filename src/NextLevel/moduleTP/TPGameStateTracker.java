package NextLevel.moduleTP;

import java.util.ArrayList;

import NextLevel.GameStateTracker;
import NextLevel.PointOfInterest;
import NextLevel.mechanicsController.TPGameMechanicsController;
import core.game.StateObservation;
import core.game.StateObservationMulti;

public class TPGameStateTracker extends GameStateTracker
{
	public void GameStateTracker(TPGameMechanicsController gameMechanicsController)
	{
		this.gameMechanicsController = gameMechanicsController;
		this.pois = new ArrayList<PointOfInterest>();
	}
	
	public void runTracker(StateObservation stateObs)
	{
		updatePOIsPosition(stateObs);
		searchForNewPOIs(stateObs);
	}
	
	protected void updatePOIsPosition(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		for (PointOfInterest poi : this.pois)
		{
			if (poi.track)
			{
				
			}
		}
	}
	
	protected void searchForNewPOIs(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
	}
}
