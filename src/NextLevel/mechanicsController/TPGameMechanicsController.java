package NextLevel.mechanicsController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;

import NextLevel.moduleFB.SpriteTypeFeatures;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPSituationOfInterest;
import NextLevel.utils.AuxUtils;
import baseStructure.utils.LogHandler;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
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
		return (sprite.type == 4 && !sprite.passable && !sprite.destroyable);
	}

	public boolean isSpriteDoingNothing(SpriteTypeFeatures sprite)
	{
		return (!sprite.collectable && !sprite.allowingVictory && !sprite.dangerousOtherwise && !sprite.givingVictory
				&& !sprite.givingDefeat && sprite.changingPoints == 0 && sprite.changingValuesOfOtherObjects == 0
				&& sprite.dangerousToAvatar == 0);
	}
	
	public ArrayList<Observation> getListOfSprites (StateObservationMulti state)
	{
		ArrayList<Observation> listOfSprites = new ArrayList<Observation>();
		ArrayList<Observation> part[];
		
		if (treatFromAvatarAsSprite)
		{
			part = state.getFromAvatarSpritesPositions();
			if (part!=null)
				for (ArrayList<Observation> array : part)
					listOfSprites.addAll(array);
		}
		
		part = state.getImmovablePositions();
		if (part!=null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);
		
		part = state.getMovablePositions();
		if (part!=null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);
		
		part = state.getNPCPositions();
		if (part!=null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);
		
		part = state.getPortalsPositions();
		if (part!=null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);
		
		part = state.getResourcesPositions();
		if (part!=null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);
		
		Observation opponent = getPlayerObservation(((TPGameKnowledge)gameKnowledge).getOppID(), state);
		if (opponent!=null)
			listOfSprites.add(opponent);
		
		return listOfSprites;
	}
	
	public Observation getPlayerObservation (int playerID, StateObservationMulti stateObs)
	{
		ArrayList<Observation> observations = stateObs.getObservationGrid()
				[(int) (stateObs.getAvatarPosition(playerID).x/stateObs.getBlockSize())]
				[(int) (stateObs.getAvatarPosition(playerID).y/stateObs.getBlockSize())];
		
		Observation opp = null;
		
		for (Observation obs : observations)
		{
			if ( obs.category == 0 )
			{
				if ( opp != null )
					return null;
				opp = obs;
			}
		}
		
		return opp;
	}
	
	/**
	 * Localizes a sprite given by observation on the map in state stateObs.
	 * Returns null if the sprite is not on the map.
	 * 
	 * @param stateObs
	 *            State in which the sprite is to be found.
	 * @param observation
	 *            Earlier observation of the sprite.
	 * @param searchBreadth
	 *            How far from the observation position to search.
	 * @param maxDistance
	 *            How far from the previous position should the sprite be searched for.
	 */
	public Observation localizeSprite(StateObservationMulti stateObs, int obsID, Vector2d position, int maxDistance,
			boolean searchFullBoard)
	{
		ArrayList<Observation> suspects;

		int[] start = { (int) (position.x / stateObs.getBlockSize()),
				(int) (position.y / stateObs.getBlockSize()) };
		LogHandler.writeLog("start x: " + start[0], "GameMechanicsController.localizeSprite", 0);
		LogHandler.writeLog("start y: " + start[1], "GameMechanicsController.localizeSprite", 0);

		int worldXDimension = stateObs.getObservationGrid().length;
		int worldYDimension = stateObs.getObservationGrid()[0].length;
		LogHandler.writeLog("worldXDimension: " + worldXDimension, "GameMechanicsController.localizeSprite", 0);
		LogHandler.writeLog("worldYDimension: " + worldYDimension, "GameMechanicsController.localizeSprite", 0);

		int distance = 0;

		while (distance <= maxDistance)
		{
			for (int i = -distance; i <= distance; i = i + 1)
			{
				if (i == -distance || i == distance)
				{
					for (int j = -distance; j <= distance; j = j + 1)
					{
						int x = AuxUtils.mod(start[0] + i, worldXDimension);
						int y = AuxUtils.mod(start[1] + j, worldYDimension);
						LogHandler.writeLog("Grid search position: [" + x + ", " + y + "]",
								"GameMechanicsController.localizeSprite", 0);
						suspects = stateObs.getObservationGrid()[x][y];
						for (Observation suspect : suspects)
							if (suspect.obsID == obsID)
								return suspect;
					}
				}
				else
				{
					for (int j = -distance; j <= distance; j = j + 2 * distance)
					{
						int x = AuxUtils.mod(start[0] + i, worldXDimension);
						int y = AuxUtils.mod(start[1] + j, worldYDimension);
						LogHandler.writeLog("Grid search position: [" + x + ", " + y + "]",
								"GameMechanicsController.localizeSprite", 0);
						suspects = stateObs.getObservationGrid()[x][y];
						for (Observation suspect : suspects)
							if (suspect.obsID == obsID)
								return suspect;
					}
				}
			}
			distance++;
		}
		if (searchFullBoard)
		{
			getListOfSprites(stateObs);
		}
		return null;
	}

	public Observation localizeSprite(StateObservationMulti stateObs, Observation observation)
	{
		return localizeSprite(stateObs, observation.obsID, observation.position);
	}

	public Observation localizeSprite(StateObservationMulti stateObs, int obsID, Vector2d position)
	{
		int worldXDimension = stateObs.getObservationGrid().length;
		int worldYDimension = stateObs.getObservationGrid()[0].length;

		int rangeX = Math.min( 2, worldXDimension/2 + 1 );
		int rangeY = Math.min( 2, worldYDimension/2 + 1 );

		return localizeSprite(stateObs, obsID, position, Math.max(rangeX, rangeY), false);

		//return localizeSprite(stateObs, obsID, position,
		//		Math.max(worldXDimension / 2 + 1, worldYDimension / 2 + 1), false);
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
		
		Observation opponent = getPlayerObservation(((TPGameKnowledge)gameKnowledge).getOppID(), state);
		if (opponent!=null)
			if (opponent.position != refPosition)
				listOfSprites.add(opponent);
		
		return listOfSprites;
	}

	private ArrayList<Observation> getRepresentatives(ArrayList<Observation>[] part, Vector2d refPosition)
	{
		ArrayList<Observation> listOfSprites = new ArrayList<Observation>();
		if (part!=null)
		{
			for (ArrayList<Observation> array : part)
			{
				for ( Observation obs : array )
				{
					if (obs.position!=refPosition)
					{
						listOfSprites.add(obs);
						break;
					}
				}
			}
		}
		return listOfSprites;
	}
	
	public double getManhattanDistanceInAvatarSteps(StateObservationMulti stateObs, int playerID, Vector2d position1, Vector2d position2)
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
}
