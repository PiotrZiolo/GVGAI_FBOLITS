package NextLevel;

import NextLevel.moveController.AgentMoveController;
import core.game.StateObservation;
import tools.ElapsedCpuTimer;

public class GameKnowledgeExplorer
{
	protected StateObservation stateObs; 
	protected GameKnowledge gameKnowledge;
	protected AgentMoveController agentMoveController;
	
	public GameKnowledgeExplorer()
	{
		
	}
	
	public GameKnowledgeExplorer(GameKnowledge gameKnowledge,
			AgentMoveController agentMoveController)
	{
		this.gameKnowledge = gameKnowledge;
		this.agentMoveController = agentMoveController;
	}

	public void learn(StateObservation stateObs, int playerID, ElapsedCpuTimer elapsedTimer, int timeForLearningDuringInitialization)
	{
		this.stateObs = stateObs;
		this.gameKnowledge.setPlayerID(playerID);
		this.gameKnowledge.setNumOfPlayers(stateObs.getNoPlayers());
	}
}
