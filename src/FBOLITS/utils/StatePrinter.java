package FBOLITS.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

import OldNextLevel.LogHandler;
import core.game.Observation;
import core.game.StateObservation;

public class StatePrinter
{
	public void printGameState(StateObservation stateObs, int iAdvances, boolean extended)
	{
		writeLog("========== State report ==========", StatePrinter.class, 3);
		writeLog("Game tick: " + stateObs.getGameTick(), StatePrinter.class, 3);
		writeLog("Advance number: " + iAdvances, StatePrinter.class, 3);

		writeLog("Avatar position: " + stateObs.getAvatarPosition().x + ", " + stateObs.getAvatarPosition().y, StatePrinter.class, 3);

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
					writeLog("Number of NPCs of type " + npcs.get(0).itype + ": " + npcs.size(), StatePrinter.class, 3);

					if (extended)
					{
						for (int i = 0; i < npcs.size(); i++)
						{
							writeLog("<<<Object>>>", StatePrinter.class, 3);
							writeLog("Category:           " + npcs.get(i).category, StatePrinter.class, 3);
							writeLog("Type:               " + npcs.get(i).itype, StatePrinter.class, 3);
							writeLog("Observation id:     " + npcs.get(i).obsID, StatePrinter.class, 3);
							writeLog("Position x:         " + npcs.get(i).position.x, StatePrinter.class, 3);
							writeLog("Position y:         " + npcs.get(i).position.y, StatePrinter.class, 3);
							writeLog("Squared distance:   " + npcs.get(i).sqDist, StatePrinter.class, 3);
							writeLog("----------", StatePrinter.class, 3);
						}
					}
				}
			}
		}
		writeLog("Number of NPCs: " + npcCounter, StatePrinter.class, 3);

		int immovableCounter = 0;
		if (immovablePositions != null)
		{
			for (ArrayList<Observation> immovable : immovablePositions)
			{
				if (immovable.size() > 0)
				{
					immovableCounter += immovable.size();
					writeLog("Number of immovables of type " + immovable.get(0).itype + ": " + immovable.size(), StatePrinter.class, 3);

					if (extended)
					{
						for (int i = 0; i < immovable.size(); i++)
						{
							writeLog("<<<Object>>>", StatePrinter.class, 3);
							writeLog("Category:           " + immovable.get(i).category, StatePrinter.class, 3);
							writeLog("Type:               " + immovable.get(i).itype, StatePrinter.class, 3);
							writeLog("Observation id:     " + immovable.get(i).obsID, StatePrinter.class, 3);
							writeLog("Position x:         " + immovable.get(i).position.x, StatePrinter.class, 3);
							writeLog("Position y:         " + immovable.get(i).position.y, StatePrinter.class, 3);
							writeLog("Squared distance:   " + immovable.get(i).sqDist, StatePrinter.class, 3);
							writeLog("----------", StatePrinter.class, 3);
						}
					}
				}
			}
		}
		writeLog("Number of immovables: " + immovableCounter, StatePrinter.class, 3);

		int movableCounter = 0;
		if (movablePositions != null)
		{
			for (ArrayList<Observation> movable : movablePositions)
			{
				if (movable.size() > 0)
				{
					movableCounter += movable.size();
					writeLog("Number of movables of type " + movable.get(0).itype + ": " + movable.size(), StatePrinter.class, 3);

					if (extended)
					{
						for (int i = 0; i < movable.size(); i++)
						{
							writeLog("<<<Object>>>", StatePrinter.class, 3);
							writeLog("Category:           " + movable.get(i).category, StatePrinter.class, 3);
							writeLog("Type:               " + movable.get(i).itype, StatePrinter.class, 3);
							writeLog("Observation id:     " + movable.get(i).obsID, StatePrinter.class, 3);
							writeLog("Position x:         " + movable.get(i).position.x, StatePrinter.class, 3);
							writeLog("Position y:         " + movable.get(i).position.y, StatePrinter.class, 3);
							writeLog("Squared distance:   " + movable.get(i).sqDist, StatePrinter.class, 3);
							writeLog("----------", StatePrinter.class, 3);
						}
					}
				}
			}
		}
		writeLog("Number of movables: " + movableCounter, StatePrinter.class, 3);

		int resourcesCounter = 0;
		if (resourcesPositions != null)
		{
			for (ArrayList<Observation> resources : resourcesPositions)
			{
				if (resources.size() > 0)
				{
					resourcesCounter += resources.size();
					writeLog("Number of resources of type " + resources.get(0).itype + ": " + resources.size(), StatePrinter.class, 3);

					if (extended)
					{
						for (int i = 0; i < resources.size(); i++)
						{
							writeLog("<<<Object>>>", StatePrinter.class, 3);
							writeLog("Category:           " + resources.get(i).category, StatePrinter.class, 3);
							writeLog("Type:               " + resources.get(i).itype, StatePrinter.class, 3);
							writeLog("Observation id:     " + resources.get(i).obsID, StatePrinter.class, 3);
							writeLog("Position x:         " + resources.get(i).position.x, StatePrinter.class, 3);
							writeLog("Position y:         " + resources.get(i).position.y, StatePrinter.class, 3);
							writeLog("Squared distance:   " + resources.get(i).sqDist, StatePrinter.class, 3);
							writeLog("----------", StatePrinter.class, 3);
						}
					}
				}
			}
		}
		writeLog("Number of resources: " + resourcesCounter, StatePrinter.class, 3);

		int portalsCounter = 0;
		if (portalPositions != null)
		{
			for (ArrayList<Observation> portals : portalPositions)
			{
				if (portals.size() > 0)
				{
					portalsCounter += portals.size();
					writeLog("Number of portals of type " + portals.get(0).itype + ": " + portals.size(), StatePrinter.class, 3);

					if (extended)
					{
						for (int i = 0; i < portals.size(); i++)
						{
							writeLog("<<<Object>>>", StatePrinter.class, 3);
							writeLog("Category:           " + portals.get(i).category, StatePrinter.class, 3);
							writeLog("Type:               " + portals.get(i).itype, StatePrinter.class, 3);
							writeLog("Observation id:     " + portals.get(i).obsID, StatePrinter.class, 3);
							writeLog("Position x:         " + portals.get(i).position.x, StatePrinter.class, 3);
							writeLog("Position y:         " + portals.get(i).position.y, StatePrinter.class, 3);
							writeLog("Squared distance:   " + portals.get(i).sqDist, StatePrinter.class, 3);
							writeLog("----------", StatePrinter.class, 3);
						}
					}
				}
			}
		}
		writeLog("Number of portals: " + portalsCounter, StatePrinter.class, 3);

		int fromAvatarSpritesCounter = 0;
		if (fromAvatarSpritesPositions != null)
		{
			for (ArrayList<Observation> fromAvatarSprites : fromAvatarSpritesPositions)
			{
				if (fromAvatarSprites.size() > 0)
				{
					fromAvatarSpritesCounter += fromAvatarSprites.size();
					writeLog("Number of from avatar sprites of type " + fromAvatarSprites.get(0).itype + ": "
							+ fromAvatarSprites.size(), StatePrinter.class, 3);

					if (extended)
					{
						for (int i = 0; i < fromAvatarSprites.size(); i++)
						{
							writeLog("<<<Object>>>", StatePrinter.class, 3);
							writeLog("Category:           " + fromAvatarSprites.get(i).category, StatePrinter.class, 3);
							writeLog("Type:               " + fromAvatarSprites.get(i).itype, StatePrinter.class, 3);
							writeLog("Observation id:     " + fromAvatarSprites.get(i).obsID, StatePrinter.class, 3);
							writeLog("Position x:         " + fromAvatarSprites.get(i).position.x, StatePrinter.class, 3);
							writeLog("Position y:         " + fromAvatarSprites.get(i).position.y, StatePrinter.class, 3);
							writeLog("Squared distance:   " + fromAvatarSprites.get(i).sqDist, StatePrinter.class, 3);
							writeLog("----------", StatePrinter.class, 3);
						}
					}
				}
			}
		}
		writeLog("Number of from avatar sprites: " + fromAvatarSpritesCounter, StatePrinter.class, 3);

		writeLog("=====================================", StatePrinter.class, 3);
		writeLog("     ", StatePrinter.class, 3);
	}
	
	public void writeLog(String message, Class origin, int iWrite)
	{
		if (iWrite != 0)
		{
			Logger logger = LoggerFactory.getLogger(origin);
			logger.info(message);
		}
	}
}
