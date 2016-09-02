package NextLevel.moduleFB.moduleFBTP;

import NextLevel.StateEvaluatorTeacher;
import NextLevel.utils.LogHandler;
import core.game.StateObservationMulti;

public class FBTPStateEvaluatorTeacher extends StateEvaluatorTeacher
{
	// Real types of fields
	// protected FBTPStateEvaluator stateEvaluator;
	// protected FBTPGameKnowledge gameKnowledge;
	// protected StateObservationMulti stateObs;

	public FBTPStateEvaluatorTeacher(FBTPStateEvaluator stateEvaluator, FBTPGameKnowledge gameKnowledge)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
	}

	public void initializeEvaluator(StateObservationMulti stateObs)
	{
		FBTPStateEvaluator fbtpStateEvaluator = (FBTPStateEvaluator) this.stateEvaluator;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		this.stateObs = stateObs;
		fbtpStateEvaluator.setInfluenceMap(new InfluenceMap(stateObs, fbtpGameKnowledge));
	}

	public void updateEvaluator()
	{

	}
}
