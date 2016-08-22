package baseStructure.featureBasedModule;

import NextLevel.moduleFB.moduleFBTP.FBTPGameKnowledge;
import tools.Vector2d;

public class InfluenceMap
{
	protected FBTPGameKnowledge gameKnowledge;
	
	public InfluenceMap(FBTPGameKnowledge gameKnowledge)
	{
		this.gameKnowledge = gameKnowledge;
	}
	
	public double getInfluenceValue(Vector2d position)
	{
		return 0;
	}
}
