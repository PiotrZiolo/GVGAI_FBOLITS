package NextLevel.mechanicsController;

import NextLevel.GameKnowledge;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class AgentMoveController
{
	protected GameKnowledge gameKnowledge;
	protected GameMechanicsController gameMechanicsController;
	protected PathFinder pathFinder;
	
	protected ElapsedCpuTimer elapsedTimer;
	
	public AgentMoveController()
	{
		this.elapsedTimer = new ElapsedCpuTimer();
	}
	
	public AgentMoveController(GameKnowledge gameKnowledge, GameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		pathFinder = new PathFinder(gameKnowledge, gameMechanicsController);
		
		this.elapsedTimer = new ElapsedCpuTimer();
	}
	
	public Types.ACTIONS moveTowardPosition(StateObservation stateObs)
	{
		return Types.ACTIONS.ACTION_NIL;
	}
}
