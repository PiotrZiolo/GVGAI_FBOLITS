package NextLevel.mechanicsController;

import java.util.ArrayList;

import NextLevel.GameKnowledge;
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
	
	public boolean isMovingAction(Types.ACTIONS act)
	{
		if (act.equals(Types.ACTIONS.ACTION_LEFT) || act.equals(Types.ACTIONS.ACTION_UP) || act.equals(Types.ACTIONS.ACTION_RIGHT) || act.equals(Types.ACTIONS.ACTION_DOWN))
			return true;
		return false;
	}

	public Vector2d findObject(int[] blockWhereObservationWasLastSeen, StateObservation stateObs, int searchedID)
	{
		int worldWidth = (int) stateObs.getWorldDimension().getWidth() / stateObs.getBlockSize();
		int worldHeight = (int) stateObs.getWorldDimension().getHeight() / stateObs.getBlockSize();
		ArrayList<Observation> suspects = stateObs
				.getObservationGrid()[blockWhereObservationWasLastSeen[0]][blockWhereObservationWasLastSeen[1]];
		boolean objectLocalized = false;
		for (Observation suspect : suspects)
		{
			if (suspect.obsID == searchedID)
				return suspect.position;
		}
		if (!objectLocalized)
		{
			for (int i = -1; i <= 1; i++)
			{
				for (int j = -1; j <= 1; j++)
				{
					suspects = stateObs.getObservationGrid()[(worldWidth + blockWhereObservationWasLastSeen[0] + i)
							% worldWidth][(worldHeight + blockWhereObservationWasLastSeen[1] + j) % worldHeight];
					for (Observation suspect : suspects)
					{
						if (suspect.obsID == searchedID)
						{
							blockWhereObservationWasLastSeen[0] = (worldWidth + blockWhereObservationWasLastSeen[0] + i)
									% worldWidth;
							blockWhereObservationWasLastSeen[1] = (worldHeight + blockWhereObservationWasLastSeen[1]
									+ j) % worldHeight;
							return suspect.position;
						}
					}
				}
			}
		}
		return null;
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
