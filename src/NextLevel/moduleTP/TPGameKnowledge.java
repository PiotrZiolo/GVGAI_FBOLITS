package NextLevel.moduleTP;

import java.util.ArrayList;

import NextLevel.GameKnowledge;
import core.game.StateObservationMulti;
import ontology.Types;

public class TPGameKnowledge extends GameKnowledge
{
	// protected int playerID;
	// protected int numOfPlayers;
	// protected int numOfPlayerActions;
	// protected ArrayList<Types.ACTIONS> playerActions;
	// protected ArrayList<Types.ACTIONS> playerMoveActions;
	protected int oppID;
	protected int numOfOpponentActions;
	protected ArrayList<Types.ACTIONS> opponentActions;
	protected ArrayList<Types.ACTIONS> opponentMoveActions;

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

		this.opponentMoveActions = new ArrayList<Types.ACTIONS>();
		Types.ACTIONS[] moveActions = { Types.ACTIONS.ACTION_RIGHT, Types.ACTIONS.ACTION_LEFT, Types.ACTIONS.ACTION_UP,
				Types.ACTIONS.ACTION_DOWN };
		for (Types.ACTIONS i : opponentActions)
			for (Types.ACTIONS j : moveActions)
				if (j == i)
					opponentMoveActions.add(j);
	}

	public ArrayList<Types.ACTIONS> getOpponentMoveActions()
	{
		return opponentMoveActions;
	}
}
