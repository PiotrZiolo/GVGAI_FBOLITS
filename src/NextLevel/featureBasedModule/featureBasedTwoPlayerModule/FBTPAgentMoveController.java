package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import NextLevel.GameKnowledge;
import NextLevel.moveController.AgentMoveController;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;

public class FBTPAgentMoveController extends AgentMoveController
{
	private FBTPGameKnowledge gameKnowledge;
	
	public FBTPAgentMoveController(FBTPGameKnowledge gameKnowledge)
	{
		this.gameKnowledge = gameKnowledge;
	}
	
	public Types.ACTIONS moveTowardPosition(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		
		
		return Types.ACTIONS.ACTION_NIL;
	}
}
