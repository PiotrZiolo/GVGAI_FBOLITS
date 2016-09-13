package FBOLITS.utils;

import java.util.ArrayList;

import OldNextLevel.LogHandler;
import core.game.Observation;
import core.game.StateObservationMulti;

public class StatePrinter
{
	public static void printGameState(StateObservationMulti stateObs, int playerID, int iAdvances, boolean extended)
	{
		LogHandler.writeLog("========== Player " + playerID + " report ==========", "", 3);
		LogHandler.writeLog("Game tick: " + stateObs.getGameTick(), "", 3);
		LogHandler.writeLog("Advance number: " + iAdvances, "", 3);

		LogHandler.writeLog("Avatar position: " + stateObs.getAvatarPosition(playerID).x + ", " + stateObs.getAvatarPosition(playerID).y, "", 3);

		//Vector2d avatarPosition = so.getAvatarPosition(id);
		ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions();
		ArrayList<Observation>[] immovablePositions = stateObs.getImmovablePositions();
		ArrayList<Observation>[] movablePositions = stateObs.getMovablePositions();
		ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions();
		ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions();
		ArrayList<Observation>[] fromAvatarSpritesPositions = stateObs.getFromAvatarSpritesPositions();

		int npcCounter = 0;
		if (npcPositions != null)
		{
			for (ArrayList<Observation> npcs : npcPositions)
			{
				if (npcs.size() > 0)
				{
					npcCounter += npcs.size();
					LogHandler.writeLog("Number of NPCs of type " + npcs.get(0).itype + ": " + npcs.size(), "", 3);

					if (extended)
					{
						for (int i = 0; i < npcs.size(); i++)
						{
							LogHandler.writeLog("<<<Object>>>", "", 3);
							LogHandler.writeLog("Category:           " + npcs.get(i).category, "", 3);
							LogHandler.writeLog("Type:               " + npcs.get(i).itype, "", 3);
							LogHandler.writeLog("Observation id:     " + npcs.get(i).obsID, "", 3);
							LogHandler.writeLog("Position x:         " + npcs.get(i).position.x, "", 3);
							LogHandler.writeLog("Position y:         " + npcs.get(i).position.y, "", 3);
							LogHandler.writeLog("Squared distance:   " + npcs.get(i).sqDist, "", 3);
							LogHandler.writeLog("----------", "", 3);
						}
					}
				}
			}
		}
		LogHandler.writeLog("Number of NPCs: " + npcCounter, "", 3);

		int immovableCounter = 0;
		if (immovablePositions != null)
		{
			for (ArrayList<Observation> immovable : immovablePositions)
			{
				if (immovable.size() > 0)
				{
					immovableCounter += immovable.size();
					LogHandler.writeLog("Number of immovables of type " + immovable.get(0).itype + ": " + immovable.size(), "", 3);

					if (extended)
					{
						for (int i = 0; i < immovable.size(); i++)
						{
							LogHandler.writeLog("<<<Object>>>", "", 3);
							LogHandler.writeLog("Category:           " + immovable.get(i).category, "", 3);
							LogHandler.writeLog("Type:               " + immovable.get(i).itype, "", 3);
							LogHandler.writeLog("Observation id:     " + immovable.get(i).obsID, "", 3);
							LogHandler.writeLog("Position x:         " + immovable.get(i).position.x, "", 3);
							LogHandler.writeLog("Position y:         " + immovable.get(i).position.y, "", 3);
							LogHandler.writeLog("Squared distance:   " + immovable.get(i).sqDist, "", 3);
							LogHandler.writeLog("----------", "", 3);
						}
					}
				}
			}
		}
		LogHandler.writeLog("Number of immovables: " + immovableCounter, "", 3);

		int movableCounter = 0;
		if (movablePositions != null)
		{
			for (ArrayList<Observation> movable : movablePositions)
			{
				if (movable.size() > 0)
				{
					movableCounter += movable.size();
					LogHandler.writeLog("Number of movables of type " + movable.get(0).itype + ": " + movable.size(), "", 3);

					if (extended)
					{
						for (int i = 0; i < movable.size(); i++)
						{
							LogHandler.writeLog("<<<Object>>>", "", 3);
							LogHandler.writeLog("Category:           " + movable.get(i).category, "", 3);
							LogHandler.writeLog("Type:               " + movable.get(i).itype, "", 3);
							LogHandler.writeLog("Observation id:     " + movable.get(i).obsID, "", 3);
							LogHandler.writeLog("Position x:         " + movable.get(i).position.x, "", 3);
							LogHandler.writeLog("Position y:         " + movable.get(i).position.y, "", 3);
							LogHandler.writeLog("Squared distance:   " + movable.get(i).sqDist, "", 3);
							LogHandler.writeLog("----------", "", 3);
						}
					}
				}
			}
		}
		LogHandler.writeLog("Number of movables: " + movableCounter, "", 3);

		int resourcesCounter = 0;
		if (resourcesPositions != null)
		{
			for (ArrayList<Observation> resources : resourcesPositions)
			{
				if (resources.size() > 0)
				{
					resourcesCounter += resources.size();
					LogHandler.writeLog("Number of resources of type " + resources.get(0).itype + ": " + resources.size(), "", 3);

					if (extended)
					{
						for (int i = 0; i < resources.size(); i++)
						{
							LogHandler.writeLog("<<<Object>>>", "", 3);
							LogHandler.writeLog("Category:           " + resources.get(i).category, "", 3);
							LogHandler.writeLog("Type:               " + resources.get(i).itype, "", 3);
							LogHandler.writeLog("Observation id:     " + resources.get(i).obsID, "", 3);
							LogHandler.writeLog("Position x:         " + resources.get(i).position.x, "", 3);
							LogHandler.writeLog("Position y:         " + resources.get(i).position.y, "", 3);
							LogHandler.writeLog("Squared distance:   " + resources.get(i).sqDist, "", 3);
							LogHandler.writeLog("----------", "", 3);
						}
					}
				}
			}
		}
		LogHandler.writeLog("Number of resources: " + resourcesCounter, "", 3);

		int portalsCounter = 0;
		if (portalPositions != null)
		{
			for (ArrayList<Observation> portals : portalPositions)
			{
				if (portals.size() > 0)
				{
					portalsCounter += portals.size();
					LogHandler.writeLog("Number of portals of type " + portals.get(0).itype + ": " + portals.size(), "", 3);

					if (extended)
					{
						for (int i = 0; i < portals.size(); i++)
						{
							LogHandler.writeLog("<<<Object>>>", "", 3);
							LogHandler.writeLog("Category:           " + portals.get(i).category, "", 3);
							LogHandler.writeLog("Type:               " + portals.get(i).itype, "", 3);
							LogHandler.writeLog("Observation id:     " + portals.get(i).obsID, "", 3);
							LogHandler.writeLog("Position x:         " + portals.get(i).position.x, "", 3);
							LogHandler.writeLog("Position y:         " + portals.get(i).position.y, "", 3);
							LogHandler.writeLog("Squared distance:   " + portals.get(i).sqDist, "", 3);
							LogHandler.writeLog("----------", "", 3);
						}
					}
				}
			}
		}
		LogHandler.writeLog("Number of portals: " + portalsCounter, "", 3);

		int fromAvatarSpritesCounter = 0;
		if (fromAvatarSpritesPositions != null)
		{
			for (ArrayList<Observation> fromAvatarSprites : fromAvatarSpritesPositions)
			{
				if (fromAvatarSprites.size() > 0)
				{
					fromAvatarSpritesCounter += fromAvatarSprites.size();
					LogHandler.writeLog("Number of from avatar sprites of type " + fromAvatarSprites.get(0).itype + ": "
							+ fromAvatarSprites.size(), "", 3);

					if (extended)
					{
						for (int i = 0; i < fromAvatarSprites.size(); i++)
						{
							LogHandler.writeLog("<<<Object>>>", "", 3);
							LogHandler.writeLog("Category:           " + fromAvatarSprites.get(i).category, "", 3);
							LogHandler.writeLog("Type:               " + fromAvatarSprites.get(i).itype, "", 3);
							LogHandler.writeLog("Observation id:     " + fromAvatarSprites.get(i).obsID, "", 3);
							LogHandler.writeLog("Position x:         " + fromAvatarSprites.get(i).position.x, "", 3);
							LogHandler.writeLog("Position y:         " + fromAvatarSprites.get(i).position.y, "", 3);
							LogHandler.writeLog("Squared distance:   " + fromAvatarSprites.get(i).sqDist, "", 3);
							LogHandler.writeLog("----------", "", 3);
						}
					}
				}
			}
		}
		LogHandler.writeLog("Number of from avatar sprites: " + fromAvatarSpritesCounter, "", 3);

		LogHandler.writeLog("=====================================", "", 3);
		LogHandler.writeLog("     ", "", 3);
	}
}
