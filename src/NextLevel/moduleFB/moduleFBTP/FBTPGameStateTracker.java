package NextLevel.moduleFB.moduleFBTP;

import java.util.ArrayList;

import NextLevel.GameStateTracker;
import NextLevel.PointOfInterest;
import NextLevel.PointOfInterest.POITYPE;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.moduleFB.SpriteTypeFeatures;
//import NextLevel.utils.LogHandler;
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
		// LogHandler.writeLog("POI list size: " + pois.size(), "FBTPGameStateTracker.updatePOIsPosition", 3);
		for (int index = 0; index < this.pois.size(); index++)
		{
			PointOfInterest poi = this.pois.get(index);
			if (poi.track)
			{
				Observation obs = this.gameMechanicsController.localizeSprite(stateObsMulti, poi.observation);
				/*
				LogHandler.writeLog("Tracking POI| id: " + poi.observation.obsID + ", type: " + poi.observation.itype 
						+ ", category: " + poi.observation.category + ", POI position: [" + poi.observation.position.x + ", " 
						+ poi.observation.position.y + "]" + " Sprite localized: " 
						+ ((obs != null) ? "yes" : "no") + " Position changed: " 
						+ ((obs != null) ? ((!poi.position.equals(obs.position)) ? "yes" : "no") : "unknown")
						+ ((obs != null) ? " Position obs: " + obs.position : ""), "FBTPGameStateTracker.updatePOIsPosition", 0);
				*/
				if (obs == null)
				{
					this.removedPOIs.add(poi);
					this.pois.remove(poi);
					index--;
					continue;
				}

				if (!poi.position.equals(obs.position))
				{
					this.pois.get(index).positionChangedFromPreviousTurn = true;
					this.pois.get(index).position = obs.position;
					
					SpriteTypeFeatures features = this.gameKnowledge.getSpriteTypeFeaturesByType(obs.itype);
					if (features != null)
						features.moving = true;
				}
				else
				{
					this.pois.get(index).positionChangedFromPreviousTurn = false;
					poi.track = false;
					SpriteTypeFeatures features = this.gameKnowledge.getSpriteTypeFeaturesByType(obs.itype);
					if (features != null)
						features.moving = false;
				}

				this.pois.get(index).observation = obs;
			}
			else
			{
				Observation obs = this.gameMechanicsController.localizeSprite(stateObsMulti, poi.observation);
				/*
				LogHandler.writeLog("Checking existence of POI| id: " + poi.observation.obsID + ", type: " + poi.observation.itype 
						+ ", category: " + poi.observation.category + ", POI position: [" + poi.observation.position.x + ", " 
						+ poi.observation.position.y + "]" + " Sprite localized: " 
						+ ((obs != null) ? "yes" : "no") + " Position changed: " 
						+ ((obs != null) ? ((!poi.position.equals(obs.position)) ? "yes" : "no") : "unknown")
						+ ((obs != null) ? " Position obs: " + obs.position : ""), "FBTPGameStateTracker.updatePOIsPosition", 0);
				*/
				if (obs == null)
				{
					this.removedPOIs.add(poi);
					this.pois.remove(poi);
					index--;
					continue;
				}
			}
		}
	}

	protected void searchForNewPOIs(StateObservation stateObs)
	{
		ArrayList<Integer> POIIDs = new ArrayList<Integer>();
		for (int index = 0; index < this.pois.size(); index++)
			POIIDs.add(this.pois.get(index).observation.obsID);

		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		ArrayList<Observation> observationList = ((TPGameMechanicsController)this.gameMechanicsController).getListOfSprites(stateObsMulti);
		this.newPOIs.clear();
		for (Observation obs : observationList)
		{
			if (!(obs.itype == 0 && obs.category == 4) && !POIIDs.contains(obs.obsID))
			{
				/*
				LogHandler.writeLog("Adding new POI| id: " + obs.obsID + ", type: " + obs.itype 
						+ ", category: " + obs.category + ", position: [" + obs.position.x + ", " 
						+ obs.position.y + "]", "FBTPGameStateTracker.searchForNewPOIs", 0);
				*/
				PointOfInterest poi = new PointOfInterest(POITYPE.SPRITE, obs, 0);
				
				/*
				SpriteTypeFeatures features = this.gameKnowledge.getSpriteTypeFeaturesByType(obs.itype);
				if (features != null)
					poi.track = features.moving;
				else
					poi.track = true;
				*/
				poi.track = true;
				this.pois.add(poi);
				this.newPOIs.add(poi);
			}
		}
	}
}
