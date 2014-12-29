package burlap.domain.singleagent.pomdp.rocksample;




import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;







import burlap.behavior.singleagent.auxiliary.StateEnumerator;
import burlap.behavior.singleagent.pomdp.POMDPEpisodeAnalysis;
import burlap.behavior.singleagent.pomdp.pomcp.LBLWPOMCP;
import burlap.behavior.singleagent.pomdp.pomcp.MonteCarloNode;
import burlap.behavior.singleagent.pomdp.pomcp.MonteCarloNodeAgent;
import burlap.behavior.singleagent.pomdp.pomcp.POMCP;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.pomdp.BeliefState;
import burlap.oomdp.singleagent.pomdp.BeliefStatistic;
import burlap.oomdp.singleagent.pomdp.ObservationFunction;
import burlap.oomdp.singleagent.pomdp.PODomain;
import burlap.oomdp.singleagent.pomdp.POEnvironment;

/**
 * Rock sample domain from the HSVI paper //TODO put link here. 
 * Two domain sizes 11 x 11 and 8 x 8, with rock positions set.
 * The value of rocks is being chosen randomly.
 * @author ngopalan at cs.brown.edu
 *
 */

public class RockSample implements DomainGenerator{

	public static int sizeOfGrid;
	public static int numberOfRocks;
	public static double halfPowerDistance = 20;
	public static Random randomGen = new Random();// = RandomFactory.getMapped(1);
	//	public static Random randomAll = new Random(-20);
	//	private Random rand = RandomFactory.getMapped(0);

	public RockSample(int size){
		if (size == 11){
			sizeOfGrid=11;
			numberOfRocks = 11;
		}
		else{
			sizeOfGrid=7;
			numberOfRocks = 8;
		}
	};

	public RockSample(){
		sizeOfGrid=11;
		numberOfRocks = 11;
	};


	// 7,8 map 
	public static List<Tuple<Integer,Integer>> rockPositions_7_8 = new ArrayList<Tuple<Integer,Integer>>(){{
		add(new Tuple<Integer,Integer>(2, 0));
		add(new Tuple<Integer,Integer>(0, 1));
		add(new Tuple<Integer,Integer>(3, 1));
		add(new Tuple<Integer,Integer>(6, 3));
		add(new Tuple<Integer,Integer>(2, 4));
		add(new Tuple<Integer,Integer>(3, 4));
		add(new Tuple<Integer,Integer>(5, 5));
		add(new Tuple<Integer,Integer>(1, 6));
	}};

	// 11, 11 map 
	public static List<Tuple<Integer,Integer>> rockPositions_11_11 = new ArrayList<Tuple<Integer,Integer>>(){{
		add(new Tuple<Integer,Integer>(0, 3));
		add(new Tuple<Integer,Integer>(0, 7));
		add(new Tuple<Integer,Integer>(1, 8));
		add(new Tuple<Integer,Integer>(2, 4));
		add(new Tuple<Integer,Integer>(3, 3));
		add(new Tuple<Integer,Integer>(3, 8));
		add(new Tuple<Integer,Integer>(4, 3));
		add(new Tuple<Integer,Integer>(5, 8));
		add(new Tuple<Integer,Integer>(6, 1));
		add(new Tuple<Integer,Integer>(9, 3));
		add(new Tuple<Integer,Integer>(9, 9));
	}};



