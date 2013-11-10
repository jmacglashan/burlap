package burlap.debugtools;


/**
 * A data structure for keeping track of elapsed and average time.
 * @author James MacGlashan
 *
 */
public class MyTimer{


	private long startTime;
	private long stopTime;
	
	private long sumTime;
	private int numTimers;
	
	private boolean timing;
	
	/**
	 * Demo of usage
	 * @param args
	 */
	public static void main(String [] args){
	
	
		MyTimer t = new MyTimer();
		for(int i = 0; i < 10; i++){
			t.start();
			try{
				Thread.sleep(1000);
			}catch (Exception e){
			
			}
			t.stop();
		}
		System.out.println("Time: " + t.getAvgTime());
	
	}
	
	
	/**
	 * Creates a new timer. Timing will not yet be started.
	 */
	public MyTimer(){
	
		timing = false;
		numTimers = 0;
		sumTime = 0;
	
	}
	
	/**
	 * Starts the timer.
	 */
	public void start(){
	
		startTime = System.currentTimeMillis();
		timing = true;
	
	}
	
	
	/**
	 * Stops the timer.
	 */
	public void stop(){
	
		if(timing){
			timing = false;
			stopTime = System.currentTimeMillis();
			long diff = stopTime - startTime;
			sumTime += diff;
			numTimers++;
		}
	
	}
	
	
	/**
	 * Returns the elapsed time in seconds since the last start-stop calls.
	 * @return the elapsed time in seconds since the last start-stop calls.
	 */
	public double getTime(){
	
		long diff = stopTime - startTime;
		double timeInSeconds = (double)diff / 1000.0;
		
		return timeInSeconds;
	
	}
	
	/**
	 * Returns the average time in seconds recorded over all start-stop calls.
	 * @return the average time in seconds recorded over all start-stop calls.
	 */
	public double getAvgTime(){
	
		return ((double)sumTime / (double) numTimers) / 1000.0;
	
	}
	
	/**
	 * Returns the total time in seconds recorded over all start-stop calls.
	 * @return the total time in seconds recorded over all start-stop calls.
	 */
	public double getTotalTime(){
		return (double)sumTime / 1000.0;
	}
	
	
	/**
	 * Resets to zero the average and total time recorded over all start-stop calls.
	 */
	public void resetAvgs(){
		sumTime = 0;
		numTimers = 0;
	}




}
