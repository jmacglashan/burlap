package burlap.behavior.singleagent.auxiliary.valuefunctionvis;

import java.awt.Graphics2D;
import java.util.Collection;

import burlap.behavior.policy.Policy;
import burlap.oomdp.core.states.State;
import burlap.oomdp.visualizer.RenderLayer;

public class PolicyRenderLayer implements RenderLayer{

	/**
	 * The states to visualize
	 */
	protected Collection <State>				statesToVisualize;
	
	/**
	 * Painter used to visualize the policy
	 */
	protected StatePolicyPainter				spp;
	
	/**
	 * The policy to use for visualizing the policy
	 */
	protected Policy							policy;
	
	
	public PolicyRenderLayer(Collection <State> states, StatePolicyPainter spp, Policy policy){
		this.statesToVisualize = states;
		this.spp = spp;
		this.policy = policy;
	}
	
	/**
	 * Returns the states that will be visualized
	 * @return the states that will be visualized
	 */
	public Collection<State> getStatesToVisualize() {
		return statesToVisualize;
	}

	
	/**
	 * Sets the states to visualize
	 * @param stateValuesToVisualize the state to visualize
	 */
	public void setStateValuesToVisualize(Collection<State> stateValuesToVisualize) {
		this.statesToVisualize = stateValuesToVisualize;
	}
	
	/**
	 * Returns the state-wise policy painter
	 * @return the state-wise policy painter
	 */
	public StatePolicyPainter getSpp() {
		return spp;
	}

	
	/**
	 * Sets the state-wise policy painter
	 * @param spp the state-wise policy painter
	 */
	public void setSpp(StatePolicyPainter spp) {
		this.spp = spp;
	}
	
	
	/**
	 * Returns the policy that will be rendered.
	 * @return the policy to be rendered
	 */
	public Policy getPolicy() {
		return policy;
	}

	
	/**
	 * Sets the policy to render
	 * @param policy the policy to render
	 */
	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	@Override
	public void render(Graphics2D g2, float width, float height) {
		
		if(this.spp == null || this.policy == null){
			return;
		}
		
		for(State s : this.statesToVisualize){
			this.spp.paintStatePolicy(g2, s, policy, width, height);
		}
		
	}
	
}
