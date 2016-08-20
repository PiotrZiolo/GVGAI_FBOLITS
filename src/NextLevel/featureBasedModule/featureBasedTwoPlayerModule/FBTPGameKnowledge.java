package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import NextLevel.GameObjectives;
import NextLevel.featureBasedModule.InfluenceMap;
import NextLevel.featureBasedModule.SpriteTypeFeatures;
import NextLevel.twoPlayer.TPGameKnowledge;
import ontology.Types;

public class FBTPGameKnowledge extends TPGameKnowledge
{
	protected InfluenceMap influenceMap;

	public FBTPGameKnowledge()
	{
		spriteTypeFeaturesMap = new HashMap<Integer, SpriteTypeFeatures>();
	}
	
	public InfluenceMap getInfluenceMap()
	{
		return influenceMap;
	}

	public void setInfluenceMap(InfluenceMap influenceMap)
	{
		this.influenceMap = influenceMap;
	}
	
	private HashMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap;

	public FBTPGameKnowledge copy()
	{
		FBTPGameKnowledge gameKnowledgeCopy = new FBTPGameKnowledge();
		gameKnowledgeCopy.setSpriteTypeFeaturesMap(new HashMap<Integer, SpriteTypeFeatures>(spriteTypeFeaturesMap));

		return gameKnowledgeCopy;
	}

	public SpriteTypeFeatures getSpriteTypeFeaturesByType(int iType)
	{
		if (spriteTypeFeaturesMap.containsKey(iType))
		{
			return spriteTypeFeaturesMap.get(iType);
		}
		else
		{
			return null;
		}
	}

	public HashMap<Integer, SpriteTypeFeatures> getSpriteTypeFeaturesByCategory(int iCategory)
	{
		HashMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMapInOneCategory = new HashMap<Integer, SpriteTypeFeatures>();

		for (Map.Entry<Integer, SpriteTypeFeatures> spriteTypeFeaturesMapEntry : spriteTypeFeaturesMap.entrySet())
		{
			if (spriteTypeFeaturesMapEntry.getValue().category == iCategory)
			{
				spriteTypeFeaturesMapInOneCategory.put(spriteTypeFeaturesMapEntry.getKey(),
						spriteTypeFeaturesMapEntry.getValue());
			}
		}

		return spriteTypeFeaturesMapInOneCategory;
	}

	public void setSpriteTypeFeaturesByType(int iType, SpriteTypeFeatures spriteTypeFeatures)
	{
		spriteTypeFeaturesMap.put(iType, spriteTypeFeatures);
	}

	public HashMap<Integer, SpriteTypeFeatures> getSpriteTypeFeaturesMap()
	{
		return spriteTypeFeaturesMap;
	}

	public void setSpriteTypeFeaturesMap(HashMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap)
	{
		this.spriteTypeFeaturesMap = spriteTypeFeaturesMap;
	}
}
