package NextLevel.moduleFB.moduleFBTP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.TreeSet;

import NextLevel.PointOfInterest;
import NextLevel.PointOfInterest.POITYPE;
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
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

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

	// protected PriorityQueue<TPSituationOfInterest> basicSituationsToCheck;
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

	public void learnBasics(StateObservation stateObs, int playerID)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		this.stateObs = stateObsMulti;
		this.playerID = playerID;
		this.oppID = 1 - playerID;
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
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;

		long startTime = elapsedTimer.remainingTimeMillis();
		fbtpGameKnowledge.setAvatarSpriteId(
				((TPGameMechanicsController) gameMechanicsController).getPlayerId(playerID, stateObsMulti));

		fbtpGameKnowledge.setShootingAllowed(checkWhetherShootingIsAllowed(stateObsMulti));
		fbtpGameKnowledge.setDeterministicGame(checkDeterminism(stateObsMulti, true));
		fbtpGameKnowledge.setOpenMap(true);
		timeForLearning -= startTime - elapsedTimer.remainingTimeMillis();

		successiveLearn(stateObs, elapsedTimer, timeForLearning);
	}

	public void successiveLearn(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int timeForLearning)
	{
		if (((FBTPGameKnowledge) gameKnowledge).isDeterministicGame())
			((FBTPGameKnowledge) gameKnowledge)
					.setDeterministicGame(checkDeterminism((StateObservationMulti) stateObs, true));

		Collection<SpriteTypeFeatures> spriteTypeFeaturesCollection = ((FBTPGameKnowledge) gameKnowledge)
				.getSpriteTypeFeaturesMap().values();

		for (SpriteTypeFeatures features : spriteTypeFeaturesCollection)
			features.featuresUpdatedThisTurn = false;

		putAllSpritesInQueue((StateObservationMulti) stateObs, -1, importantEventOccured(stateObs));

		ArrayList<Integer> typesAnalysed = new ArrayList<Integer>();
		long startTime = elapsedTimer.remainingTimeMillis();
		long loopStartTime = 0;
		long loopEndTime = 0;

		while (startTime - elapsedTimer.remainingTimeMillis() < timeForLearning - (loopStartTime - loopEndTime)
				&& !situationsToCheck.isEmpty())
		{
			loopStartTime = elapsedTimer.remainingTimeMillis();

			TPSituationOfInterest soi = situationsToCheck.peek();

			situationsToCheck.poll();

			if (!typesAnalysed.contains(soi.poi.observation.itype) || soi.inception)
			{
				if (!soi.inception)
					typesAnalysed.add(soi.poi.observation.itype);

				if (soi.afterState == null)
					approachAndInvestigate(soi, loopStartTime + timeForLearning - startTime);
				else
					investigateEvent(soi);
			}

			loopEndTime = elapsedTimer.remainingTimeMillis();
		}

		if (situationsToCheck.size() > 20)
			situationsToCheck.clear(); // reseting queue
	}

	private boolean importantEventOccured(StateObservation stateObs)
	{
		TreeSet<Event> events = stateObs.getEventsHistory();
		while (!events.isEmpty())
		{
			Event event = events.pollLast();

			if (event.gameStep != stateObs.getGameTick() - 1)
				return false;

			if (!((FBTPGameKnowledge) gameKnowledge).getSpriteTypes().contains(event.passiveTypeId))
				return true;

			if (!((TPGameMechanicsController) gameMechanicsController)
					.isSpriteWall(((FBTPGameKnowledge) gameKnowledge).getSpriteTypeFeaturesByType(event.passiveTypeId)))
			{
				return true;
			}
		}
		return false;
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
		if (interestingEvents.size() == 0 && maybeInterestingEvents.size() == 1)
			interestingEvents = maybeInterestingEvents;

		if (interestingEvents.size() == 1)
		{
			Event event = interestingEvents.first();

			if (event.activeSpriteId == ((TPGameMechanicsController) gameMechanicsController)
					.getPlayerObservation(((FBTPGameKnowledge) gameKnowledge).getOppID(), soiBaseState).obsID/*
																												 * &&
																												 * ((TPGameMechanicsController)gameMechanicsController).getSpriteCategoryFromState(
																												 * event.passiveTypeId, soiBaseState)==5
																												 */) // opponent get hit by fromAvatarSprite
			{
				int temp = event.passiveSpriteId;
				event.passiveSpriteId = event.activeSpriteId;
				event.activeSpriteId = temp;
				temp = event.passiveTypeId;
				event.passiveTypeId = event.activeTypeId;
				event.activeTypeId = temp;
				event.fromAvatar = true;
			}

			SpriteTypeFeatures features = fbtpGameKnowledge.getSpriteTypeFeaturesByType(event.passiveTypeId);

			if (!soi.inception)
			{
				if (tpGameMechanicsController.localizeSprite(soiAfterState, event.passiveSpriteId,
						event.position) == null)
				{
					if (event.fromAvatar)
						features.destroyable = true;
					else if (soiAfterState.isAvatarAlive(playerID))
						features.collectable = true;
				}
				else
				{
					if (event.fromAvatar)
						features.destroyable = false;
					else
						features.collectable = false;
				}
				if (!event.fromAvatar)
				{
					if (soiAfterState.getAvatarPosition(playerID).equals(soiBaseState.getAvatarPosition(playerID))
							|| !soiAfterState.isAvatarAlive(playerID))
						features.passable = false;
					else
						features.passable = true;
				}
				if (event.fromAvatar)
				{
					if (!features.givingVictoryUse)
						features.givingVictoryUse = soiBaseState
								.getMultiGameWinner()[playerID] == Types.WINNER.NO_WINNER
								&& soiAfterState.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS;

					if (!features.givingDefeatUse)
						features.givingDefeatUse = soiBaseState.getMultiGameWinner()[playerID] == Types.WINNER.NO_WINNER
								&& soiAfterState.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES;

					double changeInPoints = soiAfterState.getGameScore(playerID) - soiBaseState.getGameScore(playerID);
					features.changingPointsUse = changeInPoints;
				}
				else
				{
					if (!features.givingVictoryMove)
						features.givingVictoryMove = soiBaseState
								.getMultiGameWinner()[playerID] == Types.WINNER.NO_WINNER
								&& soiAfterState.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS;

					if (!features.givingDefeatMove)
						features.givingDefeatMove = soiBaseState
								.getMultiGameWinner()[playerID] == Types.WINNER.NO_WINNER
								&& soiAfterState.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES;

					double changeInPoints = soiAfterState.getGameScore(playerID) - soiBaseState.getGameScore(playerID);
					features.changingPointsMove = changeInPoints;

					double dangerousToAvatar = soiBaseState.getAvatarHealthPoints(playerID)
							- soiAfterState.getAvatarHealthPoints(playerID);

					if (features.dangerousToAvatar > dangerousToAvatar)
						features.dangerousToAvatar = dangerousToAvatar;
				}

				fbtpGameKnowledge.setSpriteTypeFeaturesByType(event.passiveTypeId, features);

				if (!soiAfterState.isGameOver()
						&& !(((TPGameMechanicsController) gameMechanicsController).isSpriteWall(features)))
				{
					putAllSpritesInQueue(soiAfterState, event.passiveTypeId, false);
				}
			}
			if (soi.inception)
			{
				SpriteTypeFeatures activatingSpriteFeatures = fbtpGameKnowledge
						.getSpriteTypeFeaturesByType(soi.activatingTypeId);
				SpriteTypeFeatures passiveSpriteFeatures = fbtpGameKnowledge
						.getSpriteTypeFeaturesByType(event.passiveTypeId);

				if (passiveSpriteFeatures == null)
				{
					Observation obs = ((TPGameMechanicsController) gameMechanicsController)
							.getSpriteRepresentant(event.passiveTypeId, soiBaseState);
					fbtpGameKnowledge.setSpriteTypeFeaturesByType(obs.itype,
							getSpriteTypeFeaturesForCategory(obs.category, obs.itype));
				}

				if (event.fromAvatar)
				{
					if (!passiveSpriteFeatures.givingVictoryUse && !activatingSpriteFeatures.allowingVictory)
					{
						activatingSpriteFeatures.allowingVictory = (soiBaseState
								.getMultiGameWinner()[playerID] == Types.WINNER.NO_WINNER
								&& soiAfterState.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS);
					}

					if (!passiveSpriteFeatures.givingDefeatUse && !activatingSpriteFeatures.dangerousOtherwise)
					{
						activatingSpriteFeatures.dangerousOtherwise = (soiBaseState
								.getMultiGameWinner()[playerID] == Types.WINNER.NO_WINNER
								&& soiAfterState.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES);
					}

					activatingSpriteFeatures.changingValuesOfOtherObjects = Math.max(
							(soiAfterState.getGameScore(playerID) - soiBaseState.getGameScore(playerID))
									- passiveSpriteFeatures.changingPointsUse,
							activatingSpriteFeatures.changingValuesOfOtherObjects);
				}
				else
				{
					if (!passiveSpriteFeatures.givingVictoryMove && !activatingSpriteFeatures.allowingVictory)
					{
						activatingSpriteFeatures.allowingVictory = (soiBaseState
								.getMultiGameWinner()[playerID] == Types.WINNER.NO_WINNER
								&& soiAfterState.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS);
					}

					if (!passiveSpriteFeatures.givingDefeatMove && !activatingSpriteFeatures.dangerousOtherwise)
					{
						activatingSpriteFeatures.dangerousOtherwise = (soiBaseState
								.getMultiGameWinner()[playerID] == Types.WINNER.NO_WINNER
								&& soiAfterState.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES);
					}

					activatingSpriteFeatures.changingValuesOfOtherObjects = Math.max(
							(soiAfterState.getGameScore(playerID) - soiBaseState.getGameScore(playerID))
									- passiveSpriteFeatures.changingPointsMove,
							activatingSpriteFeatures.changingValuesOfOtherObjects);
				}

				if (passiveSpriteFeatures.dangerousToAvatar >= 0 && !activatingSpriteFeatures.dangerousOtherwise)
				{
					activatingSpriteFeatures.dangerousOtherwise = (soiAfterState
							.getAvatarHealthPoints(playerID) < soiBaseState.getAvatarHealthPoints(playerID));
				}

				fbtpGameKnowledge.setSpriteTypeFeaturesByType(soi.activatingTypeId, activatingSpriteFeatures);
			}
		}
	}

	private void putAllSpritesInQueue(StateObservationMulti state, int typeId, boolean newObjectsOnly)
	{
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		TPGameMechanicsController tpGameMechanicsController = (TPGameMechanicsController) this.gameMechanicsController;

		ArrayList<Observation> obsList = ((TPGameMechanicsController) this.gameMechanicsController)
				.getListOfSpritesRepresentants(state, state.getAvatarPosition(playerID));
		for (Observation obs : obsList)
		{
			boolean newObjectEncountered = false;
			if (!typeIdsEncountered.contains(obs.itype))
			{
				newObjectEncountered = true;
				typeIdsEncountered.add(obs.itype);
				fbtpGameKnowledge.setSpriteTypeFeaturesByType(obs.itype,
						getSpriteTypeFeaturesForCategory(obs.category, obs.itype));
			}
			if (newObjectEncountered || (!newObjectsOnly && !tpGameMechanicsController
					.isSpriteWall(fbtpGameKnowledge.getSpriteTypeFeaturesByType(obs.itype))))
			{
				TPSituationOfInterest soi = new TPSituationOfInterest();
				soi.importance = importance(obs.category);
				if (newObjectEncountered)
					soi.importance += 10; // investigate it first
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
						false, false, 0, true, true, 1, 0, false);

			case 1:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, true, false, false, 0,
						false, false, 0, true, false, 0, 0, false);
				break;
			case 2:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, false, 0, true, false, 0, 1, true);
				break;
			case 3:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, false, 0, false, true, 1, 0, false);
				break;
			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, false, 0, false, false, 0, 0, false);
				break;
			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, false, 0, false, true, 1, 0, false);
				break;
			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, false, 0, false, true, 0, 0, false);
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
		FBTPAgentMoveController fbtpAgentMoveController = (FBTPAgentMoveController) this.agentMoveController;

		Pair<StateObservationMulti, ArrayList<Types.ACTIONS>> testingSite = fbtpAgentMoveController.approachSprite(
				(StateObservationMulti) (soi.baseState), this.playerID, soi.poi.observation, 0, timeLimit);

		if (testingSite != null)
		{
			StateObservationMulti baseState = testingSite.first();

			Types.ACTIONS oppAction = fbtpAgentMoveController.getNonDyingAction(baseState, playerID);
			if (oppAction == null)
				oppAction = Types.ACTIONS.ACTION_NIL;

			Types.ACTIONS playerMove = testingSite.second().get(testingSite.second().size() - 1);
			ArrayList<ACTIONS> availableActions = baseState.getAvailableActions(playerID);

			// try to move on sprite
			if (availableActions.contains(playerMove))
				testAction(baseState.copy(), soi, playerMove, oppAction);

			// try to use sprite
			if (availableActions.contains(Types.ACTIONS.ACTION_USE))
				testAction(baseState.copy(), soi, Types.ACTIONS.ACTION_USE, oppAction);
		}
	}

	private void testAction(StateObservationMulti baseState, TPSituationOfInterest soi, ACTIONS playerAction,
			ACTIONS oppAction)
	{
		TPSituationOfInterest testedSoi = new TPSituationOfInterest();
		testedSoi.inception = soi.inception;
		testedSoi.activatingTypeId = soi.activatingTypeId;
		testedSoi.poi = soi.poi;
		testedSoi.baseState = baseState.copy();
		Types.ACTIONS actions[] = new Types.ACTIONS[2];
		actions[this.playerID] = playerAction;
		actions[this.oppID] = oppAction;
		StateObservationMulti afterState = baseState.copy();
		afterState.advance(actions);
		testedSoi.afterState = afterState;
		investigateEvent(testedSoi);
	}

	public void addSOI(TPSituationOfInterest soi)
	{
		situationsToCheck.add(soi);
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

	@SafeVarargs
	private final ArrayList<ArrayList<Integer>> observationListToID(StateObservationMulti stateObs,
			ArrayList<Observation>[]... arrays)
	{
		int blockSize = stateObs.getBlockSize();
		int worldXDimension = stateObs.getWorldDimension().width / blockSize;
		ArrayList<ArrayList<Integer>> tab = new ArrayList<ArrayList<Integer>>();
		for (ArrayList<Observation>[] array : arrays)
		{
			if (array == null)
				continue;
			for (int i = 0; i < array.length; i++)
			{
				ArrayList<Integer> intTab = new ArrayList<Integer>();
				for (Observation obs : array[i])
				{
					Pair<Integer, Integer> position = new Pair<Integer, Integer>((int) obs.position.x / blockSize,
							(int) obs.position.y / blockSize);
					int current = worldXDimension * position.second() + position.first();
					intTab.add(current);
				}
				tab.add(intTab);
			}
		}
		return tab;
	}

	/**
	 * Checks whether game is deterministic.
	 * 
	 * @param stateObs
	 *            Observation of the current state of the game.
	 * @param fastCheck
	 *            If set true, function evaluates the state quickly as for real-time answer.
	 *            Otherwise function focuses on deep analysis.
	 */
	public boolean checkDeterminism(StateObservationMulti stateObs, boolean fastCheck)
	{
		int reps = 15;
		int depth = 10;
		int offset = 5;

		int fastReps = 2;
		int fastDepth = 2;
		int fastOffset = 1;

		if (fastCheck)
		{
			reps = fastReps;
			depth = fastDepth;
			offset = fastOffset;
		}

		@SuppressWarnings("unchecked")
		ArrayList<ArrayList<ArrayList<Integer>>>[] positions = new ArrayList[reps];
		for (int r = 0; r < reps; r++)
		{
			StateObservationMulti stateObsCopy = stateObs.copy();
			positions[r] = new ArrayList<ArrayList<ArrayList<Integer>>>();
			for (int d = 1; d <= depth; d++)
			{
				stateObsCopy.advance(new Types.ACTIONS[] { Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL });
				if (d % offset == 0)
				{
					positions[r].add(observationListToID(stateObsCopy, stateObsCopy.getNPCPositions(),
							stateObsCopy.getMovablePositions()));
				}
			}
		}

		for (int r = 0; r < reps - 1; r++)
		{
			for (int d = 0; d < depth / offset; d++)
			{
				if (positions[r].get(d).size() != positions[r + 1].get(d).size())
					return false;
				for (int i = 0; i < positions[r].get(d).size(); i++)
				{
					if (positions[r].get(d).get(i).size() != positions[r + 1].get(d).get(i).size())
						return false;
					for (int j = 0; j < positions[r].get(d).get(i).size(); j++)
						if (positions[r].get(d).get(i).get(j) != positions[r + 1].get(d).get(i).get(j))
							return false;
				}
			}
		}

		return true;
	}

	private boolean checkWhetherShootingIsAllowed(StateObservationMulti stateObs)
	{
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		ArrayList<Types.ACTIONS> initialActions = stateObs.getAvailableActions(playerID);
		if (initialActions.contains(Types.ACTIONS.ACTION_NIL))
			initialActions.remove(Types.ACTIONS.ACTION_NIL);
		initialActions.add(0, Types.ACTIONS.ACTION_NIL);
		if (!initialActions.contains(Types.ACTIONS.ACTION_USE))
			return false;
		else
			initialActions.remove(Types.ACTIONS.ACTION_USE);

		if (!stateObs.getAvatarOrientation(playerID).equals(new Vector2d(0, 0))) // no orientation
			initialActions.remove(Types.ACTIONS.fromVector(stateObs.getAvatarOrientation(playerID)));

		for (Types.ACTIONS initialAction : initialActions)
		{
			StateObservationMulti stateObsCopy = stateObs.copy();

			Types.ACTIONS actions[] = { Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL };
			actions[this.playerID] = initialAction;
			stateObsCopy.advance(actions);

			ArrayList<Observation>[] initialFromAvatarSprites = stateObsCopy
					.getFromAvatarSpritesPositions(stateObs.getAvatarPosition(fbtpGameKnowledge.getPlayerID()));
			ArrayList<Integer> initialFromAvatarSpritesId = new ArrayList<Integer>();
			if (initialFromAvatarSprites != null)
			{
				for (ArrayList<Observation> spriteArray : initialFromAvatarSprites)
					for (Observation sprite : spriteArray)
						initialFromAvatarSpritesId.add(sprite.obsID);
			}

			Types.ACTIONS actions2[] = { Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL };
			actions2[this.playerID] = Types.ACTIONS.ACTION_USE;
			stateObsCopy.advance(actions2);

			ArrayList<Observation>[] laterFromAvatarSprites = stateObsCopy
					.getFromAvatarSpritesPositions(stateObs.getAvatarPosition(fbtpGameKnowledge.getPlayerID()));
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
				continue;
			}

			StateObservationMulti stateObsCopyCopy = stateObsCopy.copy();
			stateObsCopyCopy.advance(new Types.ACTIONS[] { Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL });

			ArrayList<Observation>[] finalFromAvatarSprites = stateObsCopyCopy
					.getFromAvatarSpritesPositions(stateObs.getAvatarPosition(fbtpGameKnowledge.getPlayerID()));
			if (finalFromAvatarSprites != null)
			{
				for (ArrayList<Observation> spriteArray : finalFromAvatarSprites)
					for (Observation sprite : spriteArray)
						if (newSpriteObservationMap.containsKey(sprite.obsID))
						{
							if (!newSpriteObservationMap.get(sprite.obsID).position.equals(sprite.position))
							{
								fbtpGameKnowledge.setFromAvatarSpriteType(sprite.itype);
								return true;
							}
						}
			}
		}

		return false;
	}
}
