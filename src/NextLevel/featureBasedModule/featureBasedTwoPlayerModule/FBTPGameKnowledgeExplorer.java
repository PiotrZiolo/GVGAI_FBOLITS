package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import NextLevel.moveController.AgentMoveController;
import NextLevel.twoPlayer.TPGameKnowledgeExplorer;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import tools.ElapsedCpuTimer;

public class FBTPGameKnowledgeExplorer extends TPGameKnowledgeExplorer
{
	protected StateObservation stateObs; 
	protected FBTPGameKnowledge gameKnowledge;
	protected AgentMoveController agentMoveController;
	
	public FBTPGameKnowledgeExplorer()
	{
		
	}
	
	public FBTPGameKnowledgeExplorer(FBTPGameKnowledge gameKnowledge, AgentMoveController agentMoveController)
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
