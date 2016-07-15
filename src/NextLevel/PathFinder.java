package NextLevel;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;

import core.game.Observation;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.Vector2d;

public class PathFinder
{

	int abs(double n)
	{
		return (int) ((n < 0) ? -n : n);
	}

	int[] toDirection(Types.ACTIONS act)
	{
		if (act == Types.ACTIONS.ACTION_UP)
			return new int[] { 0, -1 };
		if (act == Types.ACTIONS.ACTION_DOWN)
			return new int[] { 0, 1 };
		if (act == Types.ACTIONS.ACTION_LEFT)
			return new int[] { 1, 0 };
		else
			return new int[] { -1, 0 };
	}

	Types.ACTIONS getDirection(int[] s, int[] g)
	{
		if (s[0] < g[0])
			return Types.ACTIONS.ACTION_LEFT;
		if (s[0] > g[0])
			return Types.ACTIONS.ACTION_RIGHT;
		if (s[1] < g[1])
			return Types.ACTIONS.ACTION_UP;
		return Types.ACTIONS.ACTION_DOWN;
	}

	int[] getCordinates(Vector2d v, int size)
	{
		return new int[] { (int) v.x / size, (int) v.y / size };
	}

	void print(byte[][] tab)
	{
		for (int i = 0; i < tab[0].length; i++)
		{
			for (int j = 0; j < tab.length; j++)
				System.out.print(tab[j][i] + " ");
			System.out.println();
		}
		System.out.println("\n");

	}

	int mod(int x, int size)
	{
		if (x >= 0 && x < size)
			return x;
		if (x < size)
			return mod(x + size, size);
		return x % size;
	}

