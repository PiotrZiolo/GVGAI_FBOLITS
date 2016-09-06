package NextLevel.mechanicsController;

import java.util.ArrayList;

import NextLevel.GameKnowledge;
import NextLevel.utils.AuxUtils;
import NextLevel.utils.PerformanceMonitor;
import baseStructure.utils.LogHandler;
import core.game.Observation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import ontology.Types.ACTIONS;
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

	public boolean isOrientationConsistentWithMove(ACTIONS act, Vector2d orientationVector)
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

		int[] start = { (int) (position.x / stateObs.getBlockSize()), (int) (position.y / stateObs.getBlockSize()) };
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
		return null;
	}

	public Observation localizeSprite(StateObservationMulti stateObs, Observation observation)
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
			double xDistance = Math.min(AuxUtils.mod((int) (position1.x - position2.x),
					(int) (gameKnowledge.getWorldXDimensionInPX())), 
					AuxUtils.mod((int) (position2.x - position1.x),
							(int) (gameKnowledge.getWorldXDimensionInPX())));
			double yDistance = Math.min(AuxUtils.mod((int) (position1.y - position2.y),
					(int) (gameKnowledge.getWorldYDimensionInPX())), 
					AuxUtils.mod((int) (position2.y - position1.y),
							(int) (gameKnowledge.getWorldYDimensionInPX())));
			
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
			double xDistance = Math.min(AuxUtils.mod((int) (position1.x - position2.x),
					(int) (stateObs.getWorldDimension().getWidth())), 
					AuxUtils.mod((int) (position2.x - position1.x),
							(int) (stateObs.getWorldDimension().getWidth())));
			double yDistance = Math.min(AuxUtils.mod((int) (position1.y - position2.y),
					(int) (stateObs.getWorldDimension().getHeight())), 
					AuxUtils.mod((int) (position2.y - position1.y),
							(int) (stateObs.getWorldDimension().getHeight())));
			
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
			double xDistance = Math.min(AuxUtils.mod((int) (position1.x - position2.x),
					(int) (gameKnowledge.getWorldXDimensionInPX())), 
					AuxUtils.mod((int) (position2.x - position1.x),
							(int) (gameKnowledge.getWorldXDimensionInPX())));
			double yDistance = Math.min(AuxUtils.mod((int) (position1.y - position2.y),
					(int) (gameKnowledge.getWorldYDimensionInPX())), 
					AuxUtils.mod((int) (position2.y - position1.y),
							(int) (gameKnowledge.getWorldYDimensionInPX())));
			
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
			double xDistance = Math.min(AuxUtils.mod((int) (position1.x - position2.x),
					(int) (stateObs.getWorldDimension().getWidth())), 
					AuxUtils.mod((int) (position2.x - position1.x),
							(int) (stateObs.getWorldDimension().getWidth())));
			double yDistance = Math.min(AuxUtils.mod((int) (position1.y - position2.y),
					(int) (stateObs.getWorldDimension().getHeight())), 
					AuxUtils.mod((int) (position2.y - position1.y),
							(int) (stateObs.getWorldDimension().getHeight())));
			
			/*
			LogHandler.writeLog("Open map " + AuxUtils.mod((int) (position1.y - position2.y),
					(int) (stateObs.getWorldDimension().getHeight())) + " " 
					+ AuxUtils.mod((int) (position2.y - position1.y),
							(int) (stateObs.getWorldDimension().getHeight())), "GameMechanicsController.Manhattan", 0);
			*/
			
			return (xDistance + yDistance) / stateObs.getBlockSize();
		}
		else
		{
			double xDistance = Math.abs(position1.x - position2.x);
			double yDistance = Math.abs(position1.y - position2.y);
			
			LogHandler.writeLog("Not open map", "GameMechanicsController.Manhattan", 3);
			
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

	// another function to check if position are on the same grid element
}
