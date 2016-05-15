package burlap.domain.singleagent.cartpole.model;

import burlap.debugtools.RandomFactory;
import burlap.domain.singleagent.cartpole.CartPoleDomain;
import burlap.domain.singleagent.cartpole.InvertedPendulum;
import burlap.domain.singleagent.cartpole.states.InvertedPendulumState;
import burlap.mdp.core.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public class IPModel implements FullStateModel {

	InvertedPendulum.IPPhysicsParams physParams;

	public IPModel(InvertedPendulum.IPPhysicsParams physParams) {
		this.physParams = physParams;
	}

	@Override
	public State sampleStateTransition(State s, Action a) {

		s = s.copy();

		double baseForce = 0.;
		if(a.actionName().equals(CartPoleDomain.ACTION_LEFT)){
			baseForce = -physParams.actionForce;
		}
		else if(a.actionName().equals(CartPoleDomain.ACTION_RIGHT)){
			baseForce = physParams.actionForce;
		}


		double roll = RandomFactory.getMapped(0).nextDouble() * (2 * physParams.actionNoise) - physParams.actionNoise;
		double force = baseForce + roll;

		return updateState(s, force);
	}

	@Override
	public List<StateTransitionProb> stateTransitions(State s, Action a) {
		if(this.physParams.actionNoise == 0.){
			return FullStateModel.Helper.deterministicTransition(this, s, a);
		}
		throw new RuntimeException("Transition Probabilities for the Inverted Pendulum with continuous action noise cannot be enumerated.");
	}

	/**
	 * Updates the given state object given the control force.
	 * @param s the input state
	 * @param controlForce the control force acted upon the cart.
	 */
	protected State updateState(State s, double controlForce){

		InvertedPendulumState is = (InvertedPendulumState)s;
		double a0 = is.angle;
		double av0 = is.angleV;


		double alpha = 1./ (physParams.cartMass + physParams.poleMass);

		double sinA = Math.sin(a0);
		double cosA = Math.cos(a0);

		double num = (physParams.gravity*sinA) -
				(alpha * physParams.poleMass*physParams.poleLength*av0*av0*Math.sin(2.*a0)*0.5) -
				(alpha * cosA * controlForce);

		double denom = ((4./3.)*physParams.poleLength) - alpha*physParams.poleMass*physParams.poleLength*cosA*cosA;

		double accel = num / denom;

		//now perform Euler's
		double af = a0 + physParams.timeDelta*av0;
		double avf = av0 + physParams.timeDelta*accel;

		//clamp it
		if(Math.abs(af) >= physParams.angleRange){
			af = Math.signum(af) * physParams.angleRange;
			avf = 0.;
		}

		if(Math.abs(avf) > physParams.maxAngleSpeed){
			avf = Math.signum(avf) * physParams.maxAngleSpeed;
		}

		//set it
		is.angle = af;
		is.angleV = avf;

		return s;
	}

}
