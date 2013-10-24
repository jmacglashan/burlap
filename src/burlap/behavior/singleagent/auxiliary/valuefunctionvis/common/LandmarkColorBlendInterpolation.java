package burlap.behavior.singleagent.auxiliary.valuefunctionvis.common;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;


public class LandmarkColorBlendInterpolation implements ColorBlend {

	protected List <Double>			landmarkValues;
	protected List <Color>			landmarkColors;
	protected double				polyDegree = 1.;
	
	
	public LandmarkColorBlendInterpolation() {
		this.landmarkValues = new ArrayList<Double>();
		this.landmarkColors = new ArrayList<Color>();
	}
	
	public LandmarkColorBlendInterpolation(double polyDegree) {
		this.landmarkValues = new ArrayList<Double>();
		this.landmarkColors = new ArrayList<Color>();
		this.polyDegree = polyDegree;
	}
	
	public void setPolynomialDegree(double polyDegree){
		this.polyDegree = polyDegree;
	}
	
	public double getPolynomialDegree(){
		return this.polyDegree;
	}
	
	
	/**
	 * Adds the next landmark between which interpolation should occur. Assumes that the value val is greater than the last landmark value added.
	 * @param val the value of the landmark
	 * @param c the color for the landmark
	 */
	public void addNextLandMark(double val, Color c){
		this.landmarkValues.add(val);
		this.landmarkColors.add(c);
	}
	
	
	@Override
	public void rescale(double minV, double maxV) {
		double curMin = this.landmarkValues.get(0);
		double curMax = this.landmarkValues.get(this.landmarkValues.size()-1);
		
		if(curMin == minV && curMax == maxV){
			return ; //already correctly scaled
		}
		
		double curRange = curMax - curMin;
		double newRange = maxV - minV;
		
		for(int i = 0; i < this.landmarkValues.size(); i++){
			double v = this.landmarkValues.get(i);
			double t = (v - curMin) / curRange;
			double nv = t*newRange + minV;
			this.landmarkValues.set(i, nv);
		}
		
		
	}

	@Override
	public Color color(double v) {
		
		//end point?
		if(v <= landmarkValues.get(0)){
			return landmarkColors.get(0);
		}
		if(v >= landmarkValues.get(landmarkValues.size()-1)){
			return landmarkColors.get(landmarkColors.size()-1);
		}
		
		//which is the interpolation end point?
		int ePoint = 1;
		for(int i = 1; i < landmarkValues.size(); i++){
			ePoint = i;
			if(landmarkValues.get(i) > v){
				break ;
			}
			
		}
		
		double sv = landmarkValues.get(ePoint-1);
		double ev = landmarkValues.get(ePoint);
		double vRange = ev - sv;
		
		double t = (v - sv) / vRange;
		t = Math.pow(t, this.polyDegree);
		
		Color sC = landmarkColors.get(ePoint-1);
		Color eC = landmarkColors.get(ePoint);
		
		float [] sColorComp = sC.getColorComponents(null);
		float [] eColorComp = eC.getColorComponents(null);
		
		float red = this.interpolate(sColorComp[0], eColorComp[0], (float)t);
		float green = this.interpolate(sColorComp[1], eColorComp[1], (float)t);
		float blue = this.interpolate(sColorComp[2], eColorComp[2], (float)t);
		
		Color finalColor = new Color(red, green, blue);
		
		return finalColor;
	}
	
	
	protected float interpolate(float s, float e, float t){
		float range = e - s;
		return s + t*range;
	}

	

}
