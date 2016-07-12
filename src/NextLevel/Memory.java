package NextLevel;

import java.util.HashMap;

public class Memory
{
	HashMap<Integer, SpriteTypeFeatures> spriteInfos;
	
	public Memory()
	{
		spriteInfos = new HashMap<Integer, SpriteTypeFeatures>();
	}
	
	public SpriteTypeFeatures getSpriteInfoByType(int iType)
	{
		return spriteInfos.get(iType);
	}
}