	@Override
	public Domain generateDomain() {
		PODomain domain = new PODomain() {
			//			@Override public State sampleInitialState() { return RockSample.getNewState(this); }
			//			@Override public State makeObservationFor(GroundedAction a, POMDPState s) { return RockSample.makeObservationFor(this, a, s); }
			//			@Override public boolean isSuccess(Observation o) { return RockSample.isSuccess(o); }
			//			@Override public boolean isTerminal(POMDPState s) { return RockSample.isTerminal(this, s); }
			//			@Override
			//			public List<POMDPState> getAllInitialStates(){
			//				NameDependentStateHashFactory hashFactory = new NameDependentStateHashFactory();
			//				Set<StateHashTuple> tempSet = new HashSet<StateHashTuple>();
			//				for(int i = 0; i<Math.pow(numberOfRocks, 2)*10; i++){
			//					State s = RockSample.getNewState(this);
			////					System.out.println(s.getStateDescription());
			//					StateHashTuple st =hashFactory.hashState(s);
			////					System.out.println(st.hashCode());
			//					tempSet.add(st);
			//				}
			//				Set<POMDPState> noDups = new HashSet<POMDPState>();
			//				for (StateHashTuple shi : tempSet){
			//					noDups.add(new POMDPState(shi.s));
			//				}
			//				
			//				return new ArrayList<POMDPState>(noDups);
			//			}


		};

		// Attributes

		Attribute xCord = new Attribute(domain, Names.ATTR_X, Attribute.AttributeType.INT);
		xCord.setLims(0, sizeOfGrid-1);

		Attribute yCord = new Attribute(domain, Names.ATTR_Y, Attribute.AttributeType.DISC);
		yCord.setDiscValuesForRange(0, sizeOfGrid-1, 1);

		Attribute rockNumber = new Attribute(domain, Names.ATTR_ROCK_NUMBER, Attribute.AttributeType.DISC);
		rockNumber.setDiscValuesForRange(0, numberOfRocks-1, 1);

		Attribute totalNumberOfRocks = new Attribute(domain, Names.ATTR_NUMBER_OF_ROCKS, Attribute.AttributeType.DISC);
		totalNumberOfRocks.setDiscValuesForRange(0, numberOfRocks, 1);

		Attribute gridSize = new Attribute(domain, Names.ATTR_GRID_SIZE, Attribute.AttributeType.DISC);
		gridSize.setDiscValuesForRange(0, sizeOfGrid, 1);

		Attribute wallDirection = new Attribute(domain, Names.ATTR_WALL_DIRECTION, Attribute.AttributeType.RELATIONAL);


		Attribute value = new Attribute(domain,Names.ATTR_VALUE, Attribute.AttributeType.BOOLEAN);

		Attribute collected = new Attribute(domain,Names.ATTR_COLLECTED, Attribute.AttributeType.BOOLEAN);

		Attribute complete = new Attribute(domain,Names.ATTR_COMPLETE, Attribute.AttributeType.BOOLEAN);

		//String attribute with cardinal directions
		Attribute direction = new Attribute(domain, Names.ATTR_DIRECTION, Attribute.AttributeType.STRING);

		//Attribute for observations
		Attribute obAtt = new Attribute(domain, Names.ATTR_OBS, Attribute.AttributeType.DISC);
		String[] obsFull = new String[4];
		obsFull[0] = Names.OBS_BAD;
		obsFull[1] = Names.OBS_GOOD;
		obsFull[2] = Names.OBS_NULL;
		obsFull[3] = Names.OBS_COMPLETE;

		obAtt.setDiscValues(obsFull);



		//Object classes

		ObjectClass rockClass = new ObjectClass(domain, Names.CLASS_ROCK);
		rockClass.addAttribute(xCord);
		rockClass.addAttribute(yCord);
		rockClass.addAttribute(rockNumber);
		rockClass.addAttribute(value);
		rockClass.addAttribute(collected);

		ObjectClass roverClass = new ObjectClass(domain, Names.CLASS_AGENT);
		roverClass.addAttribute(xCord);
		roverClass.addAttribute(yCord);
		roverClass.addAttribute(complete);

		ObjectClass gridClass = new ObjectClass(domain, Names.CLASS_GRID);
		gridClass.addAttribute(gridSize);
		gridClass.addAttribute(totalNumberOfRocks);

		// two ideas: make each direction an object with a string attribute or just make wall objects have string attributes indicating direction
		ObjectClass wallClass = new ObjectClass(domain, Names.CLASS_WALL);
		wallClass.addAttribute(wallDirection);

		ObjectClass directionClass = new ObjectClass(domain, Names.CLASS_DIRECTION);
		directionClass.addAttribute(direction);

		ObjectClass obClass = new ObjectClass(domain, Names.CLASS_OBSERVATION);
		obClass.addAttribute(obAtt);


		// Actions
		Action move = new MoveAction(domain, Names.ACTION_MOVE);
		Action sample = new SampleAction(domain, Names.ACTION_SAMPLE);
		Action check = new CheckAction(domain, Names.ACTION_CHECK);


		new RockSample.RockSampleObservations(domain);
		TerminalFunction tf = new RockSampleTerminalFunction();



		/*
		StateEnumerator senum = new StateEnumerator(domain, new NameDependentStateHashFactory());
		List<State> startStates = RockSample.getAllStartStates(domain);
		int count = 0;
		for (State sTemp: startStates){
			count++;
			System.out.println(" iter: " + count + " out of "+ startStates.size());
			senum.findReachableStatesAndEnumerate(sTemp, tf);
			senum.findReachableStatesAndEnumerate(sTemp);
			System.out.println("num states enumerated: "+ senum.numStatesEnumerated());
		}

		System.out.println("num states enumerated: "+ senum.numStatesEnumerated());
		domain.setStateEnumerator(senum);
		 */

		return domain;// need to put in domain here

	}



	
	private static List<State> getAllStartStates(PODomain domain) {
		// TODO Auto-generated method stub
		List<State> initialStates = new ArrayList<State>();
		int startStates = (int)Math.pow(2, numberOfRocks);
		for(int i=0;i<startStates;i++){
			initialStates.add(getState(domain,i));
		}
		return initialStates;
	}



