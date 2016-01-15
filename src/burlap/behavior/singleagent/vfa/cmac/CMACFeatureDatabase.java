package burlap.behavior.singleagent.vfa.cmac;

import burlap.behavior.singleagent.vfa.ActionFeaturesQuery;
import burlap.behavior.singleagent.vfa.DifferentiableFunction;
import burlap.behavior.singleagent.vfa.FeatureDatabase;
import burlap.behavior.singleagent.vfa.StateFeature;
import burlap.behavior.singleagent.vfa.cmac.Tiling.StateTile;
import burlap.behavior.singleagent.vfa.common.LinearVFA;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.AbstractObjectParameterizedGroundedAction;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.states.State;
import burlap.oomdp.singleagent.GroundedAction;

import javax.management.RuntimeErrorException;
import java.util.*;



/**
 * A feature database using CMACs [1] AKA Tiling Coding over OO-MDP states. This version of CMAC operates directly on the OO-MDP 
 * representation providing object identifier invariance. If you do not need object identifier invariance, consider
 * using the {@link FVCMACFeatureDatabase} implementation instead, which is slightly more efficient but object identifier dependent.
 * This CMAC can be set to define
 * multiple tilings, each specified for different sets attributes or different object classes. The multiple tilings
 * can be uniformly offset from each other, or randomly jittered to different alignments. The set of state
 * (or state-action) features stored are dynamically created as new states are introduced. That is, {@link Tiling}
 * objects procedurally determine which tile a state is in, but the associated state feature id for each state
 * feature is determined as needed.
 * <p/>
 * Note that different tilings in a CMAC can be defined over different attributes and object classes. For instance
 * a CMAC can consist of two one dimensional tilings that are over different attibutes, such as one that tiles an
 * x position while another tiles the y position. Alternatively, a tiling may be multidimensional with different tilings
 * being defined over different setns of attributes. Having different tilings defined over different sets of attributes
 * enables the VFA to capture value function independence over different attributes.
 * 
 * 
 * 
 * <p/>
 * 
 * 1. Albus, James S. "A theory of cerebellar function." Mathematical Biosciences 10.1 (1971): 25-61
 * @author James MacGlashan
 *
 */
public class CMACFeatureDatabase implements FeatureDatabase {

	
	/**
	 * Enum for specifying whether tilings should have their tile alignments should be chossen so that they
	 * are randomly jittered from each other, or if each subsequent tiling should be offset by a uniform amount.
	 * @author James MacGlashan
	 *
	 */
	public enum TilingArrangement{
		
		RANDOMJITTER(0),
		UNIFORM(1);
		
		private final int value;
		
		TilingArrangement(int i){
			this.value = i;
		}
		
		public int toInt(){
			return this.value;
		}
		
		public static TilingArrangement fromInt(int i){
			switch(i){
				case 0:
					return RANDOMJITTER;
				case 1:
					return UNIFORM;
				default:
					return null;
			}
		}
		
	}
	
	
	/**
	 * The number of tilings
	 */
	protected int													nTilings;
	
	/**
	 * Whether each tiling should have its offset alignment be randomly jittered or uniformly spaced.
	 */
	protected TilingArrangement										arrangement;
	
	/**
	 * A random object for jittering the tile alignments.
	 */
	protected Random												rand;
	
	
	/**
	 * The set of tilings for producing state features
	 */
	protected List <Tiling>											tilings;
	
	/**
	 * For each tiling, a map from state tiles to {@link CMACFeatureDatabase.StoredFeaturesForTiling} objects, which contain
	 * distinct state features for each action. This is useful when doing approximation for state-action values.
	 */
	protected List<Map<StateTile, StoredFeaturesForTiling>>			actionTilings;
	
	/**
	 * For each tiling, a map from state tiles to an integer representing their feature identifier
	 */
	protected List<Map<StateTile, Integer>>							stateTilings;
	
	
	/**
	 * The identifier to use for the next state-action pair feature
	 */
	protected int													nextActionFeatureId = 0;
	
	/**
	 * The identifier to use for the next state feature.
	 */
	protected int													nextStateFeatureId = 0;


