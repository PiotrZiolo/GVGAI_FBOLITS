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

public class Agent extends AbstractMultiPlayer {
	/*
	 * Chosen algorithm ID
	 * 1 - heuristic OneStep
	 * 2 - heuristic OLMCTS
	 * 3 - GA
	 */
	public static int algorithmID;
	public static int oppID; //player ID of the opponent
	public static int id; //ID of this player
	public static int no_players; //number of players in the game
	public static ArrayList<Types.ACTIONS> availableActions;
	private static double epsilon = 1e-6;
	private Random m_rnd;
	private static HashMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap;
	private static HashMap<Integer,int[][]> distanceMap;
	public static StateHeuristic heuristic;
	private static SingleMCTSPlayer mctsPlayer;
    private Brain brain;

    /**
     * initialize all variables for the agent
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @param playerID ID if this agent
     */
    public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
        m_rnd = new Random();
        spriteTypeFeaturesMap = new HashMap<Integer, SpriteTypeFeatures>();

        //get game information
        no_players = stateObs.getNoPlayers();
        id = playerID; //player ID of this agent
        oppID = (playerID + 1) % stateObs.getNoPlayers();
        availableActions = stateObs.getAvailableActions(Agent.id);
        
        // Fill spriteTypeFeaturesMap
     	
     	Types.ACTIONS[] acts = {Types.ACTIONS.ACTION_NIL, Types.ACTIONS.ACTION_NIL};
     	StateObservationMulti stateObsCopy = stateObs.copy();
     	stateObsCopy.advance(acts);

     	brain = new Brain(id);
     	brain.learn(stateObsCopy, elapsedTimer, false, true, 0);

     	/*System.out.println(stateObs.getNPCPositions(stateObs.getAvatarPosition(id)));
     	System.out.println(stateObs.getPortalsPositions(stateObs.getAvatarPosition(id)));
     	System.out.println(stateObs.getImmovablePositions(stateObs.getAvatarPosition(id)).length);
     	System.out.println(stateObs.getMovablePositions(stateObs.getAvatarPosition(id)));
     	System.out.println(stateObs.getResourcesPositions(stateObs.getAvatarPosition(id)));
     	System.out.println(stateObs.getFromAvatarSpritesPositions(stateObs.getAvatarPosition(id)));*/
     	//Observation obs = stateObs.getPortalsPositions(stateObs.getAvatarPosition(id))[0].get(0);
     	//brain.approachSprite(stateObs, obs);

     	spriteTypeFeaturesMap = brain.getSpriteTypeFeaturesMap();

     	// After filling spriteTypeFeaturesMap
        double[] weights = new double[] {0.05, 0.04, 0.04, 0.02, 0.02, 0.02, 0.01, 0.1};	// 8 weights
        double pointScale = 10;
        heuristic =  new StateHeuristic(id, oppID, spriteTypeFeaturesMap, weights, pointScale, stateObs.getWorldDimension());
        
        // choose algorithmID to play the game
        algorithmID = 2;
        
    	switch (algorithmID) {
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
     * Picks an action using chosen algorithm. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
    	
    	/*
		 * // Human player with reporting 
		 * // int id = (getPlayerID() + 1) % stateObs.getNoPlayers(); 
		 * Direction move = Utils.processMovementActionKeys(Game.ki.getMask(), id); 
		 * boolean useOn = Utils.processUseKey(Game.ki.getMask(), id);
		 * 
		 * // In the keycontroller, move has preference. 
		 * Types.ACTIONS action = Types.ACTIONS.fromVector(move); 
		 * if (action == Types.ACTIONS.ACTION_NIL && useOn) action = Types.ACTIONS.ACTION_USE;
		 * 
		 * printState(stateObs, 0);
		 * 
		 * return action;
		 * 
		 */
    	
    	//printState(stateObs, 0, true);
    	
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
    	
    	switch (algorithmID) {
	    	case 1:
	    		return oneStepLookAhead(stateObs);
	    	case 2:
	    		return heuristicOLMCTS(stateObs, elapsedTimer);
	    	case 3:
	    		return geneticAlgorithm(stateObs, elapsedTimer);
    		default:	// just in case :)
    			return random(stateObs);
    	}
    }
    
    /**
     *
     * Very simple one step lookahead agent.
     * Pass player ID to all state observation methods to query the right player.
     * Omitting the player ID will result in it being set to the default 0 (first player, whichever that is).
     *
     * @param stateObs Observation of the current state.
     * @return An action for the current state
     */
    public Types.ACTIONS oneStepLookAhead(StateObservationMulti stateObs) {
        
	    Types.ACTIONS bestAction = null;
	    double maxQ = Double.NEGATIVE_INFINITY;
	
	    //A random non-suicidal action by the opponent.
	    Types.ACTIONS oppAction = getOppNotLosingAction(stateObs);
	    double currentScore = heuristic.evaluateState(stateObs) + stateObs.getGameScore(id);
	
	    for (Types.ACTIONS action : stateObs.getAvailableActions(id)) {
	
	        StateObservationMulti stCopy = stateObs.copy();
	
	        //need to provide actions for all players to advance the forward model
	        Types.ACTIONS[] acts = new Types.ACTIONS[no_players];
	
	        //set this agent's action
	        acts[id] = action;
	        acts[oppID] = oppAction;
	
	        stCopy.advance(acts);
	
	        double Q = heuristic.evaluateState(stCopy) + stateObs.getGameScore(oppID) - currentScore;
	        Q = Utils.noise(Q, Agent.epsilon, this.m_rnd.nextDouble());
	
	        //System.out.println("Action:" + action + " score:" + Q);
	        if (Q > maxQ) {
	            maxQ = Q;
	            bestAction = action;
	        }
	    }
	    return bestAction;
    }
    
