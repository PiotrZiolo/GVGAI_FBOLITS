package NextLevel;

import java.util.ArrayList;

import ontology.Types;

public class GameKnowledge
{
	protected int playerID;
	protected int numOfPlayers;
	protected int numOfPlayerActions;
	protected ArrayList<Types.ACTIONS> playerActions;
	protected ArrayList<Types.ACTIONS> playerMoveActions;
	protected int worldXDimension;
	protected int worldYDimension;
	protected double blockSize;

	protected GameObjectives gameObjectives;

	public int getPlayerID()
	{
		return playerID;
	}

	public void setPlayerID(int playerID)
	{
		this.playerID = playerID;
	}

	public int getNumOfPlayers()
	{
		return numOfPlayers;
	}

	public void setNumOfPlayers(int numOfPlayers)
	{
		this.numOfPlayers = numOfPlayers;
	}

	public int getNumOfPlayerActions()
	{
		return numOfPlayerActions;
	}

	public void setNumOfPlayerActions(int numOfPlayerActions)
	{
		this.numOfPlayerActions = numOfPlayerActions;
	}

	public ArrayList<Types.ACTIONS> getPlayerActions()
	{
		return playerActions;
	}

	public void setPlayerActions(ArrayList<Types.ACTIONS> playerActions)
	{
		this.playerActions = playerActions;

		this.playerMoveActions = new ArrayList<Types.ACTIONS>();
		Types.ACTIONS[] moveActions = { Types.ACTIONS.ACTION_RIGHT, Types.ACTIONS.ACTION_LEFT, Types.ACTIONS.ACTION_UP,
				Types.ACTIONS.ACTION_DOWN };
		for (Types.ACTIONS i : playerActions)
			for (Types.ACTIONS j : moveActions)
				if (j == i)
					playerMoveActions.add(j);
	}

	public ArrayList<Types.ACTIONS> getPlayerMoveActions()
	{
		return playerMoveActions;
	}
	
	public int getWorldXDimension()
	{
		return worldXDimension;
	}

	public void setWorldXDimension(int worldXDimension)
	{
		this.worldXDimension = worldXDimension;
	}

	public int getWorldYDimension()
	{
		return worldYDimension;
	}

	public void setWorldYDimension(int worldYDimension)
	{
		this.worldYDimension = worldYDimension;
	}
	
	public double getBlockSize()
	{
		return blockSize;
	}

	public void setBlockSize(double blockSize)
	{
		this.blockSize = blockSize;
	}
}
