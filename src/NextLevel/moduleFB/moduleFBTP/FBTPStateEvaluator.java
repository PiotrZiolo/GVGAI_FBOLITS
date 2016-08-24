package NextLevel.moduleFB.moduleFBTP;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import NextLevel.StateEvaluator;
import NextLevel.moduleFB.SpriteTypeFeatures;
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
	private StateObservationMulti mappedState;
	private Map <Integer, InfluencePoint> mappedObjects;
	private Map <Integer, InfluencePoint> unmappedObjects;
	private double[] weights;
	private Vector2d destination;
	private FBTPGameKnowledge gameKnowledge;
	private int avatarHealthPoints;
	private int pointScale;
	
	public FBTPStateEvaluator(FBTPGameKnowledge gameKnowledge, InfluenceMap map)
	{
		this.gameKnowledge = gameKnowledge;
		this.map = map;
		this.mappedState = null;
		this.destination = null;
		this.mappedObjects = new HashMap<Integer, InfluencePoint>();
		this.unmappedObjects = new HashMap<Integer, InfluencePoint>();
		this.weights = new double[10];
		this.pointScale = 1;
		for (int i=0; i<=9; i++)
			weights[i] = 1;
	}
	
	public void setWeights(double[] newWeights)
	{
		if (newWeights.length>=10)
		{
			for (int i=0; i<10; i++)
				weights[i] = newWeights[i];
		}
		
		resetInfluenceMap();
	}
	
	private void resetInfluenceMap()
	{
		if (mappedState!=null)
		{
			map.reset();
			ArrayList<Observation>[][] grid = mappedState.getObservationGrid();
			int width = (int) (mappedState.getWorldDimension().getWidth()/mappedState.getBlockSize());
			int height = (int) (mappedState.getWorldDimension().getHeight()/mappedState.getBlockSize());
			for (int x = 0; x < width; x++)
			{
				for (int y = 0; y < height; y++)
				{
					ArrayList<Observation> observations = grid[x][y];
					for (Observation obs : observations )
					{
						SpriteTypeFeatures sprite = gameKnowledge.getSpriteTypeFeaturesByType(obs.itype);
						if (sprite!=null)
						{
							if (!sprite.moving && (sprite.allowingVictory || sprite.collectable ||
									sprite.givingVictory || sprite.changingPoints>0 || sprite.dangerousOtherwise))
							{
								mappedObjects.put(obs.obsID, new InfluencePoint(obs.obsID, obs.position,
										influenceValue(obs.itype), functionValue(obs.itype)));
								map.addObject(mappedObjects.get(obs.obsID));
							}
							else
							{
								unmappedObjects.put(obs.obsID, new InfluencePoint(obs.obsID, obs.position,
										influenceValue(obs.itype), functionValue(obs.itype)));
							}
						}
					}
				}
			}
		}
	}
	
	private double influenceValue (int type) {
		double score = 0;
		SpriteTypeFeatures sprite = gameKnowledge.getSpriteTypeFeaturesByType(type);
		if (sprite != null)
		{
			if (sprite.passable || sprite.destroyable)
			{
				if (sprite.givingVictory)
					score += weights[0];
				
				if (sprite.allowingVictory)
					score += weights[1];
				
				if (sprite.dangerousToAvatar > 0)
				{
					if (avatarHealthPoints == 0)
						score -= weights[3];
					else
						score -= weights[3] * sprite.dangerousToAvatar / avatarHealthPoints;
				}
				if (sprite.dangerousOtherwise)
					score -= weights[5];
				
				if (sprite.changingPoints != 0)
					score += weights[6] * sprite.changingPoints / pointScale;
				
				if (sprite.changingValuesOfOtherObjects != 0)
					score += weights[7] * (sprite.changingValuesOfOtherObjects / pointScale);
				
				if (sprite.collectable)
					score += weights[8];
			}
		}
		return score;
	}
	
	private int functionValue (int type) {
		if (influenceValue(type)>0)
			return 1;
		else
			return 2;
	}
	
	private void updateInfluenceMap (StateObservationMulti newState)
	{
		if (unmappedObjects!=null)
			unmappedObjects.clear();
		Map <Integer, InfluencePoint> mappedObjectsNew = new HashMap <Integer, InfluencePoint>();
		ArrayList<Observation>[][] grid = newState.getObservationGrid();
		int width = (int) (newState.getWorldDimension().getWidth()/newState.getBlockSize());
		int height = (int) (newState.getWorldDimension().getHeight()/newState.getBlockSize());
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				ArrayList<Observation> observations = grid[x][y];
				for (Observation obs : observations )
				{
					SpriteTypeFeatures sprite = gameKnowledge.getSpriteTypeFeaturesByType(obs.itype);
					if (sprite!=null)
					{
						if ( !sprite.moving && (sprite.allowingVictory || sprite.collectable ||
								sprite.givingVictory || sprite.changingPoints>0 ||sprite.dangerousOtherwise) )
						{
							mappedObjectsNew.put(obs.obsID, new InfluencePoint(obs.obsID, obs.position,
									influenceValue(obs.itype), functionValue(obs.itype)));
							map.addObject(mappedObjectsNew.get(obs.obsID));
						}
						else {
							unmappedObjects.put(obs.obsID, new InfluencePoint(obs.obsID, obs.position,
									influenceValue(obs.itype), functionValue(obs.itype)));
						}
					}
				}
			}
		}
		Set<Integer> newKeys = mappedObjectsNew.keySet();
		newKeys.removeAll(mappedObjects.keySet());
		Set<Integer> oldKeys = mappedObjects.keySet();
		oldKeys.removeAll(mappedObjectsNew.keySet());
		Set<Integer> sharedKeys = mappedObjects.keySet();
		sharedKeys.retainAll(mappedObjectsNew.keySet());
		
		for ( int key : newKeys )
			map.removeObject(key);
		
		for ( int key : newKeys )
			if (mappedObjects.get(key).position!=mappedObjectsNew.get(key).position)
				map.removeObject(key);
		
		map.update(newState, gameKnowledge);

		for ( int key : newKeys )
			map.addObject(mappedObjectsNew.get(key));
		
		for ( int key : newKeys )
			if (mappedObjects.get(key).position!=mappedObjectsNew.get(key).position)
				map.addObject(mappedObjectsNew.get(key));
		
		mappedObjects = mappedObjectsNew;
	}

	public double evaluateState(StateObservation stateObs)
	{
		mappedState = (StateObservationMulti)stateObs;
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		FBTPGameKnowledge gameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		int playerID = gameKnowledge.getPlayerID();

		if (stateObsMulti.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS)
			return 1000000000;

		if (stateObsMulti.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES)
			return -999999999;

		updateInfluenceMap(stateObsMulti);
		double score = map.getInfluenceValue(stateObsMulti.getAvatarPosition(playerID));
		score += getUnmappedScore();
		score += getMappedPoints();
		score += stateObsMulti.getGameScore(playerID);
		
		return score;
	}
	
	private double getMappedPoints() {
		double score = 0;
		if (!mappedObjects.isEmpty())
		{
			Set<Entry<Integer, InfluencePoint>> entrySet = unmappedObjects.entrySet();
			for (Entry<Integer, InfluencePoint> entry : entrySet)
				score -= entry.getValue().influence;
		}
		return score;
	}

	private double getUnmappedScore() {
		double score = 0;
		if (!unmappedObjects.isEmpty())
		{
			Set<Entry<Integer, InfluencePoint>> entrySet = unmappedObjects.entrySet();
			for (Entry<Integer, InfluencePoint> entry : entrySet)
			{
				InfluencePoint point = entry.getValue();
				double distance = point.position.dist(mappedState.getAvatarPosition(gameKnowledge.getPlayerID()));
				score += point.influence * influenceFunction (distance, point.function);
				score -= point.influence;
			}
		}
		return score;
	}
	
	private double influenceFunction (double distance, int functionType) {
		switch (functionType)
		{
		case 1:
			return 1./distance;
		case 2:
			return Math.pow(2, 1-distance);
		case 3:
			return mappedState.getWorldDimension().getWidth()+mappedState.getWorldDimension().getHeight()-distance;
		}
		return 1;
	}

	public double evaluateStateWithDestination(StateObservation stateObs, Vector2d destination)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		FBTPGameKnowledge gameKnowledge = (FBTPGameKnowledge) this.gameKnowledge;
		int playerID = gameKnowledge.getPlayerID();

		if (stateObsMulti.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_WINS)
			return 1000000000;

		if (stateObsMulti.getMultiGameWinner()[playerID] == Types.WINNER.PLAYER_LOSES)
			return -999999999;
		
		if (this.destination!=null)
			map.removeObject(-1);
		
		updateInfluenceMap(stateObsMulti);
		
		map.addObject(new InfluencePoint(-1, destination, 1, 3));			// linear function
		this.destination = destination;
		
		double score = map.getInfluenceValue(stateObsMulti.getAvatarPosition(playerID));
		
		score += stateObsMulti.getGameScore(playerID);
		
		return score;
	}
}
