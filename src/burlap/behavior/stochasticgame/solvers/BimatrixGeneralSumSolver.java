/* Original header:
 * Applet for calculating Nash equilibria for two-person bimatrix game. 
 * Copyright (c) Junling Hu and Yilian Zhang, 2001 
 * This is an Applet that allows an user to enter the payoff matrices, the then caculated possible Nash equilibria for the bimatrix game. 
 * The algorithm is Lemke-Howson method decribed in the following book:    
 * @book{Cottle91,
 *   author = {Cottle, Richard W. and Pang, J.--S. and R. E. Stone},
 *   title =	 {The Linear Complementarity Problem},
 *   year =	 1992,
 *   publisher = {Academic Press},
 *   address = {New York}
 */


package burlap.behavior.stochasticgame.solvers;


import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.debugtools.RandomFactory;


public class BimatrixGeneralSumSolver {

	static class Joint<E> extends ArrayList<E> {

		private static final long serialVersionUID = 1L;

		public Joint() {
		}

		public Joint(List<E> vals) {
			this.addAll(vals);
		}

		public E getForPlayer(int playerIdx) {
			return get(playerIdx);
		}

	}
	
	static Random random = RandomFactory.getMapped(0);
	
	
	public static void main(String [] args){
		
		/*
		double [][] p1 = {{3, 0},{5, 1}};
		double [][] p2 = {{3, 5}, {0, 1}};
		*/
		
		/*
		double [][] p1 = {{4, 1},{3, 2}};
		double [][] p2 = {{3, 3}, {1, 2}};
		*/
		
		double [][] p1 = {{0, -1},{1, -10}};
		double [][] p2 = {{0, 1}, {-1, -10}};
		
		System.out.println(generalSumNash(p1, p2)[0]);
	}
	
	public static double[] generalSumNash(double[][] payoffs1, double[][] payoffs2){

        Joint<double[]> strategies = solveForMixedStrategies(payoffs1, payoffs2);

		double[] Player1Strategy = strategies.getForPlayer(0); 
		double[] Player2Strategy = strategies.getForPlayer(1); 

		double[][] outcomeProbability = getDistributionOverJointActions(Player1Strategy, Player2Strategy);

		double ExpectedpayoffforPlayer1 = getExpectedPayoffsForPlayer(payoffs1, outcomeProbability);
		double ExpectedpayoffforPlayer2 = getExpectedPayoffsForPlayer(payoffs2, outcomeProbability);

		//System.out.println("player1Strategy: " + Arrays.toString(strategies.getForPlayer(0)) + "player1payoff" + ExpectedpayoffforPlayer1 );
		//System.out.println("player2Strategy: " + Arrays.toString(strategies.getForPlayer(1)) +"player2payoff" + ExpectedpayoffforPlayer2);
		
		return new double[]{ExpectedpayoffforPlayer1, ExpectedpayoffforPlayer2};

		


	}


	public static Joint<double[]> solveForMixedStrategies(
			double[][] player1Payoffs,
			double[][] player2Payoffs) {
		int numPlayer1Actions = player1Payoffs.length; // row
		int numPlayer2Actions = player1Payoffs[0].length; // col
		int numTotalActions = numPlayer1Actions + numPlayer2Actions; // dimM
	
		// Each row is a different equilibrium strategy. Each column gives the probability of 
		// player 1 playing each of its actions, followed by the probability of player 2 playing 
		// each of its actions.
		double[][] equilibriumMixedStrategies = new double[numPlayer1Actions][numTotalActions]; 
		getnash(player1Payoffs, player2Payoffs, equilibriumMixedStrategies, numPlayer1Actions, numPlayer2Actions, numTotalActions);
		// Get the equilibrium strategies for the two players
		int equilibriumIdxToConsider = 0; // always consider the first equilibrium found.
		double[] eqmStrategyForPlayer1 = new double[numPlayer1Actions];
		for (int actionIdx=0; actionIdx<numPlayer1Actions; actionIdx++) {
			eqmStrategyForPlayer1[actionIdx] = equilibriumMixedStrategies[equilibriumIdxToConsider][actionIdx];
		}
		double[] eqmStrategyForPlayer2 = new double[numPlayer2Actions];
		for (int actionIdx=0; actionIdx<numPlayer2Actions; actionIdx++) {
			eqmStrategyForPlayer2[actionIdx] = equilibriumMixedStrategies[equilibriumIdxToConsider][numPlayer1Actions+actionIdx];
		}
	
		Joint<double[]> mixedStrategies = new BimatrixGeneralSumSolver.Joint<double[]>();
		mixedStrategies.add(eqmStrategyForPlayer1);
		mixedStrategies.add(eqmStrategyForPlayer2);
		return mixedStrategies;
	}
	
