package burlap.mdp.core.action;

/**
 * A simple implementation of {@link Action} for unparameterized actions. Contains a single String for the name
 * of the action and nothing else.
 * @author James MacGlashan.
 */
public class SimpleAction implements Action{

	protected String name;

	public SimpleAction() {
	}

	public SimpleAction(String name) {
		this.name = name;
	}

	@Override
	public String actionName() {
		return name;
	}

	@Override
	public Action copy() {
		return new SimpleAction(name);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		SimpleAction that = (SimpleAction) o;

		return name != null ? name.equals(that.name) : that.name == null;

	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	@Override
	public String toString() {
		return name;
	}
}
