package NextLevel;

import core.game.Observation;
//import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
//import ontology.Types;
import tools.Vector2d;
import NextLevel.SpriteTypeFeatures;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA. User: ssamot Date: 11/02/14 Time: 15:44 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class StateHeuristic
{
	double initialNpcCounter = 0;
	int playerID;
	int oppID;
	HashMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap;
	double[] weights;
	double lengthScale;
	double pointScale;

	public StateHeuristic(int playerID, int oppID, HashMap<Integer, SpriteTypeFeatures> spriteTypeFeaturesMap,
			double[] weights, double pointScale, Dimension worldDimension)
	{

		this.lengthScale = worldDimension.getHeight() + worldDimension.getWidth();
		this.pointScale = pointScale;
		this.weights = new double[10];
		for (int i = 0; i < weights.length; i++)
		{
			this.weights[i] = weights[i];
		}
		// if (weights.length < this.weights.length)
		// {
		// System.out.println("Not enough weights. Missing values were set to
		// 0.");
		// }
		for (int i = weights.length; i < this.weights.length; i++)
		{
			this.weights[i] = weights[i];
		}
		this.playerID = playerID;
		this.oppID = oppID;
		this.spriteTypeFeaturesMap = spriteTypeFeaturesMap;
		//weights = new double[] { 1, 1, 1, 1, 1, 1, 1, 1 };
	}

	public double evaluateState(StateObservationMulti stateObs)
	{

		Vector2d avatarPosition = stateObs.getAvatarPosition(playerID);
		int avatarHealthPoints = stateObs.getAvatarHealthPoints(playerID);

		double score = 0;

		if (stateObs.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS)
		{
			return 1000000000;
		}

		if (stateObs.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES)
		{
			return -999999999;
		}

		ArrayList<Observation>[] npcPositions = stateObs.getNPCPositions();
		ArrayList<Observation>[] portalPositions = stateObs.getPortalsPositions();
		ArrayList<Observation>[] movablePositions = stateObs.getMovablePositions();
		ArrayList<Observation>[] immovablePositions = stateObs.getImmovablePositions();
		ArrayList<Observation>[] fromAvatarSpritesPositions = stateObs.getFromAvatarSpritesPositions();
		ArrayList<Observation>[] resourcesPositions = stateObs.getResourcesPositions();

		Observation oppAvatar = new Observation(-1 - oppID, 0, stateObs.getAvatarPosition(oppID), new Vector2d(), 0); // unused
																														// values
																														// are
																														// set
																														// to
																														// 0

		score += observationArrayPayoffFunction(npcPositions, avatarPosition, avatarHealthPoints);
		score += observationArrayPayoffFunction(portalPositions, avatarPosition, avatarHealthPoints);
		score += observationArrayPayoffFunction(movablePositions, avatarPosition, avatarHealthPoints);
		score += observationArrayPayoffFunction(immovablePositions, avatarPosition, avatarHealthPoints);
		score += observationArrayPayoffFunction(fromAvatarSpritesPositions, avatarPosition, avatarHealthPoints);
		score += observationArrayPayoffFunction(resourcesPositions, avatarPosition, avatarHealthPoints);
		score += observationPayoffFunction(oppAvatar, avatarPosition, avatarHealthPoints);

		/*
		 * if(stateObs.getAvatarLastAction(playerID)==Types.ACTIONS.ACTION_USE)
		 * { Types.ACTIONS[] acts = new Types.ACTIONS[2]; acts[playerID] =
		 * Types.ACTIONS.ACTION_NIL; acts[oppID] = Types.ACTIONS.ACTION_NIL;
		 * 
		 * StateObservationMulti stateObsSimulated = stateObs.copy();
		 * StateObservationMulti stateObsChecked; for (int time=0; time<=10;
		 * time++) { stateObsChecked = stateObsSimulated.copy();
		 * stateObsChecked.advance(acts); if (stateObsChecked.isGameOver()) {
		 * break; } } }
		 */

		return score;
	}

	private double observationArrayPayoffFunction(ArrayList<Observation>[] observations, Vector2d agentPosition,
			int avatarHealthPoints)
	{
		double score = 0;
		if (observations != null)
		{
			for (int i = 0; i < observations.length; ++i)
			{
				for (Observation obs : observations[i])
				{
					score += observationPayoffFunction(obs, agentPosition, avatarHealthPoints);
				}
			}
		}
		return score;
	}

	private double observationPayoffFunction(Observation obs, Vector2d agentPosition, int avatarHealthPoints)
	{
		double score = 0;
		if (spriteTypeFeaturesMap.containsKey(obs.itype))
		{
			SpriteTypeFeatures sprite = spriteTypeFeaturesMap.get(obs.itype);
			// System.out.println("Type: " + sprite.passable);
			if (sprite.passable || sprite.destroyable)
			{
				if (sprite.givingVictory)
				{
					score += weights[0] * distance(obs.position, agentPosition);
					score -= weights[0] * lengthScale;
				}
				if (sprite.allowingVictory)
				{
					score += weights[1] * distance(obs.position, agentPosition);
					score -= weights[1] * lengthScale;
					score -= weights[2];
				}
				if (sprite.dangerousToAvatar > 0)
				{
					if (avatarHealthPoints == 0)
						score -= weights[3] * distance(obs.position, agentPosition);
					else
						score -= weights[3] * sprite.dangerousToAvatar / avatarHealthPoints
								* distance(obs.position, agentPosition);
				}
				if (sprite.dangerousOtherwise)
				{
					score -= weights[4];
					score -= weights[5] * distance(obs.position, agentPosition);
				}
				if (sprite.changingPoints != 0)
				{
					score += weights[6] * sprite.changingPoints / pointScale * distance(obs.position, agentPosition);
					score -= weights[6] * sprite.changingPoints / pointScale * lengthScale;
				}
				if (sprite.changingValuesOfOtherObjects != 0)
				{
					score += weights[7] * (sprite.changingValuesOfOtherObjects / pointScale)
							* distance(obs.position, agentPosition);
					score -= weights[7] * sprite.changingPoints / pointScale * lengthScale * lengthScale;
				}
				if (sprite.collectable)
				{
					score += weights[8] * distance(obs.position, agentPosition);
					score -= weights[8] * lengthScale;
					score -= weights[9];
				}
			}
		}
		return score;
	}

	double distance(Vector2d v1, Vector2d v2)
	{
		double distance = Math.abs(v1.x - v2.x) + Math.abs(v1.y - v2.y);
		if (distance == 0)
			return lengthScale / 0.1;
		else
			return lengthScale / Math.pow(distance, 2);
	}
}
