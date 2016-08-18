package baseStructure;

import java.util.ArrayList;

import ontology.Types;

public class GameKnowledge
{
	protected int playerID;
	protected int numOfPlayers;
	protected int numOfPlayerActions;
	protected ArrayList<Types.ACTIONS> playerActions;
    
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
	}
}
