package NextLevel;

public class StateEvaluatorTeacher
{
	private StateEvaluator stateEvaluator;
	private GameKnowledge gameKnowledge;
	
	public StateEvaluatorTeacher()
	{
		
	}
	
	public StateEvaluatorTeacher(StateEvaluator stateEvaluator, GameKnowledge gameKnowledge)
	{
		this.stateEvaluator = stateEvaluator;
		this.gameKnowledge = gameKnowledge;
	}

	public void initializeEvaluator()
	{
		// To be overridden in subclasses
	}

	public void updateEvaluator()
	{
		// To be overridden in subclasses
	}
}