package FBOLITS.moduleFB;

import FBOLITS.StateEvaluatorTeacher;
import core.game.StateObservation;

public class FBStateEvaluatorTeacher extends StateEvaluatorTeacher
{
	// Real types of fields
	// protected FBStateEvaluator stateEvaluator;
	// protected FBGameKnowledge gameKnowledge;
	// protected StateObservationMulti stateObs;

	public FBStateEvaluatorTeacher(FBStateEvaluator stateEvaluator, FBGameKnowledge gameKnowledge)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
	}

	public void initializeEvaluator(StateObservation stateObs)
	{
		FBStateEvaluator fbtpStateEvaluator = (FBStateEvaluator) this.stateEvaluator;
		FBGameKnowledge fbtpGameKnowledge = (FBGameKnowledge) this.gameKnowledge;
		this.stateObs = stateObs;
		fbtpStateEvaluator.setInfluenceMap(new InfluenceMap(stateObs, fbtpGameKnowledge));
	}

	public void updateEvaluator()
	{

	}
}
