package FBOLITS;

import core.game.StateObservation;

public class StateEvaluatorTeacher
{
	protected StateEvaluator stateEvaluator;
	protected GameKnowledge gameKnowledge;
	protected StateObservation stateObs;
	
	public StateEvaluatorTeacher()
	{
		
	}
	
	public StateEvaluatorTeacher(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;	
	}

	public void initializeEvaluator(StateObservation stateObs)
	{
		// To be overridden in subclasses
		this.stateObs = stateObs;
	}

	public void updateEvaluator(StateObservation stateObs)
	{
		// To be overridden in subclasses
	}
}
