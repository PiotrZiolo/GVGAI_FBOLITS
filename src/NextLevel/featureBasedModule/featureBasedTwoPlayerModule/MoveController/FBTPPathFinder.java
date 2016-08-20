package NextLevel.featureBasedModule.featureBasedTwoPlayerModule.MoveController;

import NextLevel.featureBasedModule.featureBasedTwoPlayerModule.FBTPGameKnowledge;
import NextLevel.moveController.PathFinder;

public class FBTPPathFinder extends PathFinder
{
	// Real types of fields
	// protected FBTPGameKnowledge gameKnowledge;
	
	public FBTPPathFinder(FBTPGameKnowledge gameKnowledge)
	{
		this.gameKnowledge = gameKnowledge;
	}
}
