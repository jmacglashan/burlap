package burlap.behavior.functionapproximation;

import burlap.datastructures.HashedAggregator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author James MacGlashan
 */
public class GradientUtils {


    /**
     * Turns a {@link HashedAggregator} of index type Integer, and turns it into
     * {@link burlap.behavior.functionapproximation.FunctionGradient.SparseGradient}, where
     * the keys are parameter indices and the values their partial derivative.
     * @param summedParams the {@link HashedAggregator} to transform
     * @return a {@link burlap.behavior.functionapproximation.FunctionGradient.SparseGradient}
     */
    public static FunctionGradient toGradient(HashedAggregator<Integer> summedParams){
        FunctionGradient fg = new FunctionGradient.SparseGradient(summedParams.size());
        for(Map.Entry<Integer, Double> e : summedParams.entrySet()){
            fg.put(e.getKey(), e.getValue());
        }
        return fg;
    }


    /**
     * Multiplies every element in a {@link FunctionGradient} by scalar
     * @param fg the {@link FunctionGradient}
     * @param scalar the scalar value
     */
    public static void scalarMult(FunctionGradient fg, double scalar){
        for(FunctionGradient.PartialDerivative pd : fg.getNonZeroPartialDerivatives()){
            double scaled = pd.value * scalar;
            fg.put(pd.parameterId, scaled);
        }
    }


    /**
     * Creates a {@link burlap.behavior.functionapproximation.FunctionGradient.SparseGradient} that is set
     * to a gradient multiplied by a scMultiplies every element in a {@link FunctionGradient} by scalar
     * @param fg the {@link FunctionGradient}
     * @param scalar the scalar value
     */
    public static FunctionGradient scalarMultCopy(FunctionGradient fg, double scalar){
        FunctionGradient cfg = new FunctionGradient.SparseGradient(fg.numNonZeroPDs());
        for(FunctionGradient.PartialDerivative pd : fg.getNonZeroPartialDerivatives()){
            double scaled = pd.value * scalar;
            cfg.put(pd.parameterId, scaled);
        }
        return cfg;
    }


    /**
     * Adds the partial derivatives from a gradient into a {@link HashedAggregator}
     * @param fg the source gradient
     * @param sum the destination to which the partial derivatives are added
     */
    public static void sumInto(FunctionGradient fg, HashedAggregator<Integer> sum){
        for(FunctionGradient.PartialDerivative pd : fg.getNonZeroPartialDerivatives()){
            sum.add(pd.parameterId, pd.value);
        }
    }

    /**
     * Returns a-b in a new {@link burlap.behavior.functionapproximation.FunctionGradient.SparseGradient}
     * @param a the first gradient
     * @param b the second gradient
     * @return a-b as a {@link burlap.behavior.functionapproximation.FunctionGradient.SparseGradient}
     */
    public static FunctionGradient diffGrad(FunctionGradient a, FunctionGradient b){
        Set<Integer> pIds = pdIdSet(a, b);

        //now compute
        FunctionGradient fg = new FunctionGradient.SparseGradient(pIds.size());
        for(int pid : pIds){
            double v = a.getPartialDerivative(pid) - b.getPartialDerivative(pid);
            fg.put(pid, v);
        }

        return fg;

    }


    /**
     * return a+b in a new {@link burlap.behavior.functionapproximation.FunctionGradient.SparseGradient}
     * @param a the first gradient
     * @param b the second gradient
     * @return a new {@link burlap.behavior.functionapproximation.FunctionGradient.SparseGradient}
     */
    public static FunctionGradient addGrad(FunctionGradient a, FunctionGradient b){

        Set<Integer> pIds = pdIdSet(a, b);

        //now compute
        FunctionGradient fg = new FunctionGradient.SparseGradient(pIds.size());
        for(int pid : pIds){
            double v = a.getPartialDerivative(pid) + b.getPartialDerivative(pid);
            fg.put(pid, v);
        }

        return fg;

    }


    /**
     * Returns the set of parameter ids with non-zero partial derivatives across two gradients.
     *
     * That is, nonZero(a) U nonZero(b)
     * @param a the first gradient
     * @param b the second gradient
     * @return a set of the partial derivative ids
     */
    public static Set<Integer> pdIdSet(FunctionGradient a, FunctionGradient b){
        Set<FunctionGradient.PartialDerivative> aSet = a.getNonZeroPartialDerivatives();
        Set<FunctionGradient.PartialDerivative> bSet = b.getNonZeroPartialDerivatives();
        Set<Integer> pIds = new HashSet<Integer>(aSet.size()+bSet.size());
        for(FunctionGradient.PartialDerivative pd : aSet){
            pIds.add(pd.parameterId);
        }
        for(FunctionGradient.PartialDerivative pd : bSet){
            pIds.add(pd.parameterId);
        }

        return pIds;
    }

}
