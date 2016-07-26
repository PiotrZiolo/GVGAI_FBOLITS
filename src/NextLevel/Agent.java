package NextLevel;

import core.game.Event;
import core.game.Observation;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

public class Agent extends AbstractMultiPlayer
{
	/*
	 * Chosen algorithm ID 1 - heuristic OneStep 2 - heuristic OLMCTS 3 - GA
	 */
	public static int algorithmID;
	public static int oppID; // player ID of the opponent
	public static int id; // ID of this player
	public static int no_players; // number of players in the game
	public static ArrayList<Types.ACTIONS> availableActions;
	private static double epsilon = 1e-6;
	private Random m_rnd;
	private static HashMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap;
	private static HashMap<Integer, int[][]> distanceMap;
	public static StateHeuristic heuristic;
	private static SingleMCTSPlayer mctsPlayer;
	private Brain brain;

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
		LogHandler.clearLog();
		m_rnd = new Random();
		spriteTypeFeaturesMap = new HashMap<Integer, SpriteTypeFeatures>();

		// get game information
		no_players = stateObs.getNoPlayers();
		id = playerID; // player ID of this agent
		oppID = (playerID + 1) % stateObs.getNoPlayers();
		availableActions = stateObs.getAvailableActions(Agent.id);

		// Fill spriteTypeFeaturesMap

		Types.ACTIONS[] acts = { Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL };
		StateObservationMulti stateObsCopy = stateObs.copy();
		stateObsCopy.advance(acts);
		
		printState(stateObs, 0, true);
		
		PerformanceMonitor.startNanoMeasure("First learning start", "Agent.Agent", 3);

		brain = new Brain(id);
		brain.learn(stateObsCopy, elapsedTimer, false, true, 0);
		
		PerformanceMonitor.finishNanoMeasure("First learning finish", "Agent.Agent", 3);

		spriteTypeFeaturesMap = brain.getSpriteTypeFeaturesMap();

		// After filling spriteTypeFeaturesMap
		double[] weights = new double[] { 0.05, 0.04, 0.04, 0.02, 0.02, 0.02, 0.1, 0.1, 0.01, 0.01 }; // 10 weights
		double pointScale = 10;
		heuristic = new StateHeuristic(id, oppID, spriteTypeFeaturesMap, weights, pointScale,
				stateObs.getWorldDimension());

		// choose algorithmID to play the game
		algorithmID = 2;