	public static double[][] getDistributionOverJointActions(double[] player1Mix, double[] player2Mix) {
		int numPlayer1Actions = player1Mix.length; 
		int numPlayer2Actions = player2Mix.length;
		double[][] jointActionDistribution = new double[numPlayer1Actions][numPlayer2Actions];
		for (int action1Idx=0; action1Idx<numPlayer1Actions; action1Idx++) {
			for (int action2Idx=0; action2Idx<numPlayer2Actions; action2Idx++) {
				double probAction1 = player1Mix[action1Idx];
				double probAction2 = player2Mix[action2Idx];
				jointActionDistribution[action1Idx][action2Idx] = probAction1 * probAction2; 
			}
		}
		return jointActionDistribution;
	}
	
	/**
	 * Computes the expected payoff for a player, given that player's payoff matrix and the probability of each
	 * outcome.
	 * @param playerPayoffMatrix
	 * @param outcomeProbability
	 * @return
	 */
	public static double getExpectedPayoffsForPlayer(double[][] playerPayoffMatrix, double[][] outcomeProbability) {
		int numPlayer1Actions = playerPayoffMatrix.length;
		int numPlayer2Actions = playerPayoffMatrix[0].length;
		double expectedPayoff = 0;
		for (int action1=0; action1<numPlayer1Actions; action1++) {
			for (int action2=0; action2<numPlayer2Actions; action2++) {
				expectedPayoff += playerPayoffMatrix[action1][action2] * outcomeProbability[action1][action2];
			}
		}
		return expectedPayoff;
	}
	
	private static void getnash(double A[][] , double B[][],double Z[][], int row, int col, int dimM){
	
		double LA[][];
		double LB[][];
		double LZ[][];
		double M[][];
		int WList[][],ZList[][];
		double Q[];
	
	
	
		// initial Q and Z, WList, ZList	
		LA = new double[row][col];
		LB = new double[row][col];
		M = new double[dimM][dimM];
		LZ = new double[row][dimM];
		WList = new int[dimM][2];
		ZList = new int[dimM][2];
		Q = new double[dimM];
	
		/*Transform the payoff matrix into cost matrix*/
		/* LA = -A , LB = -B */
		Multiple2(A,LA,row,col,-1); 
		Multiple2(B,LB,row,col,-1);  
	
		/* find more than one Nash equilibrium solution*/
		for(int k = 0;k<row;k++){
			AllClear(WList,ZList,Q,LZ,k,dimM);    // initialize 
			Comp(LA,LB,M,row,col,dimM);
			getonenash(M,Q,WList,ZList,k,LA,LB,LZ,row,col,dimM); // get one nash solution
			Multiple1(LZ,Z,dimM,1,k);
		}
		//QMprint(LB,row,col);
	}
	
	private static void AllClear(int WList[][],int ZList[][],double Q[],double LZ[][],int k, int dimM){
		// Define index matrix WList and ZList,Q
	
		for (int i=0;i<dimM;i++){
			WList[i][1] = i; WList[i][0] =1;  //"W"
			ZList[i][1] = i; ZList[i][0] =2;  // "z"
			Q[i] = -1;
			LZ[k][i] = 0;
		}
	}
	
	public static void getonenash(double M[][],double Q[],int WList[][],int ZList[][],int k, double LA[][],double LB[][], double LZ[][], int row, int col, int dimM){
		int c=0,r=0,j1;	
		int oldWList[][];
		oldWList = new int[dimM][2];
	 		
		// find c be the best action agent 2 can take
		c=find_min_col(LB,k,col);
	
		// Pivot get new q, M and exchange-elements
		//QMprint(M,dimM,dimM);
		Pivot(Q,M,c+row,k,dimM);	    
		Exchange_element(WList,ZList,c+row,k);
	
		//if(k==0) QMprint(M,dimM,dimM);
		// find r is the best action agent 1 can take against c
		r = find_min_row(LA,c,row);		
	
		//Pivot get new q, M and exchange-elements
		Pivot(Q,M,r,c+row,dimM); 
		Exchange_element(WList,ZList,r,c+row);
		//if(k==2) QMprint(M,dimM,dimM);
		//System.out.print("c=" +c +"r="+r+"\n");
		// get Z for k
		if (r==k){  // we have the solution
			get_final_z(WList,Q,LZ,k,dimM);
		}
		else{
			j1 = find_min_ratio(Q,M,r,dimM);
			//System.out.println("j1="+j1);
			if(j1==-1) {              //we find the solution
				get_final_z(WList,Q,LZ,k,dimM);
			}
			else{
				// get to original step 
				Pivot(Q,M,j1,r,dimM);
				if (k==2) QMprint(M,dimM,dimM);
				while(j1!=-1&&WList[j1][1]!=k){
					//System.out.println("In while loop... j1=" + j1 + ", WList[j1][1]=" + WList[j1][1] + ", k=" + k);
	
					//copy Wlist to oldWlist
					Multiple2(WList,oldWList,dimM,2,1);  
					Exchange_element(WList,ZList, j1,r);
					r = find_complement(j1,oldWList,ZList,dimM);
	
					// go back to step 1. r is the new driving variable
					j1 = find_min_ratio(Q,M,r,dimM); 
					if ( j1!=-1) { Pivot(Q,M,j1,r,dimM);
					}
					//System.out.print(j1);
				}
				if(j1!=-1 ) Exchange_element(WList,ZList,j1,r);
				get_final_z(WList,Q,LZ,k,dimM);
			}
		}	   
		normalize(LZ,k,row,dimM);
	}
	
