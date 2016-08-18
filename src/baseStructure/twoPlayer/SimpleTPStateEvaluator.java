package baseStructure.twoPlayer;

import core.game.Observation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.HashMap;

import baseStructure.StateEvaluator;
import baseStructure.StateHandler;

public class SimpleTPStateEvaluator extends StateEvaluator
{
	double initialNpcCounter = 0;

	public SimpleTPStateEvaluator()
	{
		
	}
	
	public SimpleTPStateEvaluator(TPGameKnowledge gameKnowledge, StateHandler stateHandler)
	{
		this.gameKnowledge = gameKnowledge;
		this.stateHandler = stateHandler;
	}

	public double evaluateState(StateObservation stateObs)
	{
		TPGameKnowledge tpGameKnowledge = (TPGameKnowledge) this.gameKnowledge;
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		int playerID = tpGameKnowledge.getPlayerID();
		
		Vector2d avatarPosition = stateObsMulti.getAvatarPosition(playerID);
		ArrayList<Observation>[] npcPositions = stateObsMulti.getNPCPositions(avatarPosition);
		ArrayList<Observation>[] portalPositions = stateObsMulti.getPortalsPositions(avatarPosition);
		HashMap<Integer, Integer> resources = stateObsMulti.getAvatarResources(playerID);

		ArrayList<Observation>[] npcPositionsNotSorted = stateObsMulti.getNPCPositions();

		double won = 0;
		int oppID = (playerID + 1) % stateObsMulti.getNoPlayers();
		Types.WINNER[] winners = stateObsMulti.getMultiGameWinner();

		boolean bothWin = (winners[playerID] == Types.WINNER.PLAYER_WINS)
				&& (winners[oppID] == Types.WINNER.PLAYER_WINS);
		boolean meWins = (winners[playerID] == Types.WINNER.PLAYER_WINS)
				&& (winners[oppID] == Types.WINNER.PLAYER_LOSES);
		boolean meLoses = (winners[playerID] == Types.WINNER.PLAYER_LOSES)
				&& (winners[oppID] == Types.WINNER.PLAYER_WINS);
		boolean bothLose = (winners[playerID] == Types.WINNER.PLAYER_LOSES)
				&& (winners[oppID] == Types.WINNER.PLAYER_LOSES);

		if (meWins || bothWin)
			won = 1000000000;
		else if (meLoses)
			return -999999999;

		// if (winners[playerID] == Types.WINNER.PLAYER_WINS) {
		// won = 1000000000;
		// } else if (winners[playerID] == Types.WINNER.PLAYER_LOSES) {
		// return -999999999;
		// }

		double minDistance = Double.POSITIVE_INFINITY;
		Vector2d minObject = null;
		int minNPC_ID = -1;
		int minNPCType = -1;

		int npcCounter = 0;
		if (npcPositions != null)
		{
			for (ArrayList<Observation> npcs : npcPositions)
			{
				if (npcs.size() > 0)
				{
					minObject = npcs.get(0).position; // This is the closest guy
					minDistance = npcs.get(0).sqDist; // This is the (square) distance to the closest NPC.
					minNPC_ID = npcs.get(0).obsID; // This is the id of the closest NPC.
					minNPCType = npcs.get(0).itype; // This is the type of the closest NPC.
					npcCounter += npcs.size();
				}
			}
		}

		if (portalPositions == null)
		{

			double score = 0;
			if (npcCounter == 0)
			{
				score = stateObsMulti.getGameScore(playerID) + won * 100000000;
			}
			else
			{
				score = -minDistance / 100.0 + (-npcCounter) * 100.0 + stateObsMulti.getGameScore(playerID)
						+ won * 100000000;
			}

			return score;
		}

		double minDistancePortal = Double.POSITIVE_INFINITY;
		Vector2d minObjectPortal = null;
		for (ArrayList<Observation> portals : portalPositions)
		{
			if (portals.size() > 0)
			{
				minObjectPortal = portals.get(0).position; // This is the closest portal
				minDistancePortal = portals.get(0).sqDist; // This is the (square) distance to the closest portal
			}
		}

		double score = 0;
		if (minObjectPortal == null)
		{
			score = stateObsMulti.getGameScore() + won * 100000000;
		}
		else
		{
			score = stateObsMulti.getGameScore() + won * 1000000 - minDistancePortal * 10.0;
		}

		return score;
	}

}
