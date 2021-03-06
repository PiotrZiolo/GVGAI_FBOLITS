package NextLevel.moduleFB.moduleFBTP;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import NextLevel.moduleFB.SpriteTypeFeatures;
import NextLevel.moduleTP.TPGameKnowledge;

public class FBTPGameKnowledge extends TPGameKnowledge
{
	protected InfluenceMap influenceMap;

	// protected boolean useAllowed;

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
