package NextLevel.moduleFB.moduleFBTP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import NextLevel.StateEvaluator;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.moduleFB.SpriteTypeFeatures;
// import NextLevel.utils.LogHandler;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import core.game.Observation;
import ontology.Types;
import tools.Vector2d;

public class FBTPStateEvaluator extends StateEvaluator
{
	// Real types of fields
	// protected FBTPGameKnowledge gameKnowledge;
	// protected FBTPStateHandler stateHandler;
	private InfluenceMap map;
	private InfluenceMap playerInfluenceMap;
	private StateObservationMulti mappedState;
	private Map<Integer, InfluencePoint> mappedObjects;
	private Map<Integer, InfluencePoint> unmappedObjects;
	private double boardStateScore;
	private double[] weights;
	private Vector2d destination;
	private FBTPGameKnowledge gameKnowledge;
	private int avatarHealthPoints;
	private int pointScale;
	private TPGameMechanicsController gameMechanicsController;
	
	private final double curiosityFactor = 0.01;

	public FBTPStateEvaluator(FBTPGameKnowledge gameKnowledge, TPGameMechanicsController gameMechanicsController)
	{
		this.gameMechanicsController = gameMechanicsController;
		this.gameKnowledge = gameKnowledge;
		this.mappedState = null;
		this.destination = null;
		this.mappedObjects = new HashMap<Integer, InfluencePoint>();
		this.unmappedObjects = new HashMap<Integer, InfluencePoint>();
		this.weights = new double[14];
		this.pointScale = 1;
		this.boardStateScore = 0;
		for (int i = 0; i < 13; i++)
			weights[i] = 1;
	}
	
	public void setInfluenceMap(InfluenceMap map)
	{
		this.map = map;
	}

	public void setWeights(double[] newWeights)
	{
		if (newWeights.length >= 14)
		{
			for (int i = 0; i < 13; i++)
				weights[i] = newWeights[i];
		}

		resetInfluenceMap();
	}

	private void resetInfluenceMap()
	{
		boardStateScore = 0;
		if (mappedState != null)
		{
			map.reset();
			ArrayList<Observation> observations = this.gameMechanicsController.getListOfSprites(mappedState);
			for (Observation obs : observations)
			{
				SpriteTypeFeatures sprite = gameKnowledge.getSpriteTypeFeaturesByType(obs.itype);
				if (sprite != null)
				{
					if (!sprite.moving && (sprite.allowingVictory || sprite.collectable || sprite.givingVictoryUse ||
							sprite.givingVictoryMove || sprite.changingPointsUse > 0 ||
							sprite.changingPointsMove > 0 || sprite.dangerousOtherwise))
					{
						mappedObjects.put(obs.obsID, new InfluencePoint(obs.obsID, obs.position,
								getInfluenceValue(obs.itype), functionValue(obs.itype)));
						map.addObject(mappedObjects.get(obs.obsID));
					}
					else
					{
						unmappedObjects.put(obs.obsID, new InfluencePoint(obs.obsID, obs.position,
								getInfluenceValue(obs.itype), functionValue(obs.itype)));
					}
					boardStateScore += getExistanceScore(obs.itype);
				}
			}
		}
	}

	private double getInfluenceValue(int type)
	{
		double score = 0;
		SpriteTypeFeatures sprite = gameKnowledge.getSpriteTypeFeaturesByType(type);
		if (sprite != null)
		{
			// LogHandler.writeLog("Sprite features> type: " + type + ", features: " + sprite.print(), "FBTPStateEvaluator.getInfluenceValue", 0);
			
			if (sprite.passable || sprite.destroyable)
			{
				if (sprite.givingVictoryUse || sprite.givingVictoryMove)
					score += weights[0];

				if (sprite.allowingVictory)
					score += weights[1];

				if (sprite.dangerousToAvatar > 0 && sprite.moving)
				{
					if (avatarHealthPoints == 0)
						score -= weights[2];
					else
						score -= weights[2] * sprite.dangerousToAvatar / avatarHealthPoints;
				}
				if (sprite.dangerousOtherwise)
					score -= weights[3];

				if ((sprite.changingPointsMove < 0 && sprite.moving) ||
						sprite.changingPointsMove > 0 || sprite.changingPointsUse > 0)
					score += weights[4] * Math.max(sprite.changingPointsMove, sprite.changingPointsUse) / pointScale;

				if ((sprite.changingValuesOfOtherObjects < 0 && sprite.moving)
						|| sprite.changingValuesOfOtherObjects > 0)
					score += weights[5] * (sprite.changingValuesOfOtherObjects / pointScale);

				if (sprite.collectable)
					score += weights[6];
				
				if (sprite.givingDefeatMove && sprite.moving)
					score -= weights[7];
			}
		}
		return score;
	}

