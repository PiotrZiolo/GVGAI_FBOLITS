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
		updatePOIs(stateObs);
	}
	
	public ArrayList<PointOfInterest> getPOIs()
	{
		return pois;
	}
	
	public ArrayList<PointOfInterest> getNewPOIs()
	{
		return newPOIs;
	}
	
	public ArrayList<PointOfInterest> getRemovedPOIs()
	{
		return removedPOIs;
	}
	
	/**
	 * Checks changes in positions of POIs, check new POIs, check removed POIs
	 * @param stateObs
	 */
	protected void updatePOIs(StateObservation stateObs)
	{
		for (PointOfInterest poi : this.pois)
		{
			if (poi.track)
			{
				
			}
		}
	}
}
