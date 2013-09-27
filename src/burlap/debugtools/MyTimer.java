package burlap.debugtools;

public class MyTimer{


	private long startTime;
	private long stopTime;
	
	private long sumTime;
	private int numTimers;
	
	boolean timing;
	
	
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
	
	public MyTimer(){
	
		timing = false;
		numTimers = 0;
		sumTime = 0;
	
	}
	
	
	public void start(){
	
		startTime = System.currentTimeMillis();
		timing = true;
	
	}
	
	public void stop(){
	
		if(timing){
			timing = false;
			stopTime = System.currentTimeMillis();
			long diff = stopTime - startTime;
			sumTime += diff;
			numTimers++;
		}
	
	}
	
	public double getTime(){
	
		long diff = stopTime - startTime;
		double timeInSeconds = (double)diff / 1000.0;
		
		return timeInSeconds;
	
	}
	
	public double getAvgTime(){
	
		return ((double)sumTime / (double) numTimers) / 1000.0;
	
	}
	
	public double getTotalTime(){
		return (double)sumTime / 1000.0;
	}
	
	public void resetAvgs(){
		sumTime = 0;
		numTimers = 0;
	}




}
