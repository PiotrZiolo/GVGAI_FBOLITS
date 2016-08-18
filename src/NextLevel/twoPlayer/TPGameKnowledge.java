package NextLevel.twoPlayer;

import java.util.ArrayList;

import NextLevel.GameKnowledge;
import ontology.Types;

public class TPGameKnowledge extends GameKnowledge
{
	// protected int playerID;
	protected int oppID;
	// protected int numOfPlayers;
	// protected int numOfPlayerActions;
	protected int numOfOpponentActions;
	// protected ArrayList<Types.ACTIONS> playerActions;
	protected ArrayList<Types.ACTIONS> opponentActions;

	public int getOppID()
	{
		return oppID;
	}
	
	public void setOppID(int oppID)
	{
		this.oppID = oppID;
	}
	
	public int getNumOfOpponentActions()
	{
		return numOfOpponentActions;
	}
	
	public void setNumOfOpponentActions(int numOfOpponentActions)
	{
		this.numOfOpponentActions = numOfOpponentActions;
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
