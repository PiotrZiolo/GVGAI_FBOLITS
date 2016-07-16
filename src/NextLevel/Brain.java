package NextLevel;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

import core.game.Observation;
import core.game.StateObservationMulti;
import core.game.Event;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import java.lang.Math;
import java.util.Random;

public class Brain
{
	private Memory memory;
	private Memory imaginationMemory;
	private int playerID;
	private int oppID;
	private int testingSpriteAttemptsLimit; // The maximal number of attempts to
											// test a sprite
	private int approachingSpriteLimit; // The maximal number of tries to
										// approach a sprite and make an action
										// on it
	private int approachingSpriteMovesLimit; // The maximal number of moves an
												// avatar can make to approach a
												// sprite
	private int numberOfAdvancesForANewTry; // The number of state advances to
											// make before trying chasing a
											// sprite again
	private ElapsedCpuTimer elapsedTimer; // Timer for the current operation;
	private boolean fastThinking;

	/**
	 * public constructor
	 */
    @SuppressWarnings("unchecked")
	public Brain() {
		this.playerID = 0;
		this.memory = new Memory();
		this.testingSpriteAttemptsLimit = 1;
		this.approachingSpriteLimit = 1;
		this.approachingSpriteMovesLimit = 50;
		this.numberOfAdvancesForANewTry = 5;
		this.fastThinking = false;
	}

	/**
	 * Public constructor.
	 * 
	 * @param playerID
	 *            ID of this agent.
	 */
    @SuppressWarnings("unchecked")
	public Brain(int playerID) {
		this.playerID = playerID;
		this.memory = new Memory();
		this.testingSpriteAttemptsLimit = 1;
		this.approachingSpriteLimit = 1;
		this.approachingSpriteMovesLimit = 50;
		this.numberOfAdvancesForANewTry = 5;
		this.fastThinking = false;
	}

	/**
	 * Returns the map of spriteTypeFeatures.
	 */
	public HashMap<Integer, SpriteTypeFeatures> getSpriteTypeFeaturesMap() {
		return memory.getSpriteTypeFeaturesMap();
	}

	/**
	 * Cleans imaginationMemory.
	 */
	public void cleanImaginationMemory() {
		imaginationMemory = null;
	}

	/**
	 * The main method to learn about all sprites' properties.
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 * @param justImagine
	 *            If set to true, all knowledge updates will be saved to
	 *            imaginationMemory.
	 * @param recursiveImplications
	 *            Whether to check recursive changes like e.g.
	 *            changingValuesOfOtherObjects or allowingVictory.
	 * @param range
	 *            0 - All types. 1 - Only new types. 2 - Only the category given
	 *            by category.
	 * @param category
	 *            Category to be evaluated.
	 */
    @SuppressWarnings("unchecked")
	public void learn(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, boolean justImagine,
			boolean recursiveImplications, int range, int category) {
		boolean productionVersion = true;
		
		this.oppID = (playerID + 1) % stateObs.getNoPlayers();

		if (elapsedTimer == null) {
			elapsedTimer = this.elapsedTimer;
			
			if (elapsedTimer.remainingTimeMillis() < 100)
			{
				this.fastThinking = true;
			}
		}
		else
		{
			this.elapsedTimer = elapsedTimer;
		}
		
		if (justImagine)
		{
			imaginationMemory = memory;
			memory = memory.copy();
		}

		this.oppID = (playerID + 1) % stateObs.getNoPlayers();

		Vector2d avatarPosition = stateObs.getAvatarPosition(playerID);
		ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions(avatarPosition);
		ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions(avatarPosition);
		ArrayList<Observation>[] immovablePositions = stateObs.getImmovablePositions(avatarPosition);
		ArrayList<Observation>[] movablePositions = stateObs.getMovablePositions(avatarPosition);
		ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions(avatarPosition);
		ArrayList<Observation>[] fromAvatarSpritesPositions = stateObs.getFromAvatarSpritesPositions(avatarPosition);

		ArrayList<ArrayList<Observation>[]> arraysOfSprites = new ArrayList<ArrayList<Observation>[]>();
		arraysOfSprites.add(portalPositions);
		arraysOfSprites.add(npcPositions);
		arraysOfSprites.add(immovablePositions);
		arraysOfSprites.add(movablePositions);
		arraysOfSprites.add(resourcesPositions);
		arraysOfSprites.add(fromAvatarSpritesPositions);

		if (!justImagine)
		{
			//System.out.println("----------");
			//System.out.println(">>> Game turn: " + stateObs.getGameTick());
		}

		for (ArrayList<Observation>[] positions : arraysOfSprites)
		{
			if (positions != null)
			{
				for (ArrayList<Observation> observations : positions)
				{
					if (observations.size() > 0)
					{
						//if (!justImagine)
						//{
							//System.out.println("----------");
							//System.out.println("Checking sprite of type: " + observations.get(0).itype
							//		+ " and category: " + observations.get(0).category);
						//}

						if (range == 0 || range == 1 || observations.get(0).category == category)
						{
							if (range == 0 || range == 2
									|| memory.getSpriteTypeFeaturesByType(observations.get(0).itype) == null)
							{
								SpriteTypeFeatures spriteTypeFeatures;
								if (productionVersion)
								{
									//if (!justImagine)
									//{
										//System.out.println("Testing sprite of type: " + observations.get(0).itype
										//		+ " and category: " + observations.get(0).category);
										//System.out.println("Before action: " + elapsedTimer.remainingTimeMillis());
									//}
									spriteTypeFeatures = testSprite(stateObs, observations.get(0),
											recursiveImplications);
									//if (!justImagine)
									//{
										//System.out.println("After action: " + elapsedTimer.remainingTimeMillis());
										//spriteTypeFeatures.print();
										//System.out.println("----------");
									//}
								}
								else
								{
									spriteTypeFeatures = getSpriteTypeFeaturesForCategory(observations.get(0).category,
											observations.get(0).itype);
								}

								if (spriteTypeFeatures != null)
								{
									memory.setSpriteTypeFeaturesByType(observations.get(0).itype, spriteTypeFeatures);
								}
							}
						}
					}
				}
			}
		}

		//if (!justImagine)
		//{
			//System.out.println("----------");
			//System.out.println("Checking sprite of type: " + (-1 - oppID) + " and category: " + 0);
		//}
		if (range == 0 || (range == 2 && category == 0))
		{
			if (productionVersion)
			{
				if (stateObs.getNoPlayers() > 1)
				{
					//if (!justImagine)
					//{
						//System.out.println("Testing sprite of type: " + (-1 - oppID) + " and category: " + 0);
						//System.out.println("Before action: " + elapsedTimer.remainingTimeMillis());
					//}
					SpriteTypeFeatures spriteTypeFeatures = testOtherPlayer(stateObs, oppID, recursiveImplications);
					//if (!justImagine)
					//{
						//System.out.println("After action: " + elapsedTimer.remainingTimeMillis());
						//spriteTypeFeatures.print();
						//System.out.println("----------");
					//}

					if (spriteTypeFeatures != null)
					{
						// Opponents' features are saved with their ID negative
						// as type
						memory.setSpriteTypeFeaturesByType(-1 - oppID, spriteTypeFeatures);
					}
				}
			}
		}

		if (justImagine)
		{
			Memory temporaryMemory = imaginationMemory;
			imaginationMemory = memory;
			memory = temporaryMemory;
		}
	}

