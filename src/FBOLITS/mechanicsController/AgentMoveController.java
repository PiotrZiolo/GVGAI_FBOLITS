package FBOLITS.mechanicsController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import FBOLITS.GameKnowledge;
import FBOLITS.WinScoreStateEvaluator;
import FBOLITS.utils.Pair;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class AgentMoveController
{
	protected GameKnowledge gameKnowledge;
	protected GameMechanicsController gameMechanicsController;
	protected DynamicPathFinder pathFinder;

	protected ElapsedCpuTimer elapsedTimer;

	public AgentMoveController()
	{
		this.elapsedTimer = new ElapsedCpuTimer();
	}

	public AgentMoveController(GameKnowledge gameKnowledge, GameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		pathFinder = new DynamicPathFinder(gameKnowledge, gameMechanicsController);

		this.elapsedTimer = new ElapsedCpuTimer();
	}

	public ArrayList<Types.ACTIONS> findPath(Vector2d goalPosition, StateObservation stateObs,
			ElapsedCpuTimer elapsedTimer, long timeLimit)
	{
		return pathFinder.findPath(goalPosition, stateObs, elapsedTimer, timeLimit).second();
	}

	/**
	 * Checks if positions position1 and position2 are within numMoves moves of the avatar of the player.
	 * 
	 * @param avatarPosition
	 * @param poiPosition
	 * @param numMoves
	 * @return
	 */
	public boolean isPositionWithinGivenMoveRange(StateObservation stateObs, Vector2d position, int numMoves)
	{
		if (gameMechanicsController.getManhattanDistanceInAvatarSteps(stateObs.getAvatarPosition(),
				position) > numMoves)
		{
			return false;
		}
		else if (gameMechanicsController.isAvatarAtMostOneStepFromSprite(stateObs.getAvatarPosition(), position, 
				stateObs.getAvatarSpeed(), stateObs.getBlockSize()))
		{
			return true;
		}
		else
		{
			Pair<StateObservation, ArrayList<Types.ACTIONS>> approachInfo = pathFinder.findPathToAreaNearPosition(position, stateObs,
					elapsedTimer, 2);
			/*
			LogHandler.writeLog("Range path found: " + ((approachInfo != null) 
					? "yes Size: " + approachInfo.second().size() + " " + approachInfo.second()
					: "no"), 
					"FBTPAgentMoveController.isPositionWithinGivenMoveRange", 3);
			 */
			if (approachInfo != null && approachInfo.second().size() <= numMoves + 1)
				return true;
		}

		return false;
	}

	/**
	 * Moves player avatar to the observation to the distance of one field with orientation toward the sprite.
	 * 
	 * @param stateObs
	 * @param observation
	 * @param timeLimit
	 * @return Pair of the final state and a path to the goal. Null if approach was not possible within given time limit.
	 */
	// we should end one step from sprite looking at its direction
	public Pair<StateObservation, ArrayList<Types.ACTIONS>> approachSprite(StateObservation stateObs,
			Observation observation, int i, long timeLimit)
	{
		StateObservation currentState = stateObs.copy();
		long startTime = elapsedTimer.remainingTimeMillis();
		
		// boolean localized = ((this.gameMechanicsController.localizeSprite(currentState, observation) != null) ? true : false);
		
		while (startTime - elapsedTimer.remainingTimeMillis() < timeLimit)
		{
			Pair<StateObservation, ArrayList<Types.ACTIONS>> pathFinderOutput = pathFinder
					.findPathToAreaNearPosition(observation.position, currentState, elapsedTimer, timeLimit);

			if (pathFinderOutput == null)
			{
				// LogHandler.writeLog("PathFinder returned null", "FBTPAgentMoveController.approachSprite", 3);
				return null;
			}

			currentState = (StateObservation) pathFinderOutput.first();

			observation = this.gameMechanicsController.localizeSprite(currentState, observation);
			if (observation != null)
			{
				if (gameMechanicsController.isAvatarAtMostOneStepFromSprite(currentState.getAvatarPosition(), observation.position, 
						currentState.getAvatarSpeed(), currentState.getBlockSize()))
				{
					// LogHandler.writeLog("PathFinder returned path, obs was localized and it was one step from avatar at the end", "FBTPAgentMoveController.approachSprite", 3);
					return new Pair<StateObservation, ArrayList<Types.ACTIONS>>(currentState, pathFinderOutput.second());
				}
				/*
				else
				{
					LogHandler.writeLog("PathFinder returned path, obs was localized, but it was not one step from avatar", "FBTPAgentMoveController.approachSprite", 3);
				}
				*/
			}
			else
			{
				/*
				LogHandler.writeLog("PathFinder returned path, but obs was not localized" 
						+ " Path: " + pathFinderOutput.second(), "FBTPAgentMoveController.approachSprite", 3);
				*/
				return null;
			}
		}
		return null;
	}

	/**
	 * Moves player avatar to the position.
	 * 
	 * @param stateObs
	 * @param position
	 * @param maxDistance
	 * @param timeLimit
	 * @return Pair of the final state and a path to the goal. Null if approach was not possible within given time limit.
	 */
	public Pair<StateObservation, ArrayList<Types.ACTIONS>> reachPosition(StateObservation stateObs, Vector2d position, int maxDistance, long timeLimit)
	{
		ArrayList<Types.ACTIONS> allActions = new ArrayList<Types.ACTIONS>();
		StateObservation currentState = stateObs.copy();
		long startTime = elapsedTimer.remainingTimeMillis();
		while (startTime - elapsedTimer.remainingTimeMillis() < timeLimit)
		{
			Pair<StateObservation, ArrayList<Types.ACTIONS>> pathFinderOutput = pathFinder.findPath(position,
					currentState, elapsedTimer, timeLimit);

			if (pathFinderOutput == null)
				return null;

			currentState = (StateObservation) pathFinderOutput.first();
			allActions.addAll(pathFinderOutput.second());

			if (currentState.getAvatarPosition() == position)
				return new Pair<StateObservation, ArrayList<Types.ACTIONS>>(currentState, allActions);
		}
		return null;
	}
	
	/**
	 * Returns an action which guarantees not dying for the player for safeTurns.
	 * One non-dying simulation for safeTurns turns/moves is enough to return the first action of the successful sequence.
	 * 
	 * @return Non-dying action or null if such action doesn't exist.
	 */
	public Types.ACTIONS getNonDyingAction(StateObservation stateObs, int safeTurns)
	{
		ArrayList<Types.ACTIONS> availableActions = new ArrayList<Types.ACTIONS>(stateObs.getAvailableActions());
		availableActions.remove(Types.ACTIONS.ACTION_NIL);
		Collections.shuffle(availableActions);
		availableActions.add(0, Types.ACTIONS.ACTION_NIL); // so that ACTION_NIL goes always first
		StateObservation nextState;
		for (Types.ACTIONS action : availableActions)
		{
			nextState = stateObs.copy();
			nextState.advance(action);
			if (nextState.isAvatarAlive()
					&& (safeTurns == 1 || getNonDyingAction(nextState, safeTurns - 1) != null))
				return action;
		}
		return null;
	}
	
	public Types.ACTIONS getNonDyingAction(StateObservation stateObs)
	{
		ArrayList<Types.ACTIONS> availableActions = new ArrayList<Types.ACTIONS>(stateObs.getAvailableActions());
		availableActions.remove(Types.ACTIONS.ACTION_NIL);
		Collections.shuffle(availableActions);
		availableActions.add(0, Types.ACTIONS.ACTION_NIL); // so that ACTION_NIL goes always first
		StateObservation nextState;
		for (Types.ACTIONS action : availableActions)
		{
			nextState = stateObs.copy();
			nextState.advance(action);
			if (nextState.isAvatarAlive())
				return action;
		}
		return null;
	}
	
	public Types.ACTIONS getRandomNonDyingAction(StateObservation stateObs)
	{
		ArrayList<Types.ACTIONS> availableActions = new ArrayList<Types.ACTIONS>(stateObs.getAvailableActions());
		Collections.shuffle(availableActions);
		StateObservation nextState;
		for (Types.ACTIONS action : availableActions)
		{
			nextState = stateObs.copy();
			nextState.advance(action);
			if (nextState.isAvatarAlive())
				return action;
		}
		return null;
	}

	/**
	 * Returns an action which gives the best raw score (including reward for win and penalty for loss) for the player.
	 * 
	 * @return Action maximizing raw score.
	 */
	public Types.ACTIONS getGreedyAction(StateObservation stateObs)
	{
		ArrayList<Types.ACTIONS> availableActions = new ArrayList<Types.ACTIONS>(stateObs.getAvailableActions());
		StateObservation nextState;
		double bestScore = -10000000.0;
		Types.ACTIONS bestAction = Types.ACTIONS.ACTION_NIL;
		WinScoreStateEvaluator scoreEvaluator = new WinScoreStateEvaluator();
		for (Types.ACTIONS action : availableActions)
		{
			nextState = stateObs.copy();
			nextState.advance(action);
			double score = scoreEvaluator.evaluateState(nextState);
			if (score > bestScore)
			{
				bestScore = score;
				bestAction = action;
			}
		}
		return bestAction;
	}
	
	public Types.ACTIONS getRandomGreedyAction(StateObservation stateObs)
	{
		ArrayList<Types.ACTIONS> availableActions = new ArrayList<Types.ACTIONS>(stateObs.getAvailableActions());
		Collections.shuffle(availableActions);
		StateObservation nextState;
		double bestScore = -10000000.0;
		Types.ACTIONS bestAction = Types.ACTIONS.ACTION_NIL;
		WinScoreStateEvaluator scoreEvaluator = new WinScoreStateEvaluator();
		for (Types.ACTIONS action : availableActions)
		{
			nextState = stateObs.copy();
			nextState.advance(action);
			double score = scoreEvaluator.evaluateState(nextState);
			if (score > bestScore)
			{
				bestScore = score;
				bestAction = action;
			}
		}
		return bestAction;
	}
	
	/**
	 * Returns a random action.
	 * 
	 * @return Random action.
	 */
	public Types.ACTIONS getRandomAction(StateObservation stateObs)
	{
		ArrayList<Types.ACTIONS> availableActions = stateObs.getAvailableActions();
		
		return availableActions.get((new Random()).nextInt(availableActions.size()));
	}
}
