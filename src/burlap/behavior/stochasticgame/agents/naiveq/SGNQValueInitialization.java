package burlap.behavior.stochasticgame.agents.naiveq;

import burlap.oomdp.core.State;
import burlap.oomdp.stochasticgames.GroundedSingleAction;

public interface SGNQValueInitialization {

	public double qInit(State s, GroundedSingleAction gsa);

	
	public static class ConstantValueQInit implements SGNQValueInitialization{

		protected double q = 0.;
		
		public ConstantValueQInit(){
			
		}
		
		public ConstantValueQInit(double q){
			this.q = q;
		}
		
		@Override
		public double qInit(State s, GroundedSingleAction gsa) {
			return this.q;
		}
		
		
		
	}
}



