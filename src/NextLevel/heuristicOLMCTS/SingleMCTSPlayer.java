package NextLevel.heuristicOLMCTS;

import java.util.Random;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;

/**
 * Created with IntelliJ IDEA.
 * User: Diego
 * Date: 07/11/13
 * Time: 17:13
 */
public class SingleMCTSPlayer
{
    /**
     * Root of the tree.
     */
    public SingleTreeNode m_root;

    /**
     * Random generator.
     */
    public Random m_rnd;
    
    public static int ROLLOUT_DEPTH;
    public static double K;

    public SingleMCTSPlayer(Random a_rnd)
    {
        m_rnd = a_rnd;
    }

    /**
     * Inits the tree with the new observation state in the root.
     * @param a_gameState current state of the game.
     */
    public void init(StateObservationMulti a_gameState, int ROLLOUT_DEPTH, double K)
    {
        //Set the game observation to a newly root node.
        //System.out.println("learning_style = " + learning_style);
    	SingleMCTSPlayer.ROLLOUT_DEPTH = ROLLOUT_DEPTH;
    	SingleMCTSPlayer.K = ROLLOUT_DEPTH;
        m_root = new SingleTreeNode(m_rnd, a_gameState);
    }

    /**
     * Runs MCTS to decide the action to take. It does not reset the tree.
     * @param elapsedTimer Timer when the action returned is due.
     * @return the action to execute in the game.
     */
    public Types.ACTIONS run(ElapsedCpuTimer elapsedTimer)
    {
        //Do the search within the available time.
        m_root.mctsSearch(elapsedTimer);

        //Determine the best action to take and return it.
        Types.ACTIONS action = m_root.mostVisitedAction();
        //int action = m_root.bestAction();
        return action;
    }

}
