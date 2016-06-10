package burlap.mdp.singleagent.model;

import burlap.mdp.core.Action;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An implementation of {@link FullModel} that will delegate transition estimates for different actions to different
 * {@link SampleModel} or {@link FullModel} implementations. Also contains a default {@link SampleModel} to use
 * for actions that do not have specific delegates assigned.
 * @author James MacGlashan.
 */
public class DelegatedModel implements FullModel {

	SampleModel defaultMode;
	Map<String, SampleModel> delgates = new HashMap<String, SampleModel>();

	public DelegatedModel(SampleModel defaultMode) {
		this.defaultMode = defaultMode;
	}

	@Override
	public List<TransitionProb> transitions(State s, Action a) {
		SampleModel delgate = delgates.get(a.actionName());
		if(delgate == null){
			if(!(defaultMode instanceof FullModel)){
				throw new RuntimeException("Cannot get transitions because the model for the input action is only a SampleModel");
			}
			return ((FullModel)defaultMode).transitions(s, a);
		}
		return ((FullModel)delgate).transitions(s, a);
	}

	@Override
	public EnvironmentOutcome sample(State s, Action a) {
		SampleModel delgate = delgates.get(a.actionName());
		if(delgate == null){
			return defaultMode.sample(s, a);
		}
		return delgate.sample(s, a);
	}

	@Override
	public boolean terminal(State s) {
		return defaultMode.terminal(s);
	}
}
