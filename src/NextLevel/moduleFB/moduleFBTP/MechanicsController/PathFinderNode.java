package NextLevel.moduleFB.moduleFBTP.MechanicsController;

import NextLevel.utils.Pair;
import core.game.StateObservationMulti;
import tools.Vector2d;

public class PathFinderNode
{
	public double cost;

	public String path;
	public int pathLength;

	public Vector2d positionV;
	public Pair<Integer, Integer> positionG;

	public StateObservationMulti stateObs;
	public int ID;

	public PathFinderNode(double cost, String path, StateObservationMulti stateObs, int playerID)
	{
		this.cost = cost;

		this.path = path;
		pathLength = path.length();

		this.stateObs = stateObs.copy();
		setPositions(stateObs.getAvatarPosition(playerID));

		ID = (stateObs.getWorldDimension().width / stateObs.getBlockSize()) * positionG.second() + positionG.first();
	}

	public PathFinderNode(double cost, String path, StateObservationMulti stateObs, Vector2d positionV)
	{
		this.cost = cost;

		this.path = path;
		pathLength = path.length();

		this.stateObs = stateObs.copy();
		setPositions(positionV);

		ID = (stateObs.getWorldDimension().width / stateObs.getBlockSize()) * positionG.second() + positionG.first();
	}

	private void setPositions(Vector2d positionV)
	{
		this.positionV = positionV;
		positionG = new Pair<Integer, Integer>((int) positionV.x / stateObs.getBlockSize(),
				(int) positionV.y / stateObs.getBlockSize());
	}

	@Override
	public boolean equals(Object n)
	{
		if (n == null || !(n instanceof PathFinderNode))
			return false;
		return positionG.equals(((PathFinderNode) n).positionG);
	}
}
