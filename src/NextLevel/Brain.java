package NextLevel;

import java.util.ArrayList;
import java.util.HashMap;

import core.game.Observation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class Brain
{
	private Memory memory;
	private int playerID;

	/**
	 * public constructor
	 */
	public Brain()
	{
		this.playerID = 0;
		this.memory = new Memory();
	}

	/**
	 * Public constructor.
	 * 
	 * @param playerID
	 *            ID of this agent.
	 */
	public Brain(int playerID)
	{
		this.playerID = playerID;
		this.memory = new Memory();
	}

	/**
	 * Follows the sprite.
	 * 
	 * @param stateObs
	 *            Observation of the initial state from which testing is to be
	 *            done.
	 * @param observation
	 *            Observation of the sprite to approach.
	 */
	public HashMap<Integer, SpriteTypeFeatures> getSpriteTypeFeatures()
	{
		return memory.getSpriteTypeFeaturesMap();
	}

	/**
	 * The main method to learn about all sprites' properties.
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 */
	public void learn(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer)
	{
		int iAdvanceCount = 0;

		Vector2d avatarPosition = stateObs.getAvatarPosition(playerID);
		ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions(avatarPosition);
		ArrayList<Observation>[] immovablePositions = stateObs.getImmovablePositions(avatarPosition);
		ArrayList<Observation>[] movablePositions = stateObs.getMovablePositions(avatarPosition);
		ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions(avatarPosition);
		ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions(avatarPosition);
		ArrayList<Observation>[] fromAvatarSpritesPositions = stateObs.getFromAvatarSpritesPositions(avatarPosition);

		if (npcPositions != null)
		{
			for (ArrayList<Observation> npcs : npcPositions)
			{
				if (npcs.size() > 0)
				{
					for (int i = 0; i < npcs.size(); i++)
					{
						if (memory.getSpriteTypeFeaturesByType(npcs.get(i).itype) == null)
						{
							memory.setSpriteTypeFeaturesByType(npcs.get(i).itype, 
									getSpriteTypeFeaturesForCategory(npcs.get(i).category, npcs.get(i).itype));
						}
					}
				}
			}
		}

		if (immovablePositions != null)
		{
			for (ArrayList<Observation> immovable : immovablePositions)
			{
				if (immovable.size() > 0)
				{
					for (int i = 0; i < immovable.size(); i++)
					{
						if (memory.getSpriteTypeFeaturesByType(immovable.get(i).itype) == null)
						{
							memory.setSpriteTypeFeaturesByType(immovable.get(i).itype, 
									getSpriteTypeFeaturesForCategory(immovable.get(i).category, immovable.get(i).itype));
						}
					}
				}
			}
		}

		if (movablePositions != null)
		{
			for (ArrayList<Observation> movable : movablePositions)
			{
				if (movable.size() > 0)
				{
					for (int i = 0; i < movable.size(); i++)
					{
						if (memory.getSpriteTypeFeaturesByType(movable.get(i).itype) == null)
						{
							memory.setSpriteTypeFeaturesByType(movable.get(i).itype, 
									getSpriteTypeFeaturesForCategory(movable.get(i).category, movable.get(i).itype));
						}
					}
				}
			}
		}

		if (resourcesPositions != null)
		{
			for (ArrayList<Observation> resources : resourcesPositions)
			{
				if (resources.size() > 0)
				{
					for (int i = 0; i < resources.size(); i++)
					{
						if (memory.getSpriteTypeFeaturesByType(resources.get(i).itype) == null)
						{
							memory.setSpriteTypeFeaturesByType(resources.get(i).itype, 
									getSpriteTypeFeaturesForCategory(resources.get(i).category, resources.get(i).itype));
						}
					}
				}
			}
		}

		if (portalPositions != null)
		{
			for (ArrayList<Observation> portals : portalPositions)
			{
				if (portals.size() > 0)
				{
					for (int i = 0; i < portals.size(); i++)
					{
						if (memory.getSpriteTypeFeaturesByType(portals.get(i).itype) == null)
						{
							memory.setSpriteTypeFeaturesByType(portals.get(i).itype, 
									getSpriteTypeFeaturesForCategory(portals.get(i).category, portals.get(i).itype));
						}
					}
				}
			}
		}

		if (fromAvatarSpritesPositions != null)
		{
			for (ArrayList<Observation> fromAvatarSprites : fromAvatarSpritesPositions)
			{
				if (fromAvatarSprites.size() > 0)
				{
					for (int i = 0; i < fromAvatarSprites.size(); i++)
					{
						if (memory.getSpriteTypeFeaturesByType(fromAvatarSprites.get(i).itype) == null)
						{
							memory.setSpriteTypeFeaturesByType(fromAvatarSprites.get(i).itype, 
									getSpriteTypeFeaturesForCategory(fromAvatarSprites.get(i).category, fromAvatarSprites.get(i).itype));
						}
					}
				}
			}
		}
	}

	/**
	 * Updates the knowledge about different sprites if new Events have appeared
	 * in the history.
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 */
	public void update(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer)
	{

	}

	/**
	 * Dumps all acquired knowledge into a file.
	 */
	public void rememberAllInLongTermMemory()
	{

	}

	/**
	 * Retrieves knowledge saved previously to a file.
	 */
	public void initializeFromLongTermMemory()
	{

	}

	/**
	 * Updates the knowledge about different sprites if new Events have appeared
	 * in the history.
	 * 
	 * @param stateObs
	 *            Observation of the initial state from which testing is to be
	 *            done.
	 * @param observation
	 *            Observation of the sprite to test.
	 */
	private SpriteTypeFeatures testSprite(StateObservationMulti stateObs, Observation observation)
	{
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(observation.itype);

		return spriteTypeFeatures;
	}

	/**
	 * Tries to approach the sprite given in observation. Returns a state in
	 * which the avatar is one step from the sprite. If it was impossible in a
	 * certain number of tries, null is returned.
	 * 
	 * @param stateObs
	 *            Observation of the initial state from which testing is to be
	 *            done.
	 * @param observation
	 *            Observation of the sprite to approach.
	 */
	private StateObservationMulti approachSprite(StateObservationMulti stateObs, Observation observation)
	{

		return stateObs;
	}

	/**
	 * Tries to make action on the sprite given in observation. Returns states
	 * just before the action and just after. If it was impossible in a certain
	 * number of tries, null is returned.
	 * 
	 * @param stateObs
	 *            Observation of the initial state from which testing is to be
	 *            done.
	 * @param observation
	 *            Observation of the sprite to approach.
	 */
	private StateObservationMulti[] makeActionOnSprite(StateObservationMulti stateObs, Observation observation,
			Types.ACTIONS action)
	{
		StateObservationMulti stateObsJustBeforeAction = stateObs.copy();
		StateObservationMulti stateObsAfterAction = stateObs.copy();

		return new StateObservationMulti[] { stateObsJustBeforeAction, stateObsAfterAction };
	}

	/**
	 * Follows the sprite.
	 * 
	 * @param stateObs
	 *            Observation of the initial state from which testing is to be
	 *            done.
	 * @param observation
	 *            Observation of the sprite to approach.
	 */
	private StateObservationMulti chaseSprite(StateObservationMulti stateObs, Observation observation)
	{
		return stateObs;
	}
	
	private SpriteTypeFeatures getSpriteTypeFeaturesForAsteroids(int category, int type)
	{
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(type);
		
		/*
		 * int type, double dangerousToAvatar, boolean dangerousOtherwise, boolean destroyable,
			boolean collectable, boolean givingVictory, boolean givingDefeat, int givingPoints, boolean passable,
			boolean moving, double speed, boolean increasingValuesOfOtherObjects, boolean allowingVictory
		 */
		
		switch (category)
		{
			case 1:

				break;

			case 2:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, false, true, true, false, 1, true, false, 0, true, true);
				break;

			case 3:

				break;

			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, true, false, false, false, 0, false, false, 0, false, false);
				break;

			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 1, false, false, false, false, true, 0, false, true, 1, false, false);
				break;

			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 1, false, true, false, false, true, 1, false, true, 1, false, false);
				break;

			default:

				break;
		}
		
		return spriteTypeFeatures; 
	}

	private SpriteTypeFeatures getSpriteTypeFeaturesForCategory(int category, int type)
	{
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(type);
		
		/*
		 * int type, double dangerousToAvatar, boolean dangerousOtherwise, boolean destroyable,
			boolean collectable, boolean givingVictory, boolean givingDefeat, int givingPoints, boolean passable,
			boolean moving, double speed, boolean increasingValuesOfOtherObjects, boolean allowingVictory
		 */
		
		switch (category)
		{
			case 1:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, false, true, false, false, 1, true, false, 0, false, false);
				break;

			case 2:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, false, true, true, false, 1, true, false, 0, true, true);
				break;

			case 3:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0.1, true, true, false, false, true, 1, false, true, 1, false, false);
				break;

			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, true, false, false, false, 0, false, false, 0, false, false);
				break;

			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 1, false, false, false, false, true, 0, false, true, 1, false, false);
				break;

			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0.1, true, true, true, false, false, 1, true, true, 0.1, false, false);
				break;

			default:

				break;
		}
		
		return spriteTypeFeatures;
	}
}