	// calculate  A1 = alpha * A0 , scalar multiplication
	public static void Multiple2(double A0[][], double A1[][],int s,int t,double p){
	
		for (int i=0;i<s;i++){
			for (int j=0;j<t;j++){
				A1[i][j] = p*A0[i][j];
			}
		}
	}
	
	// calculate  A1 = alpha * A0 , scalar multiply, A0 and A1 are integers
	public static void Multiple2(int A0[][], int A1[][],int s,int t,int prod){
	
		for (int i=0;i<s;i++){
			for (int j=0;j<t;j++){
				A1[i][j] = prod*A0[i][j];
			}
		}
	}
	
	// calculate  A1[k] = alpha * A0[k], scalar multiply on one row
	public static void Multiple1(double A0[][], double A1[][],int t,double product,int k)
	{
		for (int i=0;i<t;i++){ 
			A1[k][i] = product*A0[k][i];
		}
	}
	
	// construct the M matrix
	public static void Comp(double A0[][],double A1[][],double M1[][], int row, int col, int dimM){
		// want M be a positive matrix at the beginning
		double MaxA,MaxB;
		MaxA = find_abs_max(A0, row, col);MaxB = find_abs_max(A1, row, col); 
	
		for (int i=0;i<dimM;i++){
			for (int j=0;j<dimM;j++){
				// M(i,j) = 0 , i<m&j<m, i>m&j>m 
				if ((i<row&&j<row)||(i>=row&&j>=row)){
					M1[i][j] = 0;
				}
				else if (j>=row&&i<row) {
					M1[i][j] = A0[i][j-row]+ MaxA +1 ;
				}
				else {
					M1[i][j] = A1[j][i-row]+ MaxB + 1;   // transpose
				}
			}
		}
	}
	
	// find the absolute maxium of Matrix A0
	public static double find_abs_max(double A0[][], int row, int col){
		double Max=0;
		double temp;
	
		for(int i=0;i<row;i++){
			for(int j=0;j<col;j++){
				temp= Math.abs(A0[i][j]);
				if ( temp > Max) Max = temp;
			}
		}
		return Max;
	}
	
	public static int find_min_col(double L1[][],int k, int col){
		double min = 200000;
		int c=0;
		    for(int j=0;j<col;j++){
			if (L1[k][j]<min) {
				min = L1[k][j];
				c = j;
			}
	    }
		return c;
	}
	
	public static int find_min_row(double L1[][],int c, int row){
		double min = 200000;
		int r=0;
		for(int i=0;i<row;i++){
				if (L1[i][c]<min) {
					min = L1[i][c];
					r = i;
				}
		}
		return r;
	}
	
	public static void Pivot(double Q1[],double M1[][], int r1, int c1, int dimM){
	
		double pPoint ;
		double MLocal[][];
		double QLocal[];
		MLocal = new double[dimM][dimM];
		QLocal = new double[dimM];
	
		// the pivot point
		pPoint = M1[r1][c1];
	
		if (Math.abs(pPoint) <= 0.000000001) {
			return ;}
	
		for (int i=0;i<dimM;i++){
			if (i==r1) QLocal[i] = -Q1[r1]/pPoint ;
			else QLocal[i] = Q1[i] - (Q1[r1]/pPoint)*M1[i][c1];
	
			for (int j=0;j<dimM;j++){
				if (i==r1){
					if(j==c1) MLocal[i][j] = 1.0/pPoint;
					else MLocal[i][j] = - M1[r1][j] /pPoint;
				}
				else {
		    if(j==c1) MLocal[i][j] = M1[i][c1]/pPoint;
		    else MLocal[i][j] = M1[i][j] -(M1[i][c1]/pPoint)*M1[r1][j];
				}
			}
		}
		for ( int i=0;i<dimM;i++) Q1[i] = QLocal[i];
		  Multiple2(MLocal,M1,dimM,dimM,1);
	}
	
