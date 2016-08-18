package baseStructure.featureBasedModule.featureBasedTwoPlayerModule;

import baseStructure.moveController.AgentMoveController;
import baseStructure.moveController.PathFinder;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;

public class FBTPAgentMoveController extends AgentMoveController
{
	// Real types of of fields
	// protected FBTPGameKnowledge gameKnowledge;
	// protected FBTPPathFinder pathFinder;
	
	public FBTPAgentMoveController(FBTPGameKnowledge gameKnowledge)
	{
		this.gameKnowledge = gameKnowledge;
		pathFinder = new FBTPPathFinder(gameKnowledge);
	}
	
	public Types.ACTIONS moveTowardPosition(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		
		return Types.ACTIONS.ACTION_NIL;
	}
}
