package FBOLITS.mechanicsController;

import java.util.ArrayList;
import java.util.TreeSet;

import FBOLITS.GameKnowledge;
import FBOLITS.SituationOfInterest;
import FBOLITS.utils.AuxUtils;
import core.game.Event;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Direction;
import tools.Vector2d;

public class GameMechanicsController
{
	protected GameKnowledge gameKnowledge;

	public GameMechanicsController()
	{

	}

	public GameMechanicsController(GameKnowledge gameKnowledge)
	{
		this.gameKnowledge = gameKnowledge;
	}

	public boolean isOrientationConsistentWithMove(Types.ACTIONS act, Direction orientation)
	{
		if (orientation.equals(Types.DNONE))
			return true;
		if (act.equals(Types.ACTIONS.ACTION_LEFT))
			return orientation.equals(Types.DLEFT);
		if (act.equals(Types.ACTIONS.ACTION_UP))
			return orientation.equals(Types.DUP);
		if (act.equals(Types.ACTIONS.ACTION_RIGHT))
			return orientation.equals(Types.DRIGHT);
		if (act.equals(Types.ACTIONS.ACTION_DOWN))
			return orientation.equals(Types.DDOWN);
		return false;
	}

	public boolean isOrientationConsistentWithMove(Types.ACTIONS act, Vector2d orientationVector)
	{
		return isOrientationConsistentWithMove(act, new Direction(orientationVector.x, orientationVector.y));
	}

