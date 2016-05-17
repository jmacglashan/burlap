package burlap.domain.singleagent.cartpole.model;

import burlap.domain.singleagent.cartpole.CartPoleDomain;
import burlap.domain.singleagent.cartpole.states.CartPoleState;
import burlap.mdp.core.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public class CPClassicModel implements FullStateModel {

	protected CartPoleDomain.CPPhysicsParams physParams;

	public CPClassicModel(CartPoleDomain.CPPhysicsParams physParams) {
		this.physParams = physParams;
	}

	@Override
	public List<StateTransitionProb> stateTransitions(State s, Action a) {
		return FullStateModel.Helper.deterministicTransition(this, s, a);
	}

	@Override
	public State sample(State s, Action a) {
		s = s.copy();

		if(a.actionName().equals(CartPoleDomain.ACTION_RIGHT)){
			return moveClassicModel(s, 1);
		}
		else if(a.actionName().equals(CartPoleDomain.ACTION_LEFT)){
			return moveClassicModel(s, -1);
		}
		throw new RuntimeException("Unknown action " + a.actionName());

	}

	/**
	 * Simulates the physics for one time step give the input state s, and the direction of force applied. The input state will be directly
	 * modified to be the next state. Physics simulated using one step of Euler's method on the non-linear differential equations provided by Barto
	 * Sutton, and Anderson [2]. <b>Note that this model is not physically correct [1] but is left in code for historical comparisons</b>. Optionally a
	 * correct model can be used instead.
	 * @param s the current state from which one time step of physics will be simulated.
	 * @param dir the direction of force applied; should be -1, or 1 and is multiplied to this objects movementForceMag parameter. 0 would cause no force.
	 * @return the input state, which has been modified to the next state after one time step of simulation.
	 */
	public State moveClassicModel(State s, double dir){

		CartPoleState cs = (CartPoleState)s;
		double x0 = cs.x;
		double xv0 = cs.v;
		double a0 = cs.angle;
		double av0 = cs.angleV;

		double f = dir * physParams.movementForceMag;

		double sMass = physParams.cartMass + physParams.poleMass;



		//compute second derivatives (x_2 and a_2) at current point
		double anumCosFactor = (-f
				- (physParams.poleMass*physParams.halfPoleLength*av0*av0*Math.sin(a0))
				+ (physParams.cartFriction * Math.signum(xv0))
		) / sMass;
		double anumPFricTerm = (physParams.poleFriction*av0) / (physParams.poleMass*physParams.halfPoleLength);

		double anum = (physParams.gravity * Math.sin(a0))
				+ (Math.cos(a0) * anumCosFactor)
				- anumPFricTerm;

		double adenom = physParams.halfPoleLength
				* ( (4./3.)
				- ((physParams.poleMass*Math.pow(Math.cos(a0), 2.)) / sMass)
		);

		double a_2 = anum / adenom;

		double xnum = f
				+ physParams.poleMass*physParams.halfPoleLength*(av0*av0*Math.sin(a0)
				- a_2 * Math.cos(a0))
				- (physParams.cartFriction*Math.signum(xv0));

		double x_2 = xnum / sMass;


		//perform Euler's method
		double xf = x0 + physParams.timeDelta * xv0;
		double xvf = xv0 + physParams.timeDelta*x_2;

		double af = a0 + physParams.timeDelta*av0;
		double avf = av0 + physParams.timeDelta*a_2;



		//clamp values
		if(Math.abs(xf) > physParams.halfTrackLength){
			xf = Math.signum(xf)*physParams.halfTrackLength;
			xvf = 0.;
		}

		if(Math.abs(xvf) > physParams.maxCartSpeed){
			xvf = Math.signum(xvf) * physParams.maxCartSpeed;
		}

		if(Math.abs(af) >= physParams.angleRange){
			af = Math.signum(af) * physParams.angleRange;
			avf = 0.;
		}

		if(Math.abs(avf) > physParams.maxAngleSpeed){
			avf = Math.signum(avf) * physParams.maxAngleSpeed;
		}


		//set new values
		if(physParams.isFiniteTrack){
			cs.x = xf;
		}
		cs.v = xvf;
		cs.angle = af;
		cs.angleV = avf;


		return s;

	}



}