	public class RockSampleObservations extends ObservationFunction{

		//		double noise = 0.15;


		public RockSampleObservations(PODomain domain) {
			super(domain);
			// TODO change things for multiple observations if needed
		}

		@Override
		public List<State> getAllPossibleObservations() {
			// only four observations in this domain, complete is the terminal observation

			List<State> observationList = new ArrayList<State>();
			observationList.add(this.createObsState(Names.OBS_BAD));
			observationList.add(this.createObsState(Names.OBS_COMPLETE));
			observationList.add(this.createObsState(Names.OBS_GOOD));
			observationList.add(this.createObsState(Names.OBS_NULL));
			return observationList;
		}

		@Override
		public double getObservationProbability(State observation, State sPrime,
				GroundedAction a) {
			String observationName = observation.getFirstObjectOfClass(Names.CLASS_OBSERVATION).getStringValForAttribute(Names.ATTR_OBS);
			
			String actionName = a.actionName();
			ObjectInstance rover = sPrime.getObject(Names.OBJ_AGENT);
			
//			System.out.println(observationName);


			// check if the observation queried for complete
			if(observationName.equals(Names.OBS_COMPLETE)){
				if(rover.getBooleanValue(Names.ATTR_COMPLETE)){
					return 1.0;
				}
				else{
					return 0.0;
				}
			}

			if(observationName.equals(Names.OBS_NULL)){
				if(actionName.equals(Names.ACTION_CHECK)){
					return 0.0;
				}
				else{
					return 1.0;
				}
			}

			if(observationName.equals(Names.OBS_BAD) || observationName.equals(Names.OBS_GOOD)){
				if(actionName.equals(Names.ACTION_CHECK)){
					int xPosRover = rover.getDiscValForAttribute(Names.ATTR_X);
					int yPosRover = rover.getDiscValForAttribute(Names.ATTR_Y);
					ObjectInstance rock = sPrime.getObject(a.params[0]);
					double xPosRock = (double)rock.getDiscValForAttribute(Names.ATTR_X);
					double yPosRock = (double)rock.getDiscValForAttribute(Names.ATTR_Y);
					double distance = Math.sqrt((xPosRock-xPosRover)*(xPosRock-xPosRover) +(yPosRock-yPosRover)*(yPosRock-yPosRover));
					double efficiency = (1 + Math.pow(2, -distance / halfPowerDistance)) * 0.5;
					boolean rockValue = rock.getBooleanValue(Names.ATTR_VALUE);
					if((rockValue && observationName.equals(Names.OBS_GOOD)) ||  (!rockValue && observationName.equals(Names.OBS_BAD))){
						return efficiency;
					}
					else{
						return 1-efficiency;
					}
				}
				else{
					return 0.0;
				}
			}



			return 0.0;
		}

