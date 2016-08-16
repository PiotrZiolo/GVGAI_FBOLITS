package baseStructure;

import baseStructure.moveController.AgentMoveController;
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
	
	public GameKnowledgeExplorer(StateObservation stateObs, GameKnowledge gameKnowledge,
			AgentMoveController agentMoveController, int playerID)
	{
		this.stateObs = stateObs;
		this.gameKnowledge = gameKnowledge;
		this.agentMoveController = agentMoveController;
		this.gameKnowledge.setPlayerID(playerID);
		this.gameKnowledge.setOppID(1 - playerID);
		this.gameKnowledge.setNumOfPlayers(stateObs.getNoPlayers());
	}

	public void learn(ElapsedCpuTimer elapsedTimer, int timeForLearningDuringInitialization)
	{
		// To be overridden in subclasses
	}
}
