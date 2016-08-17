package NextLevel.twoPlayer;

import java.util.ArrayList;

import NextLevel.GameKnowledge;
import NextLevel.GameObjectives;
import ontology.Types;

public class TPGameKnowledge extends GameKnowledge
{
	protected int playerID;
	protected int oppID;
	protected int numOfPlayers;
	protected int numOfPlayerActions;
	protected int numOfOpponentActions;
	protected ArrayList<Types.ACTIONS> playerActions;
	protected ArrayList<Types.ACTIONS> opponentActions;
    
	protected GameObjectives gameObjectives;

	public int getPlayerID()
	{
		return playerID;
	}

	public void setPlayerID(int playerID)
	{
		this.playerID = playerID;
	}

	public int getOppID()
	{
		return oppID;
	}

	public void setOppID(int oppID)
	{
		this.oppID = oppID;
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

	public int getNumOfOpponentActions()
	{
		return numOfOpponentActions;
	}

	public void setNumOfOpponentActions(int numOfOpponentActions)
	{
		this.numOfOpponentActions = numOfOpponentActions;
	}

	public ArrayList<Types.ACTIONS> getPlayerActions()
	{
		return playerActions;
	}

	public void setPlayerActions(ArrayList<Types.ACTIONS> playersActions)
	{
		this.playerActions = playersActions;
	}

	public ArrayList<Types.ACTIONS> getOpponentActions()
	{
		return opponentActions;
	}

	public void setOpponentActions(ArrayList<Types.ACTIONS> opponentActions)
	{
		this.opponentActions = opponentActions;
	}
}
