package NextLevel;

import java.util.HashMap;
import java.util.Map;

public class Memory
{
	private HashMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap;

	public Memory()
	{
		spriteTypeFeaturesMap = new HashMap<Integer, SpriteTypeFeatures>();
	}

	public Memory copy()
	{
		Memory memoryCopy = new Memory();
		memoryCopy.setSpriteTypeFeaturesMap(new HashMap<Integer, SpriteTypeFeatures>(spriteTypeFeaturesMap));

		return memoryCopy;
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