	protected State													lastState;
	protected List<StateTile>										tilesForLastState;
	
	
	/**
	 * Initializes with a set of <code>nTilings</code> and sets
	 * the offset arrangement for subsequent tilings to be determined according to <code>arrangement</code>.
	 * The OO-MDP object classes attributes over which the tilings will be defined are unspecified at the start
	 * and will need to be set using other methods on this object such as {@link #addSpecificationForAllTilings(String, Attribute, double)}
	 * or {@link #addSpecificaitonForTiling(int, String, Attribute, double)}.
	 * @param nTilings the number of tilings that will be created.
	 * @param arrangement either RANDOMJITTER or UNIFORM.
	 */
	public CMACFeatureDatabase(int nTilings, TilingArrangement arrangement) {

		this.nTilings = nTilings;
		this.arrangement = arrangement;
		
		rand = RandomFactory.getMapped(0);
		
		
		this.tilings = new ArrayList<Tiling>(nTilings);
		this.actionTilings = new ArrayList<Map<StateTile,StoredFeaturesForTiling>>(nTilings);
		this.stateTilings = new ArrayList<Map<StateTile,Integer>>(nTilings);
		
		for(int i = 0; i < nTilings; i++){
			this.tilings.add(new Tiling());
			this.actionTilings.add(new HashMap<Tiling.StateTile, CMACFeatureDatabase.StoredFeaturesForTiling>());
			this.stateTilings.add(new HashMap<Tiling.StateTile, Integer>());
		}

		
	}
	
	/**
	 * Causes all tilings in this CMAC to be defined over the given attribute for the given OO-MDP class. Along that
	 * dimension, tilings will have a width of <code>windowSize</code>.
	 * @param className the OO-MDP class name for which the provided attribute will be tiled.
	 * @param attribute the OO-MDP attribute that will be tiled
	 * @param windowSize the width of tilings over the specified attribute and OO-MDP class.
	 */
	public void addSpecificationForAllTilings(String className, Attribute attribute, double windowSize){
		
		for(int i = 0; i < nTilings; i++){
			this.addSpecificaitonForTiling(i, className, attribute, windowSize);
		}
		
	}
	
	
	/**
	 * Causes the <code>i</code>th tiling in this CMAC to be defined over the given attribute for the given OO-MDP class. Along that
	 * dimension, the tiling will have a width of <code>windowSize</code>.
	 * @param className the OO-MDP class name for which the provided attribute will be tiled.
	 * @param attribute the OO-MDP attribute that will be tiled
	 * @param windowSize the width of tilings over the specified attribute and OO-MDP class.
	 */
	public void addSpecificaitonForTiling(int i, String className, Attribute attribute, double windowSize){
		
		double bucketBoundary = 0.;
		if(this.arrangement == TilingArrangement.RANDOMJITTER){
			bucketBoundary = rand.nextDouble()*windowSize;
		}
		else if(this.arrangement == TilingArrangement.UNIFORM){
			bucketBoundary = ((double)i / (double)nTilings)*windowSize;
		}
		else{
			throw new RuntimeErrorException(new Error("Unknown CMAC tiling arrangement type"));
		}
		Tiling tiling = tilings.get(i);
		tiling.addSpecification(className, attribute, windowSize, bucketBoundary);
		
	}
	
	
	/**
	 * After all the tiling specifications have been set, this method can be called to produce a linear
	 * VFA object.
	 * @param defaultWeightValue the default value weights for the CMAC features will use.
	 * @return a linear ValueFunctionApproximation object that uses this feature database
	 */
	public DifferentiableFunction generateVFA(double defaultWeightValue){
		return new LinearVFA(this, defaultWeightValue);
	}
	
	@Override
	public List<StateFeature> getStateFeatures(State s) {
		
		List <StateFeature> result = new ArrayList<StateFeature>(nTilings);
		
		for(int i = 0; i < nTilings; i++){
			Tiling tiling = this.tilings.get(i);
			StateTile st = tiling.getStateTile(s);
			Map <StateTile, Integer> featureMapping = stateTilings.get(i);
			Integer storedFID = featureMapping.get(st);
			if(storedFID == null){
				storedFID = nextStateFeatureId;
				nextStateFeatureId++;
				featureMapping.put(st, storedFID);
			}
			StateFeature sf = new StateFeature(storedFID, 1.0); //CMACs use binary features
			result.add(sf);
		}
		

		return result;
	}

