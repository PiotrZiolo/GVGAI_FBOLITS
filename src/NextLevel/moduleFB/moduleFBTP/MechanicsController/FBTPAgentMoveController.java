package NextLevel.moduleFB.moduleFBTP.MechanicsController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.moduleFB.moduleFBTP.FBTPGameKnowledge;
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
		pathFinder = new FBTPPathFinder(gameKnowledge, gameMechanicsController);

		this.elapsedTimer = new ElapsedCpuTimer();
	}

	public void setParameters(boolean fastThinking, int approachingSpriteMovesLimit)
	{
		this.fastThinking = fastThinking;
		this.approachingSpriteMovesLimit = approachingSpriteMovesLimit;
	}

	public Types.ACTIONS moveTowardPosition(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		FBTPGameKnowledge fbtpGameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;

		return Types.ACTIONS.ACTION_NIL;
	}

	public ArrayList<Types.ACTIONS> findPath(Vector2d goalPosition, StateObservation stateObs,
			ElapsedCpuTimer elapsedTimer, int timeLimit)
	{
		return pathFinder.findPath(goalPosition, stateObs, elapsedTimer, timeLimit);
	}

	// not tested in the new class structure
	private StateObservationMulti approachSprite(StateObservationMulti stateObs, Observation observation)
	{
		FBTPGameKnowledge gameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		TPGameMechanicsController gameMechanicsController = (TPGameMechanicsController) this.gameMechanicsController;
		int playerID = gameKnowledge.getPlayerID();
		int oppID = gameKnowledge.getOppID();
		int pathFinderLimit = (fastThinking) ? 1 : 10;
		if (LogHandler.bLoggingOn && !stateObs.isAvatarAlive(oppID))
		{
			LogHandler.writeLog("Opponent died", "Brain.approachSprite", 0);
			return null;
		}

		StateObservationMulti currentState = stateObs.copy();
		StateObservationMulti temporaryState;
		int advanceLimit = approachingSpriteMovesLimit;

		Vector2d playerPreviousPosition = stateObs.getAvatarPosition(playerID);
		Vector2d playerPreviousOrientation = stateObs.getAvatarOrientation(playerID);
		ArrayList<Types.ACTIONS> playerGoodActions = stateObs.getAvailableActions(playerID);
		ArrayList<Types.ACTIONS> opponentGoodActions = stateObs.getAvailableActions(oppID);
		Types.ACTIONS playerLastAction = Types.ACTIONS.ACTION_NIL;

		Vector2d observationPosition = observation.position;
		if (observationPosition == playerPreviousPosition)
		{
			LogHandler.writeLog("Object is in the same place as player", "Brain.approachSprite", 0);
			return null;
		}
		int[] blockWhereObservationWasLastSeen = { (int) (observationPosition.x / stateObs.getBlockSize()),
				(int) (observationPosition.y / stateObs.getBlockSize()) };

		// in this while avatar is trying to minimize distance to goal
		LogHandler.writeLog("PlayerPreviousPosition = " + playerPreviousPosition, "Brain.approachSprite", 0);
		LogHandler.writeLog("ObservationPosition = " + observationPosition, "Brain.approachSprite", 0);
		LogHandler.writeLog("PlayerID = " + playerID, "Brain.approachSprite", 0);
		while (true)
		{

			// finding object position - first in the same place as last time,
			// than in the neighborhood
			if (observation.itype == -1 - oppID)
				observationPosition = currentState.getAvatarPosition(oppID);
			else
				observationPosition = gameMechanicsController.findObject(blockWhereObservationWasLastSeen, currentState, observation.obsID);
			if (observationPosition == null)
			{
				LogHandler.writeLog("Object was lost", "Brain.approachSprite", 0);
				return null;
			}

			LogHandler.writeLog(observationPosition + " " + playerPreviousPosition + " " + playerPreviousOrientation
					+ " " + observation.itype, "Brain.approachSprite", 0);
			// check whether avatar reached the object and return opponent if he
			// is the object.
			if (gameMechanicsController.isSpriteOneMoveFromAvatarWithOpponentRotation(observationPosition, playerPreviousPosition, currentState,
					playerPreviousOrientation, observation.itype))
			{
				LogHandler.writeLog("Standard approach successful", "Brain.approachSprite", 0);

				if (currentState.isGameOver())
				{
					LogHandler.writeLog("Opponent died when rotating", "Brain.approachSprite", 0);
					return null;
				}
				return currentState;
			}

			// if opponent always die finish return null
			if (opponentGoodActions.isEmpty())
			{
				LogHandler.writeLog("Opponent is dead", "Brain.approachSprite", 0);
				return null;
			}

			// choose actions for players
			Types.ACTIONS[] actions = new Types.ACTIONS[2];
			if (opponentGoodActions.contains(Types.ACTIONS.ACTION_NIL))
				actions[oppID] = Types.ACTIONS.ACTION_NIL;
			else
				actions[oppID] = opponentGoodActions.get(new Random().nextInt(opponentGoodActions.size()));

			LogHandler.writeLog("playerGoodActions = " + playerGoodActions.toString(), "Brain.approachSprite", 0);
			actions[playerID] = gameMechanicsController.chooseDirection(observationPosition.copy(), playerPreviousPosition, playerGoodActions,
					playerLastAction);
			temporaryState = currentState.copy();

			// if player don't want to move go to BFS
			if (actions[playerID] == null)
				break;

			// advance
			//
			LogHandler.writeLog("goalPosition = " + observationPosition, "Brain.approachSprite", 0);
			LogHandler.writeLog("actions = " + actions[playerID].toString(), "Brain.approachSprite", 0);
			LogHandler.writeLog("avatarPositionB = " + temporaryState.getAvatarPosition(playerID),
					"Brain.approachSprite", 0);

			if (advanceLimit == 0)
			{
				LogHandler.writeLog("advanceLimit reached", "Brain.approachSprite", 0);
				return null;
			}
			temporaryState.advance(actions);
			advanceLimit--;
			LogHandler.writeLog("avatarPositionA = " + temporaryState.getAvatarPosition(playerID),
					"Brain.approachSprite", 0);

			// check whether no one died
			boolean goodMove = true;
			if (!temporaryState.isAvatarAlive(playerID))
			{
				playerGoodActions.remove(actions[playerID]);
				goodMove = false;
			}
			if (goodMove && !temporaryState.isAvatarAlive(oppID))
			{
				opponentGoodActions.remove(actions[oppID]);
				goodMove = false;
			}

			// check whether player changed position or direction
			Vector2d playerNewPosition = temporaryState.getAvatarPosition(playerID);
			Vector2d playerNewOrientation = temporaryState.getAvatarOrientation(playerID);
			if (goodMove && playerNewPosition.equals(playerPreviousPosition)
					&& playerNewOrientation.equals(playerPreviousOrientation))
			{
				playerGoodActions.remove(actions[playerID]);
				goodMove = false;
			}

			// check whether player haven't move back
			Vector2d previousDistance = playerPreviousPosition.copy().subtract(observationPosition);
			Vector2d newDistance = playerNewPosition.copy().subtract(observationPosition);
			if (goodMove && (Math.abs(previousDistance.x) < Math.abs(newDistance.x)
					|| Math.abs(previousDistance.y) < Math.abs(newDistance.y)))
			{
				playerGoodActions.remove(actions[playerID]);
				goodMove = false;
			}

			// if goodMove=true advance to next step
			if (goodMove)
			{
				currentState = temporaryState;
				if (!playerNewPosition.equals(playerPreviousPosition))
				{
					playerGoodActions = (ArrayList<Types.ACTIONS>) stateObs.getAvailableActions(playerID).clone();
					opponentGoodActions = (ArrayList<Types.ACTIONS>) stateObs.getAvailableActions(oppID).clone();
				}
				playerPreviousPosition = playerNewPosition;
				playerPreviousOrientation = playerNewOrientation;
				playerLastAction = actions[playerID];
			}
		}
		// return null;

		// in this while avatar is trying to go along the shortest path to goal using BFS
		for (int paths = 0; paths < pathFinderLimit; paths++)
		{
			LogHandler.writeLog("playerPreviousPosition = " + playerPreviousPosition, "Brain.approachSprite", 0);
			LogHandler.writeLog("observationPosition = " + observationPosition, "Brain.approachSprite", 0);
			LogHandler.writeLog("playerPreviousPosition = " + currentState.getAvatarPosition(playerID),
					"Brain.approachSprite", 0);
			LogHandler.writeLog("playerID = " + playerID, "Brain.approachSprite", 0);

			ArrayList<Types.ACTIONS> playerMoveSequenceToGoal = pathFinder.findPath(observationPosition,
					currentState, elapsedTimer, 1);
			LogHandler.writeLog("playerID = " + playerID, "Brain.approachSprite", 0);

			Iterator<Types.ACTIONS> iterator = playerMoveSequenceToGoal.iterator();
			Types.ACTIONS forceMove = null;

			iterator = playerMoveSequenceToGoal.iterator();
			LogHandler.writeLog("BFS started", "Brain.approachSprite", 0);
			while (iterator.hasNext())
			{
				// finding object position - first in the same place as last
				// time, than in the neighborhood
				if (observation.itype == -1 - oppID)
					observationPosition = currentState.getAvatarPosition(oppID);
				else
					observationPosition = gameMechanicsController.findObject(blockWhereObservationWasLastSeen, currentState, observation.obsID);
				if (observationPosition == null)
				{
					LogHandler.writeLog("Object was lost", "Brain.approachSprite", 0);
					return null;
				}

				// check whether avatar reached the object and return opponent
				// if he is the object.
				if (gameMechanicsController.isSpriteOneMoveFromAvatarWithOpponentRotation(observationPosition, playerPreviousPosition,
						currentState, playerPreviousOrientation, observation.itype))
				{
					LogHandler.writeLog("Advanced approach successful", "Brain.approachSprite", 0);
					if (currentState.isGameOver())
					{
						LogHandler.writeLog("Opponent died when turning", "Brain.approachSprite", 0);
						return null;
					}
					return currentState;
				}

				// if opponent always die finish return null
				if (opponentGoodActions.isEmpty())
				{
					LogHandler.writeLog("Opponent died", "Brain.approachSprite", 0);
					return null;
				}

				// choose actions for players
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				if (opponentGoodActions.contains(Types.ACTIONS.ACTION_NIL))
					actions[oppID] = Types.ACTIONS.ACTION_NIL;
				else
					actions[oppID] = opponentGoodActions.get(new Random().nextInt(opponentGoodActions.size()));

				LogHandler.writeLog("playerGoodActions = " + playerGoodActions.toString(), "Brain.approachSprite", 0);
				if (forceMove == null)
				{
					actions[playerID] = iterator.next();
					if (actions[playerID] == Types.ACTIONS.ACTION_NIL)
					{
						LogHandler.writeLog("PathFinder failed to find the path", "Brain.approachSprite", 0);
						return null;
					}
				}
				else
					actions[playerID] = forceMove;
				temporaryState = currentState.copy();

				// advance
				LogHandler.writeLog("avatarPosition = " + temporaryState.getAvatarPosition(playerID),
						"Brain.approachSprite", 0);
				LogHandler.writeLog("goalPosition = " + observationPosition, "Brain.approachSprite", 0);
				LogHandler.writeLog("actions = " + actions[playerID].toString(), "Brain.approachSprite", 0);
				LogHandler.writeLog("avatarPositionB = " + temporaryState.getAvatarPosition(playerID),
						"Brain.approachSprite", 0);
				if (advanceLimit == 0)
				{
					LogHandler.writeLog("AdvancedLimit reached", "Brain.approachSprite", 0);
					return null;
				}
				temporaryState.advance(actions);
				advanceLimit--;
				LogHandler.writeLog("avatarPositionA = " + temporaryState.getAvatarPosition(playerID),
						"Brain.approachSprite", 0);
				LogHandler.writeLog("avatarPosition2 = " + temporaryState.getAvatarPosition(playerID),
						"Brain.approachSprite", 0);

				// check whether no one died
				boolean goodMove = true;
				if (!temporaryState.isAvatarAlive(playerID))
				{
					LogHandler.writeLog("Player killed", "Brain.approachSprite", 0);
					return null;

					// playerGoodActions.remove(actions[playerID]);
					// goodMove = false;
				}
				if (!temporaryState.isAvatarAlive(oppID))
				{
					opponentGoodActions.remove(actions[oppID]);
					goodMove = false;
				}

				// check whether player changed position or direction
				Vector2d playerNewPosition = temporaryState.getAvatarPosition(playerID);
				Vector2d playerNewOrientation = temporaryState.getAvatarOrientation(playerID);

				if (playerNewPosition.equals(playerPreviousPosition)
						&& playerNewOrientation.equals(playerPreviousOrientation))
				{
					break; // look for path again
				}
				if (playerNewPosition.equals(playerPreviousPosition)
						&& !playerNewOrientation.equals(playerPreviousOrientation))
				{
					forceMove = actions[playerID];
				}
				else
				{
					forceMove = null;
				}

				// if goodMove=true advance to next step
				if (goodMove)
				{
					currentState = temporaryState.copy();
					if (!playerNewPosition.equals(playerPreviousPosition))
					{
						opponentGoodActions = (ArrayList<Types.ACTIONS>) stateObs.getAvailableActions(oppID).clone();
					}
					playerPreviousPosition = playerNewPosition;
					playerPreviousOrientation = playerNewOrientation;
					playerLastAction = actions[playerID];
				}
			}
		}
		return null;
	}

	/**
	 * Checks if positions position1 and position2 are within numMoves moves of the avatar of the player with playerID id.
	 * 
	 * @param avatarPosition
	 * @param poiPosition
	 * @param numMoves
	 * @param playerID
	 * @return
	 */
	public boolean arePositionsWithinGivenMoveRange(Vector2d position1, Vector2d position2,
			int numMoves, int playerID)
	{
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Returns an action which guarantees not dying for the player with playerID id for safeTurns. 
	 * One non-dying simulation for safeTurns turns/moves is enough to return the first action of the successful sequence.  
	 * @param playerID
	 * @return Non-dying action or null if such action doesn't exist.
	 */
	public ACTIONS getNonDyingAction(int playerID, int safeTurns)
	{
		// TODO Auto-generated method stub
		// assume greedy action for the opponent
		return null;
	}

	/**
	 * Returns an action which gives the best raw score (including reward for win and penalty for loss) for the player with playerID id.
	 * @param playerID
	 * @return Action maximizing raw score.
	 */
	public ACTIONS getGreedyAction(int playerID)
	{
		// TODO Auto-generated method stub
		// use TPWinScoreStateEvaluator to evaluate states
		return null;
	}

	/**
	 * Approaches player avatar (of playerID id) to the observation to not more than minDistance (expressed in moves of the avatar).
	 * 
	 * @param stateObs
	 * @param playerID
	 * @param observation
	 * @param minDistance
	 * @param timeLimit
	 * @return Pair of the final state and a path to the goal. Null if approach was not possible within given time limit.
	 */
	public Pair<StateObservationMulti, ArrayList<ACTIONS>> approachSprite(StateObservationMulti stateObs, int playerID,
			Observation observation, int minDistance, int timeLimit)
	{
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Approaches player avatar (of playerID id) to the observation to not more than minDistance (expressed in moves of the avatar).
	 * 
	 * @param stateObs
	 * @param playerID
	 * @param observation
	 * @param minDistance
	 * @param timeLimit
	 * @return Pair of the final state and a path to the goal. Null if approach was not possible within given time limit.
	 */
	public Pair<StateObservationMulti, ArrayList<ACTIONS>> approachPosition(StateObservationMulti stateObs,
			int playerID, Observation observation, int minDistance, int timeLimit)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
