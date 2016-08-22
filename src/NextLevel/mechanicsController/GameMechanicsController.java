package NextLevel.mechanicsController;

import ontology.Types;
import tools.Direction;

public class GameMechanicsController
{
	public static boolean isOrientationConsistentWithMove(Types.ACTIONS act, Direction orientation)
	{
		if (orientation.equals(Types.DNONE))
			return true;
		if (act.equals(Types.ACTIONS.ACTION_LEFT))
			return orientation.equals(Types.DLEFT);
		if (act.equals(Types.ACTIONS.ACTION_UP))
			return orientation.equals(Types.DUP);
		if (act.equals(Types.ACTIONS.ACTION_RIGHT))
			return orientation.equals(Types.DRIGHT);
		if (act.equals(Types.ACTIONS.ACTION_DOWN))
			return orientation.equals(Types.DDOWN);
		return false;
	}
}
