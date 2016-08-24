package NextLevel;

import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.mechanicsController.GameMechanicsController;
import core.game.StateObservation;
import tools.ElapsedCpuTimer;

public class GameKnowledgeExplorer
{
	protected StateObservation stateObs; 
	protected GameKnowledge gameKnowledge;
	protected GameMechanicsController gameMechanicsController;
	protected AgentMoveController agentMoveController;
	
	protected ElapsedCpuTimer elapsedTimer;
	
	public GameKnowledgeExplorer()
	{
		
	}
	
	public GameKnowledgeExplorer(GameKnowledge gameKnowledge,
			AgentMoveController agentMoveController, GameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		this.agentMoveController = agentMoveController;
		
		this.elapsedTimer = new ElapsedCpuTimer();
	}

	public void learn(StateObservation stateObs, int playerID, ElapsedCpuTimer elapsedTimer, int timeForLearningDuringInitialization)
	{
		this.stateObs = stateObs;
		this.gameKnowledge.setPlayerID(playerID);
		this.gameKnowledge.setNumOfPlayers(stateObs.getNoPlayers());
		this.gameKnowledge.setNumOfPlayerActions(stateObs.getAvailableActions().size());
		this.gameKnowledge.setPlayerActions(stateObs.getAvailableActions());
	}
}
