package NextLevel.mechanicsController;

import java.util.ArrayList;

import NextLevel.GameKnowledge;
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

		int worldXDimension = stateObs.getObservationGrid().length;
		int worldYDimension = stateObs.getObservationGrid()[0].length;

		int distance = 0;

		while (distance <= maxDistance)
		{
			for (int i = distance; i <= distance; i = i + 1)
			{
				if (i == -distance || i == distance)
				{
					for (int j = -distance; j <= distance; j = j + 1)
					{
						suspects = stateObs.getObservationGrid()[(worldXDimension + start[0] + i)
								% worldXDimension][(worldYDimension + start[1] + j) % worldYDimension];
						for (Observation suspect : suspects)
							if (suspect.obsID == obsID)
								return suspect;
					}
				}
				else
				{
					for (int j = -distance; j <= distance; j = j + 2 * distance)
					{
						suspects = stateObs.getObservationGrid()[(worldXDimension + start[0] + i)
								% worldXDimension][(worldYDimension + start[1] + j) % worldYDimension];
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
		
		return localizeSprite(stateObs, observation.obsID, observation.position, Math.max(worldXDimension / 2, worldYDimension / 2));
	}

	public Observation localizeSprite(StateObservation stateObs, int obsID, Vector2d position)
	{
		int worldXDimension = stateObs.getObservationGrid().length;
		int worldYDimension = stateObs.getObservationGrid()[0].length;
		
		return localizeSprite(stateObs, obsID, position, Math.max(worldXDimension / 2, worldYDimension / 2));
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
}
