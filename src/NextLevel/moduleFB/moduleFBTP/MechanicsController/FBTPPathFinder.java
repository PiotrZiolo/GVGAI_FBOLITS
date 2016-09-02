package NextLevel.moduleFB.moduleFBTP.MechanicsController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.TreeSet;

import NextLevel.moduleFB.moduleFBTP.MechanicsController.PathFinderNode;
import NextLevel.mechanicsController.PathFinder;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.utils.Pair;
import baseStructure.utils.LogHandler;
import NextLevel.moduleFB.moduleFBTP.FBTPGameKnowledge;
import core.game.Event;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.Direction;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class FBTPPathFinder extends PathFinder
{
	// protected FBTPGameKnowledge gameKnowledge;
	// protected TPGameMechanicsController gameMechanicsController
	private boolean tryToDestroyObjects;

	private int playerID;
	private StateObservationMulti firstObs;
	private Vector2d goalVector;
	private ElapsedCpuTimer elapsedTimer;
	private boolean allowPointsLoosing = true;
	private double pointsValue = 1.0;
	private boolean allowTie = false;
	private long timeLimit;

	public FBTPPathFinder(FBTPGameKnowledge gameKnowledge, TPGameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		tryToDestroyObjects = false;
		/*
		 * implement getSpriteTypes() method first; than uncomment this section and comment line "tryToDestroyObjects = true;"
		 * ArrayList<Integer> types = gameKnowledge.getSpriteTypes();
		 * for (int type : types )
		 * if (gameKnowledge.getSpriteTypeFeaturesByType(type).destroyable)
		 * tryToDestroyObjects = true;
		 */
		tryToDestroyObjects = true;
	}

	/**
	 * The method responsible for finding path from current avatar position to
	 * the given vector
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
			ElapsedCpuTimer elapsedTimer, long timeLimit)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		StateObservationMulti finalObs = null;
		this.timeLimit = (long) timeLimit;
		this.elapsedTimer = elapsedTimer;
		this.playerID = gameKnowledge.getPlayerID();
		this.firstObs = stateObsMulti.copy();
		this.goalVector = goalPosition;

		final ArrayList<Types.ACTIONS> actions = gameKnowledge.getPlayerMoveActions();

		long startTime = 0;
		if (this.timeLimit > 0)
			startTime = this.elapsedTimer.remainingTimeMillis();
		// System.out.println(startTime);

		Map<Integer, Double> costMap = new HashMap<Integer, Double>();

		double currentScore = getDistanceScore(stateObsMulti);
		PriorityQueue<PathFinderNode> queue = initializeQueue(stateObsMulti);
		costMap.put(queue.peek().ID, currentScore);

		PathFinderNode goal = new PathFinderNode(0, "", firstObs, goalPosition);
		PathFinderNode bestPath = new PathFinderNode(10 * currentScore, "", stateObsMulti, playerID);

		// System.out.println(this.elapsedTimer.remainingTimeMillis());

		while (!queue.isEmpty())
		{
			if (this.timeLimit > 0)
				if (startTime - elapsedTimer.remainingTimeMillis() >= this.timeLimit)
					break;
			PathFinderNode previous = queue.peek();

			/*
			 * System.out.println("G");
			 * System.out.println(previous.positionG.first().toString());
			 * System.out.println(previous.positionG.second().toString());
			 * System.out.println(previous.cost);
			 */
			queue.poll();

			if (previous.equals(goal))
			{
				goal = previous;
				finalObs = goal.stateObs;
				break;
			}

			queue.poll();

			// System.out.println("");
			for (Types.ACTIONS act : actions)
			{
				Pair<StateObservationMulti, String> adv = advance(previous.stateObs, act);
				StateObservationMulti currentObs = adv.first();
				// System.out.println(previous.positionV + " " + currentObs.getAvatarPosition(playerID));
				if (!checkAdvance(previous.positionV, currentObs.getAvatarPosition(playerID)))
					continue;
				double cost = getScore(previous.stateObs, currentObs, true);
				PathFinderNode next = new PathFinderNode(cost, previous.path + adv.second(), currentObs, playerID);
				// System.out.println(act.toString());
				// System.out.println(cost);
				// System.out.println(goalVector.toString());
				// System.out.println(adv.first().getAvatarPosition(0).toString());
				// cost+=next.pathLength*orientationAvatar*currentObs.getBlockSize()*currentObs.getAvatarSpeed(playerID);
				cost += adv.second().length() * currentObs.getBlockSize() * currentObs.getAvatarSpeed(playerID);
				// System.out.println(cost);
				next.cost = cost;

				if (!costMap.containsKey(next.ID) || costMap.get(next.ID) > next.cost)
				{
					// System.out.println(cost);
					if (cost < bestPath.cost)
					{
						finalObs = currentObs;
						bestPath = next;
					}
					costMap.put(next.ID, next.cost);
					queue.add(next);
				}
			}

			// System.out.println(this.elapsedTimer.remainingTimeMillis());
		}

		this.timeLimit = 0;
		return new Pair<StateObservation, ArrayList<ACTIONS>>((StateObservation) finalObs,
				translateString(bestPath.path));
	}

	private double getDistanceScore(StateObservationMulti stateObs)
	{
		Vector2d a = stateObs.getAvatarPosition(playerID);
		double speed = stateObs.getAvatarSpeed(playerID);
		return 0.99 * (Math.abs(a.x - goalVector.x) + Math.abs(a.y - goalVector.y)) / speed;
	}

	private double getScore(StateObservationMulti previous, StateObservationMulti current, boolean allowPortalEvent)
	{
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		double inf = 1000000;
		double distance = getDistanceScore(current);
		double cost = previous.getGameScore(playerID) - current.getGameScore(playerID);

		if (!allowPortalEvent)
		{
			TreeSet<Event> newEvents = (TreeSet<Event>) previous.getEventsHistory().clone();
			newEvents.removeAll(current.getEventsHistory());
			for (Event event : newEvents)
				if (fbtpGameKnowledge.getSpriteCategory(event.passiveTypeId) == 2)
					return inf;
		}

		if (!allowPointsLoosing && cost > 0)
			return inf;

		cost *= pointsValue;

		if (!current.isAvatarAlive(playerID))
			return inf;

		if (current.isGameOver() && current.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES)
			return inf;

		if (!allowTie && current.isGameOver() && current.getMultiGameWinner()[playerID] == Types.WINNER.NO_WINNER)
			return inf;

		return distance + cost;
	}

	private Pair<StateObservationMulti, String> advance(StateObservationMulti stateObs, Types.ACTIONS act)
	{
		Direction orientation = vectorToDirection(stateObs.getAvatarOrientation(playerID));

		String moves = "";
		StateObservationMulti stateObsCopy = stateObs.copy();

		if (!gameMechanicsController.isOrientationConsistentWithMove(act, orientation))
		{
			stateObsCopy = advanceSimplified(stateObsCopy, act);
			moves += actToString(act);
		}
		StateObservationMulti stateObsCopy2 = stateObsCopy.copy();
		stateObsCopy2 = advanceSimplified(stateObsCopy2, act);
		if ((stateObsCopy2.getAvatarPosition(playerID).equals(stateObsCopy.getAvatarPosition(playerID))
				|| stateObsCopy2.isAvatarAlive(playerID) || stateObsCopy2.isGameOver()) && tryToDestroyObjects)
		{
			stateObsCopy = advanceSimplified(stateObsCopy, Types.ACTIONS.ACTION_USE);
			stateObsCopy = advanceSimplified(stateObsCopy, act);

			if (!stateObsCopy2.getAvatarPosition(playerID).equals(stateObsCopy.getAvatarPosition(playerID)) 
					&& stateObs.isAvatarAlive(playerID) && !stateObs.isGameOver())
			{
				stateObsCopy2 = stateObsCopy;
				moves += actToString(Types.ACTIONS.ACTION_USE);
			}
		}
		moves += actToString(act);

		return new Pair<StateObservationMulti, String>(stateObsCopy2, moves);
	}

	private StateObservationMulti advanceSimplified(StateObservationMulti stateObs, Types.ACTIONS act)
	{
		ArrayList<Types.ACTIONS> oppAvailableActions = stateObs.getAvailableActions(1 - playerID);
		oppAvailableActions.remove(Types.ACTIONS.ACTION_NIL);
		ACTIONS[] chosenActions = new Types.ACTIONS[2];
		StateObservationMulti nextStateNIL;
		StateObservationMulti nextState;
		
		nextStateNIL = stateObs.copy();
		chosenActions[playerID] = act;
		chosenActions[1 - playerID] = Types.ACTIONS.ACTION_NIL;
		nextStateNIL.advance(chosenActions);
		if (nextStateNIL.isAvatarAlive(1 - playerID))
			return nextStateNIL;
		
		for (Types.ACTIONS oppAction : oppAvailableActions)
		{
			nextState = stateObs.copy();
			chosenActions[playerID] = act;
			chosenActions[1 - playerID] = oppAction;
			nextState.advance(chosenActions);
			if (nextState.isAvatarAlive(1 - playerID))
				return nextState;
		}
		return nextStateNIL;
	}

	private boolean checkAdvance(Vector2d previous, Vector2d current)
	{
		return !(previous.x == current.x && previous.y == current.y);
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

	private PriorityQueue<PathFinderNode> initializeQueue(StateObservationMulti stateObs)
	{
		int initialSize = (int) stateObs.getWorldDimension().width / stateObs.getBlockSize();
		initialSize *= (int) stateObs.getWorldDimension().height / stateObs.getBlockSize();
		PriorityQueue<PathFinderNode> queue = new PriorityQueue<PathFinderNode>(initialSize, comp);
		queue.add(new PathFinderNode(0, "", stateObs, playerID));
		return queue;
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

	public Pair<StateObservation, Types.ACTIONS> findPathForTesting(Vector2d goalPosition, StateObservation stateObs,
			ElapsedCpuTimer elapsedTimer, long timeLimit)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		this.timeLimit = (long) timeLimit;
		this.elapsedTimer = elapsedTimer;
		this.playerID = gameKnowledge.getPlayerID();
		this.firstObs = stateObsMulti.copy();
		this.goalVector = goalPosition;

		final ArrayList<Types.ACTIONS> actions = gameKnowledge.getPlayerMoveActions();

		long startTime = 0;
		if (this.timeLimit > 0)
			startTime = this.elapsedTimer.remainingTimeMillis();

		Map<Integer, Double> costMap = new HashMap<Integer, Double>();

		double currentScore = getDistanceScore(stateObsMulti);
		PriorityQueue<PathFinderNode> queue = initializeQueue(stateObsMulti);
		costMap.put(queue.peek().ID, currentScore);

		PathFinderNode goal = new PathFinderNode(0, "", firstObs, goalPosition);
		PathFinderNode bestPath = new PathFinderNode(10 * currentScore, "", stateObsMulti, playerID);

		while (!queue.isEmpty())
		{
			if (this.timeLimit > 0)
				if (startTime - elapsedTimer.remainingTimeMillis() >= this.timeLimit)
					break;
			PathFinderNode previous = queue.peek();
			queue.poll();

			/*
			 * if (previous.equals(goal))
			 * {
			 * goal = previous;
			 * finalObs = goal.stateObs;
			 * break;
			 * }
			 */

			queue.poll(); // why is that here!?

			// System.out.println("");
			for (Types.ACTIONS act : actions)
			{
				Pair<StateObservationMulti, String> adv = advance(previous.stateObs, act);
				StateObservationMulti currentObs = adv.first();
				if (!checkAdvance(previous.positionV, currentObs.getAvatarPosition(playerID)))
					continue;
				double cost = getScore(previous.stateObs, currentObs, false);
				cost += adv.second().length() * currentObs.getBlockSize() * currentObs.getAvatarSpeed(playerID);
				PathFinderNode next = new PathFinderNode(cost, previous.path + adv.second(), currentObs, playerID);

				if (next.equals(goal))
				{
					// TODO PZi: What is this fragment for? Isn't it obvious that if the avatar reached the goal with the last act, 
					// then orientation is consistent with move?
					
					Direction orientation = vectorToDirection(previous.stateObs.getAvatarOrientation(playerID));
					StateObservationMulti stateObsCopy = previous.stateObs.copy();

					if (!gameMechanicsController.isOrientationConsistentWithMove(act, orientation))
					{
						// TODO WARNING: advanceSimpliefied returns null if the avatar dies, 
						// hence in such a case null will returned
						stateObsCopy = advanceSimplified(stateObsCopy, act);
					}
					return new Pair<StateObservation, Types.ACTIONS>(stateObsCopy, act);
				}
				next.cost = cost;

				if (!costMap.containsKey(next.ID) || costMap.get(next.ID) > next.cost)
				{
					if (cost < bestPath.cost)
					{
						bestPath = next;
					}
					costMap.put(next.ID, next.cost);
					queue.add(next);
				}
			}
		}

		this.timeLimit = 0;
		return null;
	}
}
