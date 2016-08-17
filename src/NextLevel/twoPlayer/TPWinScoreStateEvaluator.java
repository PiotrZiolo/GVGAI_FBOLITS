package NextLevel.twoPlayer;

import NextLevel.GameKnowledge;
import NextLevel.StateEvaluator;
import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;

public class TPWinScoreStateEvaluator extends StateEvaluator
{
	protected TPGameKnowledge gameKnowledge;
	
	private static final double HUGE_NEGATIVE = -10000000.0;
    private static final double HUGE_POSITIVE =  10000000.0;
	
	public TPWinScoreStateEvaluator()
	{
		
	}
	
	public TPWinScoreStateEvaluator(GameKnowledge gameKnowledge)
	{
		this.gameKnowledge = (TPGameKnowledge)gameKnowledge;
	}
	
	public double evaluate(StateObservation stateObs)
	{
		StateObservationMulti stateObsMulti = (StateObservationMulti)stateObs;
		
		boolean gameOver = stateObsMulti.isGameOver();

        Types.WINNER win = stateObsMulti.getMultiGameWinner()[gameKnowledge.getPlayerID()];
        double rawScore = stateObsMulti.getGameScore(gameKnowledge.getPlayerID());

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            rawScore += HUGE_NEGATIVE;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            rawScore += HUGE_POSITIVE;

        return rawScore;
	}
}
