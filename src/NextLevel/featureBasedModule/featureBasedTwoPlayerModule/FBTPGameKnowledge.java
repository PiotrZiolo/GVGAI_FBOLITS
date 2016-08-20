package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import java.util.ArrayList;

import NextLevel.GameObjectives;
import NextLevel.featureBasedModule.InfluenceMap;
import NextLevel.twoPlayer.TPGameKnowledge;
import ontology.Types;

public class FBTPGameKnowledge extends TPGameKnowledge
{
	protected InfluenceMap influenceMap;

	public InfluenceMap getInfluenceMap()
	{
		return influenceMap;
	}

	public void setInfluenceMap(InfluenceMap influenceMap)
	{
		this.influenceMap = influenceMap;
	}
}
