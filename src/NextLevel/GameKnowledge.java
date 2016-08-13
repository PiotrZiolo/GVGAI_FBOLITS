package NextLevel;

public class GameKnowledge
{
	private int playerID;
    private int oppID;
    private int numOfPlayers;
    
	private GameObjectives gameObjectives;

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
