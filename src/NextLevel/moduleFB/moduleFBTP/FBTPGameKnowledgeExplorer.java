package NextLevel.moduleFB.moduleFBTP;

import java.util.ArrayList;

import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPGameKnowledgeExplorer;
import core.game.Observation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import tools.ElapsedCpuTimer;

public class FBTPGameKnowledgeExplorer extends TPGameKnowledgeExplorer
{
	// Real field types
	// protected FBTPGameKnowledge gameKnowledge;
	// protected TPGameMechanicsController gameMechanicsController;
	// protected FBTPAgentMoveController agentMoveController;
	
	protected ArrayList<Observation> toCheckQueue;
	
	public FBTPGameKnowledgeExplorer()
	{
		
	}
	
	public FBTPGameKnowledgeExplorer(FBTPGameKnowledge gameKnowledge, AgentMoveController agentMoveController, TPGameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		this.agentMoveController = agentMoveController;
	}

	public void learn(StateObservation stateObs, int playerID, ElapsedCpuTimer elapsedTimer, int timeForLearning)
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