	@Override
	public List<ActionFeaturesQuery> getActionFeaturesSets(State s, List<GroundedAction> actions) {

		if(s == this.lastState){
			return this.getActionFeatureSetsFromCacheStateTiles(s, actions);
		}

		this.lastState = s;
		this.tilesForLastState = new ArrayList<StateTile>(nTilings);
		List <ActionFeaturesQuery> result = new ArrayList<ActionFeaturesQuery>(actions.size());
		for(GroundedAction ga : actions){
			ActionFeaturesQuery afq = new ActionFeaturesQuery(ga);
			result.add(afq);
		}
		
		for(int i = 0; i < nTilings; i++){
			Tiling tiling = this.tilings.get(i);
			StateTile st = tiling.getStateTile(s);
			this.tilesForLastState.add(st);
			
			Map <StateTile, StoredFeaturesForTiling> featureMapping = actionTilings.get(i);
			StoredFeaturesForTiling storedFs = featureMapping.get(st);
			if(storedFs == null){
				storedFs = new StoredFeaturesForTiling(st);
				for(ActionFeaturesQuery afq : result){
					storedFs.addActionFeature(new StoredActionFeature(afq.queryAction, nextActionFeatureId));
					afq.addFeature(new StateFeature(nextActionFeatureId, 1.0)); //CMACs use binary features
					nextActionFeatureId++;
					//System.out.println("num features: " + nextActionFeatureId);
				}
				featureMapping.put(st, storedFs);
			}
			else{
				//then we need to extract the action features for each of our queries
				for(ActionFeaturesQuery afq : result){
					StoredActionFeature af = storedFs.getStoredActionFeatureFor(st, afq.queryAction);
					if(af == null){
						storedFs.addActionFeatureFromQuery(st, afq.queryAction, nextActionFeatureId);
						afq.addFeature(new StateFeature(nextActionFeatureId, 1.0)); //CMACs use binary features
						nextActionFeatureId++;
						//System.out.println("num features (from side add): " + nextActionFeatureId);
					}
					else{
						afq.addFeature(new StateFeature(af.id, 1.0)); //CMACs use binary features
					}
				}
				
			}
			
			
		}
		
		
		return result;
	}


	protected List<ActionFeaturesQuery> getActionFeatureSetsFromCacheStateTiles(State s, List<GroundedAction> actions){

		List <ActionFeaturesQuery> result = new ArrayList<ActionFeaturesQuery>(actions.size());
		for(GroundedAction ga : actions){
			ActionFeaturesQuery afq = new ActionFeaturesQuery(ga);
			result.add(afq);
		}


		int i = 0;
		for(StateTile st : this.tilesForLastState){
			Map <StateTile, StoredFeaturesForTiling> featureMapping = actionTilings.get(i);
			StoredFeaturesForTiling storedFs = featureMapping.get(st);
			if(storedFs == null){
				storedFs = new StoredFeaturesForTiling(st);
				for(ActionFeaturesQuery afq : result){
					storedFs.addActionFeature(new StoredActionFeature(afq.queryAction, nextActionFeatureId));
					afq.addFeature(new StateFeature(nextActionFeatureId, 1.0)); //CMACs use binary features
					nextActionFeatureId++;
					//System.out.println("num features: " + nextActionFeatureId);
				}
				featureMapping.put(st, storedFs);
			}
			else{
				//then we need to extract the action features for each of our queries
				for(ActionFeaturesQuery afq : result){
					StoredActionFeature af = storedFs.getStoredActionFeatureFor(st, afq.queryAction);
					if(af == null){
						storedFs.addActionFeatureFromQuery(st, afq.queryAction, nextActionFeatureId);
						afq.addFeature(new StateFeature(nextActionFeatureId, 1.0)); //CMACs use binary features
						nextActionFeatureId++;
						//System.out.println("num features (from side add): " + nextActionFeatureId);
					}
					else{
						afq.addFeature(new StateFeature(af.id, 1.0)); //CMACs use binary features
					}
				}

			}

			i++;
		}

		return result;
	}

	@Override
	public void freezeDatabaseState(boolean toggle) {
		//don't do anything since the tiling space is defined at start and future queries cannot affect the features present for
		//previously queried states.
	}
	
	@Override
	public int numberOfFeatures() {
		return Math.max(this.nextActionFeatureId, this.nextStateFeatureId);
	}


	@Override
	public CMACFeatureDatabase copy() {
		CMACFeatureDatabase cmac = new CMACFeatureDatabase(this.nTilings, this.arrangement);
		cmac.rand = this.rand;
		List<Tiling> newTilings = new ArrayList<Tiling>(this.tilings.size());
		for(Tiling tiling : this.tilings){
			Tiling nTiling = tiling.copy();
			newTilings.add(nTiling);

		}
		cmac.tilings = newTilings;
		cmac.actionTilings = new ArrayList<Map<StateTile, StoredFeaturesForTiling>>(this.actionTilings.size());
		for(Map<StateTile, StoredFeaturesForTiling> el : this.actionTilings){
			Map<StateTile, StoredFeaturesForTiling> nel = new HashMap<StateTile, StoredFeaturesForTiling>(el.size());
			for(Map.Entry<StateTile, StoredFeaturesForTiling> e : el.entrySet()){
				StoredFeaturesForTiling ev = e.getValue();
				StoredFeaturesForTiling nev = cmac.new StoredFeaturesForTiling(e.getKey());
				nev.storedStateTile = e.getKey();
				nev.storedActionFeatures = new ArrayList<StoredActionFeature>(ev.storedActionFeatures);
				nel.put(e.getKey(), nev);
			}
			cmac.actionTilings.add(nel);
		}

		cmac.stateTilings = new ArrayList<Map<StateTile, Integer>>(this.stateTilings.size());
		for(Map<StateTile, Integer> el : this.stateTilings){
			Map<StateTile, Integer> nel = new HashMap<StateTile, Integer>(el);
			cmac.stateTilings.add(nel);
		}

		cmac.nextActionFeatureId = this.nextActionFeatureId;
		cmac.nextStateFeatureId = this.nextStateFeatureId;

		return cmac;
	}


