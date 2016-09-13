package FBOLITS;

import java.util.ArrayList;
import java.util.HashMap;

import FBOLITS.mechanicsController.AgentMoveController;
import FBOLITS.mechanicsController.GameMechanicsController;
import FBOLITS.utils.Pair;
import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class GameKnowledgeExplorer
{
	protected StateObservation stateObs;
	protected GameKnowledge gameKnowledge;
	protected GameMechanicsController gameMechanicsController;
	protected AgentMoveController agentMoveController;
	protected GameStateTracker gameStateTracker;

	protected ElapsedCpuTimer elapsedTimer;

	public GameKnowledgeExplorer()
	{

	}
	
	public GameKnowledgeExplorer(GameKnowledge gameKnowledge, GameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;

		this.elapsedTimer = new ElapsedCpuTimer();
	}

	public GameKnowledgeExplorer(GameKnowledge gameKnowledge, GameMechanicsController gameMechanicsController,
			AgentMoveController agentMoveController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		this.agentMoveController = agentMoveController;

		this.elapsedTimer = new ElapsedCpuTimer();
	}
	
	public GameKnowledgeExplorer(GameKnowledge gameKnowledge, GameMechanicsController gameMechanicsController, 
			AgentMoveController agentMoveController, GameStateTracker gameStateTracker)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		this.agentMoveController = agentMoveController;
		this.gameStateTracker = gameStateTracker;

		this.elapsedTimer = new ElapsedCpuTimer();
	}
	
	public void setGameStateTracker(GameStateTracker gameStateTracker)
	{
		this.gameStateTracker = gameStateTracker;
	}

	public void learnStaticBasics(StateObservation stateObs, int playerID)
	{
		this.stateObs = stateObs;
		this.gameKnowledge.setPlayerID(playerID);
		this.gameKnowledge.setNumOfPlayers(stateObs.getNoPlayers());
		this.gameKnowledge.setWorldXDimensionInBlocks(stateObs.getObservationGrid().length);
		this.gameKnowledge.setWorldYDimensionInBlocks(stateObs.getObservationGrid()[0].length);
		this.gameKnowledge.setBlockSize(stateObs.getBlockSize());
	}
	
	public void learnDynamicBasics(StateObservation stateObs)
	{
		this.stateObs = stateObs;
		this.gameKnowledge.setNumOfPlayerActions(stateObs.getAvailableActions().size());
		this.gameKnowledge.setPlayerActions(stateObs.getAvailableActions());
		this.gameKnowledge.setAvatarSpeed(stateObs.getAvatarSpeed());
	}
	
	public void initialLearn(StateObservation stateObs, ElapsedCpuTimer elapsedTimer,
			int timeForLearningDuringInitialization)
	{
		this.stateObs = stateObs;
		
		gameKnowledge.setAvatarSpriteId(gameMechanicsController.getPlayerId(stateObs));
		gameKnowledge.setShootingAllowed(checkWhetherShootingIsAllowed(stateObs));
		gameKnowledge.setDeterministicGame(checkDeterminism(stateObs));
		gameKnowledge.setOpenMap(checkIfMapIsOpen(stateObs));
	}
	
	public void successiveLearn(StateObservation stateObs, ElapsedCpuTimer elapsedTimer,
			int timeForLearningDuringInitialization)
	{
		this.stateObs = stateObs;
	}
	
	@SafeVarargs
	protected final ArrayList<ArrayList<Integer>> observationListToID(StateObservation stateObs,
			ArrayList<Observation>[]... arrays)
	{
		int blockSize = stateObs.getBlockSize();
		int worldXDimension = stateObs.getWorldDimension().width / blockSize;
		ArrayList<ArrayList<Integer>> tab = new ArrayList<ArrayList<Integer>>();
		for (ArrayList<Observation>[] array : arrays)
		{
			if (array == null)
				continue;
			for (int i = 0; i < array.length; i++)
			{
				ArrayList<Integer> intTab = new ArrayList<Integer>();
				for (Observation obs : array[i])
				{
					Pair<Integer, Integer> position = new Pair<Integer, Integer>((int) obs.position.x / blockSize,
							(int) obs.position.y / blockSize);
					int current = worldXDimension * position.second() + position.first();
					intTab.add(current);
				}
				tab.add(intTab);
			}
		}
		return tab;
	}
	
	/**
	 * Checks whether game is deterministic.
	 * 
	 * @param stateObs
	 *            Observation of the current state of the game.
	 * @param fastCheck
	 *            If set true, function evaluates the state quickly as for real-time answer.
	 *            Otherwise function focuses on deep analysis.
	 */
	protected boolean checkDeterminism(StateObservation stateObs)
	{
		int reps = 15;
		int depth = 10;
		int offset = 5;

		@SuppressWarnings("unchecked")
		ArrayList<ArrayList<ArrayList<Integer>>>[] positions = new ArrayList[reps];
		for (int r = 0; r < reps; r++)
		{
			StateObservation stateObsCopy = stateObs.copy();
			positions[r] = new ArrayList<ArrayList<ArrayList<Integer>>>();
			for (int d = 1; d <= depth; d++)
			{
				stateObsCopy.advance(Types.ACTIONS.ACTION_NIL);
				if (d % offset == 0)
				{
					positions[r].add(observationListToID(stateObsCopy, stateObsCopy.getNPCPositions(),
							stateObsCopy.getMovablePositions()));
				}
			}
		}

		for (int r = 0; r < reps - 1; r++)
		{
			for (int d = 0; d < depth / offset; d++)
			{
				if (positions[r].get(d).size() != positions[r + 1].get(d).size())
					return false;
				for (int i = 0; i < positions[r].get(d).size(); i++)
				{
					if (positions[r].get(d).get(i).size() != positions[r + 1].get(d).get(i).size())
						return false;
					for (int j = 0; j < positions[r].get(d).get(i).size(); j++)
						if (positions[r].get(d).get(i).get(j) != positions[r + 1].get(d).get(i).get(j))
							return false;
				}
			}
		}

		return true;
	}

	protected boolean checkWhetherShootingIsAllowed(StateObservation stateObs)
	{
		ArrayList<Types.ACTIONS> initialActions = stateObs.getAvailableActions();
		if (initialActions.contains(Types.ACTIONS.ACTION_NIL))
			initialActions.remove(Types.ACTIONS.ACTION_NIL);
		initialActions.add(0, Types.ACTIONS.ACTION_NIL);
		if (!initialActions.contains(Types.ACTIONS.ACTION_USE))
			return false;
		else
			initialActions.remove(Types.ACTIONS.ACTION_USE);

		if (!stateObs.getAvatarOrientation().equals(new Vector2d(0, 0))) // no orientation
			initialActions.remove(Types.ACTIONS.fromVector(stateObs.getAvatarOrientation()));

		for (Types.ACTIONS initialAction : initialActions)
		{
			StateObservation stateObsCopy = stateObs.copy();

			stateObsCopy.advance(initialAction);

			ArrayList<Observation>[] initialFromAvatarSprites = stateObsCopy
					.getFromAvatarSpritesPositions(stateObs.getAvatarPosition());
			ArrayList<Integer> initialFromAvatarSpritesId = new ArrayList<Integer>();
			if (initialFromAvatarSprites != null)
			{
				for (ArrayList<Observation> spriteArray : initialFromAvatarSprites)
					for (Observation sprite : spriteArray)
						initialFromAvatarSpritesId.add(sprite.obsID);
			}

			stateObsCopy.advance(Types.ACTIONS.ACTION_USE);

			ArrayList<Observation>[] laterFromAvatarSprites = stateObsCopy
					.getFromAvatarSpritesPositions(stateObs.getAvatarPosition());
			HashMap<Integer, Observation> newSpriteObservationMap = new HashMap<Integer, Observation>();
			if (laterFromAvatarSprites != null)
			{
				for (ArrayList<Observation> spriteArray : laterFromAvatarSprites)
					for (Observation sprite : spriteArray)
						if (initialFromAvatarSpritesId == null)
							newSpriteObservationMap.put(sprite.obsID, sprite);
						else if (!initialFromAvatarSpritesId.contains(sprite.obsID))
						{
							newSpriteObservationMap.put(sprite.obsID, sprite);
						}
			}
			else
			{
				continue;
			}

			StateObservation stateObsCopyCopy = stateObsCopy.copy();
			stateObsCopyCopy.advance(Types.ACTIONS.ACTION_NIL);

			ArrayList<Observation>[] finalFromAvatarSprites = stateObsCopyCopy
					.getFromAvatarSpritesPositions(stateObs.getAvatarPosition());
			if (finalFromAvatarSprites != null)
			{
				for (ArrayList<Observation> spriteArray : finalFromAvatarSprites)
					for (Observation sprite : spriteArray)
						if (newSpriteObservationMap.containsKey(sprite.obsID))
						{
							if (!newSpriteObservationMap.get(sprite.obsID).position.equals(sprite.position))
							{
								gameKnowledge.setFromAvatarSpriteType(sprite.itype);
								return true;
							}
						}
			}
		}

		return false;
	}
	
	protected boolean checkIfMapIsOpen(StateObservation stateObs)
	{
		// TODO
		return false;
	}
}
