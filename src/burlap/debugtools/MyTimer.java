package burlap.debugtools;


/**
 * A data structure for keeping track of elapsed and average time. Use {@link #start()} to start the timer
 * (or set it to start in the constructor {@link #MyTimer(boolean)}. Use {@link #peekAtTime()} to check
 * the current elapsed time since it was started. Use {@link #stop()} to stop the timer and
 * {@link #getTime()} to get the elapsed time between the last time the timer was started and stopped.
 * If you start and stop the timer multiple times, you can get the average time for all start-stops
 * using the {@link #getAvgTime()} method and the total time of all start-stops with {@link #getTotalTime()}.
 * If you want to check if the timer is running, use {@link #isRunning()}.
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
	 * Creates a new timer and starts it if start=true.
	 * @param start if true, then start the timer; if false then don't start the timer.
	 */
	public MyTimer(boolean start){

		timing = false;
		numTimers = 0;
		sumTime = 0;

		this.start();

	}
	
	/**
	 * Starts the timer if it is not running.
	 * @return returns true if the timer is started; false if it is already running and cannot be started.
	 */
	public boolean start(){

		if(!timing) {
			startTime = System.currentTimeMillis();
			timing = true;
			return true;
		}
		return false;
	
	}
	
	
	/**
	 * Stops the timer. Has no effect is the timer has not been started.
	 * @return returns true if stops the timer; false if the timer is not running and cannot be stopped.
	 */
	public boolean stop(){
	
		if(timing){
			timing = false;
			stopTime = System.currentTimeMillis();
			long diff = stopTime - startTime;
			sumTime += diff;
			numTimers++;
			return true;
		}

		return false;
	
	}


	/**
	 * Indicates whether this timer is currently running.
	 * @return true if the timer is running; false if it is not.
	 */
	public boolean isRunning(){
		return this.timing;
	}
	
	
	/**
	 * Returns the elapsed time in seconds since the last start-stop calls.
	 * The returned value is not well defined if the timer has not been started
	 * and stopped at least once.
	 * If you want the elapsed time while it's running, use {@link #peekAtTime()}.
	 * @return the elapsed time in seconds since the last start-stop calls.
	 */
	public double getTime(){
	
		long diff = stopTime - startTime;
		double timeInSeconds = (double)diff / 1000.0;
		
		return timeInSeconds;
	
	}

	/**
	 * Returns the current elapsed time since the timer was started. Returns 0 if the timer is not running.
	 * @return The current elapsed time since the timer was started or 0 if the timer is not running.
	 */
	public double peekAtTime(){

		if(!timing){
			return 0.;
		}

		long diff = System.currentTimeMillis() - startTime;
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
