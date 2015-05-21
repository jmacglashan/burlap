package burlap.examples.gridWorld;

import burlap.oomdp.core.*;

/**
 * The Function class should evaluate to true when the provided agent object's position is equal to the provided
 * location object's position.
 */
public class AtLocation extends PropositionalFunction {
    public static final String PFAT = "at";

    public AtLocation(Domain domain) {
        super(PFAT, domain, new String[] {ExampleGridWorld.CLASS_AGENT, ExampleGridWorld.CLASS_LOCATION});
    }

    @Override
    public boolean isTrue(State s, String[] params) {
        ObjectInstance agent = s.getObject(params[0]);
        ObjectInstance loc = s.getObject(params[1]);

        return (agent.getIntValForAttribute(ExampleGridWorld.ATT_X) == loc.getIntValForAttribute(ExampleGridWorld.ATT_X)) &&
                (agent.getIntValForAttribute(ExampleGridWorld.ATT_Y) == loc.getIntValForAttribute(ExampleGridWorld.ATT_Y));
    }
}
