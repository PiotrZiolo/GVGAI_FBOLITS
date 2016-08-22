package NextLevel.mechanicsController;

import java.util.ArrayList;
import NextLevel.GameKnowledge;
import core.game.StateObservation;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class PathFinder
{
	protected GameKnowledge gameKnowledge;
	protected GameMechanicsController gameMechanicsController;

	public PathFinder()
	{
		this.gameKnowledge = new GameKnowledge();
	}

	public PathFinder(GameKnowledge gameKnowledge, GameMechanicsController gameMechanicsController)
	{
		this.gameKnowledge = gameKnowledge;
		this.gameMechanicsController = gameMechanicsController;
	}

	public ArrayList<Types.ACTIONS> findPath(Vector2d goalPosition, StateObservation stateObs,
			ElapsedCpuTimer elapsedTimer, int timeLimit)
	{
		ArrayList<Types.ACTIONS> path = new ArrayList<Types.ACTIONS>();
		path.add(Types.ACTIONS.ACTION_NIL);
		return path;
	}

	public ArrayList<Types.ACTIONS> findPath(Vector2d startPosition, Vector2d goalPosition, StateObservation stateObs,
			ElapsedCpuTimer elapsedTimer, int timeLimit)
	{
		ArrayList<Types.ACTIONS> path = new ArrayList<Types.ACTIONS>();
		path.add(Types.ACTIONS.ACTION_NIL);
		return path;
	}
}
