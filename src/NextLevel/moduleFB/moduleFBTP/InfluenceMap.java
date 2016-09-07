package NextLevel.moduleFB.moduleFBTP;

import NextLevel.moduleFB.SpriteTypeFeatures;
import NextLevel.utils.VectorInt2d;
import core.game.StateObservationMulti;
import core.game.Observation;
import tools.Vector2d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class InfluenceMap
{
	private Map<VectorInt2d, Double> map;
	private Map<VectorInt2d, Boolean> passableMap;
	private Map<Integer, InfluencePoint> objectMap;
	private StateObservationMulti state;
	private FBTPGameKnowledge gameKnowledge;
	//private TPGameMechanicsController gameMechanicsController;

	public InfluenceMap(StateObservationMulti initialState, FBTPGameKnowledge initialKnowledge/*,
			TPGameMechanicsController gameMechanicsController*/)
	{
		this.state = initialState;
		this.gameKnowledge = initialKnowledge;
		//this.gameMechanicsController = gameMechanicsController;

		ArrayList<Observation> grid[][] = state.getObservationGrid();

		map = new HashMap<VectorInt2d, Double>();
		passableMap = new HashMap<VectorInt2d, Boolean>();
		objectMap = new HashMap<Integer, InfluencePoint>();

		int blockSize = state.getBlockSize();
		int width = state.getWorldDimension().width / blockSize;
		int height = state.getWorldDimension().height / blockSize;
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				VectorInt2d vector = new VectorInt2d(x, y);
				map.put(vector, 0.);
				boolean passable = true;
				ArrayList<Observation> observations = grid[x][y];
				if (!observations.isEmpty())
				{
					for (Observation obs : observations)
					{
						SpriteTypeFeatures sprite = gameKnowledge.getSpriteTypeFeaturesByType(obs.itype);
						if (sprite != null)
							if (!sprite.passable && !sprite.moving && !sprite.destroyable)
								passable = false;
					}
				}
				passableMap.put(vector, passable);
			}
		}
	}

	public void reset()
	{
		map.clear();
		objectMap.clear();

		int blockSize = state.getBlockSize();
		int width = state.getWorldDimension().width / blockSize;
		int height = state.getWorldDimension().height / blockSize;
		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++)
				map.put(new VectorInt2d(x, y), 0.);
	}

	public boolean update(StateObservationMulti updatedState, FBTPGameKnowledge updatedKnowledge)
	{
		boolean updateWholeMap = false;

		ArrayList<Observation> grid[][] = updatedState.getObservationGrid();
		int blockSize = state.getBlockSize();
		int width = state.getWorldDimension().width / blockSize;
		int height = state.getWorldDimension().height / blockSize;

		// search for changes in passability
		for (int x = 0; x < width; x++)
		{
			for (int y = 0; y < height; y++)
			{
				VectorInt2d vector = new VectorInt2d(x, y);
				map.put(vector, 0.);
				boolean passable = true;
				ArrayList<Observation> observations = grid[x][y];
				if (!observations.isEmpty())
				{
					for (Observation obs : observations)
					{
						SpriteTypeFeatures sprite = gameKnowledge.getSpriteTypeFeaturesByType(obs.itype);
						if (sprite != null)
							if (!sprite.passable && !sprite.moving && !sprite.destroyable)
								passable = false;
					}
				}
				if (passableMap.get(vector) != passable)
				{
					passableMap.put(vector, passable);
					updateWholeMap = true;
				}
			}
		}

		this.state = updatedState;
		this.gameKnowledge = updatedKnowledge;

		if (updateWholeMap)
		{
			// update whole map
			reset();
			for (int key : objectMap.keySet())
			{
				updateMapWithObject(objectMap.get(key));
			}
		}
		return updateWholeMap;
	}

	public void removeObject(int id)
	{
		if (objectMap.containsKey(id))
		{
			InfluencePoint point = objectMap.get(id);
			objectMap.remove(id);
			addObject(point.reduceInfluence());
			objectMap.remove(id);
		}
	}

	public void addObject(InfluencePoint influencePoint)
	{
		objectMap.put(influencePoint.id, influencePoint);
		updateMapWithObject(convertToVectorInt2d(influencePoint.position), influencePoint.influence,
				influencePoint.function);
	}

	private VectorInt2d convertToVectorInt2d(Vector2d position)
	{
		return new VectorInt2d(position.mul(1. / state.getBlockSize()));
	}

	public void addObject(Vector2d position, double influenceFactor, int functionType)
	{
		if (objectMap.keySet().contains(-1))
		{
			updateMapWithObject(convertToVectorInt2d(objectMap.get(-1).position), objectMap.get(-1).influence,
					objectMap.get(-1).function);
		}
		objectMap.put(-1, new InfluencePoint(-1, position, influenceFactor, functionType));
		updateMapWithObject(convertToVectorInt2d(position), influenceFactor, functionType);
	}

	private void updateMapWithObject(InfluencePoint point)
	{
		updateMapWithObject(convertToVectorInt2d(point.position), point.influence, point.function);
	}

	private void updateMapWithObject(VectorInt2d position, double influenceFactor, int functionType)
	{

		ArrayList<VectorInt2d> previousTiles = new ArrayList<VectorInt2d>();
		ArrayList<VectorInt2d> allTiles = new ArrayList<VectorInt2d>();
		previousTiles.add(position);
		int distance = 0;
		while (!previousTiles.isEmpty())
		{
			double influence = influenceFactor * influenceFunction(distance, functionType);
			ArrayList<VectorInt2d> newTiles = new ArrayList<VectorInt2d>();
			for (VectorInt2d tile : previousTiles)
			{
				if (passableMap.containsKey(tile))
				{
					if (passableMap.get(tile) && !allTiles.contains(tile))
					{
						map.put(tile, map.get(tile) + influence);
						newTiles.add(tile.add(0, 1));
						newTiles.add(tile.add(0, -1));
						newTiles.add(tile.add(1, 0));
						newTiles.add(tile.add(-1, 0));
					}
					allTiles.add(tile);
				}
			}
			previousTiles = newTiles;
			distance++;
		}
	}

	private double influenceFunction(int distance, int functionType)
	{
		switch (functionType)
		{
			case 1:
				return 1. / ((double)distance);
			case 2:
				return Math.pow(2, 1. - ((double)distance));
			case 3:
				return state.getWorldDimension().getWidth() + state.getWorldDimension().getHeight() - ((double)distance);
		}
		return 1;
	}

	public double getInfluenceValue(Vector2d position)
	{
		return map.get(new VectorInt2d(position.x / state.getBlockSize(), position.y / state.getBlockSize()));
	}
}
