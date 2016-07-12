package NextLevel;

import core.game.Game;
import core.game.Observation;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.Direction;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Random;

public class Agent extends AbstractMultiPlayer
{

	int id; // this player's ID
	Brain brain;

	/**
	 * initialize all variables for the agent
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 * @param playerID
	 *            ID if this agent
	 */
	public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID)
	{
		id = playerID;

		brain = new Brain();
		
		brain.learn(stateObs, elapsedTimer, playerID);
		
		printState(stateObs, 999);
	}

	/**
	 * return ACTION_NIL on every call to simulate doNothing player
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 * @return ACTION_NIL all the time
	 */
	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer)
	{
		// int id = (getPlayerID() + 1) % stateObs.getNoPlayers();
		Direction move = Utils.processMovementActionKeys(Game.ki.getMask(), id);
		boolean useOn = Utils.processUseKey(Game.ki.getMask(), id);

		// In the keycontroller, move has preference.
		Types.ACTIONS action = Types.ACTIONS.fromVector(move);
		if (action == Types.ACTIONS.ACTION_NIL && useOn)
			action = Types.ACTIONS.ACTION_USE;

		printState(stateObs, 0);

		return action;
	}

	private void printState(StateObservationMulti so, int iAdvances)
	{
		System.out.println("========== Player " + id + " report ==========");
		System.out.println("Advance number: " + iAdvances);
		
		System.out.println("Avatar position: " + so.getAvatarPosition(id).x + ", " + so.getAvatarPosition(id).y);

		Vector2d avatarPosition = so.getAvatarPosition(id);
		ArrayList<Observation>[] npcPositions = so.getNPCPositions(avatarPosition);
		ArrayList<Observation>[] immovablePositions = so.getImmovablePositions(avatarPosition);
		ArrayList<Observation>[] movablePositions = so.getMovablePositions(avatarPosition);
		ArrayList<Observation>[] resourcesPositions = so.getResourcesPositions(avatarPosition);
		ArrayList<Observation>[] portalPositions = so.getPortalsPositions(avatarPosition);
		ArrayList<Observation>[] fromAvatarSpritesPositions = so.getFromAvatarSpritesPositions(avatarPosition);

		int npcCounter = 0;
		if (npcPositions != null)
		{
			for (ArrayList<Observation> npcs : npcPositions)
			{
				if (npcs.size() > 0)
				{
					npcCounter += npcs.size();
					System.out.println("Number of NPCs of type " + npcs.get(0).itype + ": " + npcs.size());
				}
			}
		}
		System.out.println("Number of NPCs: " + npcCounter);

		int immovableCounter = 0;
		if (immovablePositions != null)
		{
			for (ArrayList<Observation> immovable : immovablePositions)
			{
				if (immovable.size() > 0)
				{
					immovableCounter += immovable.size();
					System.out.println(
							"Number of immovables of type " + immovable.get(0).itype + ": " + immovable.size());
				}
			}
		}
		System.out.println("Number of immovables: " + immovableCounter);

		int movableCounter = 0;
		if (movablePositions != null)
		{
			for (ArrayList<Observation> movable : movablePositions)
			{
				if (movable.size() > 0)
				{
					movableCounter += movable.size();
					System.out.println("Number of movables of type " + movable.get(0).itype + ": " + movable.size());
				}
			}
		}
		System.out.println("Number of movables: " + movableCounter);

		int resourcesCounter = 0;
		if (resourcesPositions != null)
		{
			for (ArrayList<Observation> resources : resourcesPositions)
			{
				if (resources.size() > 0)
				{
					resourcesCounter += resources.size();
					System.out
							.println("Number of resources of type " + resources.get(0).itype + ": " + resources.size());
				}
			}
		}
		System.out.println("Number of resources: " + resourcesCounter);

		int portalsCounter = 0;
		if (portalPositions != null)
		{
			for (ArrayList<Observation> portals : portalPositions)
			{
				if (portals.size() > 0)
				{
					portalsCounter += portals.size();
					System.out.println("Number of portals of type " + portals.get(0).itype + ": " + portals.size());
				}
			}
		}
		System.out.println("Number of portals: " + portalsCounter);

		int fromAvatarSpritesCounter = 0;
		if (fromAvatarSpritesPositions != null)
		{
			for (ArrayList<Observation> fromAvatarSprites : fromAvatarSpritesPositions)
			{
				if (fromAvatarSprites.size() > 0)
				{
					fromAvatarSpritesCounter += fromAvatarSprites.size();
					System.out.println("Number of from avatar sprites of type " + fromAvatarSprites.get(0).itype + ": "
							+ fromAvatarSprites.size());
				}
			}
		}
		System.out.println("Number of from avatar sprites: " + fromAvatarSpritesCounter);

		System.out.println("=====================================");
		System.out.println("");
	}
}
