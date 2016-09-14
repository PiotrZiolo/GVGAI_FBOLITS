package FBOLITS.utils;

import java.util.Random;

import core.game.StateObservation;
import core.game.StateObservationMulti;
import ontology.Types;

public class AuxUtils
{
	public static int actionToIndex(StateObservation stateObs, Types.ACTIONS action)
	{
		int index = 0;

		for (; index < stateObs.getAvailableActions().size(); index++)
		{
			if (action == stateObs.getAvailableActions().get(index))
				break;
		}

		return index;
	}
	
	public static int actionToIndex(StateObservationMulti stateObs, int playerID, Types.ACTIONS action)
	{
		int index = 0;

		for (; index < stateObs.getAvailableActions(playerID).size(); index++)
		{
			if (action == stateObs.getAvailableActions(playerID).get(index))
				break;
		}

		return index;
	}

	public static Types.ACTIONS indexToAction(StateObservation stateObs, int index)
	{
		return stateObs.getAvailableActions().get(index);
	}
	
	public static Types.ACTIONS indexToAction(StateObservationMulti stateObs, int playerID, int index)
	{
		return stateObs.getAvailableActions(playerID).get(index);
	}

	public static int mod(int x, int size)
	{
		if (x >= size)
			return x % size;
		if (x >= 0)
			return x;
		return mod(x + size, size);
	}

	public static int[] getRandomPermutation(int length)
	{
		int[] array = new int[length];
		for (int i = 0; i < array.length; i++)
			array[i] = i;

		Random rg = new Random();
		for (int i = 0; i < length; i++)
		{
			int ran = i + rg.nextInt(length - i);

			int temp = array[i];
			array[i] = array[ran];
			array[ran] = temp;
		}
		return array;
	}
}
