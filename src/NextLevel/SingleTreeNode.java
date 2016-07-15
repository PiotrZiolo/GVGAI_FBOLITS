package NextLevel;

import java.util.ArrayList;
import java.util.Random;

import NextLevel.Agent;
import core.game.StateObservationMulti;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.Utils;

public class SingleTreeNode
{
    private static final double HUGE_NEGATIVE = -10000000.0;
    private static final double HUGE_POSITIVE =  10000000.0;
    public double epsilon = 1e-6;
    public static double egreedyEpsilon = 0.05;
    public SingleTreeNode parent;
    public SingleTreeNode[] children;
    public double totValue;
    public int nVisits;
    public Random m_rnd;
    public int m_depth;
    protected static double[] bounds = new double[]{Double.MAX_VALUE, -Double.MAX_VALUE};
    public int childIdx;

    public StateObservationMulti rootState;

    public SingleTreeNode(Random rnd, StateObservationMulti a_gameState) {
    	this.parent = null;
        this.m_rnd = rnd;
        this.totValue = 0.0;
    	this.childIdx = -1;
    	this.m_depth = 0;
        this.rootState = a_gameState;
        children = new SingleTreeNode[Agent.availableActions.size()];
    }

    public SingleTreeNode(SingleTreeNode parent, int childIdx, Random rnd, StateObservationMulti a_gameState ) {
        this.parent = parent;
        this.m_rnd = rnd;
        this.totValue = 0.0;
        this.childIdx = childIdx;
        if(parent != null)
            m_depth = parent.m_depth+1;
        else
            m_depth = 0;
        children = new SingleTreeNode[Agent.availableActions.size()];
    }


    public void mctsSearch(ElapsedCpuTimer elapsedTimer) {

        double avgTimeTaken = 0;
        double acumTimeTaken = 0;
        long remaining = elapsedTimer.remainingTimeMillis();
        int numIters = 0;

        int remainingLimit = 12;
        while(remaining > 2*avgTimeTaken && remaining > remainingLimit){
            StateObservationMulti state = rootState.copy();

            ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();
            SingleTreeNode selected = treePolicy(state);
            double delta = selected.rollOut(state);
            backUp(selected, delta);

            numIters++;
            acumTimeTaken += (elapsedTimerIteration.elapsedMillis()) ;
            //System.out.println(elapsedTimerIteration.elapsedMillis() + " --> " + acumTimeTaken + " (" + remaining + ")");
            avgTimeTaken  = acumTimeTaken/numIters;
            remaining = elapsedTimer.remainingTimeMillis();
        }
    }

    public SingleTreeNode treePolicy(StateObservationMulti state) {

        SingleTreeNode cur = this;

        while (!state.isGameOver() && cur.m_depth < SingleMCTSPlayer.ROLLOUT_DEPTH)
        {
            if (cur.notFullyExpanded()) {
                return cur.expand(state);
            } else {
                SingleTreeNode next = cur.uct(state);
                cur = next;
            }
        }

        return cur;
    }


    public SingleTreeNode expand(StateObservationMulti state) {

        int bestAction = 0;
        double bestValue = -1;

        for (int i = 0; i < children.length; i++) {
            double x = m_rnd.nextDouble();
            if (x > bestValue && children[i] == null) {
                bestAction = i;
                bestValue = x;
            }
        }

        //Roll the state

        //need to provide actions for all players to advance the forward model
        Types.ACTIONS[] acts = new Types.ACTIONS[Agent.no_players];

        //set this agent's action
        acts[Agent.id] = state.getAvailableActions(Agent.id).get(bestAction);

        //get actions available to the opponent and assume they will do a random action
        //ArrayList<Types.ACTIONS> oppActions = state.getAvailableActions(Agent.oppID);
        //acts[Agent.oppID] = oppActions.get(m_rnd.nextInt(oppActions.size()));
        acts[Agent.oppID] = getNotLosingAction(state, Agent.oppID, Types.ACTIONS.ACTION_NIL);

        state.advance(acts);

        SingleTreeNode tn = new SingleTreeNode(this, bestAction, this.m_rnd, state);
        children[bestAction] = tn;
        return tn;
    }

