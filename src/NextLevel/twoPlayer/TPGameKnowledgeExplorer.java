package NextLevel.twoPlayer;

import NextLevel.GameKnowledgeExplorer;
import NextLevel.moveController.AgentMoveController;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import tools.ElapsedCpuTimer;

public class TPGameKnowledgeExplorer extends GameKnowledgeExplorer
{	
	// Real field types
	// protected TPGameKnowledge gameKnowledge;
	// protected AgentMoveController agentMoveController;
	
	public TPGameKnowledgeExplorer()
	{
		
	}
	
	public TPGameKnowledgeExplorer(TPGameKnowledge gameKnowledge, AgentMoveController agentMoveController)
	{
		this.gameKnowledge = gameKnowledge;
		this.agentMoveController = agentMoveController;
	}

	public void learn(StateObservation stateObs, int playerID, ElapsedCpuTimer elapsedTimer, int timeForLearningDuringInitialization)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs; 
		this.stateObs = stateObsMulti;
		TPGameKnowledge tpGameKnowledge = (TPGameKnowledge)this.gameKnowledge;
		tpGameKnowledge.setPlayerID(playerID);
		tpGameKnowledge.setOppID(1 - playerID);
		tpGameKnowledge.setNumOfPlayers(stateObsMulti.getNoPlayers());
		tpGameKnowledge.setNumOfPlayerActions(stateObsMulti.getAvailableActions(playerID).size());
		tpGameKnowledge.setNumOfOpponentActions(stateObsMulti.getAvailableActions(tpGameKnowledge.getOppID()).size());
		tpGameKnowledge.setPlayerActions(stateObsMulti.getAvailableActions(playerID));
		tpGameKnowledge.setOpponentActions(stateObsMulti.getAvailableActions(tpGameKnowledge.getOppID()));
	}
}
