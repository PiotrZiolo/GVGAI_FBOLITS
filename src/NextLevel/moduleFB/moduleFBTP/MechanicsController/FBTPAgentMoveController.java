package NextLevel.moduleFB.moduleFBTP.MechanicsController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Random;

import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.moduleFB.moduleFBTP.FBTPGameKnowledge;
import NextLevel.moduleTP.TPWinScoreStateEvaluator;
import NextLevel.utils.LogHandler;
import NextLevel.utils.Pair;
import core.game.Observation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class FBTPAgentMoveController extends AgentMoveController
{
	// Real types of of fields
	// protected FBTPGameKnowledge gameKnowledge;
	// protected TPGameMechanicsController gameMechanicsController;
	// protected FBTPPathFinder pathFinder;
	// protected ElapsedCpuTimer elapsedTimer;

	private boolean fastThinking;
	private int approachingSpriteMovesLimit;

	public FBTPAgentMoveController(FBTPGameKnowledge gameKnowledge, TPGameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		pathFinder = new FBTPPathFinder(gameKnowledge, this, gameMechanicsController);

		this.elapsedTimer = new ElapsedCpuTimer();
	}

	public void setParameters(boolean fastThinking, int approachingSpriteMovesLimit)
	{
		this.fastThinking = fastThinking;
		this.approachingSpriteMovesLimit = approachingSpriteMovesLimit;
	}

	public ArrayList<Types.ACTIONS> findPath(Vector2d goalPosition, StateObservation stateObs,
			ElapsedCpuTimer elapsedTimer, long timeLimit)
	{
		return pathFinder.findPath(goalPosition, stateObs, elapsedTimer, timeLimit).second();
	}

	// not tested in the new class structure
	/*
	 * public StateObservationMulti approachSprite(StateObservationMulti stateObs, Observation observation) // I think it's useless right now
	 * {
	 * FBTPGameKnowledge gameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
	 * TPGameMechanicsController gameMechanicsController = (TPGameMechanicsController) this.gameMechanicsController;
	 * int playerID = gameKnowledge.getPlayerID();
	 * int oppID = gameKnowledge.getOppID();
	 * int pathFinderLimit = (fastThinking) ? 1 : 10;
	 * if (LogHandler.bLoggingOn && !stateObs.isAvatarAlive(oppID))
	 * {
	 * LogHandler.writeLog("Opponent died", "FBTPAgentMoveController.approachSprite", 0);
	 * return null;
	 * }
	 * 
	 * StateObservationMulti currentState = stateObs.copy();
	 * StateObservationMulti temporaryState;
	 * int advanceLimit = approachingSpriteMovesLimit;
	 * 
	 * Vector2d playerPreviousPosition = stateObs.getAvatarPosition(playerID);
	 * Vector2d playerPreviousOrientation = stateObs.getAvatarOrientation(playerID);
	 * ArrayList<Types.ACTIONS> playerGoodActions = stateObs.getAvailableActions(playerID);
	 * ArrayList<Types.ACTIONS> opponentGoodActions = stateObs.getAvailableActions(oppID);
	 * Types.ACTIONS playerLastAction = Types.ACTIONS.ACTION_NIL;
	 * 
	 * Vector2d observationPosition = observation.position;
	 * if (observationPosition == playerPreviousPosition)
	 * {
	 * LogHandler.writeLog("Object is in the same place as player", "FBTPAgentMoveController.approachSprite", 0);
	 * return null;
	 * }
	 * int[] blockWhereObservationWasLastSeen = { (int) (observationPosition.x / stateObs.getBlockSize()),
	 * (int) (observationPosition.y / stateObs.getBlockSize()) };
	 * 
	 * // in this while avatar is trying to minimize distance to goal
	 * LogHandler.writeLog("PlayerPreviousPosition = " + playerPreviousPosition, "FBTPAgentMoveController.approachSprite", 0);
	 * LogHandler.writeLog("ObservationPosition = " + observationPosition, "FBTPAgentMoveController.approachSprite", 0);
	 * LogHandler.writeLog("PlayerID = " + playerID, "FBTPAgentMoveController.approachSprite", 0);
	 * while (true)
	 * {
	 * // finding object position - first in the same place as last time,
	 * // than in the neighborhood
	 * if (observation.itype == -1 - oppID)
	 * observationPosition = currentState.getAvatarPosition(oppID);
	 * else
	 * observationPosition = this.gameMechanicsController.localizeSprite(currentState, observation.obsID,
	 * new Vector2d(blockWhereObservationWasLastSeen[0], blockWhereObservationWasLastSeen[1])).position;
	 * if (observationPosition == null)
	 * {
	 * LogHandler.writeLog("Object was lost", "FBTPAgentMoveController.approachSprite", 0);
	 * return null;
	 * }
	 * 
	 * LogHandler.writeLog(observationPosition + " " + playerPreviousPosition + " " + playerPreviousOrientation
	 * + " " + observation.itype, "FBTPAgentMoveController.approachSprite", 0);
	 * // check whether avatar reached the object and return opponent if he
	 * // is the object.
	 * if (gameMechanicsController.isSpriteOneMoveFromAvatarWithOpponentRotation(observationPosition,
	 * playerPreviousPosition, currentState, playerPreviousOrientation, observation.itype))
	 * {
	 * LogHandler.writeLog("Standard approach successful", "FBTPAgentMoveController.approachSprite", 0);
	 * 
	 * if (currentState.isGameOver())
	 * {
	 * LogHandler.writeLog("Opponent died when rotating", "FBTPAgentMoveController.approachSprite", 0);
	 * return null;
	 * }
	 * return currentState;
	 * }
	 * 
	 * // if opponent always die finish return null
	 * if (opponentGoodActions.isEmpty())
	 * {
	 * LogHandler.writeLog("Opponent is dead", "FBTPAgentMoveController.approachSprite", 0);
	 * return null;
	 * }
	 * 
	 * // choose actions for players
	 * Types.ACTIONS[] actions = new Types.ACTIONS[2];
	 * if (opponentGoodActions.contains(Types.ACTIONS.ACTION_NIL))
	 * actions[oppID] = Types.ACTIONS.ACTION_NIL;
	 * else
	 * actions[oppID] = opponentGoodActions.get(new Random().nextInt(opponentGoodActions.size()));
	 * 
	 * LogHandler.writeLog("playerGoodActions = " + playerGoodActions.toString(), "FBTPAgentMoveController.approachSprite", 0);
	 * actions[playerID] = gameMechanicsController.chooseDirection(observationPosition.copy(),
	 * playerPreviousPosition, playerGoodActions, playerLastAction);
	 * temporaryState = currentState.copy();
	 * 
	 * // if player don't want to move go to BFS
	 * if (actions[playerID] == null)
	 * break;
	 * 
	 * // advance
	 * //
	 * LogHandler.writeLog("goalPosition = " + observationPosition, "FBTPAgentMoveController.approachSprite", 0);
	 * LogHandler.writeLog("actions = " + actions[playerID].toString(), "FBTPAgentMoveController.approachSprite", 0);
	 * LogHandler.writeLog("avatarPositionB = " + temporaryState.getAvatarPosition(playerID),
	 * "FBTPAgentMoveController.approachSprite", 0);
	 * 
	 * if (advanceLimit == 0)
	 * {
	 * LogHandler.writeLog("advanceLimit reached", "FBTPAgentMoveController.approachSprite", 0);
	 * return null;
	 * }
	 * temporaryState.advance(actions);
	 * advanceLimit--;
	 * LogHandler.writeLog("avatarPositionA = " + temporaryState.getAvatarPosition(playerID),
	 * "FBTPAgentMoveController.approachSprite", 0);
	 * 
	 * // check whether no one died
	 * boolean goodMove = true;
	 * if (!temporaryState.isAvatarAlive(playerID))
	 * {
	 * playerGoodActions.remove(actions[playerID]);
	 * goodMove = false;
	 * }
	 * if (goodMove && !temporaryState.isAvatarAlive(oppID))
	 * {
	 * opponentGoodActions.remove(actions[oppID]);
	 * goodMove = false;
	 * }
	 * 
	 * // check whether player changed position or direction
	 * Vector2d playerNewPosition = temporaryState.getAvatarPosition(playerID);
	 * Vector2d playerNewOrientation = temporaryState.getAvatarOrientation(playerID);
	 * if (goodMove && playerNewPosition.equals(playerPreviousPosition)
	 * && playerNewOrientation.equals(playerPreviousOrientation))
	 * {
	 * playerGoodActions.remove(actions[playerID]);
	 * goodMove = false;
	 * }
	 * 
	 * // check whether player haven't move back
	 * Vector2d previousDistance = playerPreviousPosition.copy().subtract(observationPosition);
	 * Vector2d newDistance = playerNewPosition.copy().subtract(observationPosition);
	 * if (goodMove && (Math.abs(previousDistance.x) < Math.abs(newDistance.x)
	 * || Math.abs(previousDistance.y) < Math.abs(newDistance.y)))
	 * {
	 * playerGoodActions.remove(actions[playerID]);
	 * goodMove = false;
	 * }
	 * 
	 * // if goodMove=true advance to next step
	 * if (goodMove)
	 * {
	 * currentState = temporaryState;
	 * if (!playerNewPosition.equals(playerPreviousPosition))
	 * {
	 * playerGoodActions = (ArrayList<Types.ACTIONS>) stateObs.getAvailableActions(playerID).clone();
	 * opponentGoodActions = (ArrayList<Types.ACTIONS>) stateObs.getAvailableActions(oppID).clone();
	 * }
	 * playerPreviousPosition = playerNewPosition;
	 * playerPreviousOrientation = playerNewOrientation;
	 * playerLastAction = actions[playerID];
	 * }
	 * }
	 * // return null;
	 * 
	 * // in this while avatar is trying to go along the shortest path to goal using BFS
	 * for (int paths = 0; paths < pathFinderLimit; paths++)
	 * {
	 * LogHandler.writeLog("playerPreviousPosition = " + playerPreviousPosition, "FBTPAgentMoveController.approachSprite", 0);
	 * LogHandler.writeLog("observationPosition = " + observationPosition, "FBTPAgentMoveController.approachSprite", 0);
	 * LogHandler.writeLog("playerPreviousPosition = " + currentState.getAvatarPosition(playerID),
	 * "FBTPAgentMoveController.approachSprite", 0);
	 * LogHandler.writeLog("playerID = " + playerID, "FBTPAgentMoveController.approachSprite", 0);
	 * 
	 * ArrayList<Types.ACTIONS> playerMoveSequenceToGoal = pathFinder
	 * .findPath(observationPosition, currentState, elapsedTimer, 1).second();
	 * LogHandler.writeLog("playerID = " + playerID, "FBTPAgentMoveController.approachSprite", 0);
	 * 
	 * Iterator<Types.ACTIONS> iterator = playerMoveSequenceToGoal.iterator();
	 * Types.ACTIONS forceMove = null;
	 * 
	 * iterator = playerMoveSequenceToGoal.iterator();
	 * LogHandler.writeLog("BFS started", "FBTPAgentMoveController.approachSprite", 0);
	 * while (iterator.hasNext())
	 * {
	 * // finding object position - first in the same place as last
	 * // time, than in the neighborhood
	 * if (observation.itype == -1 - oppID)
	 * observationPosition = currentState.getAvatarPosition(oppID);
	 * else
	 * observationPosition = this.gameMechanicsController.localizeSprite(currentState, observation.obsID,
	 * new Vector2d(blockWhereObservationWasLastSeen[0], blockWhereObservationWasLastSeen[1])).position;
	 * if (observationPosition == null)
	 * {
	 * LogHandler.writeLog("Object was lost", "FBTPAgentMoveController.approachSprite", 0);
	 * return null;
	 * }
	 * 
	 * // check whether avatar reached the object and return opponent
	 * // if he is the object.
	 * if (gameMechanicsController.isSpriteOneMoveFromAvatarWithOpponentRotation(observationPosition,
	 * playerPreviousPosition, currentState, playerPreviousOrientation, observation.itype))
	 * {
	 * LogHandler.writeLog("Advanced approach successful", "FBTPAgentMoveController.approachSprite", 0);
	 * if (currentState.isGameOver())
	 * {
	 * LogHandler.writeLog("Opponent died when turning", "FBTPAgentMoveController.approachSprite", 0);
	 * return null;
	 * }
	 * return currentState;
	 * }
	 * 
	 * // if opponent always die finish return null
	 * if (opponentGoodActions.isEmpty())
	 * {
	 * LogHandler.writeLog("Opponent died", "FBTPAgentMoveController.approachSprite", 0);
	 * return null;
	 * }
	 * 
	 * // choose actions for players
	 * Types.ACTIONS[] actions = new Types.ACTIONS[2];
	 * if (opponentGoodActions.contains(Types.ACTIONS.ACTION_NIL))
	 * actions[oppID] = Types.ACTIONS.ACTION_NIL;
	 * else
	 * actions[oppID] = opponentGoodActions.get(new Random().nextInt(opponentGoodActions.size()));
	 * 
	 * LogHandler.writeLog("playerGoodActions = " + playerGoodActions.toString(), "FBTPAgentMoveController.approachSprite", 0);
	 * if (forceMove == null)
	 * {
	 * actions[playerID] = iterator.next();
	 * if (actions[playerID] == Types.ACTIONS.ACTION_NIL)
	 * {
	 * LogHandler.writeLog("PathFinder failed to find the path", "FBTPAgentMoveController.approachSprite", 0);
	 * return null;
	 * }
	 * }
	 * else
	 * actions[playerID] = forceMove;
	 * temporaryState = currentState.copy();
	 * 
	 * // advance
	 * LogHandler.writeLog("avatarPosition = " + temporaryState.getAvatarPosition(playerID),
	 * "FBTPAgentMoveController.approachSprite", 0);
	 * LogHandler.writeLog("goalPosition = " + observationPosition, "FBTPAgentMoveController.approachSprite", 0);
	 * LogHandler.writeLog("actions = " + actions[playerID].toString(), "FBTPAgentMoveController.approachSprite", 0);
	 * LogHandler.writeLog("avatarPositionB = " + temporaryState.getAvatarPosition(playerID),
	 * "FBTPAgentMoveController.approachSprite", 0);
	 * if (advanceLimit == 0)
	 * {
	 * LogHandler.writeLog("AdvancedLimit reached", "FBTPAgentMoveController.approachSprite", 0);
	 * return null;
	 * }
	 * temporaryState.advance(actions);
	 * advanceLimit--;
	 * LogHandler.writeLog("avatarPositionA = " + temporaryState.getAvatarPosition(playerID),
	 * "FBTPAgentMoveController.approachSprite", 0);
	 * LogHandler.writeLog("avatarPosition2 = " + temporaryState.getAvatarPosition(playerID),
	 * "FBTPAgentMoveController.approachSprite", 0);
	 * 
	 * // check whether no one died
	 * boolean goodMove = true;
	 * if (!temporaryState.isAvatarAlive(playerID))
	 * {
	 * LogHandler.writeLog("Player killed", "FBTPAgentMoveController.approachSprite", 0);
	 * return null;
	 * 
	 * // playerGoodActions.remove(actions[playerID]);
	 * // goodMove = false;
	 * }
	 * if (!temporaryState.isAvatarAlive(oppID))
	 * {
	 * opponentGoodActions.remove(actions[oppID]);
	 * goodMove = false;
	 * }
	 * 
	 * // check whether player changed position or direction
	 * Vector2d playerNewPosition = temporaryState.getAvatarPosition(playerID);
	 * Vector2d playerNewOrientation = temporaryState.getAvatarOrientation(playerID);
	 * 
	 * if (playerNewPosition.equals(playerPreviousPosition)
	 * && playerNewOrientation.equals(playerPreviousOrientation))
	 * {
	 * break; // look for path again
	 * }
	 * if (playerNewPosition.equals(playerPreviousPosition)
	 * && !playerNewOrientation.equals(playerPreviousOrientation))
	 * {
	 * forceMove = actions[playerID];
	 * }
	 * else
	 * {
	 * forceMove = null;
	 * }
	 * 
	 * // if goodMove=true advance to next step
	 * if (goodMove)
	 * {
	 * currentState = temporaryState.copy();
	 * if (!playerNewPosition.equals(playerPreviousPosition))
	 * {
	 * opponentGoodActions = (ArrayList<Types.ACTIONS>) stateObs.getAvailableActions(oppID).clone();
	 * }
	 * playerPreviousPosition = playerNewPosition;
	 * playerPreviousOrientation = playerNewOrientation;
	 * playerLastAction = actions[playerID];
	 * }
	 * }
	 * }
	 * return null;
	 * }
	 */

	/**
	 * Checks if positions position1 and position2 are within numMoves moves of the avatar of the player with playerID id.
	 * 
	 * @param avatarPosition
	 * @param poiPosition
	 * @param numMoves
	 * @param playerID
	 * @return
	 */
	public boolean isPositionWithinGivenMoveRange(StateObservationMulti stateObs, Vector2d position, int numMoves,
			int playerID)
	{
		if (gameMechanicsController.getManhattanDistanceInBlockSizes(stateObs.getAvatarPosition(playerID),
				position) > numMoves)
		{
			return false;
		}
		else
		{
			FBTPPathFinder fbtpPathFinder = (FBTPPathFinder) this.pathFinder;
			Pair<StateObservation, ArrayList<Types.ACTIONS>> approachInfo = fbtpPathFinder.findPath(position, stateObs,
					elapsedTimer, 1);

			if (approachInfo != null && approachInfo.second().size() < numMoves)
				return true;
		}

		return false;
	}

	/**
	 * Returns an action which guarantees not dying for the player with playerID id for safeTurns.
	 * One non-dying simulation for safeTurns turns/moves is enough to return the first action of the successful sequence.
	 * 
	 * @param playerID
	 * @return Non-dying action or null if such action doesn't exist.
	 */
	public ACTIONS getNonDyingAction(StateObservationMulti stateObs, int playerID, int safeTurns)
	{
		ArrayList<Types.ACTIONS> availableActions = stateObs.getAvailableActions(playerID);
		availableActions.remove(Types.ACTIONS.ACTION_NIL);
		Collections.shuffle(availableActions);
		availableActions.add(1, Types.ACTIONS.ACTION_NIL); // so that ACTION_NIL goes always first
		Types.ACTIONS oppAction = getGreedyAction(stateObs, 1 - playerID);
		ACTIONS[] chosenActions = new Types.ACTIONS[2];
		StateObservationMulti nextState;
		for (Types.ACTIONS action : availableActions)
		{
			nextState = stateObs.copy();
			chosenActions[playerID] = action;
			chosenActions[1 - playerID] = oppAction;
			nextState.advance(chosenActions);
			if (nextState.isAvatarAlive(playerID)
					&& (safeTurns == 1 || getNonDyingAction(nextState, playerID, safeTurns - 1) != null))
				return action;
		}
		return null;
	}

	/**
	 * Returns an action which gives the best raw score (including reward for win and penalty for loss) for the player with playerID id.
	 * 
	 * @param playerID
	 * @return Action maximizing raw score.
	 */
	public ACTIONS getGreedyAction(StateObservationMulti stateObs, int playerID)
	{
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		ArrayList<Types.ACTIONS> availableActions = stateObs.getAvailableActions(playerID);
		Types.ACTIONS oppAction = Types.ACTIONS.ACTION_NIL;
		ACTIONS[] chosenActions = new Types.ACTIONS[2];
		StateObservationMulti nextState;
		double bestScore = -10000000.0;
		Types.ACTIONS bestAction = Types.ACTIONS.ACTION_NIL;
		TPWinScoreStateEvaluator scoreEvaluator = new TPWinScoreStateEvaluator(fbtpGameKnowledge);
		for (Types.ACTIONS action : availableActions)
		{
			nextState = stateObs.copy();
			chosenActions[playerID] = action;
			chosenActions[1 - playerID] = oppAction;
			nextState.advance(chosenActions);
			double score = scoreEvaluator.evaluateState(nextState);
			if (score > bestScore)
			{
				bestScore = score;
				bestAction = action;
			}
		}
		return bestAction;
		// use TPWinScoreStateEvaluator to evaluate states
	}

	/**
	 * Moves player avatar (of playerID id) to the observation to the distance of one field with orientation toward the sprite.
	 * 
	 * @param stateObs
	 * @param playerID
	 * @param observation
	 * @param timeLimit
	 * @return Pair of the final state and a path to the goal. Null if approach was not possible within given time limit.
	 */
	// we should end one step from sprite looking at its direction
	public Pair<StateObservationMulti, ArrayList<ACTIONS>> approachSprite(StateObservationMulti stateObs, int playerID,
			Observation observation, int i, long timeLimit)
	{
		FBTPPathFinder fbtpPathFinder = (FBTPPathFinder) this.pathFinder;
		StateObservationMulti currentState = stateObs.copy();
		long startTime = elapsedTimer.remainingTimeMillis();
		Observation obs = observation;
		while (startTime - elapsedTimer.remainingTimeMillis() < timeLimit)
		{
			Pair<StateObservation, ArrayList<ACTIONS>> pathFinderOutput = fbtpPathFinder
					.findPathToAreaNearPosition(obs.position, currentState, elapsedTimer, timeLimit);
			/*
			 * System.out.println("pathFinderOutput");
			 * System.out.println(pathFinderOutput);
			 * System.out.println(obs.position);
			 * System.out.println(currentState.getAvatarPosition(playerID));
			 */

			if (pathFinderOutput == null)
				return null;

			currentState = (StateObservationMulti) pathFinderOutput.first();

			obs = this.gameMechanicsController.localizeSprite(currentState, obs);
			if (obs != null)
				if (currentState.getAvatarPosition(playerID).dist(obs.position) <= currentState.getAvatarSpeed() * currentState.getBlockSize()) // not sure if its enough
					return new Pair<StateObservationMulti, ArrayList<ACTIONS>>(currentState, pathFinderOutput.second());
				else
					return null;
		}
		return null;
	}

	/**
	 * Moves player avatar (of playerID id) to the position.
	 * 
	 * @param stateObs
	 * @param playerID
	 * @param position
	 * @param maxDistance
	 * @param timeLimit
	 * @return Pair of the final state and a path to the goal. Null if approach was not possible within given time limit.
	 */
	public Pair<StateObservationMulti, ArrayList<ACTIONS>> reachPosition(StateObservationMulti stateObs,
			int playerID, Vector2d position, int maxDistance, long timeLimit)
	{
		ArrayList<Types.ACTIONS> allActions = new ArrayList<Types.ACTIONS>();
		StateObservationMulti currentState = stateObs.copy();
		long startTime = elapsedTimer.remainingTimeMillis();
		while (startTime - elapsedTimer.remainingTimeMillis() < timeLimit)
		{
			Pair<StateObservation, ArrayList<Types.ACTIONS>> pathFinderOutput = pathFinder.findPath(position,
					currentState, elapsedTimer, timeLimit);

			if (pathFinderOutput == null)
				return null;

			currentState = (StateObservationMulti) pathFinderOutput.first();
			allActions.addAll(pathFinderOutput.second());

			if (currentState.getAvatarPosition(playerID) == position)
				return new Pair<StateObservationMulti, ArrayList<ACTIONS>>(currentState, allActions);
		}
		return null;
	}

	public ACTIONS getNonDyingAction(StateObservationMulti stateObs, int playerID)
	{
		ArrayList<Types.ACTIONS> availableActions = stateObs.getAvailableActions(playerID);
		availableActions.remove(Types.ACTIONS.ACTION_NIL);
		Collections.shuffle(availableActions);
		availableActions.add(1, Types.ACTIONS.ACTION_NIL); // so that ACTION_NIL goes always first
		ACTIONS[] chosenActions = new Types.ACTIONS[2];
		StateObservationMulti nextState;
		for (Types.ACTIONS action : availableActions)
		{
			nextState = stateObs.copy();
			chosenActions[playerID] = action;
			chosenActions[1 - playerID] = Types.ACTIONS.ACTION_NIL;
			nextState.advance(chosenActions);
			if (nextState.isAvatarAlive(playerID))
				return action;
		}
		return null;
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
	
	public Types.ACTIONS getOneStepToSprite(Vector2d avatarPosition, Vector2d spritePosition, double speed, int blockSize)
	{
		double dx = (avatarPosition.x - spritePosition.x) / blockSize;
		double dy = (avatarPosition.y - spritePosition.y) / blockSize;
		// we assume that other sprites are at least 1/2 blockSize in size
		if (dx > -1 && dx < 0.5 && dy > 0 && dy < 0.5 + speed) 
			return Types.ACTIONS.ACTION_UP;
		if (dx > -1 && dx < 0.5 && dy < 0 && dy > -(1 + speed)) 
			return Types.ACTIONS.ACTION_DOWN;
		if (dy > -1 && dy < 0.5 && dx > 0 && dx < 0.5 + speed)
			return Types.ACTIONS.ACTION_LEFT;
		if (dy > -1 && dy < 0.5 && dx < 0 && dx > -(1 + speed)) 
			return Types.ACTIONS.ACTION_RIGHT;
		return null;
	}
	
	public boolean isAvatarOneStepFromSprite(Vector2d avatarPosition, Vector2d spritePosition, double speed, int blockSize)
	{
		int dx = (int) (avatarPosition.x / blockSize) - (int) (spritePosition.x / blockSize);
		int dy = (int) (avatarPosition.y / blockSize) - (int) (spritePosition.y / blockSize);
		if (dx == 0 && dy > 0 && dy < blockSize + speed)
			return true;
		if (dx == 0 && dy < 0 && dy >= -(0.5 * blockSize + speed)) // we assume that other sprites are at least 1/2 blockSize in size
			return true;
		if (dy == 0 && dx > 0 && dx <= blockSize + speed)
			return true;
		if (dy == 0 && dx < 0 && dx >= -(0.5 * blockSize + speed)) // we assume that other sprites are at least 1/2 blockSize in size
			return true;
		return false;
	}
}
