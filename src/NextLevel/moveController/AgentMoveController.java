package NextLevel.moveController;

import NextLevel.GameKnowledge;
import core.game.StateObservation;
import ontology.Types;

public class AgentMoveController
{
	private GameKnowledge gameKnowledge;
	private PathFinder pathFinder;
	
	public AgentMoveController()
	{
		
	}
	
	public AgentMoveController(GameKnowledge gameKnowledge)
	{
		this.gameKnowledge = gameKnowledge;
		pathFinder = new PathFinder(gameKnowledge);
	}
	
	public Types.ACTIONS moveTowardPosition(StateObservation stateObs)
	{
		return Types.ACTIONS.ACTION_NIL;
	}
}