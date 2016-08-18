package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import NextLevel.moveController.AgentMoveController;
import NextLevel.twoPlayer.TPGameKnowledge;
import NextLevel.twoPlayer.TPGameKnowledgeExplorer;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import tools.ElapsedCpuTimer;

public class FBTPGameKnowledgeExplorer extends TPGameKnowledgeExplorer
{
	// Real field types
	// protected FBTPGameKnowledge gameKnowledge;
	// protected AgentMoveController agentMoveController;
	
	public FBTPGameKnowledgeExplorer()
	{
		
	}
	
	public FBTPGameKnowledgeExplorer(FBTPGameKnowledge gameKnowledge, AgentMoveController agentMoveController)
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
