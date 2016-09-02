package NextLevel;

import core.game.Observation;
import tools.Vector2d;

public class PointOfInterest
{
	public Vector2d position;
	public Observation observation;
	public double importance;

	// protected int id;
	public POITYPE poiType;

	public boolean positionChangedFromPreviousTurn;

	public boolean track;
	
	public PointOfInterest(POITYPE poiType, Vector2d position)
	{
		this.position = position;
		this.observation = null;
		this.importance = 0;
		this.poiType = poiType;
		this.positionChangedFromPreviousTurn = false;
		this.track = true;
	}
	
	public PointOfInterest(POITYPE poiType, Vector2d position, double importance)
	{
		this.position = position;
		this.observation = null;
		this.importance = importance;
		this.poiType = poiType;
		this.positionChangedFromPreviousTurn = false;
		this.track = true;
	}
	
	public PointOfInterest(POITYPE poiType, Observation observation)
	{
		this.position = observation.position;
		this.observation = observation;
		this.importance = 0;
		this.poiType = poiType;
		this.positionChangedFromPreviousTurn = false;
		this.track = true;
	}
	
	public PointOfInterest(POITYPE poiType, Observation observation, double importance)
	{
		this.position = observation.position;
		this.observation = observation;
		this.importance = importance;
		this.poiType = poiType;
		this.positionChangedFromPreviousTurn = false;
		this.track = true;
	}

	public static enum POITYPE
	{
		SPRITE, POSITION, SHOT
	}
}
