package burlap.domain.singleagent.cartpole.model;

import burlap.domain.singleagent.cartpole.CartPoleDomain;
import burlap.domain.singleagent.cartpole.states.CartPoleFullState;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public class CPCorrectModel implements FullStateModel {

	protected CartPoleDomain.CPPhysicsParams physParams;

	public CPCorrectModel(CartPoleDomain.CPPhysicsParams physParams) {
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
			return moveCorrectModel(s, 1);
		}
		else if(a.actionName().equals(CartPoleDomain.ACTION_LEFT)){
			return moveCorrectModel(s, -1);
		}
		throw new RuntimeException("Unknown action " + a.actionName());

	}


	/**
	 * Simulates the physics for one time step give the input state s, and the direction of force applied. The input state will be directly
	 * modified to be the next state. Physics simulated using one step of Euler's method on the corrected non-linear differential equations [1].
	 * @param s the current state from which one time step of physics will be simulated.
	 * @param dir the direction of force applied; should be -1, or 1 and is multiplied to this objects movementForceMag parameter. 0 would cause no force.
	 * @return the input state, which has been modified to the next state after one time step of simulation.
	 */
	public State moveCorrectModel(State s, double dir){

		CartPoleFullState cs = (CartPoleFullState)s;
		double x0 = cs.x;
		double xv0 = cs.v;
		double a0 = cs.angle;
		double av0 = cs.angleV;
		double nsgn0 = cs.normSign;

		double f = dir * physParams.movementForceMag;

		double a_2 = getAngle2ndDeriv(xv0, a0, av0, nsgn0, f);
		double n = getNormForce(a0, av0, a_2);
		double nsgnf = Math.signum(n);
		if(nsgnf != nsgn0){
			a_2 = getAngle2ndDeriv(xv0, a0, av0, nsgnf, f);
		}
		double x_2 = getX2ndDeriv(xv0, a0, av0, n, f, a_2);

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
		cs.normSign = n;



		return s;
	}

	/**
	 * Computes the 2nd order derivative of the angle for a given normal force sign using the corrected model.
	 * @param xv0 the cart velocity
	 * @param a0 the pole angle
	 * @param av0 the pole angle velocity
	 * @param nsign the normal force sign
	 * @param f the force applied to the cart
	 * @return the 2nd order derivative of the angle
	 */
	protected double getAngle2ndDeriv(double xv0, double a0, double av0, double nsign, double f){

		double sMass = physParams.cartMass + physParams.poleMass;

		double sint = Math.sin(a0);
		double cost = Math.cos(a0);

		double anumCosFactor = (-f
				- (physParams.poleMass*physParams.halfPoleLength*av0*av0
				* (
				sint + physParams.cartFriction*Math.signum(nsign*xv0)*cost
		)
		)

		) / sMass;

		double anumPFricTerm = physParams.cartFriction*physParams.gravity*Math.signum(nsign*xv0);

		double anum = (physParams.gravity * Math.sin(a0))
				+ (Math.cos(a0) * anumCosFactor)
				+ anumPFricTerm;

		double adenom = physParams.halfPoleLength
				* ( (4./3.)
				- (
				(physParams.poleMass*cost / sMass)
						* (cost - physParams.cartMass * Math.signum(nsign*xv0))
		)
		);


		return anum / adenom;

	}

	/**
	 * Computes the normal force for the corrected model
	 * @param a0 the pole angle
	 * @param av0 the pole angle velocity
	 * @param a_2 the 2nd order derivative of the pole angle
	 * @return the normal force
	 */
	protected double getNormForce(double a0, double av0, double a_2){
		double norm = ((physParams.cartMass + physParams.poleMass) * physParams.gravity)
				- (physParams.poleMass * physParams.halfPoleLength
				* (a_2 * Math.sin(a0) + (av0*av0*Math.cos(a0)))
		);
		return norm;
	}

	/**
	 * Returns the second order x position derivative for the corrected model.
	 * @param xv0 the cart velocity
	 * @param a0 the pole angle
	 * @param av0 the pole angle velocity
	 * @param n the normal force
	 * @param f the force applied to the cart
	 * @param a2 the second order angle derivative
	 * @return the second order x position derivative
	 */
	protected double getX2ndDeriv(double xv0, double a0, double av0, double n, double f, double a2){

		double sMass = physParams.cartMass + physParams.poleMass;

		double sint = Math.sin(a0);
		double cost = Math.cos(a0);

		double xnum = f
				+ (physParams.poleMass * physParams.halfPoleLength
				* ((av0*av0*sint) - (a2*cost))
		)
				- (physParams.cartFriction * n * Math.signum(n*xv0));

		double x_2 = xnum/sMass;

		return x_2;

	}



}
