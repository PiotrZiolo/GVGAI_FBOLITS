package NextLevel;

import core.game.Observation;
//import core.game.StateObservation;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
//import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;

import NextLevel.StateHeuristic;

public class Agent extends AbstractMultiPlayer {
    int oppID; //player ID of the opponent
    int id; //ID of this player
    int no_players; //number of players in the game
    double epsilon = 1e-6;
    Random m_rnd;
    TreeMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap;
    StateHeuristic heuristic;
    Brain brain;

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

        heuristic =  new StateHeuristic(id, oppID, spriteTypeFeaturesMap);
        
        brain = new Brain();
		
		brain.learn(stateObs, elapsedTimer, playerID);
		
		printState(stateObs, 999);
    }

    /**
     *
     * Very simple one step lookahead agent.
     * Pass player ID to all state observation methods to query the right player.
     * Omitting the player ID will result in it being set to the default 0 (first player, whichever that is).
     *
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {

    	/* // Human player with reporting 
    	// int id = (getPlayerID() + 1) % stateObs.getNoPlayers();
    			Direction move = Utils.processMovementActionKeys(Game.ki.getMask(), id);
    			boolean useOn = Utils.processUseKey(Game.ki.getMask(), id);

    			// In the keycontroller, move has preference.
    			Types.ACTIONS action = Types.ACTIONS.fromVector(move);
    			if (action == Types.ACTIONS.ACTION_NIL && useOn)
    				action = Types.ACTIONS.ACTION_USE;

    			printState(stateObs, 0);

    			return action;
    			
    	*/    	
    	
        Types.ACTIONS bestAction = null;
        double maxQ = Double.NEGATIVE_INFINITY;

        //A random non-suicidal action by the opponent.
        Types.ACTIONS oppAction = getOppNotLosingAction(stateObs, id, oppID);

        for (Types.ACTIONS action : stateObs.getAvailableActions(id)) {

            StateObservationMulti stCopy = stateObs.copy();

            //need to provide actions for all players to advance the forward model
            Types.ACTIONS[] acts = new Types.ACTIONS[no_players];

            //set this agent's action
            acts[id] = action;
            acts[oppID] = oppAction;

            stCopy.advance(acts);

            double Q = heuristic.evaluateState(stCopy) + stateObs.getGameScore(oppID) - stateObs.getGameScore(id);
            Q = Utils.noise(Q, this.epsilon, this.m_rnd.nextDouble());

            //System.out.println("Action:" + action + " score:" + Q);
            if (Q > maxQ) {
                maxQ = Q;
                bestAction = action;
            }
        }

        //System.out.println("======== " + getPlayerID() + " " + maxQ + " " + bestAction + "============");
        //System.out.println(elapsedTimer.remainingTimeMillis());
        return bestAction;
    }

    //Returns an action, at random, that the opponent would make, assuming I do NIL, which wouldn't make it lose the game.
    private Types.ACTIONS getOppNotLosingAction(StateObservationMulti stm, int thisID, int oppID)
    {
        int no_players = stm.getNoPlayers();
        ArrayList<Types.ACTIONS> oppActions = stm.getAvailableActions(oppID);
        java.util.Collections.shuffle(oppActions);

        //Look for the opp actions that would not kill the opponent.
        for (Types.ACTIONS action : oppActions) {
            Types.ACTIONS[] acts = new Types.ACTIONS[no_players];
            acts[thisID] = Types.ACTIONS.ACTION_NIL;
            acts[oppID] = action;

            StateObservationMulti stCopy = stm.copy();
            stCopy.advance(acts);

            if(stCopy.getMultiGameWinner()[oppID] != Types.WINNER.PLAYER_LOSES)
            	return action;
        }
        
        return oppActions.get(new Random().nextInt(oppActions.size()));
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
