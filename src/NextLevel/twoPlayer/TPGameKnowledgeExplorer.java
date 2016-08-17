package NextLevel.twoPlayer;

import NextLevel.GameKnowledgeExplorer;
import NextLevel.moveController.AgentMoveController;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import tools.ElapsedCpuTimer;

public class TPGameKnowledgeExplorer extends GameKnowledgeExplorer
{
	protected StateObservation stateObs; 
	protected TPGameKnowledge gameKnowledge;
	protected AgentMoveController agentMoveController;
	
	public TPGameKnowledgeExplorer()
	{
		
	}
	
	public TPGameKnowledgeExplorer(TPGameKnowledge gameKnowledge,
			AgentMoveController agentMoveController)
	{
		this.gameKnowledge = gameKnowledge;
		this.agentMoveController = agentMoveController;
	}

	public void learn(StateObservationMulti stateObs, int playerID, ElapsedCpuTimer elapsedTimer, int timeForLearningDuringInitialization)
	{
		this.stateObs = stateObs;
		this.gameKnowledge.setPlayerID(playerID);
		this.gameKnowledge.setOppID(1 - playerID);
		this.gameKnowledge.setNumOfPlayers(stateObs.getNoPlayers());
		this.gameKnowledge.setNumOfPlayerActions(stateObs.getAvailableActions(playerID).size());
		this.gameKnowledge.setNumOfOpponentActions(stateObs.getAvailableActions(gameKnowledge.getOppID()).size());
		this.gameKnowledge.setPlayerActions(stateObs.getAvailableActions(playerID));
		this.gameKnowledge.setOpponentActions(stateObs.getAvailableActions(gameKnowledge.getOppID()));
	}
}
