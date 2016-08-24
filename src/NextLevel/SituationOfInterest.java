package NextLevel;

import core.game.StateObservation;

public class SituationOfInterest
{
	protected PointOfInterest pointOfInterest;
	protected StateObservation baseState;
	protected StateObservation afterState;
	protected double importance;
	
	public SituationOfInterest()
	{
		this.pointOfInterest = null;
		this.baseState = null;
		this.afterState = null;
	}
}
