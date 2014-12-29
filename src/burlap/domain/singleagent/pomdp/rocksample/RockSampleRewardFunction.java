package burlap.domain.singleagent.pomdp.rocksample;




import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;



public class RockSampleRewardFunction implements RewardFunction {
	public double reward(State s, GroundedAction a, State sprime) {
		ObjectInstance rover = s.getObject(Names.OBJ_AGENT);
		if(rover.getBooleanValue(Names.ATTR_COMPLETE)){return 0.0;}

		if(a.action.getName().equals(Names.ACTION_CHECK)){return -0.0;}
		//if(s.getObject(a.params[0]).getDiscValForAttribute(Names.ATTR_TIGERNESS) == 1){return -100;}
		if(a.action.getName().equals(Names.ACTION_SAMPLE)){
			// need rover position need to test rock list to check which rock
//			ObjectInstance rover = s.getObject(Names.OBJ_AGENT);
			int xPosRober = rover.getDiscValForAttribute(Names.ATTR_X);
			int yPosRober = rover.getDiscValForAttribute(Names.ATTR_Y);
			
			ObjectInstance grid = s.getObject(Names.OBJ_GRID);
			int numberOfRocks = grid.getDiscValForAttribute(Names.ATTR_NUMBER_OF_ROCKS);
			
			int rockNumber = -1;
			
			for(int i=0; i< numberOfRocks; i++){
				ObjectInstance rock = s.getObject(Names.OBJ_ROCK+i);
				int xRockPos = rock.getDiscValForAttribute(Names.ATTR_X);
				int yRockPos = rock.getDiscValForAttribute(Names.ATTR_Y);
				if(xRockPos == xPosRober && yRockPos == yPosRober){
					rockNumber=i;
				}
			}
			
			if(rockNumber<0){
				return -100;
			}
			else{
				ObjectInstance rock = s.getObject(Names.OBJ_ROCK+rockNumber);
				boolean collected = rock.getBooleanValue(Names.ATTR_COLLECTED);
				if(collected){return -100.0;}
				boolean value = rock.getBooleanValue(Names.ATTR_VALUE);
				return value ?  +10.0 : -10.0;
			}
		}
		
		if(a.action.getName().equals(Names.ACTION_MOVE)){
			String[] params = a.params;
			String direction = s.getObject(params[0]).getName();
//			ObjectInstance rover = s.getObject(Names.OBJ_AGENT);
			
			ObjectInstance grid = s.getObject(Names.OBJ_GRID);
			int sizeOfGrid = grid.getDiscValForAttribute(Names.ATTR_GRID_SIZE);
			
			if((rover.getDiscValForAttribute(Names.ATTR_X)==0 && direction.equals(Names.OBJ_WEST))||
					(rover.getDiscValForAttribute(Names.ATTR_Y)==sizeOfGrid-1 && direction.equals(Names.OBJ_SOUTH))||
					(rover.getDiscValForAttribute(Names.ATTR_Y)==0 && direction.equals(Names.OBJ_NORTH))){
				return -100.0;
			}
			
			if((rover.getDiscValForAttribute(Names.ATTR_X)==sizeOfGrid-1 && direction.equals(Names.OBJ_EAST))){
				rover.setValue(Names.ATTR_COMPLETE, true);
				return 10.0;
			}
			
		}
		return 0.0;

	}

}
