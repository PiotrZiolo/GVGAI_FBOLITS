package FBOLITS.moduleFB.MechanicsController;

import FBOLITS.mechanicsController.AgentMoveController;
import FBOLITS.mechanicsController.GameMechanicsController;
import FBOLITS.moduleFB.FBGameKnowledge;
import tools.ElapsedCpuTimer;

public class FBAgentMoveController extends AgentMoveController
{
	// Real types of of fields
	// protected FBGameKnowledge gameKnowledge;
	// protected GameMechanicsController gameMechanicsController;
	// protected FBPathFinder pathFinder;
	// protected ElapsedCpuTimer elapsedTimer;

	public FBAgentMoveController(FBGameKnowledge gameKnowledge, GameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		pathFinder = new FBDynamicPathFinder(gameKnowledge, gameMechanicsController);

		this.elapsedTimer = new ElapsedCpuTimer();
	}
}
