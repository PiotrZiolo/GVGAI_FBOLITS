package NextLevel;

import core.game.Observation;
import tools.Vector2d;

public class PointOfInterest
{
	protected Vector2d position;
	protected Observation observation;
	protected double importance;
	
	// protected int id;
	protected int poiType; // 0 - sprite, 1 - position on the map
	
	public boolean track;

}
