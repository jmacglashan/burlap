package household;

import burlap.mdp.core.oo.propositional.PropositionalFunction;
import org.apache.commons.lang3.StringUtils;

// from Cleanup Goal Description (CleanupGoalDescription.java)

public class HouseholdGoalDescription {

    private String[] params;
    private PropositionalFunction pf;

    public HouseholdGoalDescription(String[] params, PropositionalFunction pf) {
	this.params = params;
	this.pf = pf;
    }

    public String[] getParams() {
	return params;
    }

    public void setParams(String[] params) {
	this.params = params;
    }

    public PropositionalFunction getPf() {
	return pf;
    }

    public void setPf(PropositionalFunction pf) {
	this.pf = pf;
    }

    public String toString() {
	return "" + pf.getName() + "<" + StringUtils.join(params, "|") + ">";
    }
}
