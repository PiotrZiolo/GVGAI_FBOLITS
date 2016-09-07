package NextLevel.mechanicsController;

import java.util.ArrayList;
import java.util.TreeSet;

import NextLevel.moduleFB.SpriteTypeFeatures;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPSituationOfInterest;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.Vector2d;

public class TPGameMechanicsController extends GameMechanicsController
{
	// Real types of fields
	// protected TPGameKnowledge gameKnowledge;
	boolean treatFromAvatarAsSprite = false;

	public TPGameMechanicsController(TPGameKnowledge gameKnowledge)
	{
		this.gameKnowledge = gameKnowledge;
	}

	public TreeSet<Event> getEventsDuringSOI(TPSituationOfInterest soi)
	{
		TreeSet<Event> events = (TreeSet<Event>) soi.afterState.getEventsHistory().clone();
		events.removeAll(soi.baseState.getEventsHistory());
		return events;
	}

	public int getNumberOfResourcesGainedDuringSOI(TPSituationOfInterest soi, int playerID)
	{
		return ((StateObservationMulti) soi.afterState).getAvatarResources(playerID).size()
				- ((StateObservationMulti) soi.baseState).getAvatarResources(playerID).size();
	}

	public boolean isSpriteOneMoveFromAvatarWithOpponentRotation(Vector2d observationPosition, Vector2d avatarPosition,
			StateObservationMulti currentState, Vector2d avatarOrientation, int spriteType)
	{
		TPGameKnowledge gameKnowledge = (TPGameKnowledge) this.gameKnowledge;
		int playerID = gameKnowledge.getPlayerID();
		int oppID = gameKnowledge.getOppID();

		double speedInPixels = currentState.getBlockSize() * currentState.getAvatarSpeed(playerID);
		Vector2d distance = observationPosition.copy().subtract(avatarPosition);

		if ((Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_NIL
				|| Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_DOWN)
				&& Math.abs(distance.x) < currentState.getBlockSize()
				&& Math.abs(distance.y - speedInPixels) < currentState.getBlockSize())
		{
			if (Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_NIL
					&& spriteType == -1 - oppID
					&& Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_UP)
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[oppID] = Types.ACTIONS.ACTION_UP;
				currentState.advance(actions);
			}
			return true;
		}
		if ((Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_NIL
				|| Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_UP)
				&& Math.abs(distance.x) < currentState.getBlockSize()
				&& Math.abs(distance.y + speedInPixels) < currentState.getBlockSize())
		{
			if (Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_NIL
					&& spriteType == -1 - oppID
					&& Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_DOWN)
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[oppID] = Types.ACTIONS.ACTION_DOWN;
				currentState.advance(actions);
			}
			return true;
		}
		if ((Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_NIL
				|| Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_RIGHT)
				&& Math.abs(distance.x - speedInPixels) < currentState.getBlockSize()
				&& Math.abs(distance.y) < currentState.getBlockSize())
		{
			if (Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_NIL
					&& spriteType == -1 - oppID
					&& Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_LEFT)
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[oppID] = Types.ACTIONS.ACTION_LEFT;
				currentState.advance(actions);
			}
			return true;
		}
		if ((Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_NIL
				|| Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_LEFT)
				&& Math.abs(distance.x + speedInPixels) < currentState.getBlockSize()
				&& Math.abs(distance.y) < currentState.getBlockSize())
		{
			if (Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_NIL
					&& spriteType == -1 - oppID
					&& Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_RIGHT)
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[oppID] = Types.ACTIONS.ACTION_RIGHT;
				currentState.advance(actions);
			}
			return true;
		}
		return false;
	}

	public boolean isSpriteWall(SpriteTypeFeatures sprite)
	{
		return (sprite.category == 4 && !sprite.passable && !sprite.destroyable);
	}

	public boolean isSpriteDoingNothing(SpriteTypeFeatures sprite)
	{
		if (sprite == null)
			return true;

		return (!sprite.collectable && !sprite.allowingVictory && !sprite.dangerousOtherwise
				&& !sprite.givingVictoryMove && !sprite.givingDefeatMove && sprite.changingPointsMove == 0
				&& !sprite.givingVictoryUse && !sprite.givingDefeatUse && sprite.changingPointsUse == 0
				&& sprite.changingValuesOfOtherObjects == 0 && sprite.dangerousToAvatar == 0);
	}

	public ArrayList<Observation> getListOfSprites(StateObservationMulti state)
	{
		ArrayList<Observation> listOfSprites = new ArrayList<Observation>();
		ArrayList<Observation> part[];

		if (treatFromAvatarAsSprite)
		{
			part = state.getFromAvatarSpritesPositions();
			if (part != null)
				for (ArrayList<Observation> array : part)
					listOfSprites.addAll(array);
		}

		part = state.getImmovablePositions();
		if (part != null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);

		part = state.getMovablePositions();
		if (part != null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);

		part = state.getNPCPositions();
		if (part != null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);

		part = state.getPortalsPositions();
		if (part != null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);

		part = state.getResourcesPositions();
		if (part != null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);

		Observation opponent = getPlayerObservation(((TPGameKnowledge) gameKnowledge).getOppID(), state);
		if (opponent != null)
			listOfSprites.add(opponent);

		return listOfSprites;
	}

	public Observation getPlayerObservation(int playerID, StateObservationMulti stateObs)
	{
		ArrayList<Observation> observations = stateObs
				.getObservationGrid()[(int) (stateObs.getAvatarPosition(playerID).x
						/ stateObs.getBlockSize())][(int) (stateObs.getAvatarPosition(playerID).y
								/ stateObs.getBlockSize())];

		Observation opp = null;

		for (Observation obs : observations)
		{
			if (obs.category == 0)
			{
				if (gameKnowledge.getAvatarSpriteId() == obs.obsID)
					return obs;
				opp = obs;
			}
		}

		return opp;
	}

	public ArrayList<Observation> getListOfSpritesRepresentants(StateObservationMulti state, Vector2d refPosition)
	{
		ArrayList<Observation> listOfSprites = new ArrayList<Observation>();
		ArrayList<Observation> part[];

		if (treatFromAvatarAsSprite)
		{
			part = state.getFromAvatarSpritesPositions(refPosition);
			listOfSprites.addAll(getRepresentatives(part, refPosition));
		}

		part = state.getImmovablePositions(refPosition);
		listOfSprites.addAll(getRepresentatives(part, refPosition));

		part = state.getMovablePositions(refPosition);
		listOfSprites.addAll(getRepresentatives(part, refPosition));

		part = state.getNPCPositions(refPosition);
		listOfSprites.addAll(getRepresentatives(part, refPosition));

		part = state.getPortalsPositions(refPosition);
		listOfSprites.addAll(getRepresentatives(part, refPosition));

		part = state.getResourcesPositions(refPosition);
		listOfSprites.addAll(getRepresentatives(part, refPosition));

		Observation opponent = getPlayerObservation(((TPGameKnowledge) gameKnowledge).getOppID(), state);
		if (opponent != null)
			listOfSprites.add(opponent);

		return listOfSprites;
	}

	private ArrayList<Observation> getRepresentatives(ArrayList<Observation>[] part, Vector2d refPosition)
	{
		ArrayList<Observation> listOfSprites = new ArrayList<Observation>();
		if (part != null)
		{
			for (ArrayList<Observation> array : part)
			{
				for (Observation obs : array)
				{
					if (obs.position != refPosition)
					{
						listOfSprites.add(obs);
						break;
					}
				}
			}
		}
		return listOfSprites;
	}

	public double getManhattanDistanceInAvatarSteps(StateObservationMulti stateObs, int playerID, Vector2d position1,
			Vector2d position2)
	{
		return getManhattanDistanceInBlockSizes(stateObs, position1, position2) / stateObs.getAvatarSpeed(playerID);
	}

	public ArrayList<Types.ACTIONS> getPlayerMoveActions(StateObservationMulti stateObs, int playerID)
	{
		ArrayList<Types.ACTIONS> playerActions = stateObs.getAvailableActions(playerID);

		ArrayList<Types.ACTIONS> playerMoveActions = new ArrayList<Types.ACTIONS>();
		Types.ACTIONS[] moveActions = { Types.ACTIONS.ACTION_RIGHT, Types.ACTIONS.ACTION_LEFT, Types.ACTIONS.ACTION_UP,
				Types.ACTIONS.ACTION_DOWN };
		for (Types.ACTIONS i : playerActions)
			for (Types.ACTIONS j : moveActions)
				if (j == i)
					playerMoveActions.add(j);

		return playerMoveActions;
	}

	public int getSpriteCategoryFromState(int spriteType, StateObservationMulti state)
	{
		ArrayList<Observation> spritesRepresentants = getListOfSpritesRepresentants(state,
				state.getAvatarPosition(gameKnowledge.getPlayerID()));

		for (Observation sprite : spritesRepresentants)
		{
			if (sprite.itype == spriteType)
				;
			return sprite.category;
		}
		return -1;
	}

	public Observation getSpriteRepresentant(int spriteType, StateObservationMulti state)
	{
		ArrayList<Observation> spritesRepresentants = getListOfSpritesRepresentants(state,
				state.getAvatarPosition(gameKnowledge.getPlayerID()));

		for (Observation sprite : spritesRepresentants)
		{
			if (sprite.itype == spriteType)
				;
			return sprite;
		}
		return null;
	}

	public int getPlayerId(int playerID, StateObservationMulti stateObs)
	{
		ArrayList<Observation> observations = stateObs
				.getObservationGrid()[(int) (stateObs.getAvatarPosition(playerID).x
						/ stateObs.getBlockSize())][(int) (stateObs.getAvatarPosition(playerID).y
								/ stateObs.getBlockSize())];

		for (Observation obs : observations)
		{
			if (obs.category == 0)
			{
				return obs.obsID;
			}
		}

		return 0;
	}
}
