package burlap.examples.gridWorld;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Subclass the Action class to define the actions that our agent can apply within the domain
 */
public class Movement extends Action {
    //0: north; 1: south; 2:east; 3: west
    protected double[] directionProbs = new double[4];
    private Random rnd = new Random();

    public Movement(String name, Domain domain, int direction) {
        super(name, domain, "");
        for (int i=0; i < 4; i++)
            directionProbs[i] = (i == direction)? 0.8 : 0.2/3;
    }

    /**
     * Method that actually carries out a simulation of the agent in the domain from state s to s'
     */
    @Override
    protected State performActionHelper(State state, String[] params) {
        //Get the current position of the agent
        ObjectInstance agent = state.getFirstObjectOfClass(ExampleGridWorld.CLASS_AGENT);
        int curX = agent.getIntValForAttribute(ExampleGridWorld.ATT_X);
        int curY = agent.getIntValForAttribute(ExampleGridWorld.ATT_Y);
        //Get action from probability distribution
        double sumProb = 0.0, roll = rnd.nextDouble();
        int dir = 0;
        for (int i=0; i < 4; i++) {
            sumProb += this.directionProbs[i];
            if (roll < sumProb) {
                dir = i;
                break;
            }
        }
        int[] newPos = this.moveResult(curX, curY, dir);

        //update the agent
        agent.setValue(ExampleGridWorld.ATT_X, newPos[0]);
        agent.setValue(ExampleGridWorld.ATT_Y, newPos[1]);

        return state;
    }

    /**
     * Tries to move the agent in the direction requested. Will accede, or hit a wall and stay where it is
     */
    protected int[] moveResult(int curX, int curY, int direction) {
        int X,Y;
        switch (direction) {
            case 0:
                Y = curY + 1;
                X = curX;
                break;
            case 1:
                Y = curY - 1;
                X = curX;
                break;
            case 2:
                Y = curY;
                X = curX + 1;
                break;
            case 3:
                Y = curY;
                X = curX - 1;
                break;
            default:
                throw new IllegalStateException("Invalid direction requested");
        }
        int width = ExampleGridWorld.map.length;
        int height = ExampleGridWorld.map[0].length;

        if (X < 0 || X >= width || Y < 0 || Y>= height || ExampleGridWorld.map[X][Y] == 1) {
            return new int[] {curX, curY};
        } else {
            return new int[] {X, Y};
        }
    }

    /**
     * Provides a set of all possible transitions from state s to \vec{s'} with the associated probabilities
     */
    @Override
    public List<TransitionProbability> getTransitions(State state, String[] params) {
        List<TransitionProbability> result = new ArrayList<TransitionProbability>(4);
        //get agent status
        ObjectInstance agent = state.getFirstObjectOfClass(ExampleGridWorld.CLASS_AGENT);
        int curX = agent.getIntValForAttribute(ExampleGridWorld.ATT_X);
        int curY = agent.getIntValForAttribute(ExampleGridWorld.ATT_Y);
        TransitionProbability noMove = null;
        for (int i = 0; i < 4; i++) {
            int[] newPos = moveResult(curX, curY, i);
            if (newPos[0] != curX && newPos[1] != curY) {
                State newState = state.copy();
                ObjectInstance newAgent = newState.getFirstObjectOfClass(ExampleGridWorld.CLASS_AGENT);
                newAgent.setValue(ExampleGridWorld.ATT_X, newPos[0]);
                newAgent.setValue(ExampleGridWorld.ATT_Y, newPos[1]);
                result.add(new TransitionProbability(newState, directionProbs[i]));
            } else {
                //There could be multiple "no moves", so aggregate them
                if (noMove == null) {
                    noMove = new TransitionProbability(state.copy(), directionProbs[i]);
                } else {
                    noMove.p += directionProbs[i];
                }
            }
        }
        if (noMove != null) result.add(noMove);
        return result;
    }

    @Override
    public boolean applicableInState(State s, String[] params) {
        return super.applicableInState(s, params);
    }
}
