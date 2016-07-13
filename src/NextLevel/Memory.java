package NextLevel;

import java.util.HashMap;

public class Memory
{
	private HashMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap;
	
	public Memory()
	{
		spriteTypeFeaturesMap = new HashMap<Integer, SpriteTypeFeatures>();
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
	
	public void setSpriteTypeFeaturesByType(int iType, SpriteTypeFeatures spriteTypeFeatures)
	{
		spriteTypeFeaturesMap.put(iType, spriteTypeFeatures);
	}
	
	public HashMap<Integer, SpriteTypeFeatures> getSpriteTypeFeaturesMap()
	{
		return spriteTypeFeaturesMap;
	}
}
