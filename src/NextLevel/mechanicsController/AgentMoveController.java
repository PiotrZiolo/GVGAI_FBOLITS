package NextLevel.mechanicsController;

import NextLevel.GameKnowledge;
import core.game.StateObservation;
import ontology.Types;

public class AgentMoveController
{
	protected GameKnowledge gameKnowledge;
	protected GameMechanicsController gameMechanicsController;
	protected PathFinder pathFinder;
	
	public AgentMoveController()
	{
		
	}
	
	public AgentMoveController(GameKnowledge gameKnowledge, GameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		pathFinder = new PathFinder(gameKnowledge, gameMechanicsController);
	}
	
	public Types.ACTIONS moveTowardPosition(StateObservation stateObs)
	{
		return Types.ACTIONS.ACTION_NIL;
	}
}
