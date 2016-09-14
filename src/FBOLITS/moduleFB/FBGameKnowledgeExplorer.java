package FBOLITS.moduleFB;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.TreeSet;

import FBOLITS.GameKnowledgeExplorer;
import FBOLITS.PointOfInterest;
import FBOLITS.PointOfInterest.POITYPE;
import FBOLITS.SituationOfInterest;
import FBOLITS.mechanicsController.GameMechanicsController;
import FBOLITS.moduleFB.SpriteTypeFeatures;
import FBOLITS.moduleFB.MechanicsController.FBAgentMoveController;
import FBOLITS.moduleFB.MechanicsController.FBGameMechanicsController;
import FBOLITS.utils.Pair;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;

public class FBGameKnowledgeExplorer extends GameKnowledgeExplorer
{
	// Real field types
	// protected FBGameKnowledge gameKnowledge;
	// protected FBGameMechanicsController gameMechanicsController;
	// protected FBAgentMoveController agentMoveController;
	// protected FBGameStateTracker gameStateTracker;

	// protected ElapsedCpuTimer elapsedTimer;

	// protected PriorityQueue<SituationOfInterest> basicSituationsToCheck;
	protected PriorityQueue<SituationOfInterest> situationsToCheck;
	private ArrayList<Integer> typeIdsEncountered;

	public FBGameKnowledgeExplorer()
	{
		this.situationsToCheck = new PriorityQueue<SituationOfInterest>(1, comp);
		this.typeIdsEncountered = new ArrayList<Integer>();
	}

	public FBGameKnowledgeExplorer(FBGameKnowledge gameKnowledge, GameMechanicsController gameMechanicsController, 
			FBAgentMoveController agentMoveController, FBGameStateTracker gameStateTracker)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		this.agentMoveController = agentMoveController;
		this.gameStateTracker = gameStateTracker;
		this.situationsToCheck = new PriorityQueue<SituationOfInterest>(1, comp);
		this.typeIdsEncountered = new ArrayList<Integer>();

