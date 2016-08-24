package NextLevel.moduleFB.moduleFBTP;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import NextLevel.mechanicsController.AgentMoveController;
import NextLevel.mechanicsController.TPGameMechanicsController;
import NextLevel.moduleFB.moduleFBTP.MechanicsController.FBTPAgentMoveController;
import NextLevel.moduleTP.TPGameKnowledge;
import NextLevel.moduleTP.TPGameKnowledgeExplorer;
import NextLevel.utils.LogHandler;
import core.game.Observation;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class FBTPGameKnowledgeExplorer extends TPGameKnowledgeExplorer
{
	// Real field types
	// protected FBTPGameKnowledge gameKnowledge;
	// protected TPGameMechanicsController gameMechanicsController;
	// protected FBTPAgentMoveController agentMoveController;
	// protected ElapsedCpuTimer elapsedTimer;

	private int playerID = 0;
	private int oppID = 1;

	// protected PriorityQueue<TPSituationOfInterest> situationsToCheck;

	public FBTPGameKnowledgeExplorer()
	{

	}

	public FBTPGameKnowledgeExplorer(FBTPGameKnowledge gameKnowledge, AgentMoveController agentMoveController,
			TPGameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
		this.agentMoveController = agentMoveController;

		this.elapsedTimer = new ElapsedCpuTimer();
	}

	public void learn(StateObservation stateObs, int playerID, ElapsedCpuTimer elapsedTimer, int timeForLearning)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti) stateObs;
		this.stateObs = stateObsMulti;
		TPGameKnowledge tpGameKnowledge = (TPGameKnowledge) this.gameKnowledge;
		tpGameKnowledge.setPlayerID(playerID);
		tpGameKnowledge.setOppID(1 - playerID);
		tpGameKnowledge.setNumOfPlayers(stateObsMulti.getNoPlayers());
		tpGameKnowledge.setNumOfPlayerActions(stateObsMulti.getAvailableActions(playerID).size());
		tpGameKnowledge.setNumOfOpponentActions(stateObsMulti.getAvailableActions(tpGameKnowledge.getOppID()).size());
		tpGameKnowledge.setPlayerActions(stateObsMulti.getAvailableActions(playerID));
		tpGameKnowledge.setOpponentActions(stateObsMulti.getAvailableActions(tpGameKnowledge.getOppID()));

	}

	
}
