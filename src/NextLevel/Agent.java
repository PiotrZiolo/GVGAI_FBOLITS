package NextLevel;

import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Utils;
import NextLevel.SpriteTypeFeatures;
import NextLevel.heuristicOLMCTS.SingleMCTSPlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import NextLevel.StateHeuristic;

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
	public static double epsilon = 1e-6;
    Random m_rnd;
    public static TreeMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap;
    public static HashMap<Integer,int[][]> distanceMap;
    public static StateHeuristic heuristic;
    public static SingleMCTSPlayer mctsPlayer;

    /**
     * initialize all variables for the agent
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @param playerID ID if this agent
     */
    public Agent(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
        m_rnd = new Random();
        spriteTypeFeaturesMap = new TreeMap<Integer, SpriteTypeFeatures>();

        //get game information
        no_players = stateObs.getNoPlayers();
        id = playerID; //player ID of this agent
        oppID = (playerID + 1) % stateObs.getNoPlayers();
        
        // after filling spriteTypeFeaturesMap
        double[] weights = new double[] {1, 1, 1, 1, 1, 1, 1, 1};	// 8 weights
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
	
	    for (Types.ACTIONS action : stateObs.getAvailableActions(id)) {
	
	        StateObservationMulti stCopy = stateObs.copy();
	
	        //need to provide actions for all players to advance the forward model
	        Types.ACTIONS[] acts = new Types.ACTIONS[no_players];
	
	        //set this agent's action
	        acts[id] = action;
	        acts[oppID] = oppAction;
	
	        stCopy.advance(acts);
	
	        double Q = heuristic.evaluateState(stCopy) + stateObs.getGameScore(oppID) - stateObs.getGameScore(id);
	        //System.out.println("Action " + action + ", score " + Q);
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
    	int ROLLOUT_DEPTH = 10;
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
}
