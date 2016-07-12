package NextLevel;

import core.game.Observation;
//import core.game.StateObservation;
import core.game.StateObservationMulti;
//import ontology.Types;
import tools.Vector2d;
import NextLevel.SpriteTypeFeatures;

import java.util.ArrayList;
//import java.util.HashMap;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA. User: ssamot Date: 11/02/14 Time: 15:44 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class StateHeuristic
{
    double initialNpcCounter = 0;
    int playerID;
    int oppID;
    TreeMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap;
    double[] weights;
    double lengthScale;
    double pointScale;

    public StateHeuristic(int playerID, int oppID,
    		TreeMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap/*, double[] weights*/) {
    	this.playerID = playerID;
        this.oppID = oppID;
    	this.spriteTypeFeaturesMap = spriteTypeFeaturesMap;
    }

    public double evaluateState(StateObservationMulti stateObs) {
        Vector2d avatarPosition = stateObs.getAvatarPosition(playerID);
        int avatarHealthPoints = stateObs.getAvatarHealthPoints(playerID);
        
        double score = 0;
        
        ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions();
        score += positionPayoffFunction(npcPositions, avatarPosition, avatarHealthPoints);
        
        ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions(avatarPosition);
        score += positionPayoffFunction(portalPositions, avatarPosition, avatarHealthPoints);
        
        ArrayList<Observation>[] movablePositions = stateObs.getMovablePositions(avatarPosition);
        score += positionPayoffFunction(movablePositions, avatarPosition, avatarHealthPoints);
        
        ArrayList<Observation>[] immovablePositions = stateObs.getImmovablePositions(avatarPosition);
        score += positionPayoffFunction(immovablePositions, avatarPosition, avatarHealthPoints);
        
        ArrayList<Observation>[] fromAvatarSpritesPositions = stateObs.getFromAvatarSpritesPositions();
        score += positionPayoffFunction(fromAvatarSpritesPositions, avatarPosition, avatarHealthPoints);
        
        ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions(avatarPosition);
        score += positionPayoffFunction(resourcesPositions, avatarPosition, avatarHealthPoints);
        
        //ArrayList<Observation>[] avatarPosition = stateObs.getAva(oppID);
        
        /*double won = 0;
        Types.WINNER[] winners = stateObs.getMultiGameWinner();

        boolean bothWin = (winners[playerID] == Types.WINNER.PLAYER_WINS) && (winners[oppID] == Types.WINNER.PLAYER_WINS);
        boolean meWins  = (winners[playerID] == Types.WINNER.PLAYER_WINS) && (winners[oppID] == Types.WINNER.PLAYER_LOSES);
        boolean meLoses = (winners[playerID] == Types.WINNER.PLAYER_LOSES) && (winners[oppID] == Types.WINNER.PLAYER_WINS);
        boolean bothLose = (winners[playerID] == Types.WINNER.PLAYER_LOSES) && (winners[oppID] == Types.WINNER.PLAYER_LOSES);

        if(meWins || bothWin)
            won = 1000000000;
        else if (meLoses)
            return -999999999;

        npcPositions[0].sqDist;

        if (portalPositions == null) {

            double score = 0;
            if (npcCounter == 0) {
                score = stateObs.getGameScore(playerID) + won*100000000;
            } else {
                score = -minDistance / 100.0 + (-npcCounter) * 100.0 + stateObs.getGameScore(playerID) + won*100000000;
            }
            return score;
        }

        double minDistancePortal = Double.POSITIVE_INFINITY;
        Vector2d minObjectPortal = null;
        for (ArrayList<Observation> portals : portalPositions) {
            if(portals.size() > 0)
            {
                minObjectPortal   =  portals.get(0).position; //This is the closest portal
                minDistancePortal =  portals.get(0).sqDist;   //This is the (square) distance to the closest portal
            }
        }

        double score = 0;
        if (minObjectPortal == null) {
            score = stateObs.getGameScore() + won*100000000;
        }
        else {
            score = stateObs.getGameScore() + won*1000000 - minDistancePortal * 10.0;
        }
         */

        return score;
    }

    private double positionPayoffFunction(ArrayList<Observation>[] observations,
    		Vector2d agentPosition, int avatarHealthPoints) {
    	double score = 0;
        if(observations!=null) {
	        for(int i = 0; i < observations.length; ++i) {
	        	for (Observation obs : observations[i]) {
	        		if (spriteTypeFeaturesMap.containsKey(obs.itype))
	        		{
	        			SpriteTypeFeatures sprite = spriteTypeFeaturesMap.get(obs.itype);
	        			if (sprite.givingVictory) {
	        				score += weights[0]*distance(obs.position, agentPosition);
	        			}
	        			if (sprite.allowingVictory) {
	        				score += weights[1]*distance(obs.position, agentPosition);
	        				score -= weights[2];
	        			}
	        			if (sprite.dangerousToAvatar>0) {
	        				score += -weights[3]*sprite.dangerousToAvatar/avatarHealthPoints*distance(obs.position, agentPosition);
	        			}
	        			if (sprite.dangerousOtherwise) {
	        				score += -weights[4];
	        				score += -weights[5]*distance(obs.position, agentPosition);
	        			}
	        			if (sprite.changingPoints!=0) {
	        				score += weights[6]*sprite.changingPoints/pointScale*distance(obs.position, agentPosition);
	        			}
	        			if (sprite.changingValuesOfOtherObjects) {
	        				int totalPointsChange = 0;
	        				score += weights[7]*totalPointsChange*distance(obs.position, agentPosition);
	        			}
	        		}
	        	}
	        }
        }
		return score;
	}
    
    double distance ( Vector2d v1, Vector2d v2 ) {
    	return lengthScale/Math.pow( Math.abs(v1.x-v2.x) + Math.abs(v1.y-v2.y), 2 );
    }
}
