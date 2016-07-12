package NextLevel;

import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

import java.util.ArrayList;
import java.util.Random;

import NextLevel.StateHeuristic;

public class heuristicTreeSearch extends AbstractMultiPlayer {
    int oppID; //player ID of the opponent
    int id; //ID of this player
    int no_players; //number of players in the game
    double epsilon = 1e-6;
    Random m_rnd;

    /**
     * initialize all variables for the agent
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @param playerID ID if this agent
     */
    public heuristicTreeSearch(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer, int playerID) {
        m_rnd = new Random();

        //get game information
        no_players = stateObs.getNoPlayers();
        id = playerID; //player ID of this agent
        oppID = (playerID + 1) % stateObs.getNoPlayers();
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
    	return Types.ACTIONS.ACTION_NIL;
    }
}