		this.elapsedTimer = new ElapsedCpuTimer();
	}

	@Override
	public void initialLearn(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int timeForLearning)
	{
		FBGameKnowledge fbGameKnowledge = (FBGameKnowledge) this.gameKnowledge;

		long startTime = elapsedTimer.remainingTimeMillis();

		fbGameKnowledge.setAvatarSpriteId(gameMechanicsController.getPlayerId(stateObs));
		fbGameKnowledge.setShootingAllowed(checkWhetherShootingIsAllowed(stateObs));
		fbGameKnowledge.setDeterministicGame(checkDeterminism(stateObs));
		fbGameKnowledge.setOpenMap(checkIfMapIsOpen(stateObs));
		timeForLearning -= startTime - elapsedTimer.remainingTimeMillis();

		successiveLearn(stateObs, elapsedTimer, timeForLearning);
	}

	@Override
	public void successiveLearn(StateObservation stateObs, ElapsedCpuTimer elapsedTimer, int timeForLearning)
	{
		if (gameKnowledge.isDeterministicGame())
			gameKnowledge.setDeterministicGame(checkDeterminism(stateObs));

		Collection<SpriteTypeFeatures> spriteTypeFeaturesCollection = ((FBGameKnowledge) gameKnowledge)
				.getSpriteTypeFeaturesMap().values();

		for (SpriteTypeFeatures features : spriteTypeFeaturesCollection)
			features.featuresUpdatedThisTurn = false;

		putAllSpritesInQueue(stateObs, -1, importantEventOccured(stateObs));

		ArrayList<Integer> typesAnalysed = new ArrayList<Integer>();
		long startTime = elapsedTimer.remainingTimeMillis();
		long loopStartTime = 0;
		long loopEndTime = 0;

		while (startTime - elapsedTimer.remainingTimeMillis() < timeForLearning - (loopStartTime - loopEndTime)
				&& !situationsToCheck.isEmpty())
		{
			loopStartTime = elapsedTimer.remainingTimeMillis();

			SituationOfInterest soi = situationsToCheck.peek();

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
		FBGameMechanicsController fbGameMechanicsController = (FBGameMechanicsController) gameMechanicsController; 
		FBGameKnowledge fbGameKnowledge = (FBGameKnowledge) gameKnowledge;
		TreeSet<Event> events = stateObs.getEventsHistory();
		while (!events.isEmpty())
		{
			Event event = events.pollLast();

			if (event.gameStep != stateObs.getGameTick() - 1)
				return false;

			if (!fbGameKnowledge.getSpriteTypes().contains(event.passiveTypeId))
				return true;

			if (!fbGameMechanicsController
					.isSpriteWall(fbGameKnowledge.getSpriteTypeFeaturesByType(event.passiveTypeId)))
			{
				return true;
			}
		}
		return false;
	}

	private void investigateEvent(SituationOfInterest soi)
	{
		FBGameKnowledge fbGameKnowledge = (FBGameKnowledge) this.gameKnowledge;
		FBGameMechanicsController fbGameMechanicsController = (FBGameMechanicsController) this.gameMechanicsController;

		TreeSet<Event> allEvents = fbGameMechanicsController.getEventsDuringSOI(soi);
		TreeSet<Event> interestingEvents = new TreeSet<Event>();
		TreeSet<Event> maybeInterestingEvents = new TreeSet<Event>();
		for (Event event : allEvents)
		{
			maybeInterestingEvents.add(event);
			if (!fbGameMechanicsController
					.isSpriteDoingNothing(fbGameKnowledge.getSpriteTypeFeaturesByType(event.passiveTypeId)))
			{
				interestingEvents.add(event);
			}
		}
		if (interestingEvents.size() == 0 && maybeInterestingEvents.size() == 1)
			interestingEvents = maybeInterestingEvents;

		if (interestingEvents.size() == 1)
		{
			Event event = interestingEvents.first();

			SpriteTypeFeatures features = fbGameKnowledge.getSpriteTypeFeaturesByType(event.passiveTypeId);

			if (!soi.inception)
			{
				if (fbGameMechanicsController.localizeSprite(soi.afterState, event.passiveSpriteId,
						event.position) == null)
				{
					if (event.fromAvatar)
						features.destroyable = true;
					else if (soi.afterState.isAvatarAlive())
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
					if (soi.afterState.getAvatarPosition().equals(soi.baseState.getAvatarPosition())
							|| !soi.afterState.isAvatarAlive())
						features.passable = false;
					else
						features.passable = true;
				}
				if (event.fromAvatar)
				{
					if (!features.givingVictoryUse)
						features.givingVictoryUse = soi.baseState
								.getGameWinner() == Types.WINNER.NO_WINNER
								&& soi.afterState.getGameWinner() == Types.WINNER.PLAYER_WINS;

					if (!features.givingDefeatUse)
						features.givingDefeatUse = soi.baseState.getGameWinner() == Types.WINNER.NO_WINNER
								&& soi.afterState.getGameWinner() == Types.WINNER.PLAYER_LOSES;

					double changeInPoints = soi.afterState.getGameScore() - soi.baseState.getGameScore();
					features.changingPointsUse = changeInPoints;
				}
				else
				{
					if (!features.givingVictoryMove)
						features.givingVictoryMove = soi.baseState
								.getGameWinner() == Types.WINNER.NO_WINNER
								&& soi.afterState.getGameWinner() == Types.WINNER.PLAYER_WINS;

					if (!features.givingDefeatMove)
						features.givingDefeatMove = soi.baseState
								.getGameWinner() == Types.WINNER.NO_WINNER
								&& soi.afterState.getGameWinner() == Types.WINNER.PLAYER_LOSES;

					double changeInPoints = soi.afterState.getGameScore() - soi.baseState.getGameScore();
					features.changingPointsMove = changeInPoints;

					double dangerousToAvatar = soi.baseState.getAvatarHealthPoints()
							- soi.afterState.getAvatarHealthPoints();

					if (features.dangerousToAvatar > dangerousToAvatar)
						features.dangerousToAvatar = dangerousToAvatar;
				}

				fbGameKnowledge.setSpriteTypeFeaturesByType(event.passiveTypeId, features);

				if (!soi.afterState.isGameOver()
						&& !(fbGameMechanicsController.isSpriteWall(features)))
				{
					putAllSpritesInQueue(soi.afterState, event.passiveTypeId, false);
				}
			}
			if (soi.inception)
			{
				SpriteTypeFeatures activatingSpriteFeatures = fbGameKnowledge
						.getSpriteTypeFeaturesByType(soi.activatingTypeId);
				SpriteTypeFeatures passiveSpriteFeatures = fbGameKnowledge
						.getSpriteTypeFeaturesByType(event.passiveTypeId);

				if (passiveSpriteFeatures == null)
				{
					Observation obs = fbGameMechanicsController
							.getSpriteTypeRepresentative(event.passiveTypeId, soi.baseState);
					fbGameKnowledge.setSpriteTypeFeaturesByType(obs.itype,
							getSpriteTypeFeaturesForCategory(obs.category, obs.itype));
				}

				if (event.fromAvatar)
				{
					if (!passiveSpriteFeatures.givingVictoryUse && !activatingSpriteFeatures.allowingVictory)
					{
						activatingSpriteFeatures.allowingVictory = (soi.baseState
								.getGameWinner() == Types.WINNER.NO_WINNER
								&& soi.afterState.getGameWinner() == Types.WINNER.PLAYER_WINS);
					}

					if (!passiveSpriteFeatures.givingDefeatUse && !activatingSpriteFeatures.dangerousOtherwise)
					{
						activatingSpriteFeatures.dangerousOtherwise = (soi.baseState
								.getGameWinner() == Types.WINNER.NO_WINNER
								&& soi.afterState.getGameWinner() == Types.WINNER.PLAYER_LOSES);
					}

					activatingSpriteFeatures.changingValuesOfOtherObjects = Math.max(
							(soi.afterState.getGameScore() - soi.baseState.getGameScore())
									- passiveSpriteFeatures.changingPointsUse,
							activatingSpriteFeatures.changingValuesOfOtherObjects);
				}
				else
				{
					if (!passiveSpriteFeatures.givingVictoryMove && !activatingSpriteFeatures.allowingVictory)
					{
						activatingSpriteFeatures.allowingVictory = (soi.baseState
								.getGameWinner() == Types.WINNER.NO_WINNER
								&& soi.afterState.getGameWinner() == Types.WINNER.PLAYER_WINS);
					}

					if (!passiveSpriteFeatures.givingDefeatMove && !activatingSpriteFeatures.dangerousOtherwise)
					{
						activatingSpriteFeatures.dangerousOtherwise = (soi.baseState
								.getGameWinner() == Types.WINNER.NO_WINNER
								&& soi.afterState.getGameWinner() == Types.WINNER.PLAYER_LOSES);
					}

					activatingSpriteFeatures.changingValuesOfOtherObjects = Math.max(
							(soi.afterState.getGameScore() - soi.baseState.getGameScore())
									- passiveSpriteFeatures.changingPointsMove,
							activatingSpriteFeatures.changingValuesOfOtherObjects);
				}

				if (passiveSpriteFeatures.dangerousToAvatar >= 0 && !activatingSpriteFeatures.dangerousOtherwise)
				{
					activatingSpriteFeatures.dangerousOtherwise = (soi.afterState
							.getAvatarHealthPoints() < soi.baseState.getAvatarHealthPoints());
				}

				fbGameKnowledge.setSpriteTypeFeaturesByType(soi.activatingTypeId, activatingSpriteFeatures);
			}
		}
	}

	private void putAllSpritesInQueue(StateObservation state, int typeId, boolean newObjectsOnly)
	{
		FBGameKnowledge fbGameKnowledge = (FBGameKnowledge) this.gameKnowledge;
		FBGameMechanicsController fbGameMechanicsController = (FBGameMechanicsController) this.gameMechanicsController;

		ArrayList<Observation> obsList = fbGameMechanicsController
				.getListOfAllTypesRepresentatives(state, state.getAvatarPosition(), false);
		for (Observation obs : obsList)
		{
			boolean newObjectEncountered = false;
			if (!typeIdsEncountered.contains(obs.itype))
			{
				newObjectEncountered = true;
				typeIdsEncountered.add(obs.itype);
				fbGameKnowledge.setSpriteTypeFeaturesByType(obs.itype,
						getSpriteTypeFeaturesForCategory(obs.category, obs.itype));
			}
			if (newObjectEncountered || (!newObjectsOnly && !fbGameMechanicsController
					.isSpriteWall(fbGameKnowledge.getSpriteTypeFeaturesByType(obs.itype))))
			{
				SituationOfInterest soi = new SituationOfInterest();
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

	private void approachAndInvestigate(SituationOfInterest soi, long timeLimit)
	{
		FBAgentMoveController fbAgentMoveController = (FBAgentMoveController) this.agentMoveController;

		Pair<StateObservation, ArrayList<Types.ACTIONS>> testingSite = fbAgentMoveController.approachSprite(
				soi.baseState, soi.poi.observation, 0, timeLimit);

		if (testingSite != null)
		{
			StateObservation baseState = testingSite.first();

			Types.ACTIONS oppAction = fbAgentMoveController.getNonDyingAction(baseState);
			if (oppAction == null)
				oppAction = Types.ACTIONS.ACTION_NIL;

			Types.ACTIONS playerMove = testingSite.second().get(testingSite.second().size() - 1);
			ArrayList<Types.ACTIONS> availableActions = baseState.getAvailableActions();

			// try to move on sprite
			if (availableActions.contains(playerMove))
				testAction(baseState.copy(), soi, playerMove, oppAction);

			// try to use sprite
			if (availableActions.contains(Types.ACTIONS.ACTION_USE))
				testAction(baseState.copy(), soi, Types.ACTIONS.ACTION_USE, oppAction);
		}
	}

	private void testAction(StateObservation baseState, SituationOfInterest soi, Types.ACTIONS playerAction,
			Types.ACTIONS oppAction)
	{
		SituationOfInterest testedSoi = new SituationOfInterest();
		testedSoi.inception = soi.inception;
		testedSoi.activatingTypeId = soi.activatingTypeId;
		testedSoi.poi = soi.poi;
		testedSoi.baseState = baseState.copy();
		StateObservation afterState = baseState.copy();
		afterState.advance(playerAction);
		testedSoi.afterState = afterState;
		investigateEvent(testedSoi);
	}

	public void addSOI(SituationOfInterest soi)
	{
		situationsToCheck.add(soi);
	}
	
	private Comparator<SituationOfInterest> comp = new Comparator<SituationOfInterest>()
	{
		public int compare(SituationOfInterest a, SituationOfInterest b)
		{
			if (a.importance < b.importance)
				return 1;
			if (a.importance == b.importance)
				return 0;
			return -1;
		}
	};
}
