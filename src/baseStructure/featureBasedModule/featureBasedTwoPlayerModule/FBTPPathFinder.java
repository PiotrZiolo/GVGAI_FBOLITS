package baseStructure.featureBasedModule.featureBasedTwoPlayerModule;

import baseStructure.GameKnowledge;
import baseStructure.moveController.PathFinder;

public class FBTPPathFinder extends PathFinder
{
	private FBTPGameKnowledge gameKnowledge;
	
	public FBTPPathFinder(GameKnowledge gameKnowledge)
	{
		this.gameKnowledge = (FBTPGameKnowledge)gameKnowledge;
	}
}