		@Override
		public boolean isTerminalObservation(State observation) {
			String observationName = observation.getFirstObjectOfClass(Names.CLASS_OBSERVATION).getStringValForAttribute(Names.ATTR_OBS);
			if(observationName.equals(Names.OBS_COMPLETE)){return true;}
			return false;
		}


		protected State createObsState(String obsName){
			State obsStart = new State();
			ObjectInstance obL = new ObjectInstance(this.domain.getObjectClass(Names.CLASS_OBSERVATION), Names.OBJ_OBS);
			obL.setValue(Names.ATTR_OBS, obsName);
			obsStart.addObject(obL);
			return obsStart;
		}

	}


	
	
	



	//Actions
	public class MoveAction extends Action{
		public MoveAction(Domain domain, String name){
			super(name, domain, new String[]{Names.CLASS_DIRECTION});
		}

		@Override
		protected State performActionHelper(State s, String[] params) {
			
//			System.out.println("In move action: " + params[0]);
//			System.out.println("State in: " + s.getCompleteStateDescription());

			State sPrime = new State(s);
			String direction = sPrime.getObject(params[0]).getName();
			//			System.out.println(direction);
			ObjectInstance rover = sPrime.getObject(Names.OBJ_AGENT);

			if((rover.getDiscValForAttribute(Names.ATTR_X)==0 && direction.equals(Names.OBJ_WEST))||
					(rover.getDiscValForAttribute(Names.ATTR_Y)==sizeOfGrid-1 && direction.equals(Names.OBJ_SOUTH))||
					(rover.getDiscValForAttribute(Names.ATTR_Y)==0 && direction.equals(Names.OBJ_NORTH))){
				return sPrime;
			}

			if((rover.getDiscValForAttribute(Names.ATTR_X)==sizeOfGrid-1 && direction.equals(Names.OBJ_EAST))){
				rover.setValue(Names.ATTR_COMPLETE, true);
				return sPrime;
			}

			if(direction.equals(Names.OBJ_NORTH)){
				int yPos = rover.getDiscValForAttribute(Names.ATTR_Y);
				rover.setValue(Names.ATTR_Y, yPos-1);
			}
			else if(direction.equals(Names.OBJ_SOUTH)){
				int yPos = rover.getDiscValForAttribute(Names.ATTR_Y);
				rover.setValue(Names.ATTR_Y, yPos+1);
			}
			else if(direction.equals(Names.OBJ_EAST)){
				int xPos = rover.getDiscValForAttribute(Names.ATTR_X);
				rover.setValue(Names.ATTR_X, xPos+1);
			}
			else if(direction.equals(Names.OBJ_WEST)){
				int xPos = rover.getDiscValForAttribute(Names.ATTR_X);
				rover.setValue(Names.ATTR_X, xPos-1);
			}
			else{
				System.out.println("RockSample: MoveAction: I shouldn't be in the default at all");
			}
			//
//			System.out.println("State out: " + sPrime.getCompleteStateDescription());
			return sPrime;
		}

