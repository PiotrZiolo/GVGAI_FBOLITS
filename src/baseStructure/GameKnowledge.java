package baseStructure;

public class GameKnowledge
{
	protected int playerID;
	protected int oppID;
	protected int numOfPlayers;
    
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
}
