package NextLevel;

public class StateEvaluator
{
	private GameKnowledge gameKnowledge;
	
	public StateEvaluator()
	{
		
	}
	
	public StateEvaluator(GameKnowledge gameKnowledge)
	{
		this.gameKnowledge = gameKnowledge;
	}
	
	public double evaluate(State state)
	{
		// To be overridden in subclasses
		
		return 0;
	}
}