    public SingleTreeNode uct(StateObservationMulti state) {

        SingleTreeNode selected = null;
        double bestValue = -Double.MAX_VALUE;
        //System.out.println("UCT " + children.length);
        for (SingleTreeNode child : this.children)
        {
            //System.out.println(child);
            double hvVal = child.totValue;
            double childValue =  hvVal / (child.nVisits + this.epsilon);

            childValue = Utils.normalise(childValue, bounds[0], bounds[1]);
            //System.out.println("norm child value: " + childValue);

            double uctValue = childValue +
            		SingleMCTSPlayer.K * Math.sqrt(Math.log(this.nVisits + 1) / (child.nVisits + this.epsilon));

            uctValue = Utils.noise(uctValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly

            // small sampleRandom numbers: break ties in unexpanded nodes
            if (uctValue > bestValue) {
                selected = child;
                bestValue = uctValue;
            }
        }
        if (selected == null)
        {
            throw new RuntimeException("Warning! returning null: " + bestValue + " : " + this.children.length + " " +
            + bounds[0] + " " + bounds[1]);
        }

        //Roll the state:

        //need to provide actions for all players to advance the forward model
        Types.ACTIONS[] acts = new Types.ACTIONS[Agent.no_players];
        
        //set this agent's action
        acts[Agent.id] = state.getAvailableActions(Agent.id).get(selected.childIdx);

        //get actions available to the opponent and assume they will do a random action
        //ArrayList<Types.ACTIONS> oppActions = state.getAvailableActions(Agent.oppID);
        //acts[Agent.oppID] = oppActions.get(m_rnd.nextInt(oppActions.size()));
        acts[Agent.oppID] = getNotLosingAction(state, Agent.oppID, Types.ACTIONS.ACTION_NIL);

        state.advance(acts);

        return selected;
    }


    public double rollOut(StateObservationMulti state)
    {
        int thisDepth = this.m_depth;

        while (!finishRollout(state,thisDepth)) {

            //random move for all players
            Types.ACTIONS[] acts = new Types.ACTIONS[Agent.no_players];
            for (int i = 0; i < Agent.no_players; i++) {
                //ArrayList<Types.ACTIONS> availableActions = state.getAvailableActions(i);
                //acts[i] = availableActions.get(m_rnd.nextInt(availableActions.size()));
                acts[i] = getNotLosingAction(state, i, Types.ACTIONS.ACTION_NIL);
            }
            state.advance(acts);
            thisDepth++;
        }


        double delta = value(state);

        if(delta < bounds[0])
            bounds[0] = delta;
        if(delta > bounds[1])
            bounds[1] = delta;

        //double normDelta = Utils.normalise(delta ,lastBounds[0], lastBounds[1]);

        return delta;
    }

    public double value(StateObservationMulti a_gameState) {

        boolean gameOver = a_gameState.isGameOver();
        
        Types.WINNER win = a_gameState.getMultiGameWinner()[Agent.id];
        double rawScore = a_gameState.getGameScore(Agent.id);

        if(gameOver && win == Types.WINNER.PLAYER_LOSES)
            rawScore += HUGE_NEGATIVE;

        if(gameOver && win == Types.WINNER.PLAYER_WINS)
            rawScore += HUGE_POSITIVE;
        
        rawScore += Agent.heuristic.evaluateState(a_gameState);

        return rawScore;
    }

    public boolean finishRollout(StateObservationMulti rollerState, int depth)
    {
        if(depth >= SingleMCTSPlayer.ROLLOUT_DEPTH)      //rollout end condition.
            return true;

        if(rollerState.isGameOver())               //end of game
            return true;

        return false;
    }

    public void backUp(SingleTreeNode node, double result)
    {
        SingleTreeNode n = node;
        while(n != null)
        {
            n.nVisits++;
            n.totValue += result;
            n = n.parent;
        }
    }


    public Types.ACTIONS mostVisitedAction() {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;
        boolean allEqual = true;
        double first = -1;

        for (int i=0; i<children.length; i++) {

        	/*if (children[i]!=null)
        		System.out.println("Action " + rootState.getAvailableActions(Agent.id).get(i) + ", " + children[i].nVisits);
        	else
        		System.out.println("Action " + rootState.getAvailableActions(Agent.id).get(i) + ", null");*/
        	if(children[i] != null)
            {
                if(first == -1)
                    first = children[i].nVisits;
                else if(first != children[i].nVisits)
                {
                    allEqual = false;
                }
                
                double childValue = children[i].nVisits;
                childValue = Utils.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }
        //System.out.println();

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        } else if(allEqual)
        {
            //If all are equal, we opt to choose for the one with the best Q.
            selected = bestAction();
        }
        return rootState.getAvailableActions(Agent.id).get(selected);
    }

    public int bestAction()
    {
        int selected = -1;
        double bestValue = -Double.MAX_VALUE;

        for (int i=0; i<children.length; i++) {

            if(children[i] != null) {
                //double tieBreaker = m_rnd.nextDouble() * epsilon;
                double childValue = children[i].totValue / (children[i].nVisits + this.epsilon);
                System.out.println("Child = " + childValue);
                childValue = Utils.noise(childValue, this.epsilon, this.m_rnd.nextDouble());     //break ties randomly
                if (childValue > bestValue) {
                    bestValue = childValue;
                    selected = i;
                }
            }
        }

        if (selected == -1)
        {
            System.out.println("Unexpected selection!");
            selected = 0;
        }

        return selected;
    }


    public boolean notFullyExpanded() {
        for (SingleTreeNode tn : children) {
            if (tn == null) {
                return true;
            }
        }
        return false;
    }
    
    private Types.ACTIONS getNotLosingAction(StateObservationMulti state, int playerID, Types.ACTIONS oppmove)
    {
        /*int no_players = state.getNoPlayers();
        int oppID = (playerID + 1) % no_players;
        ArrayList<Types.ACTIONS> availableActions = state.getAvailableActions(playerID);
        java.util.Collections.shuffle(availableActions);

        //Look for the opponent actions that would not kill him.
        for (Types.ACTIONS action : availableActions) {
            Types.ACTIONS[] acts = new Types.ACTIONS[no_players];
            acts[oppID] = oppmove;
            acts[playerID] = action;

            StateObservationMulti stateCopy = state.copy();
            stateCopy.advance(acts);

            if(stateCopy.getMultiGameWinner()[playerID] != Types.WINNER.PLAYER_LOSES)
            	return action;
        }*/

        ArrayList<Types.ACTIONS> availableActions = state.getAvailableActions(playerID);
        return availableActions.get(new Random().nextInt(availableActions.size()));
	}
}
