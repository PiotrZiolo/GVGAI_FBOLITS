package FBOLITS.moduleFB;

import tools.Vector2d;

public class InfluencePoint
{
	public int id;
	public Vector2d position;
	public double influence;
	public int function;

	public InfluencePoint(int id, Vector2d position, double influence, int function)
	{
		this.id = id;
		this.position = position;
		this.influence = influence;
		this.function = function;
	}

	public InfluencePoint reduceInfluence()
	{
		return new InfluencePoint(id, position, -influence, function);
	}
}
