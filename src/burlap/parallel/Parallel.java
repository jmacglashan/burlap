package burlap.parallel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/// Common parallel methods, including for and for each.
public class Parallel {
	
	// The number of available processors used for parallel execution
	private static final int numCores = Runtime.getRuntime().availableProcessors();
	public static final double NO_TIME_LIMIT = -1.0;
	private static final TimeUnit timeUnit = TimeUnit.MICROSECONDS;
	// Initializes the parallel executor. If you want to save a small amonut of overhead, 
	// and reuse the same executor, create an instantiation first.
	private final ExecutorService executor;
	public Parallel() {
		this.executor = Executors.newFixedThreadPool(numCores);
	}
	
	// Static for method. Creates a copy of the callable, setting the for parameters for that
	// particular instance copy.
	public static <T> List<T> For(int start, int end, int increment, ForCallable<T> runnable) {
		Parallel instance = new Parallel();
		List<T> result = instance.parallelFor(start, end, increment, runnable);
		instance.shutdown();
		return result;
	}
	
	// Non static version of the for method.
	public <T> List<T> parallelFor(int start, int end, int increment, ForCallable<T> runnable) {
		double width = end - start;
		int size = (int)(width / increment + 1); 
		List<Callable<T>> callables = new ArrayList<Callable<T>>(size);
		
		for (int i = start; i < end; i += increment) {
			ForCallable<T> copied = runnable.copy(start, i, end, increment);
			callables.add(copied);
		}
		List<T> result = new ArrayList<T>(size);
		try {
			List<Future<T>> futures = executor.invokeAll(callables);
			for (Future<T> future : futures) {
				result.add(future.get());
			}
		} catch (InterruptedException | ExecutionException e) {}
		return result;
	}
	
	// Static for each method. 
	public static <I, R> List<R> ForEach(Collection<I> collection, ForEachCallable<I, R> runnable) {
		return Parallel.ForEach(collection, runnable, NO_TIME_LIMIT);
	}
	
	public static <I, R> List<R> ForEach(Collection<I> collection, ForEachCallable<I, R> runnable, double timeout) {
		Parallel instance = new Parallel();
		List<R> result = instance.parallelForEach(collection, runnable, timeout);
		instance.shutdown();
		return result;
	}
	
	// Member version of the for each method. It takes a collection of class 'I' (for item), 
	// and calls the callable for each item in the collection. The result is a list of the result type 'R'. 
	public <I, R> List<R> parallelForEach(Collection<I> collection, ForEachCallable<I, R> runnable) {
		return this.parallelForEach(collection, runnable, NO_TIME_LIMIT);
	}
	
	public <I, R> List<R> parallelForEach(Collection<I> collection, ForEachCallable<I, R> runnable, double timeout) {
		
		List<Callable<R>> callables = new ArrayList<Callable<R>>(collection.size());
		long timeOutLong = TimeUnit.NANOSECONDS.convert((long)timeout, TimeUnit.SECONDS);
		
		for (I item : collection) {
			ForEachCallable<I,R> copied = runnable.copy(item);
			callables.add(copied);
		}
		
		List<R> result = new ArrayList<R>(collection.size());
		try {
			
			List<Future<R>> futures = new ArrayList<Future<R>>();
			long start = System.nanoTime();
			for (Callable<R> callable : callables) {
				futures.add(executor.submit(callable));
			}
			for (Future<R> future : futures) {
				R res = null;
				try {
					if (timeout == Parallel.NO_TIME_LIMIT) {
						res = future.get();
					} else {
						long end = System.nanoTime();
						long timeToWait = timeOutLong - (end - start);
						if (timeToWait < 0) {
							timeToWait = 0L;
						}
						res = (timeout == Parallel.NO_TIME_LIMIT) ? future.get() : future.get(timeToWait, TimeUnit.NANOSECONDS);
					}
					
				} catch (TimeoutException e) { 
					System.err.println("Timeout");
				}
				result.add(res);
			}
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	// Required to clear the threads from the system. 
	public static void shutdown(ExecutorService executor) {
		executor.shutdown();
		try {
			if (!executor.awaitTermination(1L, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
		}
	}
	
	// Required to clear the threads from the system Do this if you create an instantiation when you're finished.
	// The executor cannot be restarted after shutdown, so you will have to create a new parallel instantiation
	public void shutdown() {
		Parallel.shutdown(this.executor);
	}
	
	// An abstract class for parallel for calls.
	public static abstract class ForCallable<T> implements Callable<T>{
		private int start;
		private int current;
		private int increment;
		private int end;
		
		public ForCallable(int start, int end, int increment) {
			this.start = start;
			this.end = end;
			this.increment = increment;
		}
		
		public abstract T perform(int start, int current, int end, int increment);

		public T call() {
			return this.perform(start, current, end, increment);
		}
		
		public abstract ForCallable<T> copy();
		// Init creates a copy of this object, with for parameters (if necessary).
		
		public ForCallable<T> copy(int start, int current, int end, int increment) {
			ForCallable<T> copy = this.copy();
			copy.start = start;
			copy.end = end;
			copy.increment = increment;
			copy.current = current;
			return copy;
		}
	}
	
	// An abstract class for parallel for each calls. I is the type of input, R is the type of result
	public static abstract class ForEachCallable<I, R> implements Callable<R>{
		private I current;
		public R call() {
			return this.perform(this.current);
		}
		
		public abstract R perform(I current);
		
		// This method should handle any copies to ensure thread safety than need to be performed
		public abstract ForEachCallable<I, R> copy();
		
		// Copies and sets the specific item
		public ForEachCallable<I, R> copy(I current) {
			ForEachCallable<I, R> copy = this.copy();
			copy.current = current;
			return copy;
		}
	}
	
}
