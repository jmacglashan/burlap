package burlap.examples.learning;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.EpisodeSequenceVisualizer;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.auxiliary.performance.LearningAlgorithmExperimenter;
import burlap.behavior.singleagent.auxiliary.performance.PerformanceMetric;
import burlap.behavior.singleagent.auxiliary.performance.TrialMode;
import burlap.behavior.singleagent.learning.GoalBasedRF;
import burlap.behavior.singleagent.learning.LearningAgent;
import burlap.behavior.singleagent.learning.LearningAgentFactory;
import burlap.behavior.singleagent.learning.tdmethods.QLearning;
import burlap.behavior.singleagent.learning.tdmethods.SarsaLam;
import burlap.behavior.singleagent.learning.tdmethods.vfa.GradientDescentSarsaLam;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.QComputablePlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.commonpolicies.GreedyQPolicy;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.singleagent.planning.deterministic.uninformed.dfs.DFS;
import burlap.behavior.singleagent.planning.stochastic.valueiteration.ValueIteration;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.domain.singleagent.gridworld.GridWorldDomain;
import burlap.domain.singleagent.gridworld.GridWorldRewardFunction;
import burlap.domain.singleagent.gridworld.GridWorldStateParser;
import burlap.domain.singleagent.gridworld.GridWorldVisualizer;
import burlap.oomdp.auxiliary.StateGenerator;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.auxiliary.common.ConstantStateGenerator;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.SinglePFTF;
import burlap.oomdp.singleagent.common.UniformCostRF;
import burlap.oomdp.visualizer.Visualizer;

/**
 * Implementation of tutorial at http://burlap.cs.brown.edu/tutorials/bpl/p1.html
 */
public class BasicBehaviour {
    GridWorldDomain gwdg;
    Domain domain;
    StateParser sp;
    RewardFunction rf;
    TerminalFunction tf;
    StateConditionTest goalCondition;
    State initialState;
    DiscreteStateHashFactory hashingFactory;

    public BasicBehaviour() {
        gwdg = new GridWorldDomain(11, 11);
        //Utility function that set the map of the world to the classic Four Rooms map (Sutton, R.S. et al, 1999).
        gwdg.setMapToFourRooms();
        domain = gwdg.generateDomain();
        //StateParsers handle translation of states to text/other prepresentations and back
        sp = new GridWorldStateParser(domain);
        //An example of a pre-packaged reward function that returns -1 for all S-A-S transactions. You can of course, roll your own
        rf = new UniformCostRF();
        //A pre-packaged termination function, which terminates when the specified Proposition function returns true
        tf = new SinglePFTF(domain.getPropFunction(GridWorldDomain.PFATLOCATION));
        //Only used for search methods:
        goalCondition = new TFGoalCondition(tf);
        //Set the initial state using convenience methods
        initialState = GridWorldDomain.getOneAgentOneLocationState(domain);
        GridWorldDomain.setAgent(initialState, 0, 0);
        GridWorldDomain.setLocation(initialState, 0, 10, 10);
        //This is cool. A hashing factory will automatically create a hash for each new state to provide efficient lookups on subsequent updates to the state
        //GridWorld is discrete. so use a discrete hashing function
        hashingFactory = new DiscreteStateHashFactory();
        /* This line is optional. If we did not include it, state hash codes would be computed with respect to all
         * attributes of all objects.
         * Regardless of a choice of which attributes to use for computing hash codes, it's important to note that when
         * states are compared for equality, all attributes will be checked for equality.
         * For instance, if two states with different positions for the location object were compared with this
         * definition of the hashing factory, they would produce identical hash codes, but be evaluated as different
         * states. If you wanted to not only limit which attributes were used for computing hash codes, but also which
         * ones were used for checking state equality, then instead you should use the DiscreteMaskHashingFactory class.
         */
        hashingFactory.setAttributesForClass(GridWorldDomain.CLASSAGENT,
                domain.getObjectClass(GridWorldDomain.CLASSAGENT).attributeList);

    }

    public void visualise(String outputPath) {
        Visualizer v = GridWorldVisualizer.getVisualizer(gwdg.getMap());
        EpisodeSequenceVisualizer evis = new EpisodeSequenceVisualizer(v, domain, sp, outputPath);
    }

