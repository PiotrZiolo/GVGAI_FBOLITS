package FBOLITS;

import java.util.ArrayList;

import ontology.Types;

public class GameKnowledge
{
	protected int playerID;
	protected int numOfPlayers;
	protected int numOfPlayerActions;
	protected ArrayList<Types.ACTIONS> playerActions;
	protected ArrayList<Types.ACTIONS> playerMoveActions;
	protected int fromAvatarSpriteType;
	protected int worldXDimension;
	protected int worldYDimension;
	protected double blockSize;
	protected double avatarSpeed;
	protected boolean deterministicGame;
	protected boolean shootingAllowed;
	protected boolean openMap;
	protected int avatarSpriteId;

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
	
	public int getFromAvatarSpriteType()
	{
		return fromAvatarSpriteType;
	}

	public void setFromAvatarSpriteType(int fromAvatarSpriteType)
	{
		this.fromAvatarSpriteType = fromAvatarSpriteType;
	}
	
	public int getWorldXDimensionInBlocks()
	{
		return worldXDimension;
	}
	
	public int getWorldXDimensionInPX()
	{
		return (int) (worldXDimension * blockSize);
	}

	public void setWorldXDimensionInBlocks(int worldXDimension)
	{
		this.worldXDimension = worldXDimension;
	}

	public int getWorldYDimensionInBlocks()
	{
		return worldYDimension;
	}
	
	public int getWorldYDimensionInPX()
	{
		return (int) (worldYDimension * blockSize);
	}

	public void setWorldYDimensionInBlocks(int worldYDimension)
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
	
	public double getAvatarSpeed()
	{
		return avatarSpeed;
	}

	public void setAvatarSpeed(double avatarSpeed)
	{
		this.avatarSpeed = avatarSpeed;
	}
	
	public boolean isDeterministicGame()
	{
		return deterministicGame;
	}

	public void setDeterministicGame(boolean deterministicGame)
	{
		this.deterministicGame = deterministicGame;
	}

	public boolean isOpenMap()
	{
		return openMap;
	}

	public void setOpenMap(boolean openMap)
	{
		this.openMap = openMap;
	}
	
	public boolean isShootingAllowed()
	{
		return shootingAllowed;
	}

	public void setShootingAllowed(boolean shootingAllowed)
	{
		this.shootingAllowed = shootingAllowed;
	}
	
	public void setAvatarSpriteId(int id)
	{
		this.avatarSpriteId = id;
	}

	public int getAvatarSpriteId()
	{
		return this.avatarSpriteId;
	}
}