    /**
    *Returns an action, at random, that the opponent would make,
    *assuming I do NIL, which wouldn't make it lose the game.
    */
    private Types.ACTIONS getOppNotLosingAction(StateObservationMulti state)
    {
        int no_players = state.getNoPlayers();
        ArrayList<Types.ACTIONS> oppActions = state.getAvailableActions(oppID);
        java.util.Collections.shuffle(oppActions);

        //Look for the opponent actions that would not kill him.
        for (Types.ACTIONS action : oppActions) {
            Types.ACTIONS[] acts = new Types.ACTIONS[no_players];
            acts[id] = Types.ACTIONS.ACTION_NIL;
            acts[oppID] = action;

            StateObservationMulti stateCopy = state.copy();
            stateCopy.advance(acts);

            if(stateCopy.getMultiGameWinner()[oppID] != Types.WINNER.PLAYER_LOSES)
            	return action;
        }

        return oppActions.get(new Random().nextInt(oppActions.size()));
	}
    
    /**
     * Open Loop Monte Carlo Tree Search using position-based heuristics in evaluation function.
     *
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS heuristicOLMCTS(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {

        //Set the state observation object as the new root of the tree.
    	int ROLLOUT_DEPTH = 4;
    	double K = Math.sqrt(2);
        mctsPlayer.init(stateObs, ROLLOUT_DEPTH, K);

        //Determine the action using MCTS and return it
        return mctsPlayer.run(elapsedTimer);
    }

    /**
     * Piotr Nojszewski part
     */
    public Types.ACTIONS geneticAlgorithm(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
    	return Types.ACTIONS.ACTION_NIL;
    }

    /**
     * Chooses random available action
     *
     * @param stateObs Observation of the current state.
     * @return An action for the current state
     */
    public Types.ACTIONS random(StateObservationMulti stateObs) {
		ArrayList<ACTIONS> actions = stateObs.getAvailableActions(id);
		return actions.get(new Random().nextInt(actions.size()));
    }
    
    private void printState(StateObservationMulti so, int iAdvances, boolean extended)
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

					if (extended)
					{
						for (int i = 0; i < npcs.size(); i++)
						{
							System.out.println("<<<Object>>>");
							System.out.println("Category:           " + npcs.get(i).category);
							System.out.println("Type:               " + npcs.get(i).itype);
							System.out.println("Observation id:     " + npcs.get(i).obsID);
							System.out.println("Position x:         " + npcs.get(i).position.x);
							System.out.println("Position y:         " + npcs.get(i).position.y);
							System.out.println("Squared distance:   " + npcs.get(i).sqDist);
							System.out.println("----------");
						}
					}
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

					if (extended)
					{
						for (int i = 0; i < immovable.size(); i++)
						{
							System.out.println("<<<Object>>>");
							System.out.println("Category:           " + immovable.get(i).category);
							System.out.println("Type:               " + immovable.get(i).itype);
							System.out.println("Observation id:     " + immovable.get(i).obsID);
							System.out.println("Position x:         " + immovable.get(i).position.x);
							System.out.println("Position y:         " + immovable.get(i).position.y);
							System.out.println("Squared distance:   " + immovable.get(i).sqDist);
							System.out.println("----------");
						}
					}
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

					if (extended)
					{
						for (int i = 0; i < movable.size(); i++)
						{
							System.out.println("<<<Object>>>");
							System.out.println("Category:           " + movable.get(i).category);
							System.out.println("Type:               " + movable.get(i).itype);
							System.out.println("Observation id:     " + movable.get(i).obsID);
							System.out.println("Position x:         " + movable.get(i).position.x);
							System.out.println("Position y:         " + movable.get(i).position.y);
							System.out.println("Squared distance:   " + movable.get(i).sqDist);
							System.out.println("----------");
						}
					}
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

					if (extended)
					{
						for (int i = 0; i < resources.size(); i++)
						{
							System.out.println("<<<Object>>>");
							System.out.println("Category:           " + resources.get(i).category);
							System.out.println("Type:               " + resources.get(i).itype);
							System.out.println("Observation id:     " + resources.get(i).obsID);
							System.out.println("Position x:         " + resources.get(i).position.x);
							System.out.println("Position y:         " + resources.get(i).position.y);
							System.out.println("Squared distance:   " + resources.get(i).sqDist);
							System.out.println("----------");
						}
					}
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

					if (extended)
					{
						for (int i = 0; i < portals.size(); i++)
						{
							System.out.println("<<<Object>>>");
							System.out.println("Category:           " + portals.get(i).category);
							System.out.println("Type:               " + portals.get(i).itype);
							System.out.println("Observation id:     " + portals.get(i).obsID);
							System.out.println("Position x:         " + portals.get(i).position.x);
							System.out.println("Position y:         " + portals.get(i).position.y);
							System.out.println("Squared distance:   " + portals.get(i).sqDist);
							System.out.println("----------");
						}
					}
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

					if (extended)
					{
						for (int i = 0; i < fromAvatarSprites.size(); i++)
						{
							System.out.println("<<<Object>>>");
							System.out.println("Category:           " + fromAvatarSprites.get(i).category);
							System.out.println("Type:               " + fromAvatarSprites.get(i).itype);
							System.out.println("Observation id:     " + fromAvatarSprites.get(i).obsID);
							System.out.println("Position x:         " + fromAvatarSprites.get(i).position.x);
							System.out.println("Position y:         " + fromAvatarSprites.get(i).position.y);
							System.out.println("Squared distance:   " + fromAvatarSprites.get(i).sqDist);
							System.out.println("----------");
						}
					}
				}
			}
		}
		System.out.println("Number of from avatar sprites: " + fromAvatarSpritesCounter);

		System.out.println("=====================================");
		System.out.println("");
	}
}
