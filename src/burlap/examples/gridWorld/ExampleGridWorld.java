package burlap.examples.gridWorld;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.*;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.explorer.VisualExplorer;
import burlap.oomdp.visualizer.StateRenderLayer;
import burlap.oomdp.visualizer.Visualizer;

/**
 * Example of using OO-MDP to solve a gridworld problem
 *
 * The tutorial can be found at http://burlap.cs.brown.edu/tutorials/bd/p2.html
 */
public class ExampleGridWorld implements DomainGenerator {
    public static final String ATT_X = "x";
    public static final String ATT_Y = "y";
    public static final String CLASS_AGENT = "agent";
    public static final String CLASS_LOCATION = "location";
    public static final String ACTIONNORTH = "north";
    public static final String ACTIONSOUTH = "south";
    public static final String ACTIONEAST = "east";
    public static final String ACTIONWEST = "west";

    //ordered so first dimension is x
    public static int [][] map = new int[][]{
            {0,0,0,0,0,1,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,1,0,0,0,0,0},
            {0,0,0,0,0,1,0,0,0,0,0},
            {0,0,0,0,0,1,0,0,0,0,0},
            {1,0,1,1,1,1,1,1,0,1,1},
            {0,0,0,0,1,0,0,0,0,0,0},
            {0,0,0,0,1,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,1,0,0,0,0,0,0},
            {0,0,0,0,1,0,0,0,0,0,0},
    };

    @Override
    public Domain generateDomain() {
        //Create a new State-Action domain
        SADomain domain = new SADomain();
        //Create and link objects to the domain.
        Attribute xAtt = new Attribute(domain, ATT_X, Attribute.AttributeType.INT);
        Attribute yAtt = new Attribute(domain, ATT_Y, Attribute.AttributeType.INT);
        //Limits are inclusive
        xAtt.setLims(0, 10);
        yAtt.setLims(0, 10);

        //ObjectClass - this class defines an object class name and is defined with an associated set of Attribute objects.
        ObjectClass agentClass = new ObjectClass(domain, CLASS_AGENT);
        agentClass.addAttribute(xAtt);
        agentClass.addAttribute(yAtt);

        ObjectClass locationClass = new ObjectClass(domain, CLASS_LOCATION);
        locationClass.addAttribute(xAtt);
        locationClass.addAttribute(yAtt);

        //Add actions
        new Movement(ACTIONNORTH, domain, 0);
        new Movement(ACTIONSOUTH, domain, 1);
        new Movement(ACTIONEAST, domain, 2);
        new Movement(ACTIONWEST, domain, 3);

        new AtLocation(domain);

        return domain;
    }

    public static State getInitialState(Domain domain) {
        State s = new State();
        ObjectInstance agent = new ObjectInstance(domain.getObjectClass(CLASS_AGENT), "agent0");
        ObjectInstance target = new ObjectInstance(domain.getObjectClass(CLASS_LOCATION), "target");

        agent.setValue(ATT_X, 0);
        agent.setValue(ATT_Y, 0);

        target.setValue(ATT_X, 10);
        target.setValue(ATT_Y, 10);

        s.addObject(agent);
        s.addObject(target);

        return s;
    }

    public static StateRenderLayer getStateRenderLayer(){
        StateRenderLayer rl = new StateRenderLayer();
        rl.addStaticPainter(new WallPainter());
        rl.addObjectClassPainter(CLASS_LOCATION, new LocationPainter());
        rl.addObjectClassPainter(CLASS_AGENT, new AgentPainter());
        return rl;
    }

    public static Visualizer getVisualizer(){
        return new Visualizer(getStateRenderLayer());
    }

    public static void main(String[] args) {
        ExampleGridWorld gen = new ExampleGridWorld();
        Domain domain = gen.generateDomain();
        State initialState = getInitialState(domain);
        //TerminalExplorer exp = new TerminalExplorer(domain);
        //exp.exploreFromState(initialState);
        Visualizer v = getVisualizer();
        VisualExplorer exp = new VisualExplorer(domain, v, initialState, 640, 640);
        exp.addKeyAction("w", ACTIONNORTH);
        exp.addKeyAction("a", ACTIONWEST);
        exp.addKeyAction("s", ACTIONSOUTH);
        exp.addKeyAction("d", ACTIONEAST);

        exp.initGUI();
    }

}
