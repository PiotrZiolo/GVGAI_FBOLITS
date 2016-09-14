package FBOLITS;

import FBOLITS.StateEvaluator;
import core.game.StateObservation;
import ontology.Types;

public class WinScoreStateEvaluator extends StateEvaluator
{
	private static final double HUGE_NEGATIVE = -10000000.0;
	private static final double HUGE_POSITIVE = 10000000.0;

	public WinScoreStateEvaluator()
	{

	}

	public double evaluateState(StateObservation stateObs)
	{
		boolean gameOver = stateObs.isGameOver();

		Types.WINNER win = stateObs.getGameWinner();
		double rawScore = stateObs.getGameScore();

		if (gameOver && win == Types.WINNER.PLAYER_LOSES)
			rawScore += HUGE_NEGATIVE;

		if (gameOver && win == Types.WINNER.PLAYER_WINS)
			rawScore += HUGE_POSITIVE;

		return rawScore;
	}
}
