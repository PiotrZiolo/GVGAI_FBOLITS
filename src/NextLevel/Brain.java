package NextLevel;

import java.util.HashMap;

import core.game.Observation;
import core.game.StateObservationMulti;
import tools.ElapsedCpuTimer;

public class Brain
{
	Memory memory;

	public Brain()
	{
		memory = new Memory();
	}
	
	public void learn(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID)
	{
		HashMap<Integer, SpriteTypeFeatures> spriteInfos;
		int iAdvanceCount = 0; 
		
		
	}
	
	public void update()
	{
		
	}
	
	public void rememberAllInLongTermMemory()
	{
		
	}
	
	public void initializeFromLongTermMemory()
	{
		
	}
	
	private SpriteTypeFeatures testSprite(Observation observation)
	{
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(observation.itype);
		
		
		
		return spriteTypeFeatures;
	}
}