	private double getExistanceScore(int type)
	{
		double score = 0;
		SpriteTypeFeatures sprite = gameKnowledge.getSpriteTypeFeaturesByType(type);
		if (sprite != null)
		{
			if (sprite.passable || sprite.destroyable)
			{
				if (sprite.allowingVictory)
					score -= weights[8];

				if (sprite.dangerousToAvatar > 0)
				{
					if (avatarHealthPoints == 0)
						score -= weights[9];
					else
						score -= weights[9] * sprite.dangerousToAvatar / avatarHealthPoints;
				}

				if (sprite.dangerousOtherwise)
					score -= weights[10];

				if (sprite.changingValuesOfOtherObjects != 0)
					score -= weights[11] * (sprite.changingValuesOfOtherObjects / pointScale);

				if (sprite.collectable)
					score -= weights[12];
				
				if (sprite.givingDefeatMove && sprite.moving)
					score -= weights[13];
			}
		}
		return score;
	}

	private int functionValue(int type)
	{
		if (getInfluenceValue(type) > 0)
			return 1;
		else
			return 2;
	}

	private void updateInfluenceMap(StateObservationMulti newState)
	{
		boardStateScore = 0;
		if (unmappedObjects != null)
			unmappedObjects.clear();
		Map<Integer, InfluencePoint> mappedObjectsNew = new HashMap<Integer, InfluencePoint>();
		
		ArrayList<Observation> observations = this.gameMechanicsController.getListOfSprites(newState);
		for (Observation obs : observations)
		{
			SpriteTypeFeatures sprite = gameKnowledge.getSpriteTypeFeaturesByType(obs.itype);
			if (sprite != null)
			{
				if (!sprite.moving && (sprite.allowingVictory || sprite.collectable || sprite.givingVictoryUse ||
						sprite.givingVictoryMove || sprite.changingPointsUse > 0 ||
						sprite.changingPointsMove > 0 || sprite.dangerousOtherwise))
				{
					mappedObjectsNew.put(obs.obsID, new InfluencePoint(obs.obsID, obs.position,
							getInfluenceValue(obs.itype), functionValue(obs.itype)));
					map.addObject(mappedObjectsNew.get(obs.obsID));
				}
				else
				{
					unmappedObjects.put(obs.obsID, new InfluencePoint(obs.obsID, obs.position,
							getInfluenceValue(obs.itype), functionValue(obs.itype)));
				}
				boardStateScore += getExistanceScore(obs.itype);
			}
		}
		Set<Integer> newKeys = mappedObjectsNew.keySet();
		newKeys.removeAll(mappedObjects.keySet());
		Set<Integer> oldKeys = mappedObjects.keySet();
		oldKeys.removeAll(mappedObjectsNew.keySet());
		Set<Integer> sharedKeys = mappedObjects.keySet();
		sharedKeys.retainAll(mappedObjectsNew.keySet());
		
		for (int key : oldKeys)
			map.removeObject(key);
		
		for (int key : sharedKeys)
			if (!mappedObjects.get(key).position.equals(mappedObjectsNew.get(key).position))
				map.removeObject(key);

		map.update(newState, gameKnowledge);

		for (int key : newKeys)
			map.addObject(mappedObjectsNew.get(key));

		for (int key : sharedKeys)
			if (!mappedObjects.get(key).position.equals(mappedObjectsNew.get(key).position))
				map.addObject(mappedObjectsNew.get(key));

		mappedObjects = mappedObjectsNew;
	}

	private double getMappedPoints()
	{
		double score = 0;
		if (!mappedObjects.isEmpty())
		{
			Set<Entry<Integer, InfluencePoint>> entrySet = unmappedObjects.entrySet();
			for (Entry<Integer, InfluencePoint> entry : entrySet)
				score -= entry.getValue().influence;
		}
		return score;
	}