	// LIFO is the best choice for such function
	Deque<Types.ACTIONS> pathFinder(Vector2d s, Vector2d g, StateObservationMulti stateObs, int index)
	{
		final int size = stateObs.getBlockSize();
		final int width = (int) stateObs.getWorldDimension().getWidth() / size;
		final int height = (int) stateObs.getWorldDimension().getHeight() / size;
		final ArrayList<Types.ACTIONS> actions = new ArrayList<Types.ACTIONS>();

		// in case we are hitting a block every time
		ArrayList<int[]> positions = new ArrayList<int[]>();
		positions.add(new int[] { (int) s.x / size, (int) s.y / size });
		if (s.x % size != 0)
		{
			positions.add(new int[] { positions.get(0)[0] + 1, positions.get(0)[1] });
			if (s.y % size != 0)
				positions.add(new int[] { positions.get(0)[0] + 1, positions.get(0)[1] + 1 });
		}
		if (s.y % size != 0)
			positions.add(new int[] { positions.get(0)[0], positions.get(0)[1] + 1 });

		// for(int[] pos : positions)
		// System.out.println(pos[0]+" "+pos[1]);

		// now we determine which block to which block we can approximate to
		boolean[] positions_objects = new boolean[positions.size()];
		for (int i = 0; i < positions.size(); i++)
		{
			for (int x = -1; x <= 1; x += 2)
			{
				for (Observation obs : stateObs.getObservationGrid()[(width + positions.get(i)[0] + x)
						% width][(height + positions.get(i)[1]) % height])
					positions_objects[i] = obs.category == 4;
				if (positions_objects[i])
					break;
			}
			if (positions_objects[i])
				break;
			for (int y = -1; y <= 1; y += 2)
				for (Observation obs : stateObs.getObservationGrid()[(width + positions.get(i)[0])
						% width][(height + positions.get(i)[1] + y) % height])
					positions_objects[i] = obs.category == 4;
			if (positions_objects[i])
				break;
		}

		// those are the vectors in a "divided by blckSze" metric, had to
		// initialize both
		int[] start = new int[0];
		int[] goal = new int[] { (int) g.x / size, (int) g.y / size };

		// if the positions_objects is true, then it's a pessimistic position,
		// so we can approximate to it
		boolean none = true;
		for (int i = 0; i < positions.size(); i++)
			if (positions_objects[i])
			{
				none = false;
				start = new int[] { positions.get(i)[0], positions.get(i)[1] };
				break;
			}
		if (none)
			start = new int[] { positions.get(0)[0], positions.get(0)[1] };

		// check available movements
		Types.ACTIONS[] act = { Types.ACTIONS.ACTION_RIGHT, Types.ACTIONS.ACTION_LEFT, Types.ACTIONS.ACTION_UP,
				Types.ACTIONS.ACTION_DOWN };
		for (Types.ACTIONS i : stateObs.getAvailableActions(index))
			for (Types.ACTIONS j : act)
				if (j == i)
					actions.add(j);

		// initialize array of visited vertices
		byte[][] V = new byte[width][height];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				V[i][j] = 0;

		// initiaize array of previous vertices
		int[][][] prev = new int[width][height][2];

		// eliminate immovable objects
		ArrayList<Observation>[] obs = stateObs.getImmovablePositions();
		if (obs != null)
			for (int i = 0; i < obs.length; i++)
				for (Observation o : obs[i])
					V[(int) o.position.x / stateObs.getBlockSize()][(int) o.position.y / stateObs.getBlockSize()] = 2;

		// eliminate portals objects
		obs = stateObs.getPortalsPositions();
		if (obs != null)
			for (int i = 0; i < obs.length; i++)
				for (Observation o : obs[i])
					V[(int) o.position.x / stateObs.getBlockSize()][(int) o.position.y / stateObs.getBlockSize()] = 2;

		V[goal[0]][goal[1]] = 0;

		// this line is after the elimination to prevent endless loop
		V[start[0]][start[1]] = 1;
		// System.out.println("start <- " + start[0] + " ; " + start[1]);
		// FIFO
		LinkedList<int[]> list = new LinkedList<int[]>();
		list.add(new int[] { start[0], start[1] });

		// check if the first move is possible
		int[] current;
		current = list.peek();
		list.pop();
		for (Types.ACTIONS a : actions)
		{
			int[] direction = toDirection(a);
			int[] next = new int[] { current[0] + direction[0], current[1] + direction[1] };
			if (V[mod(next[0], width)][mod(next[1], height)] == 0)
			{
				StateObservationMulti stateObsCopy = stateObs.copy();
				stateObsCopy.advance(a);
				if (stateObsCopy.isGameOver())
				{
					V[mod(next[0], width)][mod(next[1], height)] = 2;
					break;
				}
				prev[mod(next[0], width)][mod(next[1], height)] = current;
				list.add(next);
				V[mod(next[0], width)][mod(next[1], height)] = 1;
			}
		}

		// BFS
		while (!list.isEmpty() && V[goal[0]][goal[1]] == 0)
		{
			current = list.peek();
			list.pop();
			for (Types.ACTIONS a : actions)
			{
				int[] direction = toDirection(a);
				int[] next = new int[] { current[0] + direction[0], current[1] + direction[1] };
				if (V[mod(next[0], width)][mod(next[1], height)] == 0)
				{
					prev[mod(next[0], width)][mod(next[1], height)] = current;
					list.add(next);
					V[mod(next[0], width)][mod(next[1], height)] = 1;
				}
			}
			// print(V);

		}

		// check if the goal is reached

		int pathLengthLimit = 200;

		if (V[goal[0]][goal[1]] == 1)
		{
			Deque<Types.ACTIONS> path = new ArrayDeque<Types.ACTIONS>();
			path.add(getDirection(goal, prev[goal[0]][goal[1]]));
			current = prev[goal[0]][goal[1]];
			// back trace the path
			while (current[0] != start[0] || current[1] != start[1])
			{
				path.add(getDirection(current, prev[(width + current[0]) % width][(height + current[1]) % height]));

				current = prev[mod(current[0], width)][mod(current[1], height)];

				pathLengthLimit--;
				if (pathLengthLimit == 0)
				{
					path = new ArrayDeque<Types.ACTIONS>();
					path.add(Types.ACTIONS.ACTION_NIL);
					return path;
				}
			}
			return path;
		}
		// in case the goal isn't find
		Deque<Types.ACTIONS> path = new ArrayDeque<Types.ACTIONS>();
		path.add(Types.ACTIONS.ACTION_NIL);
		return path;

	}
}