	//exchange rth row of WList and cth row of ZList 
	public static void  Exchange_element(int WList1[][],int ZList1[][],int r,int c){
		int temp1,temp2;	
		temp1 = WList1[r][0]; temp2  = WList1[r][1];
		WList1[r][0]= ZList1[c][0]; WList1[r][1]=ZList1[c][1];
		ZList1[c][0]= temp1; ZList1[c][1] = temp2;
	}
	
	// This function is modified, get the final solution
	public static void  get_final_z(int WList1[][],double Q1[],double Z1[][],int k, int dimM){
	
		int j;
		for (int i=0;i<dimM;i++){
	    if (WList1[i][0] == 2) { j=WList1[i][1]; Z1[k][j] = Q1[i];}
	    
		}
	}
	
	// minumum ratio test
	public static int find_min_ratio(double Q1[],double M1[][], int r, int dimM){
		double min;
		double R[];
		int j=-1;
		double PostZero = 20000000;
		R = new double[dimM];
	
		//System.out.print("loop in min_ratio   ");
		for (int i=0;i<dimM;i++){
			if (M1[i][r]<-0.00001) R[i] = -Q1[i]/M1[i][r];
			else R[i] = PostZero;
		}
		min = PostZero;
	
		// Old method:
		boolean oldMethod = true;
		if (oldMethod) {
			for (int i=0;i<dimM;i++){
				if(R[i]!=PostZero&&R[i]<min){
					min = R[i]; j=i;
				}
			}
			return j;
		}
	
		// New method:
		// Need to handle degenerate cases. See Section 3 in:
		// http://www.public.iastate.edu/~riczw/MEGliter/general/Lemkehowson.pdf
		// Break ties randomly
		List<Integer> minIndices = new ArrayList<Integer>();
		double myMin = PostZero;
		for (int i=0;i<dimM;i++){
			if(R[i]!=PostZero&&R[i]<=myMin){
				myMin = R[i];
				// If strictly lower, clear out old mins
				if (R[i]<myMin) {
					minIndices.clear();
				}
				minIndices.add(i);
			}
		}
		// Choose amongs the min indices
		//System.out.println("minIndices=" + minIndices);
		int numTies = minIndices.size();
		if (numTies > 0) {
			j = minIndices.get( random.nextInt(numTies) );
		}
		return j;
	}
	
	// returns the position index of the complement of Wlistj in Zlist 
	public static int find_complement(int j,int WList1[][],int ZList1[][], int dimM){
		int l = -1;
		int temp1,temp2;
	
		temp1 = WList1[j][0] ;
		temp1 = 3-temp1;   /* (w,a)-> (z,a)  (z,a) ->(w,a) */
		temp2 = WList1[j][1] ;
	
		for (int i=0;i<dimM;i++){
			if (ZList1[i][0]==temp1&&ZList1[i][1]==temp2){
				l = i;
				break;
			}
		}
	
		return l;
	}
	
	
	public static void QMprint(double M[][],int row,int col){
	//	for (int i=0;i<row;i++){
	//		for (int j=0;j<col;j++){
	//			System.out.print(M[i][j]+" ");
	//		}
	//		//	    System.out.print("     " +Q[i]);
	//		System.out.print("\n");
	//	}
	}
	
	public static void QMprint(int M[][],int row,int col){
	//	for (int i=0;i<row;i++){
	//		for (int j=0;j<col;j++){
	//			System.out.print(M[i][j]+" ");
	//		}
	//		//	    System.out.print("     " +Q[i]);
	//		System.out.print("\n");
	//	}
	}
	
	
	public static void normalize(double LZ1[][],int k, int row, int dimM) {
		double sum1=0 ,sum2 = 0;
		for (int i=0;i<row;i++)
			sum1 = sum1+ Math.abs(LZ1[k][i]);
		for (int i=row;i<dimM;i++)
			sum2 = sum2+ Math.abs(LZ1[k][i]);
		for (int i=0;i<dimM;i++){
			if (i<row) LZ1[k][i]= Math.abs(LZ1[k][i])/sum1;
			else  LZ1[k][i]= Math.abs(LZ1[k][i])/sum2;
		}
	}
	
	
}
