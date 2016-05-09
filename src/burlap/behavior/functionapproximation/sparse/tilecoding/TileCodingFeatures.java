package burlap.behavior.functionapproximation.sparse.tilecoding;

import burlap.behavior.functionapproximation.dense.DenseStateFeatures;
import burlap.behavior.functionapproximation.sparse.LinearVFA;
import burlap.behavior.functionapproximation.sparse.SparseStateFeatures;
import burlap.behavior.functionapproximation.sparse.StateFeature;
import burlap.debugtools.RandomFactory;
import burlap.mdp.core.state.State;
import burlap.mdp.singleagent.GroundedAction;

import java.util.*;


/**
 * A feature database using CMACs [1] AKA Tiling Coding for states that are first converted into a feature vector. Because States are converted into a feature vector
 * before tiling them.
 * <p>
 * Different tilings can be created over different dimensions of the converted state feature vector and different tiling widths for each dimension can be specified. Each tiling
 * over the same dimensions can either be randomly jittered from each other or uniformly distributed across the space, which is specified using the {@link TilingArrangement}
 * enumerator.
 * <p>
 * To specify the tiling used, use the {@link #addTilingsForAllDimensionsWithWidths(double[], int, TilingArrangement)} or
 * {@link #addTilingsForDimensionsAndWidths(boolean[], double[], int, TilingArrangement)} method.
 * 
 * 
 * <p>
 * 
 * 1. Albus, James S. "A theory of cerebellar function." Mathematical Biosciences 10.1 (1971): 25-61
 * 
 * @author James MacGlashan
 *
 */
public class TileCodingFeatures implements SparseStateFeatures {

	/**
	 * The generator that turns OO-MDP state objects into state feature vectors.
	 */
	protected DenseStateFeatures featureVectorGenerator;
	
	
	
	/**
	 * A random object for jittering the tile alignments.
	 */
	protected Random													rand = RandomFactory.getMapped(0);
	
	/**
	 * A list of all the tilings used.
	 */
	List<Tiling>														tilings;
	
	
	/**
	 * Mapping to state features
	 */
	List<Map<Tiling.FVTile, Integer>>	 								stateFeatures;

	
	/**
	 * The identifier to use for the next state feature.
	 */
	protected int														nextStateFeatureId = 0;


	@Override
	public TileCodingFeatures copy() {
		TileCodingFeatures tilecoding = new TileCodingFeatures(this.featureVectorGenerator);
		tilecoding.rand = this.rand;
		tilecoding.tilings = new ArrayList<Tiling>(this.tilings);

		tilecoding.stateFeatures = new ArrayList<Map<Tiling.FVTile, Integer>>(this.stateFeatures.size());
		for(Map<Tiling.FVTile, Integer> el : this.stateFeatures){
			Map<Tiling.FVTile, Integer> nel = new HashMap<Tiling.FVTile, Integer>(el);
			tilecoding.stateFeatures.add(nel);
		}

		tilecoding.nextStateFeatureId = this.nextStateFeatureId;

		return tilecoding;
	}

	/**
	 * Initializes specifying the kind of state feature vector generator to use for turning OO-MDP states into feature vectors.
	 * The resulting feature vectors are what is tiled by this class.
	 * @param featureVectorGenerator the OO-MDP state to feature vector generator to use
	 */
	public TileCodingFeatures(DenseStateFeatures featureVectorGenerator){
		
		this.featureVectorGenerator = featureVectorGenerator;
		this.tilings = new ArrayList<Tiling>();
		this.stateFeatures = new ArrayList<Map<Tiling.FVTile,Integer>>();
		
		
	}
	
	
	/**
	 * Adss a number of tilings where each tile is dependent on the dimensions that are labeled as "true" in the dimensionMask parameter. The widths parameter
	 * specifies the width of each tile along that given dimension. If tileArrangement is set to {@link TilingArrangement#UNIFORM} then each of the nTilings
	 * created with will be uniformly spaced across the width of each dimension. If it is set to {@link TilingArrangement#RANDOM_JITTER} then each tiling
	 * will be offset by a random amount.
	 * @param dimensionMask each true entry in this boolen array is a dimension over which the tiling will be defined.
	 * @param widths the width of tiles along each dimension. This value should be non-zero for each dimension unless the tiling doesn't depend on that dimension.
	 * @param nTilings the number of tilings over the specified dimensions to create
	 * @param tileArrangement whether the created tiles are uniformally spaced or randomly spaced.
	 */
	public void addTilingsForDimensionsAndWidths(boolean [] dimensionMask, double [] widths, int nTilings, TilingArrangement tileArrangement){
		
		for(int i = 0; i < nTilings; i++){
			this.stateFeatures.add(new HashMap<Tiling.FVTile, Integer>());
			double [] offset;
			if(tileArrangement == TilingArrangement.RANDOM_JITTER){
				offset = this.produceRandomOffset(dimensionMask, widths);
			}
			else{
				offset = this.produceUniformTilingsOffset(dimensionMask, widths, i, nTilings);
			}
			Tiling tiling = new Tiling(widths, offset, dimensionMask);
			this.tilings.add(tiling);
		}
		
	}
	
	
	/**
	 * Adss a number of tilings where each tile is dependent on *all* the dimensions of a state feature vector. The widths parameter
	 * specifies the width of each tile along that given dimension. If tileArrangement is set to {@link TilingArrangement#UNIFORM} then each of the nTilings
	 * created with will be uniformly spaced across the width of each dimension. If it is set to {@link TilingArrangement#RANDOM_JITTER} then each tiling
	 * will be offset by a random amount.
	 * @param widths the width of tiles along each dimension. This value should be non-zero for each dimension .
	 * @param nTilings the number of tilings over the specified dimensions to create.
	 * @param tileArrangement whether the created tiles are uniformally spaced or randomly spaced.
	 */
	public void addTilingsForAllDimensionsWithWidths(double [] widths, int nTilings, TilingArrangement tileArrangement){
		
		boolean [] dimensionMask = new boolean[widths.length];
		for(int i = 0; i < dimensionMask.length; i++){
			dimensionMask[i] = true;
		}
		this.addTilingsForDimensionsAndWidths(dimensionMask, widths, nTilings, tileArrangement);
		
	}
	