	private double getUnmappedScore()
	{
		double score = 0;
		if (!unmappedObjects.isEmpty())
		{
			Set<Entry<Integer, InfluencePoint>> entrySet = unmappedObjects.entrySet();
			for (Entry<Integer, InfluencePoint> entry : entrySet)
			{
				InfluencePoint point = entry.getValue();
				double distance = point.position.dist(mappedState.getAvatarPosition(gameKnowledge.getPlayerID()));
				score += point.influence * influenceFunction(distance, point.function);
				score -= point.influence;
			}
		}
		return score;
	}

	private double influenceFunction(double distance, int functionType)
	{
		switch (functionType)
		{
			case 1:
				return 1. / distance;
			case 2:
				return Math.pow(2, 1 - distance);
			case 3:
				return mappedState.getWorldDimension().getWidth() + mappedState.getWorldDimension().getHeight()
						- distance;
		}
		return 1;
	}

	public double evaluateState(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		this.mappedState = stateObsMulti;
		FBTPGameKnowledge gameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		
		int playerID = gameKnowledge.getPlayerID();

		if (stateObsMulti.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS)
			return 1000000000;

		if (stateObsMulti.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES)
			return -999999999;

		//updateInfluenceMap(stateObsMulti);
		//double score = map.getInfluenceValue(stateObsMulti.getAvatarPosition(playerID));
		//score += getUnmappedScore();
		//score += getMappedPoints();
		//score += boardStateScore;
		double score = stateObsMulti.getGameScore(playerID);

		return score;
	}

	public double evaluateStateWithDestination(StateObservationMulti stateObsMulti, Vector2d destination)
	{
		FBTPGameKnowledge gameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		
		int playerID = gameKnowledge.getPlayerID();

		if (stateObsMulti.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS)
			return 1000000000;

		if (stateObsMulti.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES)
			return -999999999;

		if (this.destination != null)
			map.removeObject(-1);

		updateInfluenceMap(stateObsMulti);

		map.addObject(new InfluencePoint(-1, destination, 1, 3)); // linear function
		this.destination = destination;

		double score = map.getInfluenceValue(stateObsMulti.getAvatarPosition(playerID));
		score += getUnmappedScore();
		score += getMappedPoints();
		score += boardStateScore;
		score += stateObsMulti.getGameScore(playerID);

		return score;
	}
	
	public void initializePlayerInfluenceMap(StateObservationMulti stateObsMulti)
	{
		this.mappedState = stateObsMulti;
		playerInfluenceMap = new InfluenceMap(mappedState, gameKnowledge);
		InfluencePoint influencePoint = new InfluencePoint(1,
				mappedState.getAvatarPosition(gameKnowledge.getPlayerID()), 1, 1);
		playerInfluenceMap.addObject(influencePoint);
	}

	public double evaluateSprite(Observation observation)
	{
		return (getInfluenceValue(observation.itype) + this.curiosityFactor) * playerInfluenceMap.getInfluenceValue(observation.position);
	}

	public HashMap<Integer, Double> evaluateSprites(ArrayList<Observation> observations)
	{
		HashMap<Integer, Double> score = new HashMap<Integer, Double>();
		for (Observation obs : observations)
		{
			score.put(obs.obsID, (getInfluenceValue(obs.itype) + this.curiosityFactor) * playerInfluenceMap.getInfluenceValue(obs.position));
			/*
			LogHandler.writeLog(
					"POI id: " + obs.obsID + ", type: "	+ obs.itype + 
					", category: " + obs.category + ", position: " + obs.position + 
					", value: " + score.get(obs.obsID) + ", type influence value: " + getInfluenceValue(obs.itype)
					+ ", position influence value: " + playerInfluenceMap.getInfluenceValue(obs.position),
					"FBTPStateEvaluator.evaluateSprites", 0);
			SpriteTypeFeatures sprite = gameKnowledge.getSpriteTypeFeaturesByType(obs.itype);
			if (sprite != null)
			{
				LogHandler.writeLog("Sprite features> type: " + obs.itype + ", features: " + sprite.print(), "FBTPStateEvaluator.evaluateSprites", 0);
			}
			*/
		}

		return score;
	}
}