	public boolean isMovingAction(Types.ACTIONS act)
	{
		if (act.equals(Types.ACTIONS.ACTION_LEFT) || act.equals(Types.ACTIONS.ACTION_UP)
				|| act.equals(Types.ACTIONS.ACTION_RIGHT) || act.equals(Types.ACTIONS.ACTION_DOWN))
			return true;
		return false;
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
	public Observation localizeSprite(StateObservation stateObs, int obsID, Vector2d position, int maxDistance)
	{
		ArrayList<Observation> suspects;

		/*
		 * LogHandler.writeLog("Searching for sprite with id: " + obsID + " and last location: " + position,
		 * "GameMechanicsController.localizeSprite", 0);
		 */
		int[] start = { (int) (position.x / stateObs.getBlockSize()), (int) (position.y / stateObs.getBlockSize()) };
		// LogHandler.writeLog("start x: " + start[0], "GameMechanicsController.localizeSprite", 0);
		// LogHandler.writeLog("start y: " + start[1], "GameMechanicsController.localizeSprite", 0);

		int worldXDimension = stateObs.getObservationGrid().length;
		int worldYDimension = stateObs.getObservationGrid()[0].length;
		// LogHandler.writeLog("worldXDimension: " + worldXDimension, "GameMechanicsController.localizeSprite", 0);
		// LogHandler.writeLog("worldYDimension: " + worldYDimension, "GameMechanicsController.localizeSprite", 0);

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
						/*
						 * LogHandler.writeLog("Grid search position: [" + x + ", " + y + "]",
						 * "GameMechanicsController.localizeSprite", 0);
						 */
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
						/*
						 * LogHandler.writeLog("Grid search position: [" + x + ", " + y + "]",
						 * "GameMechanicsController.localizeSprite", 0);
						 */
						suspects = stateObs.getObservationGrid()[x][y];
						for (Observation suspect : suspects)
							if (suspect.obsID == obsID)
								return suspect;
					}
				}
			}
			distance++;
		}
		return null;
	}

	public Observation localizeSprite(StateObservation stateObs, Observation observation)
	{
		int worldXDimension = stateObs.getObservationGrid().length;
		int worldYDimension = stateObs.getObservationGrid()[0].length;

		return localizeSprite(stateObs, observation.obsID, observation.position,
				Math.max(worldXDimension / 2 + 1, worldYDimension / 2 + 1));
	}

	public Observation localizeSprite(StateObservation stateObs, int obsID, Vector2d position)
	{
		int worldXDimension = stateObs.getObservationGrid().length;
		int worldYDimension = stateObs.getObservationGrid()[0].length;

		return localizeSprite(stateObs, obsID, position, Math.max(worldXDimension / 2 + 1, worldYDimension / 2 + 1));
	}

	public Types.ACTIONS chooseDirection(Vector2d observationPosition, Vector2d playerNewPosition,
			ArrayList<Types.ACTIONS> playerGoodActions, Types.ACTIONS lastAction)
	{
		boolean rightAvailable = playerGoodActions.contains(Types.ACTIONS.ACTION_RIGHT)
				&& lastAction != Types.ACTIONS.ACTION_LEFT;
		boolean downAvailable = playerGoodActions.contains(Types.ACTIONS.ACTION_DOWN)
				&& lastAction != Types.ACTIONS.ACTION_UP;
		boolean leftAvailable = playerGoodActions.contains(Types.ACTIONS.ACTION_LEFT)
				&& lastAction != Types.ACTIONS.ACTION_RIGHT;
		boolean upAvailable = playerGoodActions.contains(Types.ACTIONS.ACTION_UP)
				&& lastAction != Types.ACTIONS.ACTION_DOWN;
		Vector2d distance = observationPosition.subtract(playerNewPosition);
		double distanceX = Math.abs(distance.x);
		double distanceY = Math.abs(distance.y);
		if (distanceX < distanceY)
		{
			if (distance.y > 0)
			{
				if (downAvailable)
				{
					return Types.ACTIONS.ACTION_DOWN;
				}
				else
				{
					if (distance.x > 0 && rightAvailable)
						return Types.ACTIONS.ACTION_RIGHT;
					if (distance.x < 0 && leftAvailable)
						return Types.ACTIONS.ACTION_LEFT;
				}
			}
			if (distance.y < 0)
			{
				if (upAvailable)
				{
					return Types.ACTIONS.ACTION_UP;
				}
				else
				{
					if (distance.x > 0 && rightAvailable)
						return Types.ACTIONS.ACTION_RIGHT;
					if (distance.x < 0 && leftAvailable)
						return Types.ACTIONS.ACTION_LEFT;
				}
			}
		}
		else
		{
			if (distance.x > 0)
			{
				if (rightAvailable)
				{
					return Types.ACTIONS.ACTION_RIGHT;
				}
				else
				{
					if (distance.y > 0 && downAvailable)
						return Types.ACTIONS.ACTION_DOWN;
					if (distance.y < 0 && upAvailable)
						return Types.ACTIONS.ACTION_UP;
				}
			}
			if (distance.x < 0)
			{
				if (leftAvailable)
				{
					return Types.ACTIONS.ACTION_LEFT;
				}
				else
				{
					if (distance.y > 0 && downAvailable)
						return Types.ACTIONS.ACTION_DOWN;
					if (distance.y < 0 && upAvailable)
						return Types.ACTIONS.ACTION_UP;
				}
			}
		}
		if (playerGoodActions.contains(lastAction))
			return lastAction;
		return null;
	}

	public TreeSet<Event> getEventsDuringSOI(SituationOfInterest soi)
	{
		TreeSet<Event> events = (TreeSet<Event>) soi.afterState.getEventsHistory().clone();
		events.removeAll(soi.baseState.getEventsHistory());
		return events;
	}

	public int getNumberOfResourcesGainedDuringSOI(SituationOfInterest soi)
	{
		return soi.afterState.getAvatarResources().size() - soi.baseState.getAvatarResources().size();
	}

	/**
	 * Returns Manhattan distance between position1 and position2.
	 * Assumes that upper and lower as well as right and left map edges are glued.
	 * 
	 * @param position1
	 * @param position2
	 * @return Manhattan distance.
	 */
	public double getManhattanDistanceInPX(Vector2d position1, Vector2d position2)
	{
		if (gameKnowledge.isOpenMap())
		{
			double xDistance = Math.min(
					AuxUtils.mod((int) (position1.x - position2.x), (int) (gameKnowledge.getWorldXDimensionInPX())),
					AuxUtils.mod((int) (position2.x - position1.x), (int) (gameKnowledge.getWorldXDimensionInPX())));
			double yDistance = Math.min(
					AuxUtils.mod((int) (position1.y - position2.y), (int) (gameKnowledge.getWorldYDimensionInPX())),
					AuxUtils.mod((int) (position2.y - position1.y), (int) (gameKnowledge.getWorldYDimensionInPX())));

			return (xDistance + yDistance) / gameKnowledge.getBlockSize();
		}
		else
		{
			double xDistance = Math.abs(position1.x - position2.x);
			double yDistance = Math.abs(position1.y - position2.y);

			return xDistance + yDistance;
		}
	}

	public double getManhattanDistanceInPX(StateObservation stateObs, Vector2d position1, Vector2d position2)
	{
		if (gameKnowledge.isOpenMap())
		{
			double xDistance = Math.min(
					AuxUtils.mod((int) (position1.x - position2.x), (int) (stateObs.getWorldDimension().getWidth())),
					AuxUtils.mod((int) (position2.x - position1.x), (int) (stateObs.getWorldDimension().getWidth())));
			double yDistance = Math.min(
					AuxUtils.mod((int) (position1.y - position2.y), (int) (stateObs.getWorldDimension().getHeight())),
					AuxUtils.mod((int) (position2.y - position1.y), (int) (stateObs.getWorldDimension().getHeight())));

			return (xDistance + yDistance) / stateObs.getBlockSize();
		}
		else
		{
			double xDistance = Math.abs(position1.x - position2.x);
			double yDistance = Math.abs(position1.y - position2.y);

			return xDistance + yDistance;
		}
	}

	public double getManhattanDistanceInBlockSizes(Vector2d position1, Vector2d position2)
	{
		if (gameKnowledge.isOpenMap())
		{
			double xDistance = Math.min(
					AuxUtils.mod((int) (position1.x - position2.x), (int) (gameKnowledge.getWorldXDimensionInPX())),
					AuxUtils.mod((int) (position2.x - position1.x), (int) (gameKnowledge.getWorldXDimensionInPX())));
			double yDistance = Math.min(
					AuxUtils.mod((int) (position1.y - position2.y), (int) (gameKnowledge.getWorldYDimensionInPX())),
					AuxUtils.mod((int) (position2.y - position1.y), (int) (gameKnowledge.getWorldYDimensionInPX())));

			return (xDistance + yDistance) / gameKnowledge.getBlockSize();
		}
		else
		{
			double xDistance = Math.abs(position1.x - position2.x);
			double yDistance = Math.abs(position1.y - position2.y);

			return (xDistance + yDistance) / gameKnowledge.getBlockSize();
		}
	}

	public double getManhattanDistanceInBlockSizes(StateObservation stateObs, Vector2d position1, Vector2d position2)
	{
		if (gameKnowledge.isOpenMap())
		{
			double xDistance = Math.min(
					AuxUtils.mod((int) (position1.x - position2.x), (int) (stateObs.getWorldDimension().getWidth())),
					AuxUtils.mod((int) (position2.x - position1.x), (int) (stateObs.getWorldDimension().getWidth())));
			double yDistance = Math.min(
					AuxUtils.mod((int) (position1.y - position2.y), (int) (stateObs.getWorldDimension().getHeight())),
					AuxUtils.mod((int) (position2.y - position1.y), (int) (stateObs.getWorldDimension().getHeight())));

			/*
			 * LogHandler.writeLog("Open map " + AuxUtils.mod((int) (position1.y - position2.y),
			 * (int) (stateObs.getWorldDimension().getHeight())) + " "
			 * + AuxUtils.mod((int) (position2.y - position1.y),
			 * (int) (stateObs.getWorldDimension().getHeight())), "GameMechanicsController.Manhattan", 0);
			 */

			return (xDistance + yDistance) / stateObs.getBlockSize();
		}
		else
		{
			double xDistance = Math.abs(position1.x - position2.x);
			double yDistance = Math.abs(position1.y - position2.y);

			// LogHandler.writeLog("Not open map", "GameMechanicsController.Manhattan", 3);

			return (xDistance + yDistance) / stateObs.getBlockSize();
		}
	}

	public double getManhattanDistanceInAvatarSteps(Vector2d position1, Vector2d position2)
	{
		return getManhattanDistanceInBlockSizes(position1, position2) / gameKnowledge.getAvatarSpeed();
	}

	public double getManhattanDistanceInAvatarSteps(StateObservation stateObs, Vector2d position1, Vector2d position2)
	{
		return getManhattanDistanceInBlockSizes(stateObs, position1, position2) / stateObs.getAvatarSpeed();
	}

	public Types.ACTIONS getOneStepToSprite(Vector2d avatarPosition, Vector2d spritePosition, double speed,
			int blockSize)
	{
		double dx = (avatarPosition.x - spritePosition.x) / blockSize;
		double dy = (avatarPosition.y - spritePosition.y) / blockSize;
		// we assume that other sprites are at least 1/2 blockSize in size
		if (dx > -1 && dx < 1 && dy > 0 && dy < 1 + speed)
			return Types.ACTIONS.ACTION_UP;
		if (dx > -1 && dx < 1 && dy < 0 && dy > -(1 + speed))
			return Types.ACTIONS.ACTION_DOWN;
		if (dy > -1 && dy < 1 && dx > 0 && dx < 1 + speed)
			return Types.ACTIONS.ACTION_LEFT;
		if (dy > -1 && dy < 1 && dx < 0 && dx > -(1 + speed))
			return Types.ACTIONS.ACTION_RIGHT;
		return null;
	}

	public boolean isAvatarAtMostOneStepFromSprite(Vector2d avatarPosition, Vector2d spritePosition, double speed,
			int blockSize)
	{
		double dx = (avatarPosition.x - spritePosition.x) / blockSize;
		double dy = (avatarPosition.y - spritePosition.y) / blockSize;
		// all sprites are of size blockSize * blockSize
		if (dx > -1 && dx < 1 && dy > 0 && dy < 1 + speed)
			return true;
		if (dx > -1 && dx < 1 && dy < 0 && dy > -(1 + speed))
			return true;
		if (dy > -1 && dy < 1 && dx > 0 && dx < 1 + speed)
			return true;
		if (dy > -1 && dy < 1 && dx < 0 && dx > -(1 + speed))
			return true;
		return false;
	}
	
	public boolean isAvatarExactlyOneStepFromSprite(Vector2d avatarPosition, Vector2d spritePosition, double speed,
			int blockSize)
	{
		double dx = (avatarPosition.x - spritePosition.x) / blockSize;
		double dy = (avatarPosition.y - spritePosition.y) / blockSize;
		// all sprites are of size blockSize * blockSize
		if (dx > -1 && dx < 1 && dy >= 1 && dy < 1 + speed)
			return true;
		if (dx > -1 && dx < 1 && dy <= -1 && dy > -(1 + speed))
			return true;
		if (dy > -1 && dy < 1 && dx >= 1 && dx < 1 + speed)
			return true;
		if (dy > -1 && dy < 1 && dx <= -1 && dx > -(1 + speed))
			return true;
		return false;
	}
	
	public boolean isAvatarOverlappingSprite(Vector2d avatarPosition, Vector2d spritePosition, double speed,
			int blockSize)
	{
		double dx = (avatarPosition.x - spritePosition.x) / blockSize;
		double dy = (avatarPosition.y - spritePosition.y) / blockSize;
		// all sprites are of size blockSize * blockSize
		if (dx > -1 && dx < 1 && dy > -1 && dy < 1)
			return true;
		return false;
	}

	public boolean isFromAvatarSpriteOnTheMap(StateObservation stateObs)
	{
		ArrayList<Observation>[] fromAvatarSpritesPositions = stateObs.getFromAvatarSpritesPositions();

		if (fromAvatarSpritesPositions != null)
		{
			for (ArrayList<Observation> observations : fromAvatarSpritesPositions)
			{
				if (observations.size() > 0 && observations.get(0).itype == gameKnowledge.getFromAvatarSpriteType())
				{
					return true;
				}
			}
		}
		return false;
	}

	public int actToIndex(ArrayList<Types.ACTIONS> availableActions, Types.ACTIONS act)
	{
		for (int index = 0; index < availableActions.size(); index++)
		{
			if (act == availableActions.get(index))
				return index;
		}
		return 0;
	}

	public ArrayList<Types.ACTIONS> getPlayerMoveActions(StateObservation stateObs)
	{
		ArrayList<Types.ACTIONS> playerActions = stateObs.getAvailableActions();

		ArrayList<Types.ACTIONS> playerMoveActions = new ArrayList<Types.ACTIONS>();
		Types.ACTIONS[] moveActions = { Types.ACTIONS.ACTION_RIGHT, Types.ACTIONS.ACTION_LEFT, Types.ACTIONS.ACTION_UP,
				Types.ACTIONS.ACTION_DOWN };
		for (Types.ACTIONS i : playerActions)
			for (Types.ACTIONS j : moveActions)
				if (j == i)
					playerMoveActions.add(j);

		return playerMoveActions;
	}

	public int getPlayerId(StateObservation stateObs)
	{
		ArrayList<Observation> observations = stateObs.getObservationGrid()[(int) (stateObs.getAvatarPosition().x
				/ stateObs.getBlockSize())][(int) (stateObs.getAvatarPosition().y / stateObs.getBlockSize())];

		for (Observation obs : observations)
		{
			if (obs.category == 0)
			{
				return obs.obsID;
			}
		}

		return 0;
	}

	// TODO another function to check if positions are on the same grid element

	public ArrayList<Observation> getListOfSprites(StateObservation stateObs, boolean treatFromAvatarAsSprite)
	{
		ArrayList<Observation> listOfSprites = new ArrayList<Observation>();
		ArrayList<Observation> part[];

		if (treatFromAvatarAsSprite)
		{
			part = stateObs.getFromAvatarSpritesPositions();
			if (part != null)
				for (ArrayList<Observation> array : part)
					listOfSprites.addAll(array);
		}

		part = stateObs.getImmovablePositions();
		if (part != null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);

		part = stateObs.getMovablePositions();
		if (part != null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);

		part = stateObs.getNPCPositions();
		if (part != null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);

		part = stateObs.getPortalsPositions();
		if (part != null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);

		part = stateObs.getResourcesPositions();
		if (part != null)
			for (ArrayList<Observation> array : part)
				listOfSprites.addAll(array);

		return listOfSprites;
	}

	public Observation getPlayerObservation(StateObservation stateObs)
	{
		ArrayList<Observation> observations = stateObs.getObservationGrid()[(int) (stateObs.getAvatarPosition().x
				/ stateObs.getBlockSize())][(int) (stateObs.getAvatarPosition().y / stateObs.getBlockSize())];

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

	public ArrayList<Observation> getListOfAllTypesRepresentatives(StateObservation stateObs, Vector2d refPosition,
			boolean treatFromAvatarAsSprite)
	{
		ArrayList<Observation> listOfSprites = new ArrayList<Observation>();
		ArrayList<Observation> categoryObservations[];

		if (treatFromAvatarAsSprite)
		{
			categoryObservations = stateObs.getFromAvatarSpritesPositions(refPosition);
			listOfSprites.addAll(getTypesRepresentativesFromOneCategory(categoryObservations, refPosition));
		}

		categoryObservations = stateObs.getImmovablePositions(refPosition);
		listOfSprites.addAll(getTypesRepresentativesFromOneCategory(categoryObservations, refPosition));

		categoryObservations = stateObs.getMovablePositions(refPosition);
		listOfSprites.addAll(getTypesRepresentativesFromOneCategory(categoryObservations, refPosition));

		categoryObservations = stateObs.getNPCPositions(refPosition);
		listOfSprites.addAll(getTypesRepresentativesFromOneCategory(categoryObservations, refPosition));

		categoryObservations = stateObs.getPortalsPositions(refPosition);
		listOfSprites.addAll(getTypesRepresentativesFromOneCategory(categoryObservations, refPosition));

		categoryObservations = stateObs.getResourcesPositions(refPosition);
		listOfSprites.addAll(getTypesRepresentativesFromOneCategory(categoryObservations, refPosition));

		return listOfSprites;
	}

	private ArrayList<Observation> getTypesRepresentativesFromOneCategory(ArrayList<Observation>[] categoryObservations,
			Vector2d refPosition)
	{
		ArrayList<Observation> listOfSprites = new ArrayList<Observation>();
		if (categoryObservations != null)
		{
			for (ArrayList<Observation> array : categoryObservations)
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

	public Observation getSpriteTypeRepresentative(int spriteType, StateObservation stateObs)
	{
		ArrayList<Observation> spritesRepresentatives = getListOfAllTypesRepresentatives(stateObs, stateObs.getAvatarPosition(),
				false);

		for (Observation sprite : spritesRepresentatives)
		{
			if (sprite.itype == spriteType)
				return sprite;
		}
		return null;
	}
	
	public int getSpriteCategoryFromState(int spriteType, StateObservation stateObs)
	{
		ArrayList<Observation> spritesRepresentants = getListOfAllTypesRepresentatives(stateObs,
				stateObs.getAvatarPosition(), false);

		for (Observation sprite : spritesRepresentants)
		{
			if (sprite.itype == spriteType)
				return sprite.category;
		}
		return -1;
	}
	
	public int getPlayerObsId(StateObservation stateObs)
	{
		ArrayList<Observation> observations = stateObs
				.getObservationGrid()[(int) (stateObs.getAvatarPosition().x
						/ stateObs.getBlockSize())][(int) (stateObs.getAvatarPosition().y
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
