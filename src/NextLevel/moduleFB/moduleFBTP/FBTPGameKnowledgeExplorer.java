package NextLevel.moduleFB.moduleFBTP;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeSet;

import NextLevel.PointOfInterest;
import NextLevel.PointOfInterest.POITYPE;
import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.moduleFB.SpriteTypeFeatures;
import NextLevel.moduleFB.moduleFBTP.MechanicsController.FBTPAgentMoveController;
import NextLevel.moduleTP.TPGameKnowledgeExplorer;
import NextLevel.moduleTP.TPSituationOfInterest;
import NextLevel.utils.Pair;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class FBTPGameKnowledgeExplorer extends TPGameKnowledgeExplorer
{
	// Real field types
	// protected FBTPGameKnowledge gameKnowledge;
	// protected TPGameMechanicsController gameMechanicsController;
	// protected FBTPAgentMoveController agentMoveController;
	// protected TPGameStateTracker gameStateTracker;
	
	// protected ElapsedCpuTimer elapsedTimer;

	protected int playerID;
	protected int oppID;

	//protected PriorityQueue<TPSituationOfInterest> basicSituationsToCheck;
	protected PriorityQueue<TPSituationOfInterest> situationsToCheck;
	private ArrayList<Integer> typeIdsEncountered;

	public FBTPGameKnowledgeExplorer()
	{
		this.situationsToCheck = new PriorityQueue<TPSituationOfInterest>(1, comp);
		this.typeIdsEncountered = new ArrayList<Integer>();
	}

	public FBTPGameKnowledgeExplorer(FBTPGameKnowledge gameKnowledge, FBTPAgentMoveController agentMoveController,
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

	public void initialLearn(StateObservation stateObs, int playerID, ElapsedCpuTimer elapsedTimer, int timeForLearning)
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

		fbtpGameKnowledge.setShootingAllowed(checkWhetherShootingIsAllowed(stateObsMulti));
		fbtpGameKnowledge.setDeterministicGame(false);
		fbtpGameKnowledge.setOpenMap(true);
		
		successiveLearn(stateObs, playerID, elapsedTimer, timeForLearning);
	}

	public void successiveLearn(StateObservation stateObs, int playerID, ElapsedCpuTimer elapsedTimer,
			int timeForLearning)
	{
		putAllSpritesInQueue((StateObservationMulti)stateObs, -1);
		ArrayList<Integer> typesAnalysed = new ArrayList<Integer>();
		long startTime = elapsedTimer.remainingTimeMillis();
		long loopStartTime = 0;
		long loopEndTime = 0;
		
		while (startTime - elapsedTimer.remainingTimeMillis() < timeForLearning - ( loopStartTime - loopEndTime )
				&& !situationsToCheck.isEmpty())
		{
			loopStartTime = elapsedTimer.remainingTimeMillis();
			
			TPSituationOfInterest soi = situationsToCheck.poll();

			if (!typesAnalysed.contains(soi.poi.observation.itype) || soi.inception)
			{

				/*System.out.println("Data");
				System.out.println(soi.poi.observation.category);
				System.out.println(soi.importance);
				System.out.println(soi.poi.observation.position);*/
				if (!soi.inception)
					typesAnalysed.add(soi.poi.observation.itype);

				if (soi.afterState == null)
					approachAndInvestigate(soi, loopStartTime + timeForLearning - startTime);
				else
					investigateEvent(soi);
			}
			
			loopEndTime = elapsedTimer.remainingTimeMillis();
		}
		
		if (situationsToCheck.size()>20)
			situationsToCheck.clear();	// reseting queue
	}

	private void investigateEvent(TPSituationOfInterest soi)
	{
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		TPGameMechanicsController tpGameMechanicsController = (TPGameMechanicsController) this.gameMechanicsController;
		StateObservationMulti soiBaseState = (StateObservationMulti) soi.baseState;
		StateObservationMulti soiAfterState = (StateObservationMulti) soi.afterState;

		TreeSet<Event> allEvents = tpGameMechanicsController.getEventsDuringSOI(soi);
		TreeSet<Event> interestingEvents = new TreeSet<Event>();
		TreeSet<Event> maybeInterestingEvents = new TreeSet<Event>();
		for (Event event : allEvents)
		{
			maybeInterestingEvents.add(event);
			if (!tpGameMechanicsController
					.isSpriteDoingNothing(fbtpGameKnowledge.getSpriteTypeFeaturesByType(event.passiveTypeId)))
			{
				interestingEvents.add(event);
			}
		}
		if (interestingEvents.size() == 0 && maybeInterestingEvents.size()==1)
			interestingEvents = maybeInterestingEvents;

		//System.out.println("interestingEvents.size()");
		//System.out.println(interestingEvents.size());
		
		if (interestingEvents.size() == 1)
		{
			Event event = interestingEvents.first();
			SpriteTypeFeatures features = fbtpGameKnowledge.getSpriteTypeFeaturesByType(event.passiveTypeId);

			if (tpGameMechanicsController.localizeSprite(soiAfterState, event.passiveSpriteId, event.position) == null)
			{
				if (event.fromAvatar)
					features.destroyable = true;

				if (!event.fromAvatar)
				{
					features.collectable = true;
					features.passable = true;
				}
			}
			else
			{
				if (event.fromAvatar)
					features.destroyable = false;

				if (!event.fromAvatar)
				{
					features.collectable = false;
					features.passable = true;
				}
			}
			if (soiAfterState.getAvatarPosition(playerID) == soiBaseState.getAvatarPosition(playerID))
			{
				features.passable = false;
			}

			features.givingVictory = soiBaseState.getMultiGameWinner()[playerID] == Types.WINNER.NO_WINNER
					&& soiAfterState.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS;

			features.givingDefeat = soiBaseState.getMultiGameWinner()[playerID] == Types.WINNER.NO_WINNER
					&& soiAfterState.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES;

			features.changingPoints = soiAfterState.getGameScore(playerID) - soiBaseState.getGameScore(playerID);
			
			//System.out.println("features.changingPoints");
			//System.out.println(features.changingPoints);

			features.dangerousToAvatar = soiAfterState.getAvatarHealthPoints(playerID)
					- soiBaseState.getAvatarHealthPoints(playerID);

			// features.dangerousOtherwise;
			// features.allowingVictory;
			// features.changingValuesOfOtherObjects;
			fbtpGameKnowledge.setSpriteTypeFeaturesByType(event.passiveTypeId, features);
			
			if (!soiAfterState.isGameOver())
				putAllSpritesInQueue(soiAfterState, event.passiveTypeId);
		}
	}

	private void putAllSpritesInQueue(StateObservationMulti state, int typeId)
	{
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		TPGameMechanicsController tpGameMechanicsController = (TPGameMechanicsController) this.gameMechanicsController;
		
		ArrayList<Observation> obsList = ((TPGameMechanicsController)this.gameMechanicsController).getListOfSpritesRepresentants(state, state.getAvatarPosition(playerID));
		for (Observation obs : obsList)
		{
			/*System.out.println(obs.category);
			System.out.println(obs.obsID);
			System.out.println(obs.position);*/
			boolean newObjectEncountered = false;
			if (!typeIdsEncountered.contains(obs.itype))
			{
				newObjectEncountered = true;
				typeIdsEncountered.add(obs.itype);
				fbtpGameKnowledge.setSpriteTypeFeaturesByType(obs.itype,
						getSpriteTypeFeaturesForCategory(obs.category, obs.itype));
			}
			if (!tpGameMechanicsController.isSpriteWall(fbtpGameKnowledge.getSpriteTypeFeaturesByType(obs.itype))
					|| newObjectEncountered)
			{
				TPSituationOfInterest soi = new TPSituationOfInterest();
				soi.importance = importance(obs.category);
				if (newObjectEncountered)
					soi.importance += 10;	// investigate it first
				soi.inception = (typeId != -1);
				if (!soi.inception)
					soi.importance = soi.importance + 20;
				soi.activatingTypeId = typeId;
				soi.baseState = state;
				soi.afterState = null;
				soi.poi = new PointOfInterest(POITYPE.SPRITE, obs);
				soi.poi.track = false;
				addSOI(soi);
			}
		}
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
			case 0:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						true, true, 1, 0, false);
			
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

	private double importance(int category)
	{
		switch (category)
		{
			case 0:
				return 6;
			case 1:
				return 5;
			case 2:
				return 7;
			case 3:
				return 2;
			case 4:
				return 1;
			case 5:
				return 3;
			case 6:
				return 4;
		}
		return 0;
	}

	private void approachAndInvestigate(TPSituationOfInterest soi, long timeLimit)
	{
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;
		
		Pair<StateObservationMulti, ArrayList<Types.ACTIONS>> testingSite = fbtpAgentMoveController.approachSprite
				((StateObservationMulti) (soi.baseState), gameKnowledge.getPlayerID(), soi.poi.observation, 0, timeLimit);
		
		//System.out.println(testingSite);
		if (testingSite != null)
		{
			StateObservationMulti baseState = testingSite.first();
	
			Types.ACTIONS actions[] = { Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL };
			actions[fbtpGameKnowledge.getOppID()] = fbtpAgentMoveController.getNonDyingAction(baseState, playerID);
			StateObservationMulti afterState;
			TPSituationOfInterest testedSoi = new TPSituationOfInterest();
			testedSoi.inception = soi.inception;
			testedSoi.baseState = baseState;
	
			// try to move on sprite
			// System.out.println("move");
			actions[fbtpGameKnowledge.getPlayerID()] = testingSite.second().get(testingSite.second().size() - 1);
			afterState = baseState.copy();
			afterState.advance(actions);
			testedSoi.afterState = afterState;
			investigateEvent(testedSoi);
	
			// try to use sprite
			// System.out.println("use");
			actions[fbtpGameKnowledge.getPlayerID()] = Types.ACTIONS.ACTION_USE;
			afterState = baseState.copy();
			afterState.advance(actions);
			testedSoi.afterState = afterState;
			investigateEvent(testedSoi);
		}
	}

	public void addSOI(TPSituationOfInterest soi)
	{
		situationsToCheck.add(soi);
	}

	private boolean checkWhetherShootingIsAllowed(StateObservationMulti stateObs)
	{
		ArrayList<Observation>[] initialFromAvatarSprites = stateObs.getFromAvatarSpritesPositions();
		ArrayList<Integer> initialFromAvatarSpritesId = new ArrayList<Integer>();
		if (initialFromAvatarSprites != null)
		{
			for (ArrayList<Observation> spriteArray : initialFromAvatarSprites)
				for (Observation sprite : spriteArray)
					initialFromAvatarSpritesId.add(sprite.obsID);
		}

		Types.ACTIONS actions[] = { Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL };
		actions[this.playerID] = Types.ACTIONS.ACTION_USE;
		stateObs.advance(actions);

		ArrayList<Observation>[] laterFromAvatarSprites = stateObs.getFromAvatarSpritesPositions();
		HashMap<Integer, Observation> newSpriteObservationMap = new HashMap<Integer, Observation>();
		if (laterFromAvatarSprites != null)
		{
			for (ArrayList<Observation> spriteArray : laterFromAvatarSprites)
				for (Observation sprite : spriteArray)
					if (initialFromAvatarSpritesId == null)
						newSpriteObservationMap.put(sprite.obsID, sprite);
					else if (!initialFromAvatarSpritesId.contains(sprite.obsID))
					{
						newSpriteObservationMap.put(sprite.obsID, sprite);
					}
		}
		else
		{
			return false;
		}

		stateObs.advance(new Types.ACTIONS[] { Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL });

		ArrayList<Observation>[] finalFromAvatarSprites = stateObs.getFromAvatarSpritesPositions();
		if (finalFromAvatarSprites != null)
		{
			for (ArrayList<Observation> spriteArray : finalFromAvatarSprites)
				for (Observation sprite : spriteArray)
					if (newSpriteObservationMap.containsKey(sprite.obsID))
						if (newSpriteObservationMap.get(sprite.obsID).position != sprite.position)
							return true;
		}

		return false;
	}

	private Comparator<TPSituationOfInterest> comp = new Comparator<TPSituationOfInterest>()
	{
		public int compare(TPSituationOfInterest a, TPSituationOfInterest b)
		{
			if (a.importance < b.importance)
				return 1;
			if (a.importance == b.importance)
				return 0;
			return -1;
		}
	};
}
