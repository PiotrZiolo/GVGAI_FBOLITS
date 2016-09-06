package NextLevel.moduleFB.moduleFBTP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeSet;

import NextLevel.GameStateTracker;
import NextLevel.PointOfInterest;
import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.moduleFB.SpriteTypeFeatures;
import NextLevel.moduleFB.moduleFBTP.MechanicsController.FBTPAgentMoveController;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPGameKnowledgeExplorer;
import NextLevel.moduleTP.TPSituationOfInterest;
import NextLevel.utils.LogHandler;
import NextLevel.utils.Pair;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class BasicFBTPGameKnowledgeExplorer extends TPGameKnowledgeExplorer
{
	// Real field types
	// protected FBTPGameKnowledge gameKnowledge;
	// protected TPGameMechanicsController gameMechanicsController;
	// protected FBTPAgentMoveController agentMoveController;
	// protected TPGameStateTracker gameStateTracker;

	// protected ElapsedCpuTimer elapsedTimer;

	protected int playerID;
	protected int oppID;

	protected PriorityQueue<TPSituationOfInterest> situationsToCheck;
	private ArrayList<Integer> typeIdsEncountered;

	public BasicFBTPGameKnowledgeExplorer()
	{
		this.situationsToCheck = new PriorityQueue<TPSituationOfInterest>(1, comp);
		this.typeIdsEncountered = new ArrayList<Integer>();
	}

	public BasicFBTPGameKnowledgeExplorer(FBTPGameKnowledge gameKnowledge, FBTPAgentMoveController agentMoveController,
			TPGameMechanicsController gameMechanicsController, FBTPGameStateTracker gameStateTracker)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		this.agentMoveController = agentMoveController;
		this.gameStateTracker = gameStateTracker;
		this.situationsToCheck = new PriorityQueue<TPSituationOfInterest>(1, comp);
		this.typeIdsEncountered = new ArrayList<Integer>();

		this.elapsedTimer = new ElapsedCpuTimer();
	}
	
	public void learnBasics(StateObservation stateObs, int playerID)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		this.stateObs = stateObsMulti;
		this.playerID = playerID;
		this.oppID = 1 - playerID;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		fbtpGameKnowledge.setPlayerID(playerID);
		fbtpGameKnowledge.setOppID(1 - playerID);
		fbtpGameKnowledge.setNumOfPlayers(stateObsMulti.getNoPlayers());
		fbtpGameKnowledge.setNumOfPlayerActions(stateObsMulti.getAvailableActions(playerID).size());
		fbtpGameKnowledge.setNumOfOpponentActions(stateObsMulti.getAvailableActions(this.oppID).size());
		fbtpGameKnowledge.setPlayerActions(stateObsMulti.getAvailableActions(playerID));
		fbtpGameKnowledge.setOpponentActions(stateObsMulti.getAvailableActions(this.oppID));
		fbtpGameKnowledge.setWorldXDimension(stateObs.getObservationGrid().length);
		fbtpGameKnowledge.setWorldYDimension(stateObs.getObservationGrid()[0].length);
		fbtpGameKnowledge.setBlockSize(stateObs.getBlockSize());
		fbtpGameKnowledge.setAvatarSpeed(stateObs.getAvatarSpeed());
	}

	public void initialLearn(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int timeForLearning)
	{
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		
		fbtpGameKnowledge.setShootingAllowed(true);
		fbtpGameKnowledge.setDeterministicGame(false);
		fbtpGameKnowledge.setOpenMap(true);

		Vector2d avatarPosition = stateObsMulti.getAvatarPosition(playerID);
		ArrayList<Observation>[] portalPositions = stateObsMulti.getPortalsPositions(avatarPosition);
		ArrayList<Observation>[] npcPositions = stateObsMulti.getNPCPositions(avatarPosition);
		ArrayList<Observation>[] immovablePositions = stateObsMulti.getImmovablePositions(avatarPosition);
		ArrayList<Observation>[] movablePositions = stateObsMulti.getMovablePositions(avatarPosition);
		ArrayList<Observation>[] resourcesPositions = stateObsMulti.getResourcesPositions(avatarPosition);
		ArrayList<Observation>[] fromAvatarSpritesPositions = stateObsMulti
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
						LogHandler.writeLog(
								"Testing sprite of type: " + observations.get(0).itype + " and category: "
										+ observations.get(0).category,
								"BasicFBTPGameKnowledgeExplorer.initialLearn", 3);

						SpriteTypeFeatures spriteTypeFeatures = getSpriteTypeFeaturesForAsteroids(
								observations.get(0).category, observations.get(0).itype);
						fbtpGameKnowledge.setSpriteTypeFeaturesByType(observations.get(0).itype, spriteTypeFeatures);
					}
				}
			}
		}

		LogHandler.writeLog("Testing sprite of type: " + stateObsMulti.getAvatarType(oppID) + " and category: " + 0,
				"BasicFBTPGameKnowledgeExplorer.initialLearn", 3);

		SpriteTypeFeatures spriteTypeFeatures = getSpriteTypeFeaturesForAsteroids(0,
				stateObsMulti.getAvatarType(oppID));

		fbtpGameKnowledge.setSpriteTypeFeaturesByType(stateObsMulti.getAvatarType(oppID), spriteTypeFeatures);
	}

	public void successiveLearn(StateObservation stateObs, ElapsedCpuTimer elapsedTimer,
			int timeForLearning)
	{

	}

	public void addSOI(TPSituationOfInterest soi)
	{
		situationsToCheck.add(soi);
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
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, true, false, false, 0,
						true, false, 0, 0, false);
				break;
			case 2:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, true, false, 0,
						true, false, 0, 1, true);
				break;
			case 3:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, true, 1, 0, false);
				break;
			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, false, 0, 0, false);
				break;
			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, true, 1, 0, false);
				break;
			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, true, 0, 0, false);
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
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 1, false, true, false, false, true, 1,
						false, true, 1, 0, false);
				break;
			case 1:

				break;

			case 2:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, true, true, false, 1, true,
						false, 0, 1, true);
				break;

			case 3:

				break;

			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, true, false, false, false, 0,
						false, false, 0, 0, false);
				break;

			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 1, false, false, false, false, true, 0,
						false, true, 1, 0, false);
				break;

			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 1, false, true, false, false, true, 1,
						false, true, 1, 0, false);
				break;

			default:

				break;
		}

		return spriteTypeFeatures;
	}

	private Comparator<TPSituationOfInterest> comp = new Comparator<TPSituationOfInterest>()
	{
		public int compare(TPSituationOfInterest a, TPSituationOfInterest b)
		{
			if (a.importance > b.importance)
				return 1;
			if (a.importance == b.importance)
				return 0;
			return -1;
		}
	};
}
