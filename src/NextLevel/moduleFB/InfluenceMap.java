package NextLevel.moduleFB;

import NextLevel.GameKnowledge;
import NextLevel.moduleFB.VectorInt2d;
import NextLevel.moduleFB.moduleFBTP.FBTPGameKnowledge;
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
	private Map<VectorInt2d, Double> objectMap;
	private StateObservationMulti state;
	private FBTPGameKnowledge gameKnowledge;
	
	public InfluenceMap (StateObservationMulti initialState, GameKnowledge initialKnowledge)
	{
		state = initialState;
		gameKnowledge = (FBTPGameKnowledge)initialKnowledge;
		
		ArrayList<Observation> grid[][] = state.getObservationGrid();
		
		map = new HashMap<VectorInt2d, Double>();
		passableMap = new HashMap<VectorInt2d, Boolean>();
		objectMap = new HashMap<VectorInt2d, Double>();
		
		int blockSize = state.getBlockSize();
		int width = state.getWorldDimension().width/blockSize;
		int height = state.getWorldDimension().height/blockSize;
		for (int x=0; x<width; x++)
		{
			for (int y=0; y<height; y++)
			{
				VectorInt2d vector = new VectorInt2d(x, y);
				map.put(vector, 0.);
				boolean passable = true;
				ArrayList<Observation> observations = grid[x][y];
				if (!observations.isEmpty())
					for ( Observation obs : observations )
						if (!gameKnowledge.getSpriteTypeFeaturesByType(obs.itype).passable &&
							!gameKnowledge.getSpriteTypeFeaturesByType(obs.itype).moving &&
							!gameKnowledge.getSpriteTypeFeaturesByType(obs.itype).destroyable )
							passable = false;
				passableMap.put(vector, passable);
			}
		}
	}
	
	public void update (StateObservationMulti updatedState, FBTPGameKnowledge updatedKnowledge)
	{
		boolean updateWholeMap = false;
		
		ArrayList<Observation> grid[][] = updatedState.getObservationGrid();
		int blockSize = state.getBlockSize();
		int width = state.getWorldDimension().width/blockSize;
		int height = state.getWorldDimension().height/blockSize;
		
		// search for changes in passability
		for (int x=0; x<width; x++)
		{
			for (int y=0; y<height; y++)
			{
				VectorInt2d vector = new VectorInt2d(x, y);
				map.put(vector, 0.);
				boolean passable = true;
				ArrayList<Observation> observations = grid[x][y];
				if (!observations.isEmpty())
					for ( Observation obs : observations )
						if (!gameKnowledge.getSpriteTypeFeaturesByType(obs.itype).passable &&
							!gameKnowledge.getSpriteTypeFeaturesByType(obs.itype).moving &&
							!gameKnowledge.getSpriteTypeFeaturesByType(obs.itype).destroyable )
							passable = false;
				if ( passableMap.get(vector) != passable )
				{
					passableMap.put(vector, passable);
					updateWholeMap = true;
				}
			}
		}

		state = updatedState;
		gameKnowledge = (FBTPGameKnowledge)updatedKnowledge;
		
		if (!updateWholeMap)
		{
			// search for changes of position of sprites if needed
		}
		else
		{
			// update whole map
			for (Map.Entry<VectorInt2d, Double> object : objectMap.entrySet() )
			{
				addObject( object.getKey(), object.getValue() );
			}
		}
	}
	
	public void addObject (Vector2d position, double influenceFactor)
	{
		addObject(new VectorInt2d(position.mul(1./state.getBlockSize())), influenceFactor);
	}
	
	public void addObject (Observation obs, double influenceFactor)
	{
		addObject(obs.position, influenceFactor);
	}
		
	public void addObject (VectorInt2d position, double influenceFactor)
	{
		objectMap.put(position, influenceFactor);

		ArrayList<VectorInt2d> previousTiles = new ArrayList<VectorInt2d>();
		ArrayList<VectorInt2d> allTiles = new ArrayList<VectorInt2d>();
		previousTiles.add(position);
		int distance = 0;
		while (!previousTiles.isEmpty())
		{
			double influence = influenceFactor * influenceFunction(distance);
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
	
	private double influenceFunction (int distance) {
		return 1./distance;
	}
	
	public double getInfluenceValue(Vector2d position)
	{
		return map.get( new VectorInt2d( position.mul( 1./state.getBlockSize() ) ) );
	}
}
