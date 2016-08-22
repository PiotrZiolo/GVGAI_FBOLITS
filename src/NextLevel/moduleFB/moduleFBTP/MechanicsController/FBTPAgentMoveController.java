package NextLevel.moduleFB.moduleFBTP.MechanicsController;

import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.moduleFB.moduleFBTP.FBTPGameKnowledge;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;

public class FBTPAgentMoveController extends AgentMoveController
{
	// Real types of of fields
	// protected FBTPGameKnowledge gameKnowledge;
	// protected TPGameMechanicsController gameMechanicsController;
	// protected FBTPPathFinder pathFinder;
	
	public FBTPAgentMoveController(FBTPGameKnowledge gameKnowledge, TPGameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		pathFinder = new FBTPPathFinder(gameKnowledge, gameMechanicsController);
	}
	
	public Types.ACTIONS moveTowardPosition(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		
		return Types.ACTIONS.ACTION_NIL;
	}
}
