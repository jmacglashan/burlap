package burlap.behavior.singleagent.auxiliary.valuefunctionvis;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.visualizer.RenderLayer;


/**
 * Used to visualize the value function for a collection of states. This class makes use of the {@link StateValuePainter} {@link StatePolicyPainter}
 * and {@link StaticDomainPainter} to paint the value function. Each state is iterated over and the the respective classes are used to pain its value function.
 * The visualizer requires a QComputablePlanner to retrieve the value function.
 * @author James MacGlashan
 *
 */
public class ValueFunctionRenderLayer implements RenderLayer {

	/**
	 * The states to visualize
	 */
	protected Collection <State>				statesToVisualize;

	
	/**
	 * Painter used to visualize the value function
	 */
	protected StateValuePainter					svp;
	
	
	/**
	 * The QComputable planner to use for finding the value function
	 */
	protected QFunction planner;
	
	
	
	/**
	 * Initializes the visualizer.
	 * @param states the states whose value should be rendered.
	 * @param svp the value function state visualizer to use.
	 * @param planner the planner that can return the value function.
	 */
	public ValueFunctionRenderLayer(Collection <State> states, StateValuePainter svp, QFunction planner){
		this.statesToVisualize = states;
		this.svp = svp;
		this.planner = planner;
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
	public void setStatesToVisualize(Collection<State> stateValuesToVisualize) {
		this.statesToVisualize = stateValuesToVisualize;
	}

	/**
	 * Returns the State-wise value function painter
	 * @return the State-wise value function painter
	 */
	public StateValuePainter getSvp() {
		return svp;
	}

	
	/**
	 * Sets the state-wise value function painter
	 * @param svp state-wise value function painter
	 */
	public void setSvp(StateValuePainter svp) {
		this.svp = svp;
	}
	
	
	@Override
	public void render(Graphics2D g2, float width, float height) {
		
		List <Double> values = new ArrayList<Double>(this.statesToVisualize.size());
		double minV = Double.POSITIVE_INFINITY;
		double maxV = Double.NEGATIVE_INFINITY;
		for(State s : this.statesToVisualize){
			double v = this.getVValue(s);
			values.add(v);
			if(v < minV){
				minV = v;
			}
			if(v > maxV){
				maxV = v;
			}
		}
		
		this.svp.rescale(minV, maxV);
		
		Iterator<Double> vIter = values.iterator();
		for(State s : this.statesToVisualize){
			this.svp.paintStateValue(g2, s, vIter.next(), width, height);
		}
		

	}
	
	
	/**
	 * Returns the value for a state
	 * @param s the state for which to get the value
	 * @return the value for a state
	 */
	protected double getVValue(State s){
		
		/*
		 * check the terminal function for the planner if we can get it. If
		 * the input state is a terminal state, then reutrn 0 since the value of all terminal states is 0.
		 */
		TerminalFunction tf = null;
		if(this.planner instanceof OOMDPPlanner){
			tf = ((OOMDPPlanner)this.planner).getTF();
		}
		
		if(tf != null){
			if(tf.isTerminal(s)){
				return 0.;
			}
		}
		
		List <QValue> qs = this.planner.getQs(s);
		double max = Double.NEGATIVE_INFINITY;
		for(QValue q : qs){
			if(q.q > max){
				max = q.q;
			}
		}
		return max;
	}
	

}
