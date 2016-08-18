package NextLevel.twoPlayer;

import NextLevel.GameKnowledge;
import NextLevel.StateEvaluator;
import NextLevel.StateHandler;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;

public class TPWinScoreStateEvaluator extends StateEvaluator
{
	// Real types of fields
	// protected TPGameKnowledge gameKnowledge;
	// protected StateHandler stateHandler;
	
	private static final double HUGE_NEGATIVE = -10000000.0;
    private static final double HUGE_POSITIVE =  10000000.0;
	
	public TPWinScoreStateEvaluator()
	{
		
	}
	
	public TPWinScoreStateEvaluator(TPGameKnowledge gameKnowledge, StateHandler stateHandler)
	{
		this.gameKnowledge = gameKnowledge;
		this.stateHandler = stateHandler;
	}
	
	public double evaluateState(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti)stateObs;
		TPGameKnowledge tpGameKnowledge = (TPGameKnowledge) this.gameKnowledge;
		
		boolean gameOver = stateObsMulti.isGameOver();

        Types.WINNER win = stateObsMulti.getMultiGameWinner()[tpGameKnowledge.getPlayerID()];
        double rawScore = stateObsMulti.getGameScore(tpGameKnowledge.getPlayerID());

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            rawScore += HUGE_NEGATIVE;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            rawScore += HUGE_POSITIVE;

        return rawScore;
	}
}