	/**
	 * Overwritten method learn without specifying category.
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 * @param justImagine
	 *            If set to true, all knowledge updates will be saved to
	 *            imaginationMemory.
	 * @param recursiveImplications
	 *            Whether to check recursive changes like e.g.
	 *            changingValuesOfOtherObjects or allowingVictory.
	 * @param range
	 *            0 - All types. 1 - Only new types.
	 */
	public void learn(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, boolean justImagine,
			boolean recursiveImplications, int range)
	{
		learn(stateObs, elapsedTimer, justImagine, recursiveImplications, range, 0);
	}

	/**
	 * Updates the knowledge about sprites when a new Event have appeared in the
	 * history.
	 * 
	 * @param stateObsJustBeforeAction
	 *            Observation of the state just before the Event.
	 * @param stateObsAfterAction
	 *            Observation of the state just after the Event.
	 */
	public void updateKnowledgeAfterEvent(StateObservationMulti stateObsJustBeforeAction,
			StateObservationMulti stateObsJustAfterAction)
	{
		// Check events history and update the knowledge

		// !!!!!!!!!!!!
		// To be filled
		// !!!!!!!!!!!!
	}

	/**
	 * Dumps all acquired knowledge into a file.
	 */
	public void rememberAllInLongTermMemory()
	{
		// !!!!!!!!!!!!
		// To be filled
		// !!!!!!!!!!!!
	}

	/**
	 * Retrieves knowledge saved previously to a file.
	 */
	public void initializeFromLongTermMemory()
	{
		// !!!!!!!!!!!!
		// To be filled
		// !!!!!!!!!!!!
	}

	/**
	 * Updates the knowledge about different sprites if new Events have appeared
	 * in the history.
	 * 
	 * @param stateObs
	 *            Observation of the initial state from which testing is to be
	 *            done.
	 * @param observation
	 *            Observation of the sprite to test.
	 * @param recursiveImplications
	 *            Whether to check recursive changes like e.g.
	 *            changingValuesOfOtherObjects or allowingVictory.
	 */
	private SpriteTypeFeatures testSprite(StateObservationMulti stateObs, Observation observation,
			boolean recursiveImplications)
	{
		SpriteTypeFeatures spriteTypeFeatures = null;
		stateObs = stateObs.copy();

		spriteTypeFeatures = memory.getSpriteTypeFeaturesByType(observation.itype);

		if (spriteTypeFeatures == null)
		{
			spriteTypeFeatures = getSpriteTypeFeaturesForCategory(observation.category, observation.itype);
		}

		boolean movingOntoTested = false;
		boolean useTested = false;
		boolean otherPlayerUseTested = false; // Indicates testing the action
												// USE of the other player on
												// our player
		boolean isSpriteNonPlayer = (observation.category == 0) ? false : true;
		int attemptCount = 0;

		while (attemptCount < this.testingSpriteAttemptsLimit
				&& (!movingOntoTested || !useTested || (!isSpriteNonPlayer && !otherPlayerUseTested)))
		{
			StateObservationMulti stateObsApproached = approachSprite(stateObs, observation);
			//if (stateObsApproached == null)
				//System.out.println("Approach failed");

			if (stateObsApproached != null)
			{

				// Test going onto sprite
				if (!movingOntoTested)
				{
					StateObservationMulti[] actionStates = makeActionOnSprite(stateObsApproached, observation, 1);

					if (actionStates != null)
					{
						spriteTypeFeatures = updateKnowledgeAfterActionOnSprite(spriteTypeFeatures, actionStates[0],
								actionStates[1], observation, 1, recursiveImplications);
						movingOntoTested = true;
					}
				}

				// Test making USE on sprite
				if (!useTested)
				{
					StateObservationMulti[] actionStates = makeActionOnSprite(stateObsApproached, observation, 0);

					if (actionStates != null)
					{
						spriteTypeFeatures = updateKnowledgeAfterActionOnSprite(spriteTypeFeatures, actionStates[0],
								actionStates[1], observation, 0, recursiveImplications);
						useTested = true;
					}
				}

				// Test the action USE of the other player on our player
				if (!isSpriteNonPlayer && !otherPlayerUseTested)
				{
					StateObservationMulti[] actionStates = makeActionOnSprite(stateObsApproached, observation, 2);

					if (actionStates != null)
					{
						spriteTypeFeatures = updateKnowledgeAfterActionOnSprite(spriteTypeFeatures, actionStates[0],
								actionStates[1], observation, 2, recursiveImplications);
						useTested = true;
					}
				}
			}

			attemptCount++;

			// Advance the state a couple of times to try to test the sprite in
			// a different situation if any of the tests did not succeed
			if (!movingOntoTested || !useTested || (!isSpriteNonPlayer && !otherPlayerUseTested))
			{
				for (int advancesCount = 0; advancesCount < this.numberOfAdvancesForANewTry; advancesCount++)
				{
					stateObs.advance(new Types.ACTIONS[] { Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL });
				}
			}
		}

		return spriteTypeFeatures;
	}