		@Override
		public boolean applicableInState(State s, String[] params){
			String direction = s.getObject(params[0]).getName();
			//			System.out.println(direction);
			ObjectInstance rover = s.getObject(Names.OBJ_AGENT);

			if((rover.getDiscValForAttribute(Names.ATTR_X)==0 && direction.equals(Names.OBJ_WEST))||
					(rover.getDiscValForAttribute(Names.ATTR_Y)==sizeOfGrid-1 && direction.equals(Names.OBJ_SOUTH))||
					(rover.getDiscValForAttribute(Names.ATTR_Y)==0 && direction.equals(Names.OBJ_NORTH))){
				return false;
			}
			return true;
		}

	}


	public class SampleAction extends Action{
		public SampleAction(Domain domain, String name){
			super(name, domain, new String[]{});
		}

		@Override
		protected State performActionHelper(State s, String[] params) {
//			System.out.println("In sample action");
//			System.out.println("State in: " + s.getCompleteStateDescription());

			State sPrime = new State(s);
			ObjectInstance rover = sPrime.getObject(Names.OBJ_AGENT);
			int xPos = rover.getDiscValForAttribute(Names.ATTR_X);
			int yPos = rover.getDiscValForAttribute(Names.ATTR_Y);
			for(int i=0; i < numberOfRocks; i++){
				ObjectInstance rock = sPrime.getObject(Names.OBJ_ROCK+i);
				if((rock.getDiscValForAttribute(Names.ATTR_X) == xPos) && (rock.getDiscValForAttribute(Names.ATTR_Y) == yPos)){
					rock.setValue(Names.ATTR_VALUE, false);
					rock.setValue(Names.ATTR_COLLECTED, true);	
				}
			}
//			System.out.println("State out: " + sPrime.getCompleteStateDescription());
			return sPrime;
		}

		@Override
		public boolean applicableInState(State s, String [] params){
			ObjectInstance rover = s.getObject(Names.OBJ_AGENT);
			int xPos = rover.getDiscValForAttribute(Names.ATTR_X);
			int yPos = rover.getDiscValForAttribute(Names.ATTR_Y);
			for(int i=0; i < numberOfRocks; i++){
				ObjectInstance rock = s.getObject(Names.OBJ_ROCK+i);
				if((rock.getDiscValForAttribute(Names.ATTR_X) == xPos) && (rock.getDiscValForAttribute(Names.ATTR_Y) == yPos) && !rock.getBooleanValue(Names.ATTR_COLLECTED)){
					return true;	
				}
			}
			return false; 
		}


	}

	public class CheckAction extends Action{
		public CheckAction(Domain domain, String name){
			super(name, domain, new String[]{Names.CLASS_ROCK});
		}

		@Override
		protected State performActionHelper(State s, String[] params) {
//			System.out.println("In check action: " + params[0]);
//			System.out.println("State in: " + s.getCompleteStateDescription());
			State sPrime = new State(s); 
//			System.out.println("State out (should be same as in): " + sPrime.getCompleteStateDescription());
			return sPrime;
		}
	}


	public static BeliefStatistic getInitialBeliefStatistic(PODomain domain, int numParticles){
		MonteCarloNode bs = new MonteCarloNode(domain);
		//		bs.initializeBeliefsUniformly();
		
		for(int i=0;i<numParticles;i++){
			State s = RockSample.getNewState(domain);
			bs.addParticle(s);
		}
		/*
		
		int count = 0;
		double[] beliefVector = bs.getBeliefVector();
		StateEnumerator senum = domain.getStateEnumerator();
		for(int i=0;i<senum.numStatesEnumerated();i++){
			State s = senum.getStateForEnumertionId(i);
			ObjectInstance rover = s.getObject(Names.OBJ_AGENT);
			int xPosRover = rover.getDiscValForAttribute(Names.ATTR_X);
			int yPosRover = rover.getDiscValForAttribute(Names.ATTR_Y);
			boolean complete = rover.getBooleanValue(Names.ATTR_COMPLETE);
			int xStartPos = 0;
			int yStartPos = sizeOfGrid/2;



			//TODO: check condition with the way initial states are defined
			if(xPosRover == xStartPos && yPosRover == yStartPos && !complete){
				count++;
				beliefVector[i] = 1;
			}
			else{
				beliefVector[i]=0;
			}
		}
		if(count>0){
			for(int i=0;i<senum.numStatesEnumerated();i++){
				beliefVector[i]=beliefVector[i]/count;
			}
		}
		else{
			System.out.println("RockSample: Domain not initialized correctly");
			beliefVector = null;
		}

		bs.setBeliefCollection(beliefVector);
		*/
		return bs;
	}





