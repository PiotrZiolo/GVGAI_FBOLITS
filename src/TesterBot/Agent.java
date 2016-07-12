package TesterBot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import core.game.Game;
import core.game.Observation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.Direction;
import tools.ElapsedCpuTimer;
import tools.Utils;
import tools.Vector2d;

/**
 * Created by diego on 06/02/14.
 */
public class Agent extends AbstractMultiPlayer
{
    int id; //this player's ID
    int turn = 0;
    int direction = 0;
    Random rd;
    int firstTurns = 0;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     * @param playerID ID if this agent
     */
    public Agent(StateObservationMulti so, ElapsedCpuTimer elapsedTimer, int playerID)
    {
        id = playerID;
        rd = new Random();
        Types.ACTIONS action = Types.ACTIONS.ACTION_NIL;
        
        int iAdvances = 0;
    	
    	while (elapsedTimer.remainingTimeMillis() > 0)
    	{
    		ArrayList<ACTIONS> actions = so.getAvailableActions(id);

    		if (actions.size() > 0)
    		{
    			actions.get(rd.nextInt(actions.size()));
    		}
    		else
    		{
    			action = Types.ACTIONS.ACTION_NIL;
    		}
    		so.advance(action);
    		iAdvances++;
    		
    		if (iAdvances <= 99000 && iAdvances % 1000 == 0)
    		{
    			printState(so, iAdvances);
    		}
    	}
    	System.out.println("Player " + id + " | Initialization advance operations: " + iAdvances);
    	System.out.println("");
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer)
    {
    	Types.ACTIONS action = Types.ACTIONS.ACTION_USE;

    	/*
    	if (turn == 0)
    	{
    		switch (direction)
    		{
    			case 0:
    				action = Types.ACTIONS.ACTION_UP;
    				break;
    			case 1:
    				action = Types.ACTIONS.ACTION_RIGHT;
    				break;
    			case 2:
    				action = Types.ACTIONS.ACTION_DOWN;
    				break;
    			case 3:
    				action = Types.ACTIONS.ACTION_LEFT;
    				break;
    		}
    		
    		direction = (direction + 1) % 4;
    	}
    	else
    	{
    		action = Types.ACTIONS.ACTION_USE;
    	}
    	
    	turn = (turn + 1) % 30;
    	*/
    	
    	int iAdvances = 0;
    	
    	printState(stateObs, iAdvances);
    	
    	while (elapsedTimer.remainingTimeMillis() > 0)
    	{
    		ArrayList<ACTIONS> actions = stateObs.getAvailableActions(id);

    		if (actions.size() > 0)
    		{
    			actions.get(rd.nextInt(actions.size()));
    		}
    		else
    		{
    			action = Types.ACTIONS.ACTION_NIL;
    		}
    		action = Types.ACTIONS.ACTION_NIL;
    		stateObs.advance(action);
    		iAdvances++;
    		
    		if (iAdvances <= 90 && iAdvances % 10 == 0)
    		{
    			//printState(stateObs, iAdvances);
    		}
    	}
    	System.out.println("Player " + id + " | Advance operations: " + iAdvances);
    	System.out.println("");

    	
    	if (firstTurns < 50)
    	{
    		action = Types.ACTIONS.ACTION_USE;
    	}
    	else
    	{
    		action = Types.ACTIONS.ACTION_NIL;
    	}
    	firstTurns++;
    	
        return action;
    }

    public void result(StateObservation stateObservation, ElapsedCpuTimer elapsedCpuTimer)
    {
        //System.out.println("Thanks for playing! " + stateObservation.isAvatarAlive());
    }
    
    private void printState(StateObservationMulti so, int iAdvances)
    {
  System.out.println("========== Player " + id + " report ==========");
        System.out.println("Advance number: " + iAdvances);
  
     Vector2d avatarPosition = so.getAvatarPosition(id);
        ArrayList<Observation>[] npcPositions = so.getNPCPositions(avatarPosition);
        ArrayList<Observation>[] immovablePositions = so.getImmovablePositions(avatarPosition);
        ArrayList<Observation>[] movablePositions = so.getMovablePositions(avatarPosition);
        ArrayList<Observation>[] resourcesPositions = so.getResourcesPositions(avatarPosition);
        ArrayList<Observation>[] portalPositions = so.getPortalsPositions(avatarPosition);
        ArrayList<Observation>[] fromAvatarSpritesPositions = so.getFromAvatarSpritesPositions(avatarPosition);
        
  int npcCounter = 0;
        if (npcPositions != null) {
            for (ArrayList<Observation> npcs : npcPositions) {
                if(npcs.size() > 0)
                {
                    npcCounter += npcs.size();
                    System.out.println("Number of NPCs of type " + npcs.get(0).itype + ": " + npcs.size());
                }
            }
        }
        System.out.println("Number of NPCs: " + npcCounter);
        
        int immovableCounter = 0;
        if (immovablePositions != null) {
            for (ArrayList<Observation> immovable : immovablePositions) {
                if(immovable.size() > 0)
                {
                 immovableCounter += immovable.size();
                 System.out.println("Number of immovables of type " + immovable.get(0).itype + ": " + immovable.size());
                }
            }
        }
        System.out.println("Number of immovables: " + immovableCounter);
        
        int movableCounter = 0;
        if (movablePositions != null) {
            for (ArrayList<Observation> movable : movablePositions) {
                if(movable.size() > 0)
                {
                 movableCounter += movable.size();
                 System.out.println("Number of movables of type " + movable.get(0).itype + ": " + movable.size());
                }
            }
        }
        System.out.println("Number of movables: " + movableCounter);
        
        int resourcesCounter = 0;
        if (resourcesPositions != null) {
            for (ArrayList<Observation> resources : resourcesPositions) {
                if(resources.size() > 0)
                {
                 resourcesCounter += resources.size();
                 System.out.println("Number of resources of type " + resources.get(0).itype + ": " + resources.size());
                }
            }
        }
        System.out.println("Number of resources: " + resourcesCounter);
        
        int portalsCounter = 0;
        if (portalPositions != null) {
            for (ArrayList<Observation> portals : portalPositions) {
                if(portals.size() > 0)
                {
                 portalsCounter += portals.size();
                 System.out.println("Number of portals of type " + portals.get(0).itype + ": " + portals.size());
                }
            }
        }
        System.out.println("Number of portals: " + portalsCounter);
        
        int fromAvatarSpritesCounter = 0;
        if (fromAvatarSpritesPositions != null) {
            for (ArrayList<Observation> fromAvatarSprites : fromAvatarSpritesPositions) {
                if(fromAvatarSprites.size() > 0)
                {
                 fromAvatarSpritesCounter += fromAvatarSprites.size();
                 System.out.println("Number of from avatar sprites of type " + fromAvatarSprites.get(0).itype + ": " + fromAvatarSprites.size());
                }
            }
        }
        System.out.println("Number of from avatar sprites: " + fromAvatarSpritesCounter);
        System.out.println("=====================================");
		System.out.println("");
    }
}