	/**
	 * Updates the knowledge about different sprites if new Events have appeared
	 * in the history.
	 * 
	 * @param stateObs
	 *            Observation of the initial state from which testing is to be
	 *            done.
	 * @param observation
	 *            Observation of the sprite to test.
	 * @param recursiveImplications
	 *            Whether to check recursive changes like e.g.
	 *            changingValuesOfOtherObjects or allowingVictory.
	 */
	private SpriteTypeFeatures testOtherPlayer(StateObservationMulti stateObs, int oppID, boolean recursiveImplications)
	{
		// Create an artificial observation for the opponent to use the same
		// code as for sprites.
		// Type and ID are assumed to be the negative of oppID, category is set
		// to 0.
		Observation opponentObservation = new Observation(-1 - oppID, -1 - oppID, stateObs.getAvatarPosition(oppID),
				stateObs.getAvatarPosition(playerID), 0);

		return testSprite(stateObs, opponentObservation, recursiveImplications);
	}

	/**
	 * Tries to approach the sprite given in observation. Returns a state in
	 * which the avatar is one step from the sprite and in the direction of the
	 * sprite. If it was impossible in a certain number of tries, null is
	 * returned.
	 * 
	 * @param stateObs
	 *            Observation of the initial state from which testing is to be
	 *            done.
	 * @param observation
	 *            Observation of the sprite to approach.
	 */
	 
