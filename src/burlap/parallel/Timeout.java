package burlap.parallel;

public class Timeout {
	public Timeout(Stoppable stoppable, double timeout) {
		
	}
	
	public abstract class Stoppable {
		abstract void start();
		abstract void stop();
	}

}