    //Test the Breadth-First method
    public void BFSExample(String outputPath) {
        if(!outputPath.endsWith("/")){
            outputPath = outputPath + "/";
        }
        //BFS ignores reward; it just searches for a goal condition satisfying state
        DeterministicPlanner planner = new BFS(domain, goalCondition, hashingFactory);
        planner.planFromState(initialState);
        //capture the computed plan in a partial policy
        //Read http://burlap.cs.brown.edu/tutorials/bpl/p3.html#bfs for some interesting details on the BFS
        Policy p = new SDPlannerPolicy(planner);
        //record the plan results to a .episode file
        p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);

    }

    //Test the Depth-First method
    public void DFSExample(String outputPath) {
        if (!outputPath.endsWith("/")) {
            outputPath = outputPath + "/";
        }
        //DFS ignores reward; it just searches for a goal condition satisfying state
        DeterministicPlanner planner = new DFS(domain, goalCondition, hashingFactory);
        planner.planFromState(initialState);
        //capture the computed plan in a partial policy
        Policy p = new SDPlannerPolicy(planner);
        //record the plan results to a file
        p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
    }

    //Test the A* Method
    public void AStarExample(String outputPath) {
        if (!outputPath.endsWith("/")) {
            outputPath = outputPath + "/";
        }
        Heuristic mdistHeuristic = new Heuristic() {
            @Override
            public double h(State s) {
                ObjectInstance agent = s.getObjectsOfClass(GridWorldDomain.CLASSAGENT).get(0);
                ObjectInstance location = s.getObjectsOfClass(GridWorldDomain.CLASSLOCATION).get(0);
                //get agent position
                int ax = agent.getIntValForAttribute(GridWorldDomain.ATTX);
                int ay = agent.getIntValForAttribute(GridWorldDomain.ATTY);
                //get location position
                int lx = location.getIntValForAttribute(GridWorldDomain.ATTX);
                int ly = location.getIntValForAttribute(GridWorldDomain.ATTY);
                //compute Manhattan distance
                double mdist = Math.abs(ax-lx) + Math.abs(ay-ly);
                return -mdist;
            }
        };
        //provide A* the heuristic as well as the reward function so that it can keep
        //track of the actual cost
        DeterministicPlanner planner = new AStar(domain, rf, goalCondition, hashingFactory, mdistHeuristic);
        planner.planFromState(initialState);
        //capture the computed plan in a partial policy
        Policy p = new SDPlannerPolicy(planner);
        //record the plan results to a file
        p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
    }

    //Use the Value Iteration method - it computes the value policy for the entire state space
    public void ValueIterationExample(String outputPath){
        if(!outputPath.endsWith("/")){
            outputPath = outputPath + "/";
        }
        OOMDPPlanner planner = new ValueIteration(domain, rf, tf, 0.99, hashingFactory, 0.001, 100);
        planner.planFromState(initialState);
        /* Since VI is a stochastic planning algorithm, rather than a deterministic one like the previous algorithms we
         * used, we cannot capture its planning results in a SDPlannerPolicy Policy class. Instead, a policy can be
         * derived from the value function the planner estimates for each state using the GreedyQPolicy class that can
         * be defined for any planner that adheres to the QComputablePlanner interface, which the VI algorithm does.
         */
        Policy p = new GreedyQPolicy((QComputablePlanner)planner);
        //record the plan results to a file
        p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
    }

    //Use Q-learning to solve the maze
    public void QLearningExample(String outputPath) {
        if(!outputPath.endsWith("/")){
            outputPath = outputPath + "/";
        }
        /* This constructor will by default set Q-learning to use a 0.1 epsilon greedy policy
         */
        QLearning agent = new QLearning(domain, rf, tf, 0.99, hashingFactory, 0.0, 0.9);
//        for (int i =0; i<100; i++) {
//            EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState);
//            ea.writeToFile(String.format("%se%03d", outputPath, i), sp);
//            System.out.println(i + ": " + ea.numTimeSteps());
//        }
        // OR
        agent.setMaximumEpisodesForPlanning(100);
        agent.planFromState(initialState);
        Policy p = new GreedyQPolicy(agent);
        p.evaluateBehavior(initialState, rf, tf).writeToFile(outputPath + "planResult", sp);
    }

    public void SarsaLearningExample(String outputPath){
        if(!outputPath.endsWith("/")){
            outputPath = outputPath + "/";
        }
        //discount= 0.99; initialQ=0.0; learning rate=0.5; lambda=1.0
        LearningAgent agent = new SarsaLam(domain, rf, tf, 0.99,  hashingFactory, 0., 0.5, 1.0);
        //run learning for 100 episodes
        for(int i = 0; i < 100; i++){
            EpisodeAnalysis ea = agent.runLearningEpisodeFrom(initialState);
            ea.writeToFile(String.format("%se%03d", outputPath, i), sp);
            System.out.println(i + ": " + ea.numTimeSteps());
        }

    }

    public void experimenterAndPlotter(){
        //custom reward function for more interesting results
        final RewardFunction rf = new GoalBasedRF(this.goalCondition, 5., -0.1);
        StateGenerator sg = new ConstantStateGenerator(this.initialState);
        LearningAlgorithmExperimenter exp = new LearningAlgorithmExperimenter((SADomain) domain, rf, sg,
                10, 100, qLearningFactory, sarsaLearningFactory);
        exp.setUpPlottingConfiguration(500, 250, 2, 1000,
                TrialMode.MOSTRECENTANDAVERAGE,
                PerformanceMetric.CUMULATIVESTEPSPEREPISODE,
                PerformanceMetric.AVERAGEEPISODEREWARD);
        exp.startExperiment();
        exp.writeStepAndEpisodeDataToCSV("expData");
    }



    public static void main(String[] args) {
        BasicBehaviour example = new BasicBehaviour();
        String outputPath = "output/";

        //example.BFSExample(outputPath);
        //example.DFSExample(outputPath);
        //example.AStarExample(outputPath);
        //example.ValueIterationExample(outputPath);
        example.QLearningExample(outputPath);
        //example.SarsaLearningExample(outputPath);
        //Path does NOT have to exist
        example.visualise(outputPath);
        //example.experimenterAndPlotter();

    }

    /**
     * Create factories for Q-learning agent and SARSA agent to compare
     */

    LearningAgentFactory qLearningFactory = new LearningAgentFactory() {
        @Override
        public String getAgentName() {
            return "Lambda=0.5";
        }
        @Override
        public LearningAgent generateAgent() {
            //return new QLearning(domain, rf, tf, 0.99, hashingFactory, 0.3, 0.1);
            return new SarsaLam(domain, rf, tf, 0.99, hashingFactory, 0.0, 0.65, 0.7);
        }
    };


    LearningAgentFactory sarsaLearningFactory = new LearningAgentFactory() {
        @Override
        public String getAgentName() {
            return "Lambda=1.0";
        }
        @Override
        public LearningAgent generateAgent() {
            return new SarsaLam(domain, rf, tf, 0.99, hashingFactory, 0.0, 0.5, 1.0);
        }
    };
}
