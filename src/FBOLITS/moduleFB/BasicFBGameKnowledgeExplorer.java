package FBOLITS.moduleFB;

import java.util.ArrayList;

import FBOLITS.GameKnowledgeExplorer;
import FBOLITS.mechanicsController.GameMechanicsController;
import FBOLITS.moduleFB.MechanicsController.FBAgentMoveController;
import core.game.Observation;
import core.game.StateObservation;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class BasicFBGameKnowledgeExplorer extends GameKnowledgeExplorer
{
	// Real field types
	// protected StateObservation rootStateObs;
	// protected FBGameKnowledge gameKnowledge;
	// protected GameMechanicsController gameMechanicsController;
	// protected FBAgentMoveController agentMoveController;
	// protected GameStateTracker gameStateTracker;

	// protected ElapsedCpuTimer elapsedTimer;

	public BasicFBGameKnowledgeExplorer()
	{
	}

	public BasicFBGameKnowledgeExplorer(FBGameKnowledge gameKnowledge, FBAgentMoveController agentMoveController,
			GameMechanicsController gameMechanicsController, FBGameStateTracker gameStateTracker)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		this.agentMoveController = agentMoveController;
		this.gameStateTracker = gameStateTracker;

		this.elapsedTimer = new ElapsedCpuTimer();
	}

	@Override
	public void initialLearn(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int timeForLearning)
	{
		FBGameKnowledge fbGameKnowledge = (FBGameKnowledge) this.gameKnowledge;
		this.rootStateObs = stateObs;
		this.elapsedTimer = elapsedTimer;
		
		fbGameKnowledge.setAvatarSpriteId(gameMechanicsController.getPlayerId(stateObs));
		fbGameKnowledge.setShootingAllowed(checkWhetherShootingIsAllowed(stateObs));
		fbGameKnowledge.setDeterministicGame(checkDeterminism(stateObs));
		fbGameKnowledge.setOpenMap(checkIfMapIsOpen(stateObs));

		Vector2d avatarPosition = stateObs.getAvatarPosition();
		ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions(avatarPosition);
		ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions(avatarPosition);
		ArrayList<Observation>[] immovablePositions = stateObs.getImmovablePositions(avatarPosition);
		ArrayList<Observation>[] movablePositions = stateObs.getMovablePositions(avatarPosition);
		ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions(avatarPosition);
		ArrayList<Observation>[] fromAvatarSpritesPositions = stateObs
				.getFromAvatarSpritesPositions(avatarPosition);

		ArrayList<ArrayList<Observation>[]> arraysOfSprites = new ArrayList<ArrayList<Observation>[]>();
		arraysOfSprites.add(portalPositions);
		arraysOfSprites.add(npcPositions);
		arraysOfSprites.add(immovablePositions);
		arraysOfSprites.add(movablePositions);
		arraysOfSprites.add(resourcesPositions);
		arraysOfSprites.add(fromAvatarSpritesPositions);

		for (ArrayList<Observation>[] positions : arraysOfSprites)
		{
			if (positions != null)
			{
				for (ArrayList<Observation> observations : positions)
				{
					if (observations.size() > 0)
					{
						SpriteTypeFeatures spriteTypeFeatures = getSpriteTypeFeaturesForCategory(
								observations.get(0).category, observations.get(0).itype);
						fbGameKnowledge.setSpriteTypeFeaturesByType(observations.get(0).itype, spriteTypeFeatures);
					}
				}
			}
		}
	}

	@Override
	public void successiveLearn(StateObservation stateObs, ElapsedCpuTimer elapsedTimer,
			int timeForLearning)
	{
		this.rootStateObs = stateObs;
		this.elapsedTimer = elapsedTimer;
	}

	private SpriteTypeFeatures getSpriteTypeFeaturesForCategory(int category, int type)
	{
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(category, type);

		/*
		 * int type, double dangerousToAvatar, boolean dangerousOtherwise,
		 * boolean destroyable, boolean collectable, boolean givingVictory,
		 * boolean givingDefeat, int givingPoints, boolean passable, boolean
		 * moving, double speed, boolean increasingValuesOfOtherObjects, boolean
		 * allowingVictory
		 */

		switch (category)
		{
			case 1:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, true, false, false,
						0,false, false, 0, true, false, 0, 0, false);
				break;
			case 2:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false,
						0, true, false, 0, true, false, 0, 1, true);
				break;
			case 3:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false,
						0, false, false, 0, false, true, 1, 0, false);
				break;
			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false,
						0, false, false, 0, false, false, 0, 0, false);
				break;
			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false,
						0, false, false, 0, false, true, 1, 0, false);
				break;
			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false,
						0, false, false, 0, false, true, 0, 0, false);
				break;
			default:
				break;
		}
		return spriteTypeFeatures;
	}

	private SpriteTypeFeatures getSpriteTypeFeaturesForAsteroids(int category, int type)
	{
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(category, type);

		/*
		 * int type, double dangerousToAvatar, boolean dangerousOtherwise,
		 * boolean destroyable, boolean collectable, boolean givingVictory,
		 * boolean givingDefeat, int givingPoints, boolean passable, boolean
		 * moving, double speed, boolean increasingValuesOfOtherObjects, boolean
		 * allowingVictory
		 */

		switch (category)
		{
			case 0:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, true, false, false,
						0,false, false, 0, true, false, 0, 0, false);
				break;
			case 1:

				break;

			case 2:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false,
						0, true, false, 0, true, false, 0, 1, true);
				break;

			case 3:

				break;

			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false,
						0, false, false, 0, false, false, 0, 0, false);
				break;

			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false,
						0, false, false, 0, false, true, 1, 0, false);
				break;

			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false,
						0, false, false, 0, false, true, 0, 0, false);
				break;

			default:

				break;
		}

		return spriteTypeFeatures;
	}
}
