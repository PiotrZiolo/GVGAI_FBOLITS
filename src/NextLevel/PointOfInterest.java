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

	public static enum POITYPE
	{
		SPRITE, POSITION, SHOT
	}
}
