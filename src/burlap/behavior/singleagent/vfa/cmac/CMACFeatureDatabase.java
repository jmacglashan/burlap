package burlap.behavior.singleagent.vfa.cmac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.management.RuntimeErrorException;

import burlap.behavior.singleagent.vfa.ActionFeaturesQuery;
import burlap.behavior.singleagent.vfa.FeatureDatabase;
import burlap.behavior.singleagent.vfa.LinearVFA;
import burlap.behavior.singleagent.vfa.StateFeature;
import burlap.behavior.singleagent.vfa.ValueFunctionApproximation;
import burlap.behavior.singleagent.vfa.cmac.Tiling.StateTile;
import burlap.debugtools.RandomFactory;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;


public class CMACFeatureDatabase implements FeatureDatabase {

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
	
	
	protected int													nTilings;
	protected TilingArrangement										arrangement;
	
	protected Random												rand;
	
	protected List <Tiling>											tilings;
	protected List<Map<StateTile, StoredFeaturesForTiling>>			actionTilings;
	protected List<Map<StateTile, Integer>>							stateTilings;
	
	
	protected int													nextActionFeatureId = 0;
	protected int													nextStateFeatureId = 0;
	
	
	
	
	
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
	
	public void addSpecificationForAllTilings(String className, Attribute attribute, double windowSize){
		
		for(int i = 0; i < nTilings; i++){
			this.addSpecificaitonForTiling(i, className, attribute, windowSize);
		}
		
	}
	
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
	
	
	public ValueFunctionApproximation generateVFA(double defaultWeightValue){
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
		
		List <ActionFeaturesQuery> result = new ArrayList<ActionFeaturesQuery>(actions.size());
		for(GroundedAction ga : actions){
			ActionFeaturesQuery afq = new ActionFeaturesQuery(ga);
			result.add(afq);
		}
		
		for(int i = 0; i < nTilings; i++){
			Tiling tiling = this.tilings.get(i);
			StateTile st = tiling.getStateTile(s);
			
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

	@Override
	public void freezeDatabaseState(boolean toggle) {
		//don't do anything since the tiling space is defined at start and future queries cannot affect the features present for
		//previously queried states.
	}
	
	
	
	
	class StoredFeaturesForTiling{
		
		public StateTile						storedStateTile;
		public List <StoredActionFeature>		storedActionFeatures;
		
		public StoredFeaturesForTiling(StateTile stateTile){
			this.storedStateTile = stateTile;
			this.storedActionFeatures = new ArrayList<StoredActionFeature>();
		}
		
		public StoredFeaturesForTiling(StateTile stateTile, List <StoredActionFeature> actionFeatures){
			this.storedStateTile = stateTile;
			this.storedActionFeatures = actionFeatures;
		}
		
		public void addActionFeature(StoredActionFeature af){
			this.storedActionFeatures.add(af);
		}
		
		public void addActionFeatureFromQuery(StateTile queryTile, GroundedAction queryAction, int featureId){
			if(queryAction.params.length > 0){
				throw new RuntimeErrorException(new Error("CMAC currently does not supported paramaterized Actions. Support will be added in a later version"));
			}
			
			this.storedActionFeatures.add(new StoredActionFeature(queryAction, featureId));
		}
		
		public StoredActionFeature getStoredActionFeatureFor(StateTile queryTile, GroundedAction queryAction){
			if(queryAction.params.length > 0){
				throw new RuntimeErrorException(new Error("CMAC currently does not supported paramaterized Actions. Support will be added in a later version"));
			}
			
			for(StoredActionFeature saf : this.storedActionFeatures){
				if(saf.srcGA.action.getName().equals(queryAction.action.getName())){
					return saf;
				}
			}
			
			return null;
			
		}
		
	}
	
	class StoredActionFeature{
		
		public GroundedAction			srcGA;
		public int						id;
		
		public StoredActionFeature(GroundedAction ga, int id){
			this.srcGA = ga;
			this.id = id;
		}
		
	}

}
