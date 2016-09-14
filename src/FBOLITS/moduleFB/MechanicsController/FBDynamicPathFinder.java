package FBOLITS.moduleFB.MechanicsController;

import FBOLITS.mechanicsController.DynamicPathFinder;
import FBOLITS.mechanicsController.GameMechanicsController;
import FBOLITS.moduleFB.FBGameKnowledge;

public class FBDynamicPathFinder extends DynamicPathFinder
{
	// protected FBGameKnowledge gameKnowledge;
	// protected GameMechanicsController gameMechanicsController

	public FBDynamicPathFinder(FBGameKnowledge gameKnowledge, GameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		tryToDestroyObjects = true;
	}
}
