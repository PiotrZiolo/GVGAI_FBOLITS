package FBOLITS.mechanicsController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import FBOLITS.mechanicsController.PathFinder;
import FBOLITS.mechanicsController.PathFinderNode;
import FBOLITS.GameKnowledge;
import FBOLITS.mechanicsController.GameMechanicsController;
import FBOLITS.utils.Pair;
import core.game.StateObservation;
import ontology.Types;
import tools.Direction;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class DynamicPathFinder extends PathFinder
{
	// protected GameKnowledge gameKnowledge;
	// protected GameMechanicsController gameMechanicsController
	protected StateObservation firstObs;
	protected Vector2d goalVector;
	protected ElapsedCpuTimer elapsedTimer;
	protected boolean tryToDestroyObjects;
	protected boolean takePointsIntoAccount = false;
	protected boolean prohibitNegativePoints = false;
	protected double pointsValue = 1.0;
	protected boolean allowTie = false;
	protected long timeLimit;
	protected int numberOfTurnTriesNearGoal = 5;
	
	public DynamicPathFinder()
	{

	}
	
	public DynamicPathFinder(GameKnowledge gameKnowledge, GameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		tryToDestroyObjects = true;
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
		StateObservation finalObs = null;
		this.timeLimit = (long) timeLimit;
		this.elapsedTimer = elapsedTimer;
		this.firstObs = stateObs.copy();
		this.goalVector = goalPosition;
		this.takePointsIntoAccount = takePointsIntoAccount;
		this.prohibitNegativePoints = prohibitNegativePoints;

		final ArrayList<Types.ACTIONS> actions = gameKnowledge.getPlayerMoveActions();

		long initialRemainingTime = 0;
		if (this.timeLimit > 0)
			initialRemainingTime = this.elapsedTimer.remainingTimeMillis();

		Map<Integer, Double> costMap = new HashMap<Integer, Double>();

		double currentScore = getDistanceToGoalScore(stateObs);
		PriorityQueue<PathFinderNode> queue = initializeQueue(stateObs);
		costMap.put(queue.peek().ID, currentScore);

		PathFinderNode goal = new PathFinderNode(0, "", firstObs, goalPosition);

		while (!queue.isEmpty())
		{
			if (this.timeLimit > 0)
				if (initialRemainingTime - elapsedTimer.remainingTimeMillis() >= this.timeLimit)
					break;

			PathFinderNode previous = queue.poll();

			if (previous.equals(goal))
			{
				goal = previous;
				finalObs = goal.stateObs;
				break;
			}

			for (Types.ACTIONS act : actions)
			{
				Pair<StateObservation, String> adv = advance(previous.stateObs, act);
				StateObservation currentObs = adv.first();
				if (!hasPositionChanged(previous.positionV, currentObs.getAvatarPosition()))
					continue;
				double distanceToGoal = getDistanceToGoalScore(currentObs);
				double cost = previous.pathLength + 1 - getScore(previous.stateObs, currentObs);
				PathFinderNode next = new PathFinderNode(cost + distanceToGoal, previous.path + adv.second(),
						currentObs);
				next.pathLength = previous.pathLength + 1;

				if (!costMap.containsKey(next.ID) || costMap.get(next.ID) > next.cost)
				{
					costMap.put(next.ID, next.cost);
					queue.add(next);
				}
			}
		}

		this.timeLimit = 0;
		if (finalObs != null && translateString(goal.path) != null)
			return new Pair<StateObservation, ArrayList<Types.ACTIONS>>((StateObservation) finalObs,
					translateString(goal.path));

		return null;
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
		StateObservation finalObs = null;
		this.timeLimit = timeLimit;
		this.elapsedTimer = elapsedTimer;
		this.firstObs = stateObs.copy();
		this.goalVector = goalPosition;
		this.takePointsIntoAccount = takePointsIntoAccount;
		this.prohibitNegativePoints = prohibitNegativePoints;

		//PerformanceMonitor performanceMonitor = new PerformanceMonitor();
		//performanceMonitor.startNanoMeasure("Start", "FBTPPathFinder.findPathToAreaNearPosition", 3);

		//final ArrayList<Types.ACTIONS> actions = gameKnowledge.getPlayerMoveActions();
		final ArrayList<Types.ACTIONS> actions = gameKnowledge.getPlayerMoveActions();
		
		long initialRemainingTime = 0;
		if (this.timeLimit > 0)
			initialRemainingTime = elapsedTimer.remainingTimeMillis();

		Map<Integer, Double> costMap = new HashMap<Integer, Double>();
		//performanceMonitor.finishNanoMeasure("Finish", "FBTPPathFinder.findPathToAreaNearPosition", 3);
		
		//performanceMonitor.startNanoMeasure("Start", "FBTPPathFinder.findPathToAreaNearPosition", 3);
		double currentScore = getDistanceToGoalScore(stateObs);
		//performanceMonitor.finishNanoMeasure("Finish", "FBTPPathFinder.findPathToAreaNearPosition", 3);
		
		//performanceMonitor.startNanoMeasure("Start", "FBTPPathFinder.findPathToAreaNearPosition", 3);
		PriorityQueue<PathFinderNode> queue = initializeQueue(stateObs);
		//performanceMonitor.finishNanoMeasure("Finish", "FBTPPathFinder.findPathToAreaNearPosition", 3);
		//performanceMonitor.startNanoMeasure("Start", "FBTPPathFinder.findPathToAreaNearPosition", 3);
		costMap.put(queue.peek().ID, currentScore);
		//performanceMonitor.finishNanoMeasure("Finish", "FBTPPathFinder.findPathToAreaNearPosition", 3);

		PathFinderNode goal = new PathFinderNode(0, "", firstObs, goalPosition);

		while (!queue.isEmpty())
		{
			//performanceMonitor.startNanoMeasure("Start loop iteration", "FBTPPathFinder.findPathToAreaNearPosition", 3);
			if (this.timeLimit > 0)
				if (initialRemainingTime - elapsedTimer.remainingTimeMillis() >= this.timeLimit)
					break;

			PathFinderNode previous = queue.peek();

			queue.poll();

			Types.ACTIONS lastMove = gameMechanicsController.getOneStepToSprite(
					previous.stateObs.getAvatarPosition(), goal.positionV,
					previous.stateObs.getAvatarSpeed(), previous.stateObs.getBlockSize());
			/*
			LogHandler.writeLog(
					"Avatar position: " + previous.stateObs.getAvatarPosition(playerID) + ", sprite position: "
							+ goal.positionV + ", one step: " + ((lastMove != null) ? "yes" : "no"),
					"FBTPAgentMoveController.findPathToAreaNearPosition", 0);
			*/
			if (lastMove != null)
			{
				Direction orientation = vectorToDirection(previous.stateObs.getAvatarOrientation());
				StateObservation afterTurnState = previous.stateObs;
				String path = previous.path;
				if (!gameMechanicsController.isOrientationConsistentWithMove(lastMove, orientation))
				{
					if (gameKnowledge.isDeterministicGame())
					{
						afterTurnState = previous.stateObs.copy();
						afterTurnState.advance(lastMove);
						if (afterTurnState.isGameOver())
							continue;
					}
					else
					{
						afterTurnState = previous.stateObs.copy();
						afterTurnState.advance(lastMove);
						int i = 0;
						while (i < numberOfTurnTriesNearGoal && afterTurnState.isGameOver())
						{
							afterTurnState = previous.stateObs.copy();
							afterTurnState.advance(lastMove);
							i++;
						}
						if (afterTurnState.isGameOver())
							continue;
					}
					path += actToString(lastMove);
				}
				path += actToString(lastMove);
				goal = new PathFinderNode(0, path, afterTurnState); // The cost doesn't matter at this stage, hence 0
				finalObs = goal.stateObs;
				break;
			}
			else
			{
				for (Types.ACTIONS act : actions)
				{
					Pair<StateObservation, String> adv = advance(previous.stateObs, act);
					StateObservation currentObs = adv.first();
					if (!hasPositionChanged(previous.positionV, currentObs.getAvatarPosition()))
						continue;
					double distanceToGoal = getDistanceToGoalScore(currentObs);
					double cost = previous.pathLength + 1 - getScore(previous.stateObs, currentObs);
					PathFinderNode next = new PathFinderNode(cost + 2 * distanceToGoal, previous.path + adv.second(),
							currentObs);
					next.pathLength = previous.pathLength + 1;
					/*
					LogHandler.writeLog(
							"Position: " + currentObs.getAvatarPosition(playerID) + ", distance to goal: "
									+ distanceToGoal + ", cost: " + cost,
							"FBTPAgentMoveController.findPathToAreaNearPosition", 0);
					*/
					if (!costMap.containsKey(next.ID) || costMap.get(next.ID) > next.cost)
					{
						costMap.put(next.ID, next.cost);
						queue.add(next);
					}
				}
			}
			//performanceMonitor.finishNanoMeasure("Finish loop iteration", "FBTPPathFinder.findPathToAreaNearPosition", 3);
		}
		/*
		LogHandler.writeLog("Path finding start> Goal position: " + goalPosition
				+ " Avatar position: " + stateObsMulti.getAvatarPosition(playerID)
				+ " Time limit: " + timeLimit
				+ " Path found: " + ((finalObs != null) ? "yes" : "no"), 
				"FBTPPathFinder.findPathToAreaNearPosition", 3);
		*/
		this.timeLimit = 0;
		if (finalObs != null && translateString(goal.path) != null)
			return new Pair<StateObservation, ArrayList<Types.ACTIONS>>((StateObservation) finalObs,
					translateString(goal.path));
		return null;
	}

	public Pair<StateObservation, ArrayList<Types.ACTIONS>> findPathToAreaNearPosition(Vector2d goalPosition,
			StateObservation stateObs, ElapsedCpuTimer elapsedTimer, long timeLimit)
	{
		return findPathToAreaNearPosition(goalPosition, stateObs, elapsedTimer, timeLimit, false, false);
	}

	private PriorityQueue<PathFinderNode> initializeQueue(StateObservation stateObs)
	{
		// int initialSize = (int) stateObs.getWorldDimension().width / stateObs.getBlockSize();
		// initialSize *= (int) stateObs.getWorldDimension().height / stateObs.getBlockSize();
		PriorityQueue<PathFinderNode> queue = new PriorityQueue<PathFinderNode>(100, comp);
		queue.add(new PathFinderNode(0, "", stateObs));
		return queue;
	}

	private double getDistanceToGoalScore(StateObservation stateObs)
	{
		return gameMechanicsController.getManhattanDistanceInAvatarSteps(stateObs,
				stateObs.getAvatarPosition(), goalVector);
	}

	private double getScore(StateObservation previous, StateObservation current)
	{
		double inf = 1000000;
		double cost = 0;

		// PZi: I cut "&& current.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES" from the condition,
		// because even if the player won, this path couldn't be further developed until it reaches the goal
		if (current.isGameOver())
			return inf;

		// PZi: I commented the following lines; explanation above
		// if (!allowTie && current.isGameOver() && current.getMultiGameWinner()[playerID] == Types.WINNER.NO_WINNER)
		// return inf;

		if (!current.isAvatarAlive())
			return inf;

		/*
		 * // PZi: I commented this fragment, since type=2 doesn't identify portals. Category is needed, and it is not available in the Event class.
		 * // One could take category from sprite features from game knowledge, but it won't work during initialization.
		 * if (!allowPortalEvent)
		 * {
		 * // PZI: I changed previous for current and vice versa in the following two lines
		 * TreeSet<Event> newEvents = (TreeSet<Event>) current.getEventsHistory().clone();
		 * newEvents.removeAll(previous.getEventsHistory());
		 * for (Event event : newEvents)
		 * if (fbtpGameKnowledge.getSpriteCategory(event.passiveTypeId) == 2)
		 * return inf;
		 * }
		 */

		if (takePointsIntoAccount)
		{
			cost = previous.getGameScore() - current.getGameScore();

			if (!prohibitNegativePoints && cost > 0)
				return inf;

			cost *= pointsValue;
		}

		return cost;
	}

	/**
	 * This method tries to execute a move described by act. If there is an obstacle prohibiting the agent to make this move,
	 * a use action is invoked in hope of destroying the obstacle.
	 * 
	 * @param stateObs
	 * @param act
	 * @return A pair of the state after the act and a sequence of moves - act and possibly turning and use before it.
	 */
	private Pair<StateObservation, String> advance(StateObservation stateObs, Types.ACTIONS act)
	{
		Direction orientation = vectorToDirection(stateObs.getAvatarOrientation());

		String moves = "";
		StateObservation stateObsCopy = stateObs.copy();

		if (!gameMechanicsController.isOrientationConsistentWithMove(act, orientation))
		{
			stateObsCopy.advance(act);
			moves += actToString(act);
		}
		StateObservation stateObsCopy2 = stateObsCopy.copy();
		stateObsCopy2.advance(act);
		if ((stateObsCopy2.getAvatarPosition().equals(stateObsCopy.getAvatarPosition())
				|| stateObsCopy2.isAvatarAlive() || stateObsCopy2.isGameOver()) && tryToDestroyObjects)
		{
			stateObsCopy.advance(Types.ACTIONS.ACTION_USE);
			stateObsCopy.advance(act);

			if (!stateObsCopy2.getAvatarPosition().equals(stateObsCopy.getAvatarPosition())
					&& stateObsCopy.isAvatarAlive() && !stateObsCopy.isGameOver())
			{
				stateObsCopy2 = stateObsCopy;
				moves += actToString(Types.ACTIONS.ACTION_USE);
			}
		}
		moves += actToString(act);

		return new Pair<StateObservation, String>(stateObsCopy2, moves);
	}

	private boolean hasPositionChanged(Vector2d previous, Vector2d current)
	{
		return !(previous.x == current.x && previous.y == current.y);
	}

	private String actToString(Types.ACTIONS act)
	{
		if (act.equals(Types.ACTIONS.ACTION_LEFT))
			return "0";
		if (act.equals(Types.ACTIONS.ACTION_UP))
			return "1";
		if (act.equals(Types.ACTIONS.ACTION_RIGHT))
			return "2";
		if (act.equals(Types.ACTIONS.ACTION_DOWN))
			return "3";
		if (act.equals(Types.ACTIONS.ACTION_USE))
			return "4";
		return "z";
	}

	private ArrayList<Types.ACTIONS> translateString(String actions)
	{
		ArrayList<Types.ACTIONS> moves = new ArrayList<Types.ACTIONS>(actions.length());
		for (int i = 0; i < actions.length(); i++)
		{
			char act = actions.charAt(i);
			if (act == '0')
				moves.add(Types.ACTIONS.ACTION_LEFT);
			else if (act == '1')
				moves.add(Types.ACTIONS.ACTION_UP);
			else if (act == '2')
				moves.add(Types.ACTIONS.ACTION_RIGHT);
			else if (act == '3')
				moves.add(Types.ACTIONS.ACTION_DOWN);
			else
				moves.add(Types.ACTIONS.ACTION_USE);
		}
		return moves;
	}

	private Direction vectorToDirection(Vector2d v)
	{
		return new Direction(v.x, v.y);
	}

	private Comparator<PathFinderNode> comp = new Comparator<PathFinderNode>()
	{
		public int compare(PathFinderNode a, PathFinderNode b)
		{
			if (a.cost > b.cost)
				return 1;
			if (a.cost == b.cost)
				return 0;
			return -1;
		}
	};
}
