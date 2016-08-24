package NextLevel;

import java.util.ArrayList;

import NextLevel.mechanicsController.GameMechanicsController;
import core.game.StateObservation;

public class GameStateTracker
{
	protected GameMechanicsController gameMechanicsController;
	
	protected ArrayList<PointOfInterest> pois;
	protected ArrayList<PointOfInterest> newPOIs;
	protected ArrayList<PointOfInterest> removedPOIs;
	
	public GameStateTracker()
	{
		this.pois = new ArrayList<PointOfInterest>();
	}
	
	public GameStateTracker(GameMechanicsController gameMechanicsController)
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
		for (PointOfInterest poi : this.pois)
		{
			if (poi.track)
			{
				
			}
		}
	}
	
	protected void searchForNewPOIs(StateObservation stateObs)
	{
		
	}
}