		switch (algorithmID)
		{
			case 1:
				// no oneStepLookAhead specific initialization needed
				break;
			case 2:
				mctsPlayer = new SingleMCTSPlayer(new Random());
				break;
			case 3:
				// GA specific initialization
				break;
		}
	}

	/**
	 * Picks an action using chosen algorithm. This function is called every
	 * game step to request an action from the player.
	 * 
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 * @return An action for the current state
	 */
	public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer)
	{
		/*
		 * // Human player with reporting // int id = (getPlayerID() + 1) %
		 * stateObs.getNoPlayers(); Direction move =
		 * Utils.processMovementActionKeys(Game.ki.getMask(), id); boolean useOn
		 * = Utils.processUseKey(Game.ki.getMask(), id);
		 * 
		 * // In the keycontroller, move has preference. Types.ACTIONS action =
		 * Types.ACTIONS.fromVector(move); if (action ==
		 * Types.ACTIONS.ACTION_NIL && useOn) action = Types.ACTIONS.ACTION_USE;
		 * 
		 * printState(stateObs, 0);
		 * 
		 * return action;
		 * 
		 */

		PerformanceMonitor.startNanoMeasure("Act learning start", "Agent.act", 3);
		brain.learn(stateObs, elapsedTimer, false, false, 1);
		TreeSet<Event> eventsHistory = stateObs.getEventsHistory();
		Iterator<Event> eventsIterator = eventsHistory.descendingIterator();
		Event event;

		if (eventsIterator.hasNext())
		{
			event = eventsIterator.next();
			if (event.gameStep == stateObs.getGameTick() - 1)
			{
				brain.learn(stateObs, elapsedTimer, false, false, 2, 2);
			}
		}

		PerformanceMonitor.finishNanoMeasure("Act learning finish", "Agent.act", 3);

		switch (algorithmID)
		{
			case 1:
				return oneStepLookAhead(stateObs);
			case 2:
				return heuristicOLMCTS(stateObs, elapsedTimer);
			case 3:
				return geneticAlgorithm(stateObs, elapsedTimer);
			default: // just in case :)
				return random(stateObs);
		}
	}

	/**
	 *
	 * Very simple one step lookahead agent. Pass player ID to all state
	 * observation methods to query the right player. Omitting the player ID
	 * will result in it being set to the default 0 (first player, whichever
	 * that is).
	 *
	 * @param stateObs
	 *            Observation of the current state.
	 * @return An action for the current state
	 */
	public Types.ACTIONS oneStepLookAhead(StateObservationMulti stateObs)
	{
		Types.ACTIONS bestAction = null;
		double maxQ = Double.NEGATIVE_INFINITY;

		// A random non-suicidal action by the opponent.
		Types.ACTIONS oppAction = getOppNotLosingAction(stateObs);
		double currentScore = heuristic.evaluateState(stateObs) + stateObs.getGameScore(id);

		for (Types.ACTIONS action : stateObs.getAvailableActions(id))
		{
			StateObservationMulti stCopy = stateObs.copy();

			// need to provide actions for all players to advance the forward
			// model
			Types.ACTIONS[] acts = new Types.ACTIONS[no_players];

			// set this agent's action
			acts[id] = action;
			acts[oppID] = oppAction;

			stCopy.advance(acts);

			double Q = heuristic.evaluateState(stCopy) + stateObs.getGameScore(id) - currentScore;
			// System.out.println("Action: " + action + ", score: " + Q);
			Q = Utils.noise(Q, Agent.epsilon, this.m_rnd.nextDouble());

			if (Q > maxQ)
			{
				maxQ = Q;
				bestAction = action;
			}
		}
		return bestAction;
	}

	/**
	 * Returns an action, at random, that the opponent would make, assuming I do
	 * NIL, which wouldn't make it lose the game.
	 */
	private Types.ACTIONS getOppNotLosingAction(StateObservationMulti state)
	{
		int no_players = state.getNoPlayers();
		ArrayList<Types.ACTIONS> oppActions = state.getAvailableActions(oppID);
		java.util.Collections.shuffle(oppActions);

		// Look for the opponent actions that would not kill him.
		for (Types.ACTIONS action : oppActions)
		{
			Types.ACTIONS[] acts = new Types.ACTIONS[no_players];
			acts[id] = Types.ACTIONS.ACTION_NIL;
			acts[oppID] = action;

			StateObservationMulti stateCopy = state.copy();
			stateCopy.advance(acts);

			if (stateCopy.getMultiGameWinner()[oppID] != Types.WINNER.PLAYER_LOSES)
				return action;
		}

		return oppActions.get(new Random().nextInt(oppActions.size()));
	}

	/**
	 * Open Loop Monte Carlo Tree Search using position-based heuristics in
	 * evaluation function.
	 *
	 * @param stateObs
	 *            Observation of the current state.
	 * @param elapsedTimer
	 *            Timer when the action returned is due.
	 * @return An action for the current state
	 */
	public Types.ACTIONS heuristicOLMCTS(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer)
	{

		// Set the state observation object as the new root of the tree.
		int ROLLOUT_DEPTH = 4;
		double K = Math.sqrt(2);
		mctsPlayer.init(stateObs, ROLLOUT_DEPTH, K);

		// Determine the action using MCTS and return it
		return mctsPlayer.run(elapsedTimer);
	}

	/**
	 * Piotr Nojszewski part
	 */
	public Types.ACTIONS geneticAlgorithm(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer)
	{
		return Types.ACTIONS.ACTION_NIL;
	}

	/**
	 * Chooses random available action
	 *
	 * @param stateObs
	 *            Observation of the current state.
	 * @return An action for the current state
	 */
	public Types.ACTIONS random(StateObservationMulti stateObs)
	{
		ArrayList<ACTIONS> actions = stateObs.getAvailableActions(id);
		return actions.get(new Random().nextInt(actions.size()));
	}

	private void printState(StateObservationMulti so, int iAdvances, boolean extended)
	{
		LogHandler.writeLog("========== Player " + id + " report ==========", "Agent.printState", 3);
		LogHandler.writeLog("Game tick: " + so.getGameTick(), "Agent.printState", 3);
		LogHandler.writeLog("Advance number: " + iAdvances, "Agent.printState", 3);

		LogHandler.writeLog("Avatar position: " + so.getAvatarPosition(id).x + ", " + so.getAvatarPosition(id).y, "Agent.printState", 3);

		//Vector2d avatarPosition = so.getAvatarPosition(id);
		ArrayList<Observation>[] npcPositions = so.getNPCPositions();
		ArrayList<Observation>[] immovablePositions = so.getImmovablePositions();
		ArrayList<Observation>[] movablePositions = so.getMovablePositions();
		ArrayList<Observation>[] resourcesPositions = so.getResourcesPositions();
		ArrayList<Observation>[] portalPositions = so.getPortalsPositions();
		ArrayList<Observation>[] fromAvatarSpritesPositions = so.getFromAvatarSpritesPositions();

		int npcCounter = 0;
		if (npcPositions != null)
		{
			for (ArrayList<Observation> npcs : npcPositions)
			{
				if (npcs.size() > 0)
				{
					npcCounter += npcs.size();
					LogHandler.writeLog("Number of NPCs of type " + npcs.get(0).itype + ": " + npcs.size(), "Agent.printState", 3);

					if (extended)
					{
						for (int i = 0; i < npcs.size(); i++)
						{
							LogHandler.writeLog("<<<Object>>>", "Agent.printState", 3);
							LogHandler.writeLog("Category:           " + npcs.get(i).category, "Agent.printState", 3);
							LogHandler.writeLog("Type:               " + npcs.get(i).itype, "Agent.printState", 3);
							LogHandler.writeLog("Observation id:     " + npcs.get(i).obsID, "Agent.printState", 3);
							LogHandler.writeLog("Position x:         " + npcs.get(i).position.x, "Agent.printState", 3);
							LogHandler.writeLog("Position y:         " + npcs.get(i).position.y, "Agent.printState", 3);
							LogHandler.writeLog("Squared distance:   " + npcs.get(i).sqDist, "Agent.printState", 3);
							LogHandler.writeLog("----------", "Agent.printState", 3);
						}
					}
				}
			}
		}
		LogHandler.writeLog("Number of NPCs: " + npcCounter, "Agent.printState", 3);

		int immovableCounter = 0;
		if (immovablePositions != null)
		{
			for (ArrayList<Observation> immovable : immovablePositions)
			{
				if (immovable.size() > 0)
				{
					immovableCounter += immovable.size();
					LogHandler.writeLog("Number of immovables of type " + immovable.get(0).itype + ": " + immovable.size(), "Agent.printState", 3);

					if (extended)
					{
						for (int i = 0; i < immovable.size(); i++)
						{
							LogHandler.writeLog("<<<Object>>>", "Agent.printState", 3);
							LogHandler.writeLog("Category:           " + immovable.get(i).category, "Agent.printState", 3);
							LogHandler.writeLog("Type:               " + immovable.get(i).itype, "Agent.printState", 3);
							LogHandler.writeLog("Observation id:     " + immovable.get(i).obsID, "Agent.printState", 3);
							LogHandler.writeLog("Position x:         " + immovable.get(i).position.x, "Agent.printState", 3);
							LogHandler.writeLog("Position y:         " + immovable.get(i).position.y, "Agent.printState", 3);
							LogHandler.writeLog("Squared distance:   " + immovable.get(i).sqDist, "Agent.printState", 3);
							LogHandler.writeLog("----------", "Agent.printState", 3);
						}
					}
				}
			}
		}
		LogHandler.writeLog("Number of immovables: " + immovableCounter, "Agent.printState", 3);

		int movableCounter = 0;
		if (movablePositions != null)
		{
			for (ArrayList<Observation> movable : movablePositions)
			{
				if (movable.size() > 0)
				{
					movableCounter += movable.size();
					LogHandler.writeLog("Number of movables of type " + movable.get(0).itype + ": " + movable.size(), "Agent.printState", 3);

					if (extended)
					{
						for (int i = 0; i < movable.size(); i++)
						{
							LogHandler.writeLog("<<<Object>>>", "Agent.printState", 3);
							LogHandler.writeLog("Category:           " + movable.get(i).category, "Agent.printState", 3);
							LogHandler.writeLog("Type:               " + movable.get(i).itype, "Agent.printState", 3);
							LogHandler.writeLog("Observation id:     " + movable.get(i).obsID, "Agent.printState", 3);
							LogHandler.writeLog("Position x:         " + movable.get(i).position.x, "Agent.printState", 3);
							LogHandler.writeLog("Position y:         " + movable.get(i).position.y, "Agent.printState", 3);
							LogHandler.writeLog("Squared distance:   " + movable.get(i).sqDist, "Agent.printState", 3);
							LogHandler.writeLog("----------", "Agent.printState", 3);
						}
					}
				}
			}
		}
		LogHandler.writeLog("Number of movables: " + movableCounter, "Agent.printState", 3);

		int resourcesCounter = 0;
		if (resourcesPositions != null)
		{
			for (ArrayList<Observation> resources : resourcesPositions)
			{
				if (resources.size() > 0)
				{
					resourcesCounter += resources.size();
					LogHandler.writeLog("Number of resources of type " + resources.get(0).itype + ": " + resources.size(), "Agent.printState", 3);

					if (extended)
					{
						for (int i = 0; i < resources.size(); i++)
						{
							LogHandler.writeLog("<<<Object>>>", "Agent.printState", 3);
							LogHandler.writeLog("Category:           " + resources.get(i).category, "Agent.printState", 3);
							LogHandler.writeLog("Type:               " + resources.get(i).itype, "Agent.printState", 3);
							LogHandler.writeLog("Observation id:     " + resources.get(i).obsID, "Agent.printState", 3);
							LogHandler.writeLog("Position x:         " + resources.get(i).position.x, "Agent.printState", 3);
							LogHandler.writeLog("Position y:         " + resources.get(i).position.y, "Agent.printState", 3);
							LogHandler.writeLog("Squared distance:   " + resources.get(i).sqDist, "Agent.printState", 3);
							LogHandler.writeLog("----------", "Agent.printState", 3);
						}
					}
				}
			}
		}
		LogHandler.writeLog("Number of resources: " + resourcesCounter, "Agent.printState", 3);

		int portalsCounter = 0;
		if (portalPositions != null)
		{
			for (ArrayList<Observation> portals : portalPositions)
			{
				if (portals.size() > 0)
				{
					portalsCounter += portals.size();
					LogHandler.writeLog("Number of portals of type " + portals.get(0).itype + ": " + portals.size(), "Agent.printState", 3);

					if (extended)
					{
						for (int i = 0; i < portals.size(); i++)
						{
							LogHandler.writeLog("<<<Object>>>", "Agent.printState", 3);
							LogHandler.writeLog("Category:           " + portals.get(i).category, "Agent.printState", 3);
							LogHandler.writeLog("Type:               " + portals.get(i).itype, "Agent.printState", 3);
							LogHandler.writeLog("Observation id:     " + portals.get(i).obsID, "Agent.printState", 3);
							LogHandler.writeLog("Position x:         " + portals.get(i).position.x, "Agent.printState", 3);
							LogHandler.writeLog("Position y:         " + portals.get(i).position.y, "Agent.printState", 3);
							LogHandler.writeLog("Squared distance:   " + portals.get(i).sqDist, "Agent.printState", 3);
							LogHandler.writeLog("----------", "Agent.printState", 3);
						}
					}
				}
			}
		}
		LogHandler.writeLog("Number of portals: " + portalsCounter, "Agent.printState", 3);

		int fromAvatarSpritesCounter = 0;
		if (fromAvatarSpritesPositions != null)
		{
			for (ArrayList<Observation> fromAvatarSprites : fromAvatarSpritesPositions)
			{
				if (fromAvatarSprites.size() > 0)
				{
					fromAvatarSpritesCounter += fromAvatarSprites.size();
					LogHandler.writeLog("Number of from avatar sprites of type " + fromAvatarSprites.get(0).itype + ": "
							+ fromAvatarSprites.size(), "Agent.printState", 3);

					if (extended)
					{
						for (int i = 0; i < fromAvatarSprites.size(); i++)
						{
							LogHandler.writeLog("<<<Object>>>", "Agent.printState", 3);
							LogHandler.writeLog("Category:           " + fromAvatarSprites.get(i).category, "Agent.printState", 3);
							LogHandler.writeLog("Type:               " + fromAvatarSprites.get(i).itype, "Agent.printState", 3);
							LogHandler.writeLog("Observation id:     " + fromAvatarSprites.get(i).obsID, "Agent.printState", 3);
							LogHandler.writeLog("Position x:         " + fromAvatarSprites.get(i).position.x, "Agent.printState", 3);
							LogHandler.writeLog("Position y:         " + fromAvatarSprites.get(i).position.y, "Agent.printState", 3);
							LogHandler.writeLog("Squared distance:   " + fromAvatarSprites.get(i).sqDist, "Agent.printState", 3);
							LogHandler.writeLog("----------", "Agent.printState", 3);
						}
					}
				}
			}
		}
		LogHandler.writeLog("Number of from avatar sprites: " + fromAvatarSpritesCounter, "Agent.printState", 3);

		LogHandler.writeLog("=====================================", "Agent.printState", 3);
		LogHandler.writeLog("     ", "Agent.printState", 3);
	}
}
