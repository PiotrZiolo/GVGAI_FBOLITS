package FBOLITS;

import core.game.StateObservation;

public class StateEvaluator
{
	protected GameKnowledge gameKnowledge;
	protected StateHandler stateHandler;

	public StateEvaluator()
	{

	}
	
	public StateEvaluator(GameKnowledge gameKnowledge)
	{
		this.gameKnowledge = gameKnowledge;
	}

	public StateEvaluator(GameKnowledge gameKnowledge, StateHandler stateHandler)
	{
		this.gameKnowledge = gameKnowledge;
		this.stateHandler = stateHandler;
	}

	public double evaluateState(StateObservation stateObs)
	{
		// To be overridden in subclasses
		//State state = stateHandler.prepareState(stateObs);

		return 0;
	}
}