	protected static State getNewState(PODomain pomdpDomain) {

		State s = new State();


		ObjectClass rockClass = pomdpDomain.getObjectClass(Names.CLASS_ROCK);
		ObjectClass agentClass = pomdpDomain.getObjectClass(Names.CLASS_AGENT);
		ObjectClass wallClass = pomdpDomain.getObjectClass(Names.CLASS_WALL);
		ObjectClass directionClass = pomdpDomain.getObjectClass(Names.CLASS_DIRECTION);
		ObjectClass gridClass = pomdpDomain.getObjectClass(Names.CLASS_GRID);


		ObjectInstance roverAgent = new ObjectInstance(agentClass, Names.OBJ_AGENT);
		roverAgent.setValue(Names.ATTR_X, 0);
		roverAgent.setValue(Names.ATTR_Y, sizeOfGrid/2);
		roverAgent.setValue(Names.ATTR_COMPLETE, false);
		s.addObject(roverAgent);

		ObjectInstance grid = new ObjectInstance(gridClass, Names.OBJ_GRID);
		grid.setValue(Names.ATTR_GRID_SIZE, sizeOfGrid);
		grid.setValue(Names.ATTR_NUMBER_OF_ROCKS, numberOfRocks);
		s.addObject(grid);

		List<Tuple<Integer,Integer>> rockPositions = new ArrayList<Tuple<Integer,Integer>>();
		if (sizeOfGrid == 11){
			rockPositions = rockPositions_11_11;
		}
		else
		{
			rockPositions = rockPositions_7_8;
		}

		for(int i = 0; i<numberOfRocks;i++){
			ObjectInstance rockObject = new ObjectInstance(rockClass, Names.OBJ_ROCK+i);
			//			boolean value = new java.util.Random().nextBoolean();
			boolean value = randomGen.nextBoolean();
			rockObject.setValue(Names.ATTR_X, rockPositions.get(i).getX());
			rockObject.setValue(Names.ATTR_Y, rockPositions.get(i).getY());
			rockObject.setValue(Names.ATTR_VALUE, value);
			rockObject.setValue(Names.ATTR_COLLECTED, false);
			rockObject.setValue(Names.ATTR_ROCK_NUMBER, i);
			s.addObject(rockObject);
		}
		// adding direction objects - adding direction objects so params can point to them

		ObjectInstance east = new ObjectInstance(directionClass, Names.OBJ_EAST);
		east.setValue(Names.ATTR_DIRECTION, Names.DIR_EAST);
		s.addObject(east);


		ObjectInstance north = new ObjectInstance(directionClass, Names.OBJ_NORTH);
		north.setValue(Names.ATTR_DIRECTION, Names.DIR_NORTH);
		s.addObject(north);

		ObjectInstance south = new ObjectInstance(directionClass, Names.OBJ_SOUTH);
		south.setValue(Names.ATTR_DIRECTION, Names.DIR_SOUTH);
		s.addObject(south);



		ObjectInstance west = new ObjectInstance(directionClass, Names.OBJ_WEST);
		west.setValue(Names.ATTR_DIRECTION, Names.DIR_WEST);
		s.addObject(west);

		// adding wall objects - relational objects point to directions - these could just have been string objects!
		ObjectInstance westWall = new ObjectInstance(wallClass, Names.OBJ_WALL_WEST);
		westWall.addRelationalTarget(Names.ATTR_WALL_DIRECTION, Names.OBJ_WEST);
		s.addObject(westWall);


		ObjectInstance northWall = new ObjectInstance(wallClass, Names.OBJ_WALL_NORTH);
		northWall.addRelationalTarget(Names.ATTR_WALL_DIRECTION, Names.OBJ_NORTH);
		s.addObject(northWall);

		ObjectInstance southWall = new ObjectInstance(wallClass, Names.OBJ_WALL_SOUTH);
		southWall.addRelationalTarget(Names.ATTR_WALL_DIRECTION, Names.OBJ_SOUTH);
		s.addObject(southWall);

		ObjectInstance eastWall = new ObjectInstance(wallClass, Names.OBJ_WALL_EAST);
		eastWall.addRelationalTarget(Names.ATTR_WALL_DIRECTION, Names.OBJ_EAST);
		s.addObject(eastWall);



		//		System.out.println(s.getCompleteStateDescription());
		return s;
	}

