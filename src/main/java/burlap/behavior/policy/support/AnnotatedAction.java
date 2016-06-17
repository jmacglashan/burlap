package burlap.behavior.policy.support;

import burlap.mdp.core.action.Action;

/**
 * @author James MacGlashan.
 */
public class AnnotatedAction implements Action {
	public Action srcAction;
	public String annotation;


	public AnnotatedAction() {
	}

	public AnnotatedAction(Action srcAction, String annotation) {
		this.srcAction = srcAction;
		this.annotation = annotation;
	}

	@Override
	public String actionName() {
		return srcAction.actionName();
	}

	@Override
	public Action copy() {
		return new AnnotatedAction(srcAction, annotation);
	}


	@Override
	public int hashCode() {
		return srcAction.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		AnnotatedAction that = (AnnotatedAction) o;

		if(srcAction != null ? !srcAction.equals(that.srcAction) : that.srcAction != null) return false;
		return annotation != null ? annotation.equals(that.annotation) : that.annotation == null;

	}

	@Override
	public String toString() {
		return "*" + this.annotation + "--" + this.srcAction.toString();
	}
}
