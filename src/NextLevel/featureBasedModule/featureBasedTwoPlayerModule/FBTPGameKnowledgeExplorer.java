package NextLevel.featureBasedModule.featureBasedTwoPlayerModule;

import baseStructure.GameKnowledge;
import baseStructure.GameKnowledgeExplorer;
import baseStructure.moveController.AgentMoveController;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import tools.ElapsedCpuTimer;

public class FBTPGameKnowledgeExplorer extends GameKnowledgeExplorer
{
	private StateObservationMulti stateObs;
	private FBTPGameKnowledge gameKnowledge;
	private FBTPAgentMoveController agentMoveController;
	
	public FBTPGameKnowledgeExplorer(StateObservation stateObs, GameKnowledge gameKnowledge,
			AgentMoveController agentMoveController, int playerID)
	{
		this.stateObs = (StateObservationMulti)stateObs;
		this.gameKnowledge = (FBTPGameKnowledge)gameKnowledge;
		this.agentMoveController = (FBTPAgentMoveController)agentMoveController;
		this.gameKnowledge.setPlayerID(playerID);
		this.gameKnowledge.setOppID(1 - playerID);
		this.gameKnowledge.setNumOfPlayers(stateObs.getNoPlayers());
	}
	
	public void learn(ElapsedCpuTimer elapsedTimer, int timeForLearningDuringInitialization)
	{
		
	}
}
