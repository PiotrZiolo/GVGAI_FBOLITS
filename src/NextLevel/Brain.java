package NextLevel;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;

import core.game.Observation;
import core.game.StateObservationMulti;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;
import java.util.Random;

public class Brain
{
	private Memory memory;
	private int playerID;
	private int approachingSpriteMovesLimit = 100; // The maximal number of moves an
    // avatar can make to approach a
    // sprite

	/**
	 * public constructor
	 */
	public Brain()
	{
		this.playerID = 0;
		this.memory = new Memory();
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
		int iAdvanceCount = 0;

		Vector2d avatarPosition = stateObs.getAvatarPosition(playerID);
		ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions(avatarPosition);
		ArrayList<Observation>[] immovablePositions = stateObs.getImmovablePositions(avatarPosition);
		ArrayList<Observation>[] movablePositions = stateObs.getMovablePositions(avatarPosition);
		ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions(avatarPosition);
		ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions(avatarPosition);
		ArrayList<Observation>[] fromAvatarSpritesPositions = stateObs.getFromAvatarSpritesPositions(avatarPosition);

		if (npcPositions != null)
		{
			for (ArrayList<Observation> npcs : npcPositions)
			{
				if (npcs.size() > 0)
				{
					for (int i = 0; i < npcs.size(); i++)
					{
						if (memory.getSpriteTypeFeaturesByType(npcs.get(i).itype) == null)
						{
							memory.setSpriteTypeFeaturesByType(npcs.get(i).itype, 
									getSpriteTypeFeaturesForCategory(npcs.get(i).category, npcs.get(i).itype));
						}
					}
				}
			}
		}

		if (immovablePositions != null)
		{
			for (ArrayList<Observation> immovable : immovablePositions)
			{
				if (immovable.size() > 0)
				{
					for (int i = 0; i < immovable.size(); i++)
					{
						if (memory.getSpriteTypeFeaturesByType(immovable.get(i).itype) == null)
						{
							memory.setSpriteTypeFeaturesByType(immovable.get(i).itype, 
									getSpriteTypeFeaturesForCategory(immovable.get(i).category, immovable.get(i).itype));
						}
					}
				}
			}
		}

		if (movablePositions != null)
		{
			for (ArrayList<Observation> movable : movablePositions)
			{
				if (movable.size() > 0)
				{
					for (int i = 0; i < movable.size(); i++)
					{
						if (memory.getSpriteTypeFeaturesByType(movable.get(i).itype) == null)
						{
							memory.setSpriteTypeFeaturesByType(movable.get(i).itype, 
									getSpriteTypeFeaturesForCategory(movable.get(i).category, movable.get(i).itype));
						}
					}
				}
			}
		}

		if (resourcesPositions != null)
		{
			for (ArrayList<Observation> resources : resourcesPositions)
			{
				if (resources.size() > 0)
				{
					for (int i = 0; i < resources.size(); i++)
					{
						if (memory.getSpriteTypeFeaturesByType(resources.get(i).itype) == null)
						{
							memory.setSpriteTypeFeaturesByType(resources.get(i).itype, 
									getSpriteTypeFeaturesForCategory(resources.get(i).category, resources.get(i).itype));
						}
					}
				}
			}
		}

		if (portalPositions != null)
		{
			for (ArrayList<Observation> portals : portalPositions)
			{
				if (portals.size() > 0)
				{
					for (int i = 0; i < portals.size(); i++)
					{
						if (memory.getSpriteTypeFeaturesByType(portals.get(i).itype) == null)
						{
							memory.setSpriteTypeFeaturesByType(portals.get(i).itype, 
									getSpriteTypeFeaturesForCategory(portals.get(i).category, portals.get(i).itype));
						}
					}
				}
			}
		}

		if (fromAvatarSpritesPositions != null)
		{
			for (ArrayList<Observation> fromAvatarSprites : fromAvatarSpritesPositions)
			{
				if (fromAvatarSprites.size() > 0)
				{
					for (int i = 0; i < fromAvatarSprites.size(); i++)
					{
						if (memory.getSpriteTypeFeaturesByType(fromAvatarSprites.get(i).itype) == null)
						{
							memory.setSpriteTypeFeaturesByType(fromAvatarSprites.get(i).itype, 
									getSpriteTypeFeaturesForCategory(fromAvatarSprites.get(i).category, fromAvatarSprites.get(i).itype));
						}
					}
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

	}

	/**
	 * Dumps all acquired knowledge into a file.
	 */
	public void rememberAllInLongTermMemory()
	{

	}

	/**
	 * Retrieves knowledge saved previously to a file.
	 */
	public void initializeFromLongTermMemory()
	{

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
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(observation.itype);

		return spriteTypeFeatures;
	}

	/**
	 * Tries to approach the sprite given in observation. Returns a state in
	 * which the avatar is one step from the sprite. If it was impossible in a
	 * certain number of tries, null is returned.
	 * 
	 * @param stateObs
	 *            Observation of the initial state from which testing is to be
	 *            done.
	 * @param observation
	 *            Observation of the sprite to approach.
	 */
	public StateObservationMulti approachSprite(StateObservationMulti stateObs, Observation observation)
	{
		if (!stateObs.isAvatarAlive(1-playerID))
			return null;
		
		StateObservationMulti currentState = stateObs.copy();
		StateObservationMulti temporaryState;
		int advanceLimit = approachingSpriteMovesLimit;
		
		Vector2d playerPreviousPosition = stateObs.getAvatarPosition(playerID);
		Vector2d playerPreviousOrientation = stateObs.getAvatarOrientation(playerID);
		ArrayList<Types.ACTIONS> playerGoodActions = stateObs.getAvailableActions(playerID);
		ArrayList<Types.ACTIONS> opponentGoodActions = stateObs.getAvailableActions(1-playerID);
		Types.ACTIONS playerLastAction = Types.ACTIONS.ACTION_NIL;
		
		Vector2d observationPosition = observation.position;
		int[] blockWhereObservationWasLastSeen = {(int)(observationPosition.x/stateObs.getBlockSize()),
				(int)(observationPosition.y/stateObs.getBlockSize())};
		
		// in this while avatar is trying to minimize distance to goal
		while (true) {
			
			// finding object position - first in the same place as last time, than in the neighborhood
			observationPosition = FindObject(blockWhereObservationWasLastSeen, stateObs, observation.obsID);
			
			// check whether avatar reached the object and return opponent if he is the object.
			if (isSpriteOneMoveFromAvatarWithOpponentRotation(observationPosition, playerPreviousPosition,
					currentState, playerPreviousOrientation, observation.itype)) {
				if (currentState.isGameOver())
					return null;
				return currentState;
			}
			
			// if opponent always die finish return null
			if (opponentGoodActions.isEmpty())
				return null;

			// choose actions for players
			Types.ACTIONS[] actions = new Types.ACTIONS[2];
			if (opponentGoodActions.contains(Types.ACTIONS.ACTION_NIL))
				actions[1-playerID] = Types.ACTIONS.ACTION_NIL;
			else
				actions[1-playerID] = opponentGoodActions.get(new Random().nextInt(opponentGoodActions.size()));
			
			//System.out.println("playerGoodActions = " + playerGoodActions.toString());
			actions[playerID] = chooseDirection(observationPosition.copy(), playerPreviousPosition,
					playerGoodActions, playerLastAction);
			temporaryState = currentState.copy();

			// if player don't want to move go to BFS
			if (actions[playerID]==null)
				break;
			
			// advance
			//System.out.println("avatarPosition = " + temporaryState.getAvatarPosition(playerID));
			//System.out.println("goalPosition = " + observationPosition);
			//System.out.println("actions = " + actions[playerID].toString());
			if (advanceLimit==0)
				return null;
			temporaryState.advance(actions);
			advanceLimit--;
			
			// check whether no one died
			boolean goodMove = true;
			if (!temporaryState.isAvatarAlive(playerID)) {
				playerGoodActions.remove(actions[playerID]);
				goodMove = false;
			}
			if (!temporaryState.isAvatarAlive(1-playerID)) {
				opponentGoodActions.remove(actions[1-playerID]);
				goodMove = false;
			}
			
			// check whether player changed position or direction
			Vector2d playerNewPosition = temporaryState.getAvatarPosition(playerID);
			Vector2d playerNewOrientation = temporaryState.getAvatarOrientation(playerID);
			if (playerNewPosition.equals(playerPreviousPosition) && playerNewOrientation.equals(playerPreviousOrientation)) {
				playerGoodActions.remove(actions[playerID]);
				goodMove = false;
			}

			// if goodMove=true advance to next step
			if (goodMove) {
				currentState = temporaryState;
				if (!playerNewPosition.equals(playerPreviousPosition)) {
					playerGoodActions = (ArrayList<Types.ACTIONS>)stateObs.getAvailableActions(playerID).clone();
					opponentGoodActions = (ArrayList<Types.ACTIONS>)stateObs.getAvailableActions(1-playerID).clone();
				}
				playerPreviousPosition = playerNewPosition;
				playerPreviousOrientation = playerNewOrientation;
				playerLastAction = actions[playerID];
			}
		}
		//return null;

		// in this while avatar is trying to go along the shortest path to goal using BFS
		while(true) {
			//System.out.println("playerPreviousPosition = " + playerPreviousPosition);
			//System.out.println("observationPosition = " + observationPosition);
			//System.out.println("playerID = " + playerID);
			PathFinder pathFinder = new PathFinder();
			Deque<Types.ACTIONS> playerMoveSequenceToGoal = pathFinder.pathFinder(playerPreviousPosition,
					observationPosition, currentState, playerID);

			Iterator<Types.ACTIONS> iterator = playerMoveSequenceToGoal.iterator();
			Types.ACTIONS forceMove = null;

			/*while(iterator.hasNext()) {
				System.out.println("actions = " + iterator.next().toString());
			}
			return null;*/
			while(iterator.hasNext()) {
				// finding object position - first in the same place as last time, than in the neighborhood
				observationPosition = FindObject(blockWhereObservationWasLastSeen, stateObs, observation.obsID);
				
				// check whether avatar reached the object and return opponent if he is the object.
				if (isSpriteOneMoveFromAvatarWithOpponentRotation(observationPosition, playerPreviousPosition,
						currentState, playerPreviousOrientation, observation.itype)) {
					if (currentState.isGameOver())
						return null;
					return currentState;
				}

				// if opponent always die finish return null
				if (opponentGoodActions.isEmpty())
					return null;

				// choose actions for players
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				if (opponentGoodActions.contains(Types.ACTIONS.ACTION_NIL))
					actions[1-playerID] = Types.ACTIONS.ACTION_NIL;
				else
					actions[1-playerID] = opponentGoodActions.get(new Random().nextInt(opponentGoodActions.size()));
				
				//System.out.println("playerGoodActions = " + playerGoodActions.toString());
				if (forceMove==null)
					actions[playerID] = iterator.next();
				else
					actions[playerID] = forceMove;
				temporaryState = currentState.copy();
				
				// advance
				//System.out.println("avatarPosition = " + temporaryState.getAvatarPosition(playerID));
				//System.out.println("goalPosition = " + observationPosition);
				//System.out.println("actions = " + actions[playerID].toString());
				if (advanceLimit==0)
					return null;
				temporaryState.advance(actions);
				advanceLimit--;
				//System.out.println("avatarPosition2 = " + temporaryState.getAvatarPosition(playerID));
				
				// check whether no one died
				boolean goodMove = true;
				if (!temporaryState.isAvatarAlive(playerID)) {
					return null;	// do poprawy - na razie jak zginê id¹c do obiektu to siê poddaje
					
					//playerGoodActions.remove(actions[playerID]);
					//goodMove = false;
				}
				if (!temporaryState.isAvatarAlive(1-playerID)) {
					opponentGoodActions.remove(actions[1-playerID]);
					goodMove = false;
				}
				
				// check whether player changed position or direction
				Vector2d playerNewPosition = temporaryState.getAvatarPosition(playerID);
				Vector2d playerNewOrientation = temporaryState.getAvatarOrientation(playerID);
				if (playerNewPosition.equals(playerPreviousPosition) &&
						playerNewOrientation.equals(playerPreviousOrientation)) {
					break; // look for path again
				}
				if (playerNewPosition.equals(playerPreviousPosition) &&
						!playerNewOrientation.equals(playerPreviousOrientation)) {
					forceMove = actions[playerID];
				} else {
					forceMove = null;
				}

				// if goodMove=true advance to next step
				if (goodMove) {
					currentState = temporaryState;
					if (!playerNewPosition.equals(playerPreviousPosition)) {
						opponentGoodActions = (ArrayList<Types.ACTIONS>)stateObs.getAvailableActions(1-playerID).clone();
					}
					playerPreviousPosition = playerNewPosition;
					playerPreviousOrientation = playerNewOrientation;
					playerLastAction = actions[playerID];
				}
			}
		}
	}

	private boolean isSpriteOneMoveFromAvatarWithOpponentRotation(Vector2d observationPosition,
			Vector2d avatarPosition, StateObservationMulti currentState, Vector2d avatarOrientation,
			int spriteType) {
		
		double speedInPixels = currentState.getBlockSize() * currentState.getAvatarSpeed(playerID);
		Vector2d distance = observationPosition.copy().subtract(avatarPosition);
		if (Types.ACTIONS.fromVector(avatarOrientation)==Types.ACTIONS.ACTION_DOWN &&
				Math.abs(distance.x) < currentState.getBlockSize() &&
				Math.abs(distance.y - speedInPixels) < currentState.getBlockSize()) {
			if (spriteType==0 &&
					Types.ACTIONS.fromVector(currentState.getAvatarOrientation(1-playerID))!=
					Types.ACTIONS.ACTION_UP) {
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[1-playerID] = Types.ACTIONS.ACTION_UP;
				currentState.advance(actions);
			}
			return true;
		}
		if (Types.ACTIONS.fromVector(avatarOrientation)==Types.ACTIONS.ACTION_UP &&
				Math.abs(distance.x) < currentState.getBlockSize() &&
				Math.abs(distance.y + speedInPixels) < currentState.getBlockSize()) {
			if (spriteType==0 &&
					Types.ACTIONS.fromVector(currentState.getAvatarOrientation(1-playerID))!=
					Types.ACTIONS.ACTION_DOWN) {
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[1-playerID] = Types.ACTIONS.ACTION_DOWN;
				currentState.advance(actions);
			}
			return true;
		}
		if (Types.ACTIONS.fromVector(avatarOrientation)==Types.ACTIONS.ACTION_RIGHT &&
				Math.abs(distance.x - speedInPixels) < currentState.getBlockSize() &&
				Math.abs(distance.y) < currentState.getBlockSize()) {
			if (spriteType==0 &&
					Types.ACTIONS.fromVector(currentState.getAvatarOrientation(1-playerID))!=
					Types.ACTIONS.ACTION_LEFT) {
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[1-playerID] = Types.ACTIONS.ACTION_LEFT;
				currentState.advance(actions);
			}
			return true;
		}
		if (Types.ACTIONS.fromVector(avatarOrientation)==Types.ACTIONS.ACTION_LEFT &&
				Math.abs(distance.x + speedInPixels) < currentState.getBlockSize() &&
				Math.abs(distance.y) < currentState.getBlockSize()) {
			if (spriteType==0 &&
					Types.ACTIONS.fromVector(currentState.getAvatarOrientation(1-playerID))!=
					Types.ACTIONS.ACTION_RIGHT) {
				Types.ACTIONS[] actions = new Types.ACTIONS[2];
				actions[playerID] = Types.ACTIONS.ACTION_NIL;
				actions[1-playerID] = Types.ACTIONS.ACTION_RIGHT;
				currentState.advance(actions);
			}
			return true;
		}
		return false;
	}

	private Vector2d FindObject(int[] blockWhereObservationWasLastSeen,
			StateObservationMulti stateObs, int searchedID) {
		ArrayList<Observation> suspects = stateObs.getObservationGrid()
				[blockWhereObservationWasLastSeen[0]][blockWhereObservationWasLastSeen[1]];
		boolean objectLocalized = false;
		for (Observation suspect : suspects) {
			if (suspect.obsID == searchedID)
				return suspect.position;
		}
		if (!objectLocalized) {
			for (int i=-1; i<=1; i++) {
				for (int j=-1; j<=1; j++) {
					suspects = stateObs.getObservationGrid()
							[blockWhereObservationWasLastSeen[0]+i][blockWhereObservationWasLastSeen[1]+j];
					for (Observation suspect : suspects) {
						if (suspect.obsID == searchedID) {
							blockWhereObservationWasLastSeen[0] += i;
							blockWhereObservationWasLastSeen[1] += j;
							return suspect.position;
						}
					}
				}
			}
		}
		return null;
	}

	private Types.ACTIONS chooseDirection(Vector2d observationPosition, Vector2d playerNewPosition,
			ArrayList<Types.ACTIONS> playerGoodActions, Types.ACTIONS lastAction) {
		boolean rightAvailable = playerGoodActions.contains(Types.ACTIONS.ACTION_RIGHT)
				&& lastAction!=Types.ACTIONS.ACTION_LEFT;
		boolean downAvailable = playerGoodActions.contains(Types.ACTIONS.ACTION_DOWN)
				&& lastAction!=Types.ACTIONS.ACTION_UP;
		boolean leftAvailable = playerGoodActions.contains(Types.ACTIONS.ACTION_LEFT)
				&& lastAction!=Types.ACTIONS.ACTION_RIGHT;
		boolean upAvailable = playerGoodActions.contains(Types.ACTIONS.ACTION_UP)
				&& lastAction!=Types.ACTIONS.ACTION_DOWN;
		Vector2d distance = observationPosition.subtract(playerNewPosition);
		double distanceX = Math.abs(distance.x);
		double distanceY = Math.abs(distance.y);
		if (distanceX<distanceY) {
			if (distance.y > 0) {
				if (downAvailable) {
					return Types.ACTIONS.ACTION_DOWN;
				} else {
					if (distance.x>0 && rightAvailable)
						return Types.ACTIONS.ACTION_RIGHT;
					if (distance.x<0 && leftAvailable)
						return Types.ACTIONS.ACTION_LEFT;
				}
			}
			if (distance.y < 0) {
				if (upAvailable) {
					return Types.ACTIONS.ACTION_UP;
				} else {
					if (distance.x>0 && rightAvailable)
						return Types.ACTIONS.ACTION_RIGHT;
					if (distance.x<0 && leftAvailable)
						return Types.ACTIONS.ACTION_LEFT;
				}
			}
		} else {
			if (distance.x > 0) {
				if (rightAvailable) {
					return Types.ACTIONS.ACTION_RIGHT;
				} else {
					if (distance.y>0 && downAvailable)
						return Types.ACTIONS.ACTION_DOWN;
					if (distance.y<0 && upAvailable)
						return Types.ACTIONS.ACTION_UP;
				}
			}
			if (distance.x < 0) {
				if (leftAvailable) {
					return Types.ACTIONS.ACTION_LEFT;
				} else {
					if (distance.y>0 && downAvailable)
						return Types.ACTIONS.ACTION_DOWN;
					if (distance.y<0 && upAvailable)
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
	 */
	private StateObservationMulti[] makeActionOnSprite(StateObservationMulti stateObs, Observation observation,
			Types.ACTIONS action)
	{
		StateObservationMulti stateObsJustBeforeAction = stateObs.copy();
		StateObservationMulti stateObsAfterAction = stateObs.copy();

		return new StateObservationMulti[] { stateObsJustBeforeAction, stateObsAfterAction };
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
		return stateObs;
	}
	
	private SpriteTypeFeatures getSpriteTypeFeaturesForAsteroids(int category, int type)
	{
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(type);
		
		/*
		 * int type, double dangerousToAvatar, boolean dangerousOtherwise, boolean destroyable,
			boolean collectable, boolean givingVictory, boolean givingDefeat, int givingPoints, boolean passable,
			boolean moving, double speed, boolean increasingValuesOfOtherObjects, boolean allowingVictory
		 */
		
		switch (category)
		{
			case 1:

				break;

			case 2:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, false, true, true, false, 1, true, false, 0, true, true);
				break;

			case 3:

				break;

			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, true, false, false, false, 0, false, false, 0, false, false);
				break;

			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 1, false, false, false, false, true, 0, false, true, 1, false, false);
				break;

			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 1, false, true, false, false, true, 1, false, true, 1, false, false);
				break;

			default:

				break;
		}
		
		return spriteTypeFeatures; 
	}

	private SpriteTypeFeatures getSpriteTypeFeaturesForCategory(int category, int type)
	{
		SpriteTypeFeatures spriteTypeFeatures = new SpriteTypeFeatures(type);
		
		/*
		 * int type, double dangerousToAvatar, boolean dangerousOtherwise, boolean destroyable,
			boolean collectable, boolean givingVictory, boolean givingDefeat, int givingPoints, boolean passable,
			boolean moving, double speed, boolean increasingValuesOfOtherObjects, boolean allowingVictory
		 */
		
		switch (category)
		{
			case 1:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, false, true, false, false, 1, true, false, 0, false, false);
				break;

			case 2:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, false, true, true, false, 1, true, false, 0, true, true);
				break;

			case 3:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0.1, true, true, false, false, true, 1, false, true, 1, false, false);
				break;

			case 4:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0, false, true, false, false, false, 0, false, false, 0, false, false);
				break;

			case 5:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 1, false, false, false, false, true, 0, false, true, 1, false, false);
				break;

			case 6:
				spriteTypeFeatures = new SpriteTypeFeatures(type, 0.1, true, true, true, false, false, 1, true, true, 0.1, false, false);
				break;

			default:

				break;
		}
		
		return spriteTypeFeatures;
	}
}