	protected static State getState(PODomain pomdpDomain, int stateCombination) {
		
		
		String formatPattern = "%" + numberOfRocks + "s";
		String binarizedStateOfRocks = String.format(formatPattern, Integer.toBinaryString(stateCombination)).replace(' ', '0');
		

		State s = new State();


		ObjectClass rockClass = pomdpDomain.getObjectClass(Names.CLASS_ROCK);
		ObjectClass agentClass = pomdpDomain.getObjectClass(Names.CLASS_AGENT);
		ObjectClass wallClass = pomdpDomain.getObjectClass(Names.CLASS_WALL);
		ObjectClass directionClass = pomdpDomain.getObjectClass(Names.CLASS_DIRECTION);
		ObjectClass gridClass = pomdpDomain.getObjectClass(Names.CLASS_GRID);


		ObjectInstance roverAgent = new ObjectInstance(agentClass, Names.OBJ_AGENT);
		roverAgent.setValue(Names.ATTR_X, Math.floor((int)(0)));
		roverAgent.setValue(Names.ATTR_Y, Math.floor((int)(sizeOfGrid/2)));
		roverAgent.setValue(Names.ATTR_COMPLETE, false);
		s.addObject(roverAgent);

		ObjectInstance grid = new ObjectInstance(gridClass, Names.OBJ_GRID);
		grid.setValue(Names.ATTR_GRID_SIZE, sizeOfGrid);
		grid.setValue(Names.ATTR_NUMBER_OF_ROCKS, numberOfRocks);
		s.addObject(grid);

		List<Tuple<Integer,Integer>> rockPositions = new ArrayList<Tuple<Integer,Integer>>();
		if (numberOfRocks == 11){
			rockPositions = rockPositions_11_11;
		}
		else
		{
			rockPositions = rockPositions_7_8;
		}

		for(int i = 0; i<numberOfRocks;i++){
			ObjectInstance rockObject = new ObjectInstance(rockClass, Names.OBJ_ROCK+i);
			//			boolean value = new java.util.Random().nextBoolean();
			boolean value; 
			if(binarizedStateOfRocks.charAt(i)== '0'){
				 value = false;
	            }
	            else{
	            value = true;
	            }
			
			rockObject.setValue(Names.ATTR_X, rockPositions.get(i).getX());
			rockObject.setValue(Names.ATTR_Y, rockPositions.get(i).getY());
			rockObject.setValue(Names.ATTR_VALUE, value);
			rockObject.setValue(Names.ATTR_COLLECTED, false);
			rockObject.setValue(Names.ATTR_ROCK_NUMBER, i);

			s.addObject(rockObject);
		}
		// adding direction objects - adding direction objects so params can point to them

		ObjectInstance east = new ObjectInstance(directionClass, Names.OBJ_EAST);
		east.setValue(Names.ATTR_DIRECTION, Names.DIR_EAST);
		s.addObject(east);


		ObjectInstance north = new ObjectInstance(directionClass, Names.OBJ_NORTH);
		north.setValue(Names.ATTR_DIRECTION, Names.DIR_NORTH);
		s.addObject(north);

		ObjectInstance south = new ObjectInstance(directionClass, Names.OBJ_SOUTH);
		south.setValue(Names.ATTR_DIRECTION, Names.DIR_SOUTH);
		s.addObject(south);



		ObjectInstance west = new ObjectInstance(directionClass, Names.OBJ_WEST);
		west.setValue(Names.ATTR_DIRECTION, Names.DIR_WEST);
		s.addObject(west);

		// adding wall objects - relational objects point to directions - these could just have been string objects!
		ObjectInstance westWall = new ObjectInstance(wallClass, Names.OBJ_WALL_WEST);
		westWall.addRelationalTarget(Names.ATTR_WALL_DIRECTION, Names.OBJ_WEST);
		s.addObject(westWall);


		ObjectInstance northWall = new ObjectInstance(wallClass, Names.OBJ_WALL_NORTH);
		northWall.addRelationalTarget(Names.ATTR_WALL_DIRECTION, Names.OBJ_NORTH);
		s.addObject(northWall);

		ObjectInstance southWall = new ObjectInstance(wallClass, Names.OBJ_WALL_SOUTH);
		southWall.addRelationalTarget(Names.ATTR_WALL_DIRECTION, Names.OBJ_SOUTH);
		s.addObject(southWall);

		ObjectInstance eastWall = new ObjectInstance(wallClass, Names.OBJ_WALL_EAST);
		eastWall.addRelationalTarget(Names.ATTR_WALL_DIRECTION, Names.OBJ_EAST);
		s.addObject(eastWall);



		//		System.out.println(s.getCompleteStateDescription());
		return s;
	}
	
	
	public static void main(String [] args){
		RockSample rsGen = new RockSample();
		PODomain rs = (PODomain) rsGen.generateDomain();
		int numParticles = 512;
		double discountFactor = 0.95;
		
		RewardFunction rf = new RockSampleRewardFunction();
		TerminalFunction tf = new RockSampleTerminalFunction();
		BeliefStatistic bs = RockSample.getInitialBeliefStatistic(rs, numParticles);
		
		POEnvironment env = new POEnvironment(rs, rf, tf);
		env.setCurMPDStateTo(bs.sampleStateFromBelief());
		
		
		
		POMCP pomcpPlanner = new POMCP(rs, rf, tf, discountFactor, new NameDependentStateHashFactory(), 88, 20, numParticles);
		pomcpPlanner.planFromBeliefStatistic(bs);
		
		MonteCarloNodeAgent mcAgent = new MonteCarloNodeAgent(pomcpPlanner);
		env.setCurMPDStateTo(bs.sampleStateFromBelief());
		mcAgent.setEnvironment(env);
		
		POMDPEpisodeAnalysis ea = mcAgent.actUntilTerminalOrMaxSteps(88);
		
		
		for(int i = 0; i < ea.numTimeSteps()-1; i++){
			State tempS = ea.getState(i);
			ObjectInstance rover = tempS.getObject(Names.OBJ_AGENT);
			int xPos = rover.getDiscValForAttribute(Names.ATTR_X);
			int yPos = rover.getDiscValForAttribute(Names.ATTR_Y);
			String tval = "RockSample domain " + "x: "+ xPos + " y: " + yPos + " ";
			double tempRW = ea.getReward(i+1);
			String obsVal = ea.getObservation(i).getFirstObjectOfClass(Names.CLASS_OBSERVATION).getStringValForAttribute(Names.ATTR_OBS).replaceAll("#(.*)$", "");
			System.out.println(tval + ": " + ea.getAction(i).toString() + " , Observation: " + obsVal + ", reward: " +tempRW );
		}
		
		System.out.println("Sum of rewards = " +ea.getDiscountedReturn(0.95));
	}





}