	@Override
	public List<StateFeature> features(State s) {
		
		double [] input = this.featureVectorGenerator.features(s);
		List<StateFeature> features = new ArrayList<StateFeature>();
		for(int i = 0; i < this.tilings.size(); i++){
			Tiling tiling = this.tilings.get(i);
			Map<Tiling.FVTile, Integer> tileFeatureMap = this.stateFeatures.get(i);
			
			Tiling.FVTile tile = tiling.getFVTile(input);
			int f = this.getOrGenerateFeature(tileFeatureMap, tile);
			StateFeature sf = new StateFeature(f, 1.);
			features.add(sf);
			
		}
		
		return features;
	}

	@Override
	public int numFeatures() {
		return nextStateFeatureId;
	}

	/**
	 * Returns the stored feature id or creates, stores and returns one. If a feature id is created, then the {@link #nextStateFeatureId} data member of this
	 * object is incremented.
	 * @param tileFeatureMap the map from tiles to feature ids
	 * @param tile the tile for which a feature id is returned.
	 * @return the feature id for the tile.
	 */
	protected int getOrGenerateFeature(Map<Tiling.FVTile, Integer> tileFeatureMap, Tiling.FVTile tile){
		Integer stored = tileFeatureMap.get(tile);
		if(stored == null){
			stored = this.nextStateFeatureId;
			tileFeatureMap.put(tile, stored);
			this.nextStateFeatureId++;
		}
		return stored;
	}
	
	
	/**
	 * Returns or creates stores and returns the list of action feature ids in the given map for the given tile. If a new list is created, it will
	 * be empty and will need to action features added to it.
	 * @param tileFeatureMap the map from tiles for a state to the list of action features for that state 
	 * @param tile the tile for which the list of action features is returned.
	 * @return the list of action features.
	 */
	protected List<ActionFeatureID> getOrGenerateActionFeatureList(Map<Tiling.FVTile, List<ActionFeatureID>> tileFeatureMap, Tiling.FVTile tile){
		
		List<ActionFeatureID> stored = tileFeatureMap.get(tile);
		if(stored == null){
			stored = new ArrayList<TileCodingFeatures.ActionFeatureID>();
			tileFeatureMap.put(tile, stored);
		}
		return stored;
		
	}


	
	
	/**
	 * After all the tiling specifications have been set, this method can be called to produce a linear
	 * VFA object.
	 * @param defaultWeightValue the default value weights for the CMAC features will use.
	 * @return a linear ValueFunctionApproximation object that uses this feature database
	 */
	public LinearVFA generateVFA(double defaultWeightValue){
		return new LinearVFA(this, defaultWeightValue);
	}

	
	
	/**
	 * Creates and returns a random tiling offset for the given widths and required dimensions.
	 * @param dimensionMask each true entry is a dimension on which the tiling depends
	 * @param widths the width of each dimension
	 * @return a random tiling offset.
	 */
	protected double [] produceRandomOffset(boolean [] dimensionMask, double [] widths){
		double [] offset = new double[dimensionMask.length];
		
		for(int i = 0; i < offset.length; i++){
			if(dimensionMask[i]){
				offset[i] = this.rand.nextDouble()*widths[i];
			}
			else{
				offset[i] = 0.;
			}
		}
		
		return offset;
	}
	
	
	/**
	 * Creates and returns an offset that is uniformly spaced from other tilings.
	 * @param dimensionMask each true entry is a dimension on which the tiling depends
	 * @param widths widths the width of each dimension
	 * @param ithTiling which tiling of the nTilings for which this offset is to be generated
	 * @param nTilings the total number of tilings that will be uniformaly spaced
	 * @return an offset that is uniformly spaced from other tilings.
	 */
	protected double [] produceUniformTilingsOffset(boolean [] dimensionMask, double [] widths, int ithTiling, int nTilings){
		double [] offset = new double[dimensionMask.length];
		
		for(int i = 0; i < offset.length; i++){
			if(dimensionMask[i]){
				offset[i] = ((double)ithTiling / (double)nTilings)*widths[i];
			}
			else{
				offset[i] = 0.;
			}
		}
		
		return offset;
	}
	
	
	/**
	 * Returns the {@link ActionFeatureID} with an equivalent {@link GroundedAction} in the given list or null if there is none.
	 * @param actionFeatures the list of {@link ActionFeatureID} objects to search.
	 * @param forAction the {@link GroundedAction} for which a match is to be found.
	 * @return the {@link ActionFeatureID} with an equivalent {@link GroundedAction} in the given list or null if there is none.
	 */
	protected ActionFeatureID matchingActionFeature(List<ActionFeatureID> actionFeatures, GroundedAction forAction){
		
		for(ActionFeatureID aid : actionFeatures){
			if(aid.ga.equals(forAction)){
				return aid;
			}
		}
		
		return null;
	}
	
	
	/**
	 * A class for associating a {@link GroundedAction} with a feature id.
	 * @author James MacGlashan
	 *
	 */
	protected class ActionFeatureID{
		public int id;
		public GroundedAction ga;
		
		public ActionFeatureID(GroundedAction ga, int id){
			this.id = id;
			this.ga = ga;
		}
		
	}

}
