package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import NextLevel.GameKnowledge;
import NextLevel.moveController.PathFinder;

public class FBTPPathFinder extends PathFinder
{
	private FBTPGameKnowledge gameKnowledge;
	
	public FBTPPathFinder(GameKnowledge gameKnowledge)
	{
		this.gameKnowledge = (FBTPGameKnowledge)gameKnowledge;
	}
}