    @SuppressWarnings("unchecked")
	public StateObservationMulti approachSprite(StateObservationMulti stateObs, Observation observation)
	{
		int pathFinderLimit = (fastThinking) ? 1 : 10;
		if (!stateObs.isAvatarAlive(oppID))
		{
			//System.out.println("Opponent died.");
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
		if (observationPosition == playerPreviousPosition) {
			//System.out.println("Object is in the same place as player");
			return null;
		}
		int[] blockWhereObservationWasLastSeen = { (int) (observationPosition.x / stateObs.getBlockSize()),
				(int) (observationPosition.y / stateObs.getBlockSize()) };
		
		// in this while avatar is trying to minimize distance to goal
		// System.out.println("playerPreviousPosition = " +
		// playerPreviousPosition);
		// System.out.println("observationPosition = " + observationPosition);
		// System.out.println("playerID = " + playerID);
		while (true)
		{

			// finding object position - first in the same place as last time,
			// than in the neighborhood
			if (observation.itype == -1 - oppID)
				observationPosition = currentState.getAvatarPosition(oppID);
			else
				observationPosition = FindObject(blockWhereObservationWasLastSeen, currentState, observation.obsID);
			if (observationPosition == null)
			{
				//System.out.println("Object was lost.");
				return null;
			}

			// System.out.println(observationPosition + " " +
			// playerPreviousPosition + " " + playerPreviousOrientation + " " +
			// observation.itype);
			// check whether avatar reached the object and return opponent if he
			// is the object.
			if (isSpriteOneMoveFromAvatarWithOpponentRotation(observationPosition, playerPreviousPosition, currentState,
					playerPreviousOrientation, observation.itype))
			{
				//System.out.println("Standard approach successful.");

				if (currentState.isGameOver())
				{
					//System.out.println("Opp died when rotating.");
					return null;
				}
				return currentState;
			}

			// if opponent always die finish return null
			if (opponentGoodActions.isEmpty())
			{
				//System.out.println("Opp is dead.");
				return null;
			}

			// choose actions for players
			Types.ACTIONS[] actions = new Types.ACTIONS[2];
			if (opponentGoodActions.contains(Types.ACTIONS.ACTION_NIL))
				actions[oppID] = Types.ACTIONS.ACTION_NIL;
			else
				actions[oppID] = opponentGoodActions.get(new Random().nextInt(opponentGoodActions.size()));

			// System.out.println("playerGoodActions = " +
			// playerGoodActions.toString());
			actions[playerID] = chooseDirection(observationPosition.copy(), playerPreviousPosition, playerGoodActions,
					playerLastAction);
			temporaryState = currentState.copy();

			// if player don't want to move go to BFS
			if (actions[playerID] == null)
				break;

			// advance
			//
			// System.out.println("goalPosition = " + observationPosition);
			// System.out.println("actions = " + actions[playerID].toString());
			// System.out.println("avatarPositionB = " +
			// temporaryState.getAvatarPosition(playerID));
			if (advanceLimit == 0)
			{
				//System.out.println("advanceLimit reached.");
				return null;
			}
			temporaryState.advance(actions);
			advanceLimit--;
			// System.out.println("avatarPositionA = " +
			// temporaryState.getAvatarPosition(playerID));

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

		// in this while avatar is trying to go along the shortest path to goal
		// using BFS
		for (int paths=0; paths<pathFinderLimit; paths++)
		{
			PathFinder pathFinder = new PathFinder();
			
			//System.out.println("playerPreviousPosition = " + playerPreviousPosition);
			//System.out.println("observationPosition = " + observationPosition);
			//System.out.println("playerPreviousPosition = " + currentState.getAvatarPosition(playerID));
			//System.out.println("playerID = " + playerID);
			
			Deque<Types.ACTIONS> playerMoveSequenceToGoal = pathFinder.pathFinder(playerPreviousPosition,
					observationPosition, currentState, playerID);
			//System.out.println("After pathfinding: " + elapsedTimer.remainingTimeMillis());

			Iterator<Types.ACTIONS> iterator = playerMoveSequenceToGoal.descendingIterator();
			Types.ACTIONS forceMove = null;

			iterator = playerMoveSequenceToGoal.descendingIterator();
			// System.out.println("BFS started");
			while (iterator.hasNext())
			{
				// finding object position - first in the same place as last
				// time, than in the neighborhood
				if (observation.itype == -1 - oppID)
					observationPosition = currentState.getAvatarPosition(oppID);
				else
					observationPosition = FindObject(blockWhereObservationWasLastSeen, currentState, observation.obsID);
				if (observationPosition == null)
				{
					//System.out.println("Object was lost.");
					return null;
				}

				// check whether avatar reached the object and return opponent
				// if he is the object.
				if (isSpriteOneMoveFromAvatarWithOpponentRotation(observationPosition, playerPreviousPosition,
						currentState, playerPreviousOrientation, observation.itype))
				{
					//System.out.println("Advanced approach successful.");
					if (currentState.isGameOver())
					{
						//System.out.println("Opponent died when turning.");
						return null;
					}
					return currentState;
				}

				// if opponent always die finish return null
				if (opponentGoodActions.isEmpty())
				{
					//System.out.println("Opponent died.");
					return null;
				}

				// choose actions for players
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				if (opponentGoodActions.contains(Types.ACTIONS.ACTION_NIL))
					actions[oppID] = Types.ACTIONS.ACTION_NIL;
				else
					actions[oppID] = opponentGoodActions.get(new Random().nextInt(opponentGoodActions.size()));

				// System.out.println("playerGoodActions = " +
				// playerGoodActions.toString());
				if (forceMove == null)
				{
					actions[playerID] = iterator.next();
					if (actions[playerID] == Types.ACTIONS.ACTION_NIL) {
						//System.out.println("PathFinder failed to find the path.");
						return null;
					}
				}
				else
					actions[playerID] = forceMove;
				temporaryState = currentState.copy();

				// advance
				// System.out.println("avatarPosition = " +
				// temporaryState.getAvatarPosition(playerID));
				// System.out.println("goalPosition = " + observationPosition);
				// System.out.println("actions = " +
				// actions[playerID].toString());
				// System.out.println("avatarPositionB = " +
				// temporaryState.getAvatarPosition(playerID));
				if (advanceLimit == 0)
				{
					//System.out.println("AdvancedLimit reached.");
					return null;
				}
				temporaryState.advance(actions);
				advanceLimit--;
				// System.out.println("avatarPositionA = " +
				// temporaryState.getAvatarPosition(playerID));
				// System.out.println("avatarPosition2 = " +
				// temporaryState.getAvatarPosition(playerID));

				// check whether no one died
				boolean goodMove = true;
				if (!temporaryState.isAvatarAlive(playerID))
				{
					//System.out.println("Player killed.");
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

	private boolean isSpriteOneMoveFromAvatarWithOpponentRotation(Vector2d observationPosition, Vector2d avatarPosition,
			StateObservationMulti currentState, Vector2d avatarOrientation, int spriteType)
	{

		double speedInPixels = currentState.getBlockSize() * currentState.getAvatarSpeed(playerID);
		Vector2d distance = observationPosition.copy().subtract(avatarPosition);

		if ((Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_NIL
				|| Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_DOWN)
				&& Math.abs(distance.x) < currentState.getBlockSize()
				&& Math.abs(distance.y - speedInPixels) < currentState.getBlockSize())
		{
			if (Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_NIL && spriteType == -1 - oppID
					&& Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_UP)
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[oppID] = Types.ACTIONS.ACTION_UP;
				currentState.advance(actions);
			}
			return true;
		}
		if ((Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_NIL
				|| Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_UP)
				&& Math.abs(distance.x) < currentState.getBlockSize()
				&& Math.abs(distance.y + speedInPixels) < currentState.getBlockSize())
		{
			if (Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_NIL && spriteType == -1 - oppID
					&& Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_DOWN)
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[oppID] = Types.ACTIONS.ACTION_DOWN;
				currentState.advance(actions);
			}
			return true;
		}
		if ((Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_NIL
				|| Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_RIGHT)
				&& Math.abs(distance.x - speedInPixels) < currentState.getBlockSize()
				&& Math.abs(distance.y) < currentState.getBlockSize())
		{
			if (Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_NIL && spriteType == -1 - oppID
					&& Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_LEFT)
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[oppID] = Types.ACTIONS.ACTION_LEFT;
				currentState.advance(actions);
			}
			return true;
		}
		if ((Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_NIL
				|| Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_LEFT)
				&& Math.abs(distance.x + speedInPixels) < currentState.getBlockSize()
				&& Math.abs(distance.y) < currentState.getBlockSize())
		{
			if (Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_NIL && spriteType == -1 - oppID
					&& Types.ACTIONS.fromVector(currentState.getAvatarOrientation(oppID)) != Types.ACTIONS.ACTION_RIGHT)
			{
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[oppID] = Types.ACTIONS.ACTION_RIGHT;
				currentState.advance(actions);
			}
			return true;
		}
		return false;
	}

    @SuppressWarnings("unchecked")
	private Vector2d FindObject(int[] blockWhereObservationWasLastSeen, StateObservationMulti stateObs, int searchedID)
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
					suspects = stateObs.getObservationGrid()
							[(worldWidth + blockWhereObservationWasLastSeen[0] + i) % worldWidth]
							[(worldHeight + blockWhereObservationWasLastSeen[1] + j) % worldHeight];
					for (Observation suspect : suspects)
					{
						if (suspect.obsID == searchedID)
						{
							blockWhereObservationWasLastSeen[0] = (worldWidth + blockWhereObservationWasLastSeen[0] + i) % worldWidth;
							blockWhereObservationWasLastSeen[1] = (worldHeight + blockWhereObservationWasLastSeen[1] + j) % worldHeight;
							return suspect.position;
						}
					}
				}
			}
		}
		return null;
	}

    @SuppressWarnings("unchecked")
	private Types.ACTIONS chooseDirection(Vector2d observationPosition, Vector2d playerNewPosition,
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
	 * Tries to make action on the sprite given in observation. Returns states
	 * just before the action and just after. If it was impossible in a certain
	 * number of tries, null is returned.
	 * 
	 * @param stateObs
	 *            Observation of the initial state from which testing is to be
	 *            done.
	 * @param observation
	 *            Observation of the sprite to approach.
	 * @param actionType
	 *            Type of action: 0 - use, 1 - move onto, 2 - actions of the
	 *            other player on our player.
	 */
	private StateObservationMulti[] makeActionOnSprite(StateObservationMulti stateObs, Observation observation,
			int actionType)
	{
		StateObservationMulti baseState = stateObs.copy();
		StateObservationMulti stateObsJustBeforeAction = stateObs.copy();
		StateObservationMulti stateObsJustAfterAction = stateObsJustBeforeAction.copy();

		int triesCount = 0;
		boolean succeeded = false;

		while (triesCount < this.approachingSpriteLimit && !succeeded)
		{
			if (stateObsJustBeforeAction != null)
			{
				// Try to make the action

				switch (actionType)
				{
					// Use
					case 0:
						stateObsJustAfterAction.advance(new Types.ACTIONS[] {
								(playerID == 0) ? Types.ACTIONS.ACTION_USE : Types.ACTIONS.ACTION_NIL,
								(playerID == 1) ? Types.ACTIONS.ACTION_USE : Types.ACTIONS.ACTION_NIL });

						break;

					// Move onto
					case 1:
						Vector2d orientation = stateObsJustBeforeAction.getAvatarOrientation(playerID);
						Types.ACTIONS action = Types.ACTIONS.ACTION_NIL;

						if (orientation.x == 1)
						{
							action = Types.ACTIONS.ACTION_RIGHT;
						}
						else if (orientation.x == -1)
						{
							action = Types.ACTIONS.ACTION_LEFT;
						}
						else if (orientation.y == 1)
						{
							action = Types.ACTIONS.ACTION_DOWN;
						}
						else if (orientation.y == -1)
						{
							action = Types.ACTIONS.ACTION_UP;
						}
						else
						{
							Vector2d spritePosition = stateObsJustBeforeAction.getAvatarPosition(playerID).copy()
									.subtract(observation.position);

							if (Math.abs(spritePosition.x) > Math.abs(spritePosition.y))
							{
								if (spritePosition.x > 0)
								{
									action = Types.ACTIONS.ACTION_LEFT;
								}
								else
								{
									action = Types.ACTIONS.ACTION_RIGHT;
								}
							}
							else
							{
								if (spritePosition.y > 0)
								{
									action = Types.ACTIONS.ACTION_UP;
								}
								else
								{
									action = Types.ACTIONS.ACTION_DOWN;
								}
							}
						}

						stateObsJustAfterAction
								.advance(new Types.ACTIONS[] { (playerID == 0) ? action : Types.ACTIONS.ACTION_NIL,
										(playerID == 1) ? action : Types.ACTIONS.ACTION_NIL });

						// System.out.println("Action: " + action);

						break;

					// Action of the other player on our player
					case 2:
						stateObsJustAfterAction.advance(new Types.ACTIONS[] {
								(playerID == 0) ? Types.ACTIONS.ACTION_NIL : Types.ACTIONS.ACTION_USE,
								(playerID == 1) ? Types.ACTIONS.ACTION_NIL : Types.ACTIONS.ACTION_USE });
						break;
				}

				/*
				 * Assess if succeeded There are two conditions for success: - a
				 * new event with the avatar and the sprite, - not changed
				 * position of the sprite.
				 */

				// System.out.println("Testing if action succeeded");

				switch (actionType)
				{
					// Use
					case 0:
						// Do the same as in the case of move

						// Move onto
					case 1:
						// Localize the sprite after action
						Vector2d spriteCurrentPosition = localizeSprite(stateObsJustAfterAction, observation, 5);

						if (spriteCurrentPosition == null)
						{
							succeeded = true;
							// System.out.println("Succeeded");
						}
						else if (spriteCurrentPosition != null && isSpriteOneMoveFromAvatar(stateObsJustAfterAction,
								stateObsJustBeforeAction.getAvatarPosition(playerID),
								stateObsJustBeforeAction.getAvatarOrientation(playerID), spriteCurrentPosition))
						{
							succeeded = true;
							// System.out.println("Succeeded");
						}
						else
						{
							// System.out.println("Not succeeded");
						}

						break;

					/*
					 * Action of the other player on our player. Must succeed
					 * because we control both objects (accidentally some other
					 * object can kill our avatar in this very moment, but this
					 * case will be treated in future versions)
					 */
					case 2:
						succeeded = true;
						break;
				}

				// If not succeeded, approach and try again
				if (!succeeded && triesCount < this.approachingSpriteLimit - 1)
				{
					stateObsJustBeforeAction = chaseSprite(stateObsJustBeforeAction, observation);
					if (stateObsJustBeforeAction != null)
					{
						stateObsJustAfterAction = stateObsJustBeforeAction.copy();
					}
					else
					{
						stateObsJustAfterAction = null;
					}
				}
			}
			else
			{
				for (int advancesCount = 0; advancesCount < numberOfAdvancesForANewTry; advancesCount++)
				{
					baseState.advance(new Types.ACTIONS[] { Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL });
				}

				stateObsJustBeforeAction = chaseSprite(baseState, observation);
				if (stateObsJustBeforeAction != null)
				{
					stateObsJustAfterAction = stateObsJustBeforeAction.copy();
				}
				else
				{
					stateObsJustAfterAction = null;
				}
			}

			triesCount++;
		}

		if (succeeded)
		{
			return new StateObservationMulti[] { stateObsJustBeforeAction, stateObsJustAfterAction };
		}
		else
		{
			return null;
		}
	}

	/**
	 * Follows the sprite.
	 * 
	 * @param stateObs
	 *            Observation of the initial state from which testing is to be
	 *            done.
	 * @param observation
	 *            Observation of the sprite to approach.
	 */
	private StateObservationMulti chaseSprite(StateObservationMulti stateObs, Observation observation)
	{
		StateObservationMulti stateObsApproached = approachSprite(stateObs, observation);
		//if (stateObsApproached == null)
		//	System.out.println("Approach failed");
		return stateObsApproached;
	}

	/**
	 * Evaluates the results of an action on a given sprite based on the states
	 * just before and just after the action. Returns features of the tested
	 * sprite.
	 * 
	 * @param currentSpriteTypeFeatures
	 *            Current knowledge about the sprite.
	 * @param stateObsJustBeforeAction
	 *            Observation of the state just before action.
	 * @param stateObsAfterAction
	 *            Observation of the state just after action.
	 * @param observation
	 *            Observation on which the action was made.
	 * @param actionType
	 *            Type of action: 0 - use, 1 - move onto, 2 - actions of the
	 *            other player on our player.
	 * @param recursiveImplications
	 *            Whether to check recursive changes like e.g.
	 *            changingValuesOfOtherObjects or allowingVictory.
	 */
	private SpriteTypeFeatures updateKnowledgeAfterActionOnSprite(SpriteTypeFeatures currentSpriteTypeFeatures,
			StateObservationMulti stateObsJustBeforeAction, StateObservationMulti stateObsJustAfterAction,
			Observation observation, int actionType, boolean recursiveImplications)
	{
		// Get the event if something happened

		TreeSet<Event> eventsHistory = stateObsJustAfterAction.getEventsHistory();
		Iterator<Event> eventsIterator = eventsHistory.descendingIterator();
		Event event = null;
		boolean eventHappened = false;

		while (!eventHappened && eventsIterator.hasNext())
		{
			event = eventsIterator.next();

			if (event.gameStep == stateObsJustAfterAction.getGameTick() - 1)
			{
				eventHappened = true;
				//System.out.println("Event happened");
			}
		}

		if (!eventHappened)
		{
			event = null;
			//System.out.println("Event not happened");
		}

		// Process different types of situations

		switch (actionType)
		{
			// Use
			case 0:
				if (eventHappened)
				{
					// Treat the other player and sprites differently
					if (observation.obsID == -1 - oppID)
					{
						if (stateObsJustAfterAction.isAvatarAlive(oppID) && (stateObsJustAfterAction
								.getAvatarHealthPoints(oppID) == stateObsJustBeforeAction.getAvatarHealthPoints(oppID)))
						{
							// currentSpriteTypeFeatures.destroyable = false;
						}
						else
						{
							currentSpriteTypeFeatures.destroyable = true;
						}
					}
					else
					{
						Vector2d spriteCurrentPosition = localizeSprite(stateObsJustAfterAction, observation, 5);
						if (spriteCurrentPosition != null)
						{
							currentSpriteTypeFeatures.destroyable = false;
						}
						else
						{
							currentSpriteTypeFeatures.destroyable = true;
						}
					}

					if (stateObsJustAfterAction.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS)
					{
						currentSpriteTypeFeatures.givingVictory = true;
					}

					if (stateObsJustAfterAction.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES)
					{
						currentSpriteTypeFeatures.givingDefeat = true;
					}

					currentSpriteTypeFeatures.changingPoints = stateObsJustAfterAction.getGameScore(playerID)
							- stateObsJustBeforeAction.getGameScore(playerID);

					if (recursiveImplications)
					{
						// changingValuesOfOtherObjects and allowingVictory:
						// Looking for changes in changingPoints and
						// allowingVictory

						currentSpriteTypeFeatures = processImplicationsOfActionOnOtherSprites(currentSpriteTypeFeatures,
								stateObsJustBeforeAction, stateObsJustAfterAction, observation);
					}
				}
				else
				{
					currentSpriteTypeFeatures.destroyable = false;
				}

				break;

			// Move onto
			case 1:
				if (eventHappened)
				{
					currentSpriteTypeFeatures.dangerousToAvatar = stateObsJustBeforeAction
							.getAvatarHealthPoints(playerID) - stateObsJustAfterAction.getAvatarHealthPoints(playerID);

					if (!stateObsJustAfterAction.isAvatarAlive(playerID))
					{
						if (stateObsJustBeforeAction.getAvatarMaxHealthPoints(playerID) != 0)
						{
							currentSpriteTypeFeatures.dangerousToAvatar = stateObsJustBeforeAction
									.getAvatarMaxHealthPoints(playerID);
						}
						else
						{
							currentSpriteTypeFeatures.dangerousToAvatar = 100;
						}
					}

					// Treat the other player and sprites differently
					if (observation.obsID == -1 - oppID)
					{
						if (stateObsJustAfterAction.isAvatarAlive(oppID) && (stateObsJustAfterAction
								.getAvatarHealthPoints(oppID) == stateObsJustBeforeAction.getAvatarHealthPoints(oppID)))
						{
							// currentSpriteTypeFeatures.destroyable = false;
						}
						else
						{
							currentSpriteTypeFeatures.destroyable = true;
						}
					}
					else
					{
						Vector2d spriteCurrentPosition = localizeSprite(stateObsJustAfterAction, observation, 5);
						if (spriteCurrentPosition != null)
						{
							currentSpriteTypeFeatures.destroyable = false;
						}
						else
						{
							currentSpriteTypeFeatures.destroyable = true;
						}
					}

					// Check if avatar is on the same position as the sprite
					// Treat the other player and sprites differently
					if (observation.obsID == -1 - oppID)
					{
						// We control the other avatar in the simulation, hence
						// it's in the same place
						if (stateObsJustAfterAction.getAvatarPosition(playerID).x == stateObsJustAfterAction
								.getAvatarPosition(oppID).x
								&& stateObsJustAfterAction.getAvatarPosition(playerID).y == stateObsJustAfterAction
										.getAvatarPosition(oppID).y)
						{
							currentSpriteTypeFeatures.passable = true;
							//System.out.println("Passable");
						}
						else
						{
							currentSpriteTypeFeatures.passable = false;
							//System.out.println("Not passable");
						}
					}
					else
					{
						Vector2d spriteCurrentPosition = localizeSprite(stateObsJustAfterAction, observation, 3);
						if (spriteCurrentPosition != null)
						{
							Vector2d distance = stateObsJustAfterAction.getAvatarPosition(playerID).copy()
									.subtract(spriteCurrentPosition);

							if (Math.abs(distance.x) < stateObsJustAfterAction.getBlockSize()
									&& Math.abs(distance.y) < stateObsJustAfterAction.getBlockSize())
							{
								currentSpriteTypeFeatures.passable = true;
								//System.out.println("Passable");
							}
							else
							{
								currentSpriteTypeFeatures.passable = false;
								//System.out.println("Not passable");
							}
						}
						else
						{
							currentSpriteTypeFeatures.collectable = true;
							currentSpriteTypeFeatures.passable = true;
							//System.out.println("Collectable");
							//System.out.println("Passable");
						}
					}

					if (stateObsJustAfterAction.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS)
					{
						currentSpriteTypeFeatures.givingVictory = true;
					}

					if (stateObsJustAfterAction.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES)
					{
						currentSpriteTypeFeatures.givingDefeat = true;
					}

					currentSpriteTypeFeatures.changingPoints = stateObsJustAfterAction.getGameScore(playerID)
							- stateObsJustBeforeAction.getGameScore(playerID);

					//System.out.println("Points: " + currentSpriteTypeFeatures.changingPoints);

					if (recursiveImplications)
					{
						// changingValuesOfOtherObjects and allowingVictory:
						// Looking for changes in changingPoints and
						// allowingVictory

						currentSpriteTypeFeatures = processImplicationsOfActionOnOtherSprites(currentSpriteTypeFeatures,
								stateObsJustBeforeAction, stateObsJustAfterAction, observation);
					}
				}
				else
				{
					currentSpriteTypeFeatures.destroyable = false;

					// Check if avatar is on the same position as the sprite
					// Treat the other player and sprites differently
					if (observation.obsID == -1 - oppID)
					{
						// We control the other avatar in the simulation, hence
						// it's in the same place
						if (stateObsJustAfterAction.getAvatarPosition(playerID).x == stateObsJustAfterAction
								.getAvatarPosition(oppID).x
								&& stateObsJustAfterAction.getAvatarPosition(playerID).y == stateObsJustAfterAction
										.getAvatarPosition(oppID).y)
						{
							currentSpriteTypeFeatures.passable = true;
							//System.out.println("Passable");
						}
						else
						{
							currentSpriteTypeFeatures.passable = false;
							//System.out.println("Not passable");
						}
					}
					else
					{
						currentSpriteTypeFeatures.collectable = false;
						//System.out.println("Not collectable");

						Vector2d spriteCurrentPosition = localizeSprite(stateObsJustAfterAction, observation, 3);
						if (spriteCurrentPosition != null)
						{
							Vector2d distance = stateObsJustAfterAction.getAvatarPosition(playerID).copy()
									.subtract(spriteCurrentPosition);

							if (Math.abs(distance.x) < stateObsJustAfterAction.getBlockSize()
									&& Math.abs(distance.y) < stateObsJustAfterAction.getBlockSize())
							{
								currentSpriteTypeFeatures.passable = true;
								//System.out.println("Passable");
							}
							else
							{
								currentSpriteTypeFeatures.passable = false;
								//System.out.println("Not passable");
							}
						}
						else
						{
							//System.out.println("Object not found");
						}
					}
				}

				break;

			// Action of the other player on our player
			case 2:
				if (eventHappened)
				{
					currentSpriteTypeFeatures.dangerousToAvatar = stateObsJustBeforeAction
							.getAvatarHealthPoints(playerID) - stateObsJustAfterAction.getAvatarHealthPoints(playerID);

					if (!stateObsJustAfterAction.isAvatarAlive(playerID))
					{
						if (stateObsJustBeforeAction.getAvatarMaxHealthPoints(playerID) != 0)
						{
							currentSpriteTypeFeatures.dangerousToAvatar = stateObsJustBeforeAction
									.getAvatarMaxHealthPoints(playerID);
						}
						else
						{
							currentSpriteTypeFeatures.dangerousToAvatar = 100;
						}
					}

					if (stateObsJustAfterAction.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS)
					{
						currentSpriteTypeFeatures.givingVictory = true;
					}

					if (stateObsJustAfterAction.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES)
					{
						currentSpriteTypeFeatures.givingDefeat = true;
					}

					currentSpriteTypeFeatures.changingPoints = stateObsJustAfterAction.getGameScore(playerID)
							- stateObsJustBeforeAction.getGameScore(playerID);

					if (recursiveImplications)
					{
						// changingValuesOfOtherObjects and allowingVictory:
						// Looking for changes in changingPoints and
						// allowingVictory

						currentSpriteTypeFeatures = processImplicationsOfActionOnOtherSprites(currentSpriteTypeFeatures,
								stateObsJustBeforeAction, stateObsJustAfterAction, observation);
					}
				}
				else
				{
					currentSpriteTypeFeatures.dangerousToAvatar = 0;
					currentSpriteTypeFeatures.givingDefeat = false;
				}

				break;
		}

		return currentSpriteTypeFeatures;
	}

	/**
	 * Checks for changes in features between states stateObsJustBeforeAction
	 * and stateObsJustAfterAction.
	 * 
	 * @param currentSpriteTypeFeatures
	 *            Current knowledge about the sprite.
	 * @param stateObsJustBeforeAction
	 *            Observation of the state just before action.
	 * @param stateObsAfterAction
	 *            Observation of the state just after action.
	 * @param observation
	 *            Observation on which the action was made.
	 */
	private SpriteTypeFeatures processImplicationsOfActionOnOtherSprites(SpriteTypeFeatures currentSpriteTypeFeatures,
			StateObservationMulti stateObsJustBeforeAction, StateObservationMulti stateObsJustAfterAction,
			Observation observation)
	{
		learn(stateObsJustAfterAction, null, true, false, 2, 2);
		currentSpriteTypeFeatures.changingValuesOfOtherObjects = 0;
		currentSpriteTypeFeatures.allowingVictory = false;

		HashMap<Integer, SpriteTypeFeatures> portalsTypeFeaturesMap = memory.getSpriteTypeFeaturesByCategory(2);
		HashMap<Integer, SpriteTypeFeatures> portalsTypeFeaturesMapImagined = imaginationMemory
				.getSpriteTypeFeaturesByCategory(2);

		boolean victoryConditionAppeared = false;

		// Check old portals
		for (Map.Entry<Integer, SpriteTypeFeatures> spriteTypeFeaturesMapEntry : portalsTypeFeaturesMap.entrySet())
		{
			//System.out.println("Old portal type: " + spriteTypeFeaturesMapEntry.getKey());
			if (portalsTypeFeaturesMapImagined.containsKey(spriteTypeFeaturesMapEntry.getKey()))
			{
				currentSpriteTypeFeatures.changingValuesOfOtherObjects += portalsTypeFeaturesMapImagined
						.get(spriteTypeFeaturesMapEntry.getKey()).changingPoints
						- spriteTypeFeaturesMapEntry.getValue().changingPoints;

				if (!victoryConditionAppeared && !spriteTypeFeaturesMapEntry.getValue().givingVictory
						&& portalsTypeFeaturesMapImagined.get(spriteTypeFeaturesMapEntry.getKey()).givingVictory)
				{
					//System.out.println("Allowing victory portal opened");
					currentSpriteTypeFeatures.allowingVictory = true;
					victoryConditionAppeared = true;
				}
			}
			else
			{
				// Portals dissapeared
				currentSpriteTypeFeatures.changingValuesOfOtherObjects += -spriteTypeFeaturesMapEntry
						.getValue().changingPoints;
			}
		}

		// Check new portals
		for (Map.Entry<Integer, SpriteTypeFeatures> spriteTypeFeaturesMapImaginedEntry : portalsTypeFeaturesMapImagined
				.entrySet())
		{
			//System.out.println("New portal type: " + spriteTypeFeaturesMapImaginedEntry.getKey());
			if (!portalsTypeFeaturesMap.containsKey(spriteTypeFeaturesMapImaginedEntry.getKey()))
			{
				currentSpriteTypeFeatures.changingValuesOfOtherObjects += spriteTypeFeaturesMapImaginedEntry
						.getValue().changingPoints;

				if (!victoryConditionAppeared && spriteTypeFeaturesMapImaginedEntry.getValue().givingVictory)
				{
					currentSpriteTypeFeatures.allowingVictory = true;
					//System.out.println("Allowing victory new portal");
					victoryConditionAppeared = true;
				}
			}
		}

		return currentSpriteTypeFeatures;
	}

	/**
	 * Returns features optimized for Asteroids.
	 * 
	 * @param category
	 *            Category of sprite.
	 * @param type
	 *            Type of sprite.
	 */
	private SpriteTypeFeatures getSpriteTypeFeaturesForAsteroids(int category, int type)
	{
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(category, type);

		/*
		 * int type, double dangerousToAvatar, boolean dangerousOtherwise,
		 * boolean destroyable, boolean collectable, boolean givingVictory,
		 * boolean givingDefeat, int givingPoints, boolean passable, boolean
		 * moving, double speed, boolean increasingValuesOfOtherObjects, boolean
		 * allowingVictory
		 */

		switch (category)
		{
			case 1:

				break;

			case 2:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, true, true, false, 1, true,
						false, 0, 1, true);
				break;

			case 3:

				break;

			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, true, false, false, false, 0,
						false, false, 0, 0, false);
				break;

			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 1, false, false, false, false, true, 0,
						false, true, 1, 0, false);
				break;

			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 1, false, true, false, false, true, 1,
						false, true, 1, 0, false);
				break;

			default:

				break;
		}

		return spriteTypeFeatures;
	}

	/**
	 * Returns default features for different categories of sprites.
	 * 
	 * @param category
	 *            Category of sprite.
	 * @param type
	 *            Type of sprite.
	 */
	private SpriteTypeFeatures getSpriteTypeFeaturesForCategory(int category, int type)
	{
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(category, type);

		/*
		 * int type, double dangerousToAvatar, boolean dangerousOtherwise,
		 * boolean destroyable, boolean collectable, boolean givingVictory,
		 * boolean givingDefeat, int givingPoints, boolean passable, boolean
		 * moving, double speed, boolean increasingValuesOfOtherObjects, boolean
		 * allowingVictory
		 */

		switch (category)
		{
			case 1:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, true, false, false, 0,
						true, false, 0, 0, false);
				break;

			case 2:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, true, false, 0,
						true, false, 0, 1, true);
				break;

			case 3:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, true, 1, 0, false);
				break;

			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, false, 0, 0, false);
				break;

			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, true, 1, 0, false);
				break;

			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(category, type, 0, false, false, false, false, false, 0,
						false, true, 0, 0, false);
				break;

			default:

				break;
		}

		return spriteTypeFeatures;
	}

	/**
	 * Returns true if the sprite is reachable by avatar in one move.
	 * 
	 * @param stateObs
	 *            State to be tested.
	 * @param avatarPosition
	 *            Position of the avatar.
	 * @param avatarOrientation
	 *            Orientation of the avatar.
	 * @param spritePosition
	 *            Position of the sprite.
	 */
	private boolean isSpriteOneMoveFromAvatar(StateObservationMulti stateObs, Vector2d avatarPosition,
			Vector2d avatarOrientation, Vector2d spritePosition)
	{
		Vector2d distance = avatarPosition.copy().subtract(spritePosition);
		double distanceX = Math.abs(distance.x);
		double distanceY = Math.abs(distance.y);
		double speedInPixels = stateObs.getBlockSize() * stateObs.getAvatarSpeed(playerID);

		if (Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_DOWN
				&& Math.abs(distanceX) < stateObs.getBlockSize()
				&& Math.abs(distanceY - speedInPixels) < stateObs.getBlockSize())
		{
			return true;
		}
		if (Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_UP
				&& Math.abs(distanceX) < stateObs.getBlockSize()
				&& Math.abs(distanceY - speedInPixels) < stateObs.getBlockSize())
		{
			return true;
		}
		if (Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_RIGHT
				&& Math.abs(distanceX - speedInPixels) < stateObs.getBlockSize()
				&& Math.abs(distanceY) < stateObs.getBlockSize())
		{
			return true;
		}
		if (Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_LEFT
				&& Math.abs(distanceX - speedInPixels) < stateObs.getBlockSize()
				&& Math.abs(distanceY) < stateObs.getBlockSize())
		{
			return true;
		}

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
	 */
    @SuppressWarnings("unchecked")
	private Vector2d localizeSprite(StateObservationMulti stateObs, Observation observation, int searchBreadth)
	{
		ArrayList<Observation> suspects;

		int[] blockWhereObservationWasLastSeen = { (int) (observation.position.x / stateObs.getBlockSize()),
				(int) (observation.position.y / stateObs.getBlockSize()) };

		int worldXDimension = stateObs.getObservationGrid().length; // (int) (stateObs.getWorldDimension().getWidth() / stateObs.getBlockSize());
		int worldYDimension = stateObs.getObservationGrid()[0].length; // (int) (stateObs.getWorldDimension().getHeight() / stateObs.getBlockSize());

		boolean objectLocalized = false;
		int distance = 0;

		while (!objectLocalized && distance <= searchBreadth && worldXDimension + blockWhereObservationWasLastSeen[0] - distance >= 0
			    && worldYDimension + blockWhereObservationWasLastSeen[1] - distance >= 0)
		{
			for (int i = -distance; i <= distance; i = i + 1)
			{
				if (i == -distance || i == distance)
				{
					for (int j = -distance; j <= distance; j = j + 1)
					{
						// System.out.println("Searching for object on: "
						// + ((worldXDimension +
						// blockWhereObservationWasLastSeen[0] + i) %
						// worldXDimension) + ","
						// + ((worldYDimension +
						// blockWhereObservationWasLastSeen[1] + j) %
						// worldYDimension));
						suspects = stateObs
								.getObservationGrid()[(worldXDimension + blockWhereObservationWasLastSeen[0] + i)
										% worldXDimension][(worldYDimension + blockWhereObservationWasLastSeen[1] + j)
												% worldYDimension];
						for (Observation suspect : suspects)
						{
							if (suspect.obsID == observation.obsID)
								return suspect.position;
						}
					}
				}
				else
				{
					for (int j = -distance; j <= distance; j = j + 2 * distance)
					{
						// System.out.println("Searching for object on: "
						// + ((worldXDimension +
						// blockWhereObservationWasLastSeen[0] + i) %
						// worldXDimension) + ","
						// + ((worldYDimension +
						// blockWhereObservationWasLastSeen[1] + j) %
						// worldYDimension));
						suspects = stateObs
								.getObservationGrid()[(worldXDimension + blockWhereObservationWasLastSeen[0] + i)
										% worldXDimension][(worldYDimension + blockWhereObservationWasLastSeen[1] + j)
												% worldYDimension];
						for (Observation suspect : suspects)
						{
							if (suspect.obsID == observation.obsID)
								return suspect.position;
						}
					}
				}
			}

			distance++;
		}

		return null;
	}
}
