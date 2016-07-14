package NextLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

import core.game.Observation;
import core.game.StateObservationMulti;
import core.game.Event;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import java.lang.Math;

public class Brain
{
	private Memory memory;
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

	/**
	 * public constructor
	 */
	public Brain()
	{
		this.playerID = 0;
		this.memory = new Memory();
		this.testingSpriteAttemptsLimit = 1;
		this.approachingSpriteLimit = 3;
		this.approachingSpriteMovesLimit = 100;
		this.numberOfAdvancesForANewTry = 5;
	}

	/**
	 * Public constructor.
	 * 
	 * @param playerID
	 *            ID of this agent.
	 */
	public Brain(int playerID)
	{
		this.playerID = playerID;
		this.memory = new Memory();
		this.testingSpriteAttemptsLimit = 1;
		this.approachingSpriteLimit = 3;
		this.approachingSpriteMovesLimit = 100;
		this.numberOfAdvancesForANewTry = 5;
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
	public HashMap<Integer, SpriteTypeFeatures> getSpriteTypeFeatures()
	{
		return memory.getSpriteTypeFeaturesMap();
	}

	/**
	 * The main method to learn about all sprites' properties.
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 */
	public void learn(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer)
	{
		boolean productionVersion = false;

		this.oppID = (playerID + 1) % stateObs.getNoPlayers();

		Vector2d avatarPosition = stateObs.getAvatarPosition(playerID);
		ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions(avatarPosition);
		ArrayList<Observation>[] immovablePositions = stateObs.getImmovablePositions(avatarPosition);
		ArrayList<Observation>[] movablePositions = stateObs.getMovablePositions(avatarPosition);
		ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions(avatarPosition);
		ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions(avatarPosition);
		ArrayList<Observation>[] fromAvatarSpritesPositions = stateObs.getFromAvatarSpritesPositions(avatarPosition);

		ArrayList<ArrayList<Observation>[]> arraysOfSprites = new ArrayList<ArrayList<Observation>[]>();
		arraysOfSprites.add(npcPositions);
		arraysOfSprites.add(immovablePositions);
		arraysOfSprites.add(movablePositions);
		arraysOfSprites.add(resourcesPositions);
		arraysOfSprites.add(portalPositions);
		arraysOfSprites.add(fromAvatarSpritesPositions);

		for (ArrayList<Observation>[] positions : arraysOfSprites)
		{
			if (positions != null)
			{
				for (ArrayList<Observation> observations : positions)
				{
					if (observations.size() > 0)
					{
						for (int i = 0; i < observations.size(); i++)
						{
							if (memory.getSpriteTypeFeaturesByType(observations.get(i).itype) == null)
							{
								SpriteTypeFeatures spriteTypeFeatures;
								if (productionVersion)
								{
									spriteTypeFeatures = testSprite(stateObs, observations.get(i));
								}
								else
								{
									spriteTypeFeatures = getSpriteTypeFeaturesForCategory(observations.get(i).category,
											observations.get(i).itype);
								}

								if (spriteTypeFeatures != null)
								{
									memory.setSpriteTypeFeaturesByType(observations.get(i).itype, spriteTypeFeatures);
								}
							}
						}
					}
				}
			}
		}

		if (productionVersion)
		{
			if (stateObs.getNoPlayers() > 1)
			{
				SpriteTypeFeatures spriteTypeFeatures = testOtherPlayer(stateObs, oppID);
		
				if (spriteTypeFeatures != null)
				{
					// Opponents' features are saved with their ID negative as type
					memory.setSpriteTypeFeaturesByType(-oppID, spriteTypeFeatures);
				}
			}
		}
	}

	/**
	 * Updates the knowledge about different sprites if new Events have appeared
	 * in the history.
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 */
	public void update(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer)
	{
		// Check events history and update the knowledge

		// Checks for new types of sprites and add knowledge about them

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
	 */
	private SpriteTypeFeatures testSprite(StateObservationMulti stateObs, Observation observation)
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

			if (stateObsApproached != null)
			{

				// Test going onto sprite
				if (!movingOntoTested)
				{
					StateObservationMulti[] actionStates = makeActionOnSprite(stateObsApproached, observation, 1);

					if (actionStates != null)
					{
						updateKnowledgeAfterActionOnSprite(spriteTypeFeatures, actionStates[0], actionStates[1],
								observation, 1);
						movingOntoTested = true;
					}
				}

				// Test making USE on sprite
				if (!useTested)
				{
					StateObservationMulti[] actionStates = makeActionOnSprite(stateObsApproached, observation, 0);

					if (actionStates != null)
					{
						updateKnowledgeAfterActionOnSprite(spriteTypeFeatures, actionStates[0], actionStates[1],
								observation, 0);
						useTested = true;
					}
				}

				// Test the action USE of the other player on our player
				if (!isSpriteNonPlayer && !otherPlayerUseTested)
				{
					StateObservationMulti[] actionStates = makeActionOnSprite(stateObsApproached, observation, 2);

					if (actionStates != null)
					{
						updateKnowledgeAfterActionOnSprite(spriteTypeFeatures, actionStates[0], actionStates[1],
								observation, 2);
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
	 */
	private SpriteTypeFeatures testOtherPlayer(StateObservationMulti stateObs, int oppID)
	{
		// Create an artificial observation for the opponent to use the same
		// code as for sprites.
		// Type and ID are assumed to be the negative of oppID, category is set
		// to 0.
		Observation opponentObservation = new Observation(-oppID, -oppID, stateObs.getAvatarPosition(oppID),
				stateObs.getAvatarPosition(playerID), 0);

		return testSprite(stateObs, opponentObservation);
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
	private StateObservationMulti approachSprite(StateObservationMulti stateObs, Observation observation)
	{
		// !!!!!!!!!!!!
		// To be filled
		// !!!!!!!!!!!!

		return stateObs;
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

						stateObsJustAfterAction
								.advance(new Types.ACTIONS[] { (playerID == 0) ? action : Types.ACTIONS.ACTION_NIL,
										(playerID == 1) ? action : Types.ACTIONS.ACTION_NIL });

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
						}
						else if (spriteCurrentPosition != null && isSpriteOneMoveFromAvatar(stateObsJustAfterAction,
								stateObsJustBeforeAction.getAvatarPosition(playerID),
								stateObsJustBeforeAction.getAvatarOrientation(playerID), spriteCurrentPosition))
						{
							succeeded = true;
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
		return approachSprite(stateObs, observation);
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
	 */
	private SpriteTypeFeatures updateKnowledgeAfterActionOnSprite(SpriteTypeFeatures currentSpriteTypeFeatures,
			StateObservationMulti stateObsJustBeforeAction, StateObservationMulti stateObsJustAfterAction,
			Observation observation, int actionType)
	{
		// Get the event if something happened

		TreeSet<Event> eventsHistory = stateObsJustAfterAction.getEventsHistory();
		Iterator<Event> eventsIterator = eventsHistory.descendingIterator();
		Event event = null;
		boolean eventHappened = false;

		while (!eventHappened && eventsIterator.hasNext())
		{
			event = eventsIterator.next();

			if (event.gameStep == stateObsJustAfterAction.getGameTick())
			{
				eventHappened = true;
			}
		}

		if (!eventHappened)
		{
			event = null;
		}

		// Process different types of situations

		switch (actionType)
		{
			// Use
			case 0:
				if (eventHappened)
				{

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

				}
				else
				{

				}

				break;

			// Action of the other player on our player
			case 2:
				if (eventHappened)
				{

				}
				else
				{

				}

				break;
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
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(type);

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
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, false, true, true, false, 1, true, false, 0,
						true, true);
				break;

			case 3:

				break;

			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, true, false, false, false, 0, false, false,
						0, false, false);
				break;

			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 1, false, false, false, false, true, 0, false, true,
						1, false, false);
				break;

			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 1, false, true, false, false, true, 1, false, true, 1,
						false, false);
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
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(type);

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
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, false, true, false, false, 1, true, false,
						0, false, false);
				break;

			case 2:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, false, true, true, false, 1, true, false, 0,
						true, true);
				break;

			case 3:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0.1, true, true, false, false, true, 1, false, true,
						1, false, false);
				break;

			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, true, false, false, false, 0, false, false,
						0, false, false);
				break;

			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 1, false, false, false, false, true, 0, false, true,
						1, false, false);
				break;

			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0.1, true, true, true, false, false, 1, true, true,
						0.1, false, false);
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
		Vector2d distance = avatarPosition.subtract(spritePosition);
		double distanceX = Math.abs(distance.x);
		double distanceY = Math.abs(distance.y);
		double speedInPixels = stateObs.getBlockSize() * stateObs.getAvatarSpeed(playerID);

		if (Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_DOWN
				&& Math.abs(distance.x) < stateObs.getBlockSize()
				&& Math.abs(distance.y - speedInPixels) < stateObs.getBlockSize())
		{
			return true;
		}
		if (Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_UP
				&& Math.abs(distance.x) < stateObs.getBlockSize()
				&& Math.abs(distance.y + speedInPixels) < stateObs.getBlockSize())
		{
			return true;
		}
		if (Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_RIGHT
				&& Math.abs(distance.x - speedInPixels) < stateObs.getBlockSize()
				&& Math.abs(distance.y) < stateObs.getBlockSize())
		{
			return true;
		}
		if (Types.ACTIONS.fromVector(avatarOrientation) == Types.ACTIONS.ACTION_LEFT
				&& Math.abs(distance.x + speedInPixels) < stateObs.getBlockSize()
				&& Math.abs(distance.y) < stateObs.getBlockSize())
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
	private Vector2d localizeSprite(StateObservationMulti stateObs, Observation observation, int searchBreadth)
	{
		ArrayList<Observation> suspects;

		int[] blockWhereObservationWasLastSeen = { (int) (observation.position.x / stateObs.getBlockSize()),
				(int) (observation.position.y / stateObs.getBlockSize()) };

		int worldXDimension = (int) (stateObs.getWorldDimension().getWidth() / stateObs.getBlockSize());
		int worldYDimension = (int) (stateObs.getWorldDimension().getHeight() / stateObs.getBlockSize());

		boolean objectLocalized = false;
		int distance = 0;

		while (!objectLocalized && distance <= searchBreadth)
		{
			for (int i = -distance; i <= distance; i = i + 1)
			{
				if (i == -distance || i == distance)
				{
					for (int j = -distance; j <= distance; j = j + 1)
					{
						if (!(i == 0 && j == 0))
						{
							suspects = stateObs.getObservationGrid()[(blockWhereObservationWasLastSeen[0] + i)
									% worldXDimension][(blockWhereObservationWasLastSeen[1] + j) % worldYDimension];
							for (Observation suspect : suspects)
							{
								if (suspect.obsID == observation.obsID)
									return suspect.position;
							}
						}
					}
				}
				else
				{
					for (int j = -distance; j <= distance; j = j + 2 * distance)
					{
						if (!(i == 0 && j == 0))
						{
							suspects = stateObs.getObservationGrid()[(blockWhereObservationWasLastSeen[0] + i)
									% worldXDimension][(blockWhereObservationWasLastSeen[1] + j) % worldYDimension];
							for (Observation suspect : suspects)
							{
								if (suspect.obsID == observation.obsID)
									return suspect.position;
							}
						}
					}
				}
			}

			distance++;
		}

		return null;
	}
}
