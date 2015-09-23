package burlap.parallel;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Concurrent {
	private static final int numCores = Runtime.getRuntime().availableProcessors();
	private static ExecutorService executor;
	
	private static void start() {
		if (Concurrent.executor == null || Concurrent.executor.isShutdown()) {
			Concurrent.executor = Executors.newFixedThreadPool(numCores);
		}
	}
	
	public static <T> Future<T> run(Callable<T> callable) {
		Concurrent.start();
		return Concurrent.executor.submit(callable);
	}

	public static void shutdown() {
		Concurrent.executor.shutdown();try {
			if (!Concurrent.executor.awaitTermination(1L, TimeUnit.SECONDS)) {
				Concurrent.executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			Concurrent.executor.shutdownNow();
		}
	}
}
