package NextLevel.moduleTP;

import NextLevel.GameKnowledge;
import NextLevel.StateEvaluator;
import NextLevel.StateHandler;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;

public class TPWinScoreStateEvaluator extends StateEvaluator
{
	private int playerID;
	
	private static final double HUGE_NEGATIVE = -10000000.0;
    private static final double HUGE_POSITIVE =  10000000.0;
	
	public TPWinScoreStateEvaluator()
	{
		
	}
	
	public TPWinScoreStateEvaluator(int playerID)
	{
		this.playerID = playerID;
	}
	
	public double evaluateState(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti)stateObs;
		
		boolean gameOver = stateObsMulti.isGameOver();

        Types.WINNER win = stateObsMulti.getMultiGameWinner()[playerID];
        double rawScore = stateObsMulti.getGameScore(playerID);

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            rawScore += HUGE_NEGATIVE;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            rawScore += HUGE_POSITIVE;

        return rawScore;
	}
}
