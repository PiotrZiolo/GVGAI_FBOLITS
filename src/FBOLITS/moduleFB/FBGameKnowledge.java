package FBOLITS.moduleFB;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import FBOLITS.moduleFB.SpriteTypeFeatures;
import FBOLITS.GameKnowledge;

public class FBGameKnowledge extends GameKnowledge
{
	public FBGameKnowledge()
	{
		spriteTypeFeaturesMap = new HashMap<Integer, SpriteTypeFeatures>();
	}

	private HashMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap;

	public FBGameKnowledge copy()
	{
		FBGameKnowledge gameKnowledgeCopy = new FBGameKnowledge();
		gameKnowledgeCopy.setSpriteTypeFeaturesMap(new HashMap<Integer, SpriteTypeFeatures>(spriteTypeFeaturesMap));

		return gameKnowledgeCopy;
	}

	public SpriteTypeFeatures getSpriteTypeFeaturesByType(int type)
	{
		if (spriteTypeFeaturesMap.containsKey(type))
		{
			return spriteTypeFeaturesMap.get(type);
		}
		return null;
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
		spriteTypeFeatures.featuresUpdatedThisTurn = true;
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

	public Set<Integer> getSpriteTypes()
	{
		return spriteTypeFeaturesMap.keySet();
	}

	public int getSpriteCategory(int typeId)
	{
		if (spriteTypeFeaturesMap.containsKey(typeId))
			return spriteTypeFeaturesMap.get(typeId).category;
		else
			return 0;
	}
}
