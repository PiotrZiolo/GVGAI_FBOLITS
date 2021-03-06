package FBOLITS;

import core.game.StateObservation;

public class SituationOfInterest
{
	public PointOfInterest poi;
	public StateObservation baseState;
	public StateObservation afterState;
	public double importance;

	public boolean inception;
	public int activatingTypeId;

	public SituationOfInterest()
	{
		this.poi = null;
		this.baseState = null;
		this.afterState = null;
		this.activatingTypeId = 0;
		this.importance = 0;
		this.inception = false;
	}
}