	/**
	 * A class that is used to assign unique feature identifiers for each action for each state tile.
	 * @author James MacGlashan
	 *
	 */
	class StoredFeaturesForTiling{
		
		/**
		 * The tile for which unique feature identifiers should be assigned
		 */
		public StateTile						storedStateTile;
		
		/**
		 * The features assigned to each action that could be applied to states in the associate tile
		 */
		public List <StoredActionFeature>		storedActionFeatures;
		
		
		/**
		 * Initializes for a given state tile and an empty list of actions associated with it.
		 * @param stateTile the state tile for which unique state-action features will be associated.
		 */
		public StoredFeaturesForTiling(StateTile stateTile){
			this.storedStateTile = stateTile;
			this.storedActionFeatures = new ArrayList<StoredActionFeature>();
		}
		
		
		/**
		 * Initializes for a given state tile and list of unique action features.
		 * @param stateTile the state tile for which unique state-action features will be associated.
		 * @param actionFeatures the unique action features to associate with this tile
		 */
		public StoredFeaturesForTiling(StateTile stateTile, List <StoredActionFeature> actionFeatures){
			this.storedStateTile = stateTile;
			this.storedActionFeatures = actionFeatures;
		}
		
		
		/**
		 * Adds an action feature to be associated with this objects state tile.
		 * @param af the action feature to add.
		 */
		public void addActionFeature(StoredActionFeature af){
			this.storedActionFeatures.add(af);
		}
		
		
		/**
		 * Given a query tile (which contains the query state) an action and a new state-action feature id, adds a new action feature to associate with this tile.
		 * In the future, this method will support action parameters and mapping between states with different object name identifiers. Currently that support
		 * is not implement, however, and this method will only work with parameter-less actions.
		 * @param queryTile the query tile (which contains a query state) for which an action was associated
		 * @param queryAction the action that is being considered in the queryTile
		 * @param featureId the feature idenitifer to assign to the state-action pair.
		 */
		public void addActionFeatureFromQuery(StateTile queryTile, GroundedAction queryAction, int featureId){
			if(queryAction instanceof AbstractObjectParameterizedGroundedAction){
				throw new RuntimeErrorException(new Error("CMAC currently does not supported AbstractObjectParameterizedGroundedActions."));
			}
			
			this.storedActionFeatures.add(new StoredActionFeature(queryAction, featureId));
		}
		
		
		/**
		 * Given a query state and action, returns the associated action feature. In the future, this method will support action 
		 * parameters and mapping between states with different object name identifiers. Currently, that support
		 * is not implement, however, and this method will only work with parameter-less actions.
		 * @param queryTile a query tile (which contains the query state)
		 * @param queryAction the query action being considered in the query state.
		 * @return the action feature associated with the state-action pair.
		 */
		public StoredActionFeature getStoredActionFeatureFor(StateTile queryTile, GroundedAction queryAction){
			if(queryAction instanceof AbstractObjectParameterizedGroundedAction){
				throw new RuntimeErrorException(new Error("CMAC currently does not supported AbstractObjectParameterizedGroundedActions."));
			}
			
			for(StoredActionFeature saf : this.storedActionFeatures){
				if(saf.srcGA.equals(queryAction)){
					return saf;
				}
			}
			
			return null;
			
		}
		
	}
	
	
	/**
	 * A class for associating an action with a unique state-action feature identifier.
	 * @author James MacGlashan
	 *
	 */
	class StoredActionFeature{
		
		/**
		 * The action
		 */
		public GroundedAction			srcGA;
		
		/**
		 * The unique state-action feature identifier
		 */
		public int						id;
		
		
		/**
		 * Initializes.
		 * @param ga the action
		 * @param id the unique state-action feature identifier
		 */
		public StoredActionFeature(GroundedAction ga, int id){
			this.srcGA = ga;
			this.id = id;
		}
		
	}





}
