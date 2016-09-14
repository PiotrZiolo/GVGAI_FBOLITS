package FBOLITS.mechanicsController;

import java.util.ArrayList;

import FBOLITS.GameKnowledge;
import FBOLITS.utils.Pair;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class PathFinder
{
	protected GameKnowledge gameKnowledge;
	protected GameMechanicsController gameMechanicsController;

	public PathFinder()
	{
		this.gameKnowledge = new GameKnowledge();
	}

	public PathFinder(GameKnowledge gameKnowledge, GameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
	}

	/**
	 * Method responsible for finding a path from current avatar position to the given position.
	 * Null is returned if a path is not found in given time limit.
	 * 
	 * @param goalV
	 *            Desired position given in Vector2d.
	 * @param stateObs
	 *            Current game observation.
	 * @param elapsedTimer
	 *            Timer to determine remaining time
	 * @param playerID
	 *            Id of the desired player.
	 */
	public Pair<StateObservation, ArrayList<Types.ACTIONS>> findPath(Vector2d goalPosition, StateObservation stateObs,
			ElapsedCpuTimer elapsedTimer, long timeLimit, boolean takePointsIntoAccount, boolean prohibitNegativePoints)
	{
		ArrayList<Types.ACTIONS> path = new ArrayList<Types.ACTIONS>();
		path.add(Types.ACTIONS.ACTION_NIL);
		return new Pair<StateObservation, ArrayList<Types.ACTIONS>>(stateObs, path);
	}
	
	public Pair<StateObservation, ArrayList<Types.ACTIONS>> findPath(Vector2d goalPosition, StateObservation stateObs,
			ElapsedCpuTimer elapsedTimer, long timeLimit)
	{
		return findPath(goalPosition, stateObs, elapsedTimer, timeLimit, false, false);
	}
	
	/**
	 * Method responsible for finding a path from current avatar position to the given position or to its vicinity.
	 * Null is returned if a path is not found in given time limit.
	 * 
	 * @param goalV
	 *            Desired position given in Vector2d.
	 * @param stateObs
	 *            Current game observation.
	 * @param elapsedTimer
	 *            Timer to determine remaining time
	 * @param playerID
	 *            Id of the desired player.
	 */
	public Pair<StateObservation, ArrayList<Types.ACTIONS>> findPathToAreaNearPosition(Vector2d goalPosition,
			StateObservation stateObs, ElapsedCpuTimer elapsedTimer, long timeLimit, boolean takePointsIntoAccount,
			boolean prohibitNegativePoints)
	{
		ArrayList<Types.ACTIONS> path = new ArrayList<Types.ACTIONS>();
		path.add(Types.ACTIONS.ACTION_NIL);
		return new Pair<StateObservation, ArrayList<Types.ACTIONS>>(stateObs, path);
	}
	
	public Pair<StateObservation, ArrayList<Types.ACTIONS>> findPathToAreaNearPosition(Vector2d goalPosition,
			StateObservation stateObs, ElapsedCpuTimer elapsedTimer, long timeLimit)
	{
		return findPathToAreaNearPosition(goalPosition, stateObs, elapsedTimer, timeLimit, false, false);
	}
}
