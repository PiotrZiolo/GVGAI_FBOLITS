package NextLevel.moduleFB.moduleFBTP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import NextLevel.GameStateTracker;
import NextLevel.PointOfInterest;
import NextLevel.PointOfInterest.POITYPE;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.moduleFB.SpriteTypeFeatures;
import NextLevel.utils.LogHandler;
import core.game.Observation;
import core.game.StateObservation;
import core.game.StateObservationMulti;

public class FBTPGameStateTracker extends GameStateTracker
{
	private FBTPGameKnowledge gameKnowledge;

	public FBTPGameStateTracker(TPGameMechanicsController gameMechanicsController, FBTPGameKnowledge gameKnowledge)
	{
		this.gameMechanicsController = gameMechanicsController;
		this.gameKnowledge = gameKnowledge;
		this.pois = new ArrayList<PointOfInterest>();
		this.newPOIs = new ArrayList<PointOfInterest>();
		this.removedPOIs = new ArrayList<PointOfInterest>();
	}

	public void runTracker(StateObservation stateObs)
	{
		updatePOIsPosition(stateObs);
		searchForNewPOIs(stateObs);
	}

	protected void updatePOIsPosition(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		this.removedPOIs.clear();
		for (int index = 0; index < this.pois.size(); index++)
		{
			PointOfInterest poi = this.pois.get(index);
			if (poi.track)
			{
				LogHandler.writeLog("Tracking POI| id: " + poi.observation.obsID + ", type: " + poi.observation.itype 
						+ ", category: " + poi.observation.category + ", position: [" + poi.observation.position.x + ", " 
						+ poi.observation.position.y + "]", "FBTPGameStateTracker.searchForNewPOIs", 0);
				
				Observation obs = this.gameMechanicsController.localizeSprite(stateObsMulti, poi.observation);
				if (obs == null)
				{
					this.removedPOIs.add(poi);
					this.pois.remove(poi);
					index--;
					continue;
				}

				if (poi.position != obs.position)
				{
					this.pois.get(index).positionChangedFromPreviousTurn = true;
					this.pois.get(index).position = obs.position;
				}
				else
					this.pois.get(index).positionChangedFromPreviousTurn = false;

				this.pois.get(index).observation = obs;
			}
		}
	}

	protected void searchForNewPOIs(StateObservation stateObs)
	{
		Map<Integer, Integer> observationIdToPoiIndex = new HashMap<Integer, Integer>();
		for (int index = 0; index < this.pois.size(); index++)
			observationIdToPoiIndex.put(this.pois.get(index).observation.obsID, index);

		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		ArrayList<Observation>[][] observationTable = stateObsMulti.getObservationGrid();
		this.newPOIs.clear();
		for (ArrayList<Observation>[] observationArray : observationTable)
		{
			for (ArrayList<Observation> observationList : observationArray)
			{
				for (Observation obs : observationList)
				{
					if (!observationIdToPoiIndex.containsKey(obs.itype))
					{
						LogHandler.writeLog("Adding new POI| id: " + obs.obsID + ", type: " + obs.itype 
								+ ", category: " + obs.category + ", position: [" + obs.position.x + ", " 
								+ obs.position.y + "]", "FBTPGameStateTracker.searchForNewPOIs", 3);
						
						PointOfInterest poi = new PointOfInterest(POITYPE.SPRITE, obs, 0);
						
						SpriteTypeFeatures features = this.gameKnowledge.getSpriteTypeFeaturesByType(obs.itype);
						if (features != null)
							poi.track = features.moving;
						else
							poi.track = true;
						this.pois.add(poi);
						this.newPOIs.add(poi);
					}
				}
			}
		}
	}
}
