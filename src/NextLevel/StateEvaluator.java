package NextLevel;

import core.game.StateObservation;

public class StateEvaluator
{
	protected GameKnowledge gameKnowledge;
	
	public StateEvaluator()
	{
		
	}
	
	public StateEvaluator(GameKnowledge gameKnowledge)
	{
		this.gameKnowledge = gameKnowledge;
	}
	
	public double evaluate(StateObservation stateObs)
	{
		// To be overridden in subclasses
		
		return 0;
	}
}
