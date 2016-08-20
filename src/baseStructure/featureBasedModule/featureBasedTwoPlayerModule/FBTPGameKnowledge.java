package baseStructure.featureBasedModule.featureBasedTwoPlayerModule;

import java.util.ArrayList;

import NextLevel.featureBasedModule.InfluenceMap;
import baseStructure.GameObjectives;
import baseStructure.twoPlayer.TPGameKnowledge;
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
