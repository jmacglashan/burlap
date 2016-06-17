package burlap.domain.singleagent.lunarlander;

import burlap.domain.singleagent.lunarlander.state.LLAgent;
import burlap.domain.singleagent.lunarlander.state.LLBlock;
import burlap.domain.singleagent.lunarlander.state.LLState;
import burlap.mdp.core.action.Action;
import burlap.mdp.core.StateTransitionProb;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.model.statemodel.FullStateModel;

import java.util.List;

/**
 * @author James MacGlashan.
 */
public class LunarLanderModel implements FullStateModel {

	LunarLanderDomain.LLPhysicsParams physParams;

	public LunarLanderModel(LunarLanderDomain.LLPhysicsParams physParams) {
		this.physParams = physParams;
	}

	@Override
	public List<StateTransitionProb> stateTransitions(State s, Action a) {
		return FullStateModel.Helper.deterministicTransition(this, s, a);
	}

	@Override
	public State sample(State s, Action a) {

		LLState ls = (LLState)s.copy();

		double force  = 0.;
		if(a.actionName().equals(LunarLanderDomain.ACTION_TURN_LEFT)){
			incAngle(ls, -1);
		}
		else if(a.actionName().equals(LunarLanderDomain.ACTION_TURN_RIGHT)){
			incAngle(ls, 1);
		}
		else if(a instanceof LunarLanderDomain.ThrustType.ThrustAction){
			force = ((LunarLanderDomain.ThrustType.ThrustAction)a).thrust;
		}

		updateMotion(ls, force);

		return ls;
	}





	/**
	 * Turns the lander in the direction indicated by the domains defined change in angle for turn actions.
	 * @param s the state in which the lander's angle should be changed
	 * @param dir the direction to turn; +1 is clockwise, -1 is counterclockwise
	 */
	protected void incAngle(LLState s, double dir){

		LLAgent agent = s.touchAgent();

		double curA = agent.angle;

		double newa = curA + (dir * physParams.anginc);
		if(newa > physParams.angmax){
			newa = physParams.angmax;
		}
		else if(newa < -physParams.angmax){
			newa = -physParams.angmax;
		}

		agent.angle = newa;

	}


	/**
	 * Updates the position of the agent/lander given the provided thrust force that has been exerted
	 * @param s the state in which the agent/lander should be modified
	 * @param thrust the amount of thrust force exerted by the lander.
	 */
	protected void updateMotion(LLState s, double thrust){

		double ti = 1.;
		double tt = ti*ti;

		LLAgent agent = s.touchAgent();

		double ang = agent.angle;
		double x = agent.x;
		double y = agent.y;
		double vx = agent.vx;
		double vy = agent.vy;

		double worldAngle = (Math.PI/2.) - ang;

		double tx = Math.cos(worldAngle)*thrust;
		double ty = Math.sin(worldAngle)*thrust;

		double ax = tx;
		double ay = ty + physParams.gravity;

		double nx = x + vx*ti + (0.5*ax*tt);
		double ny = y + vy*ti + (0.5*ay*tt);

		double nvx = vx + ax*ti;
		double nvy = vy + ay*ti;

		double nang = ang;

		//check for boundaries
		if(ny > physParams.ymax){
			ny = physParams.ymax;
			nvy = 0.;
		}
		else if(ny <= physParams.ymin){
			ny = physParams.ymin;
			nvy = 0.;
			nang = 0.;
			nvx = 0.;
		}

		if(nx > physParams.xmax){
			nx = physParams.xmax;
			nvx = 0.;
		}
		else if(nx < physParams.xmin){
			nx = physParams.xmin;
			nvx = 0.;
		}

		if(nvx > physParams.vmax){
			nvx = physParams.vmax;
		}
		else if(nvx < -physParams.vmax){
			nvx = -physParams.vmax;
		}

		if(nvy > physParams.vmax){
			nvy = physParams.vmax;
		}
		else if(nvy < -physParams.vmax){
			nvy = -physParams.vmax;
		}



		//check for collisions
		List <LLBlock.LLObstacle> obstacles = s.obstacles;
		for(LLBlock.LLObstacle o : obstacles){
			double l = o.left;
			double r = o.right;
			double b = o.bottom;
			double t = o.top;

			//are we intersecting?
			if(nx > l && nx < r && ny >= b && ny < t){
				//intersection!

				//from which direction did we hit it (check previous position)?
				if(x <= l){
					nx = l;
					nvx = 0.;
				}
				else if(x >= r){
					nx = r;
					nvx = 0.;
				}

				if(y <= b){
					ny = b;
					nvy = 0.;
				}
				else if(y >= t){
					ny = t;
					nvy = 0.;
					nang = 0.;
					nvx = 0.;
				}


				//can only hit one obstacle so break out of search
				break;

			}


		}


		//check the pad collision
		LLBlock.LLPad pad = s.pad;
		if(pad != null) {
			double l = pad.left;
			double r = pad.right;
			double b = pad.bottom;
			double t = pad.top;

			//did we collide?
			if(nx > l && nx < r && ny >= b && ny < t) {
				//intersection!

				//from which direction did we hit it (check previous position)?
				if(x <= l) {
					nx = l;
					nvx = 0.;
				} else if(x >= r) {
					nx = r;
					nvx = 0.;
				}

				if(y <= b) {
					ny = b;
					nvy = 0.;
				} else if(y >= t) {
					ny = t;
					nvy = 0.;
					nang = 0.;
					nvx = 0.;
				}


			}
		}


		//now set the new values
		agent.x = nx;
		agent.y = ny;
		agent.vx = nvx;
		agent.vy = nvy;
		agent.angle = nang;

	}


}
