package burlap.behavior.singleagent.learning.experiencereplay;

import burlap.debugtools.RandomFactory;
import burlap.mdp.singleagent.environment.EnvironmentOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A straightforward implementation of a fixed size memory. When this memory already holds the fixed size of experiences
 * and a new experience is added to it, it deletes the oldest memory to make room. The sample method
 * will return a uniform random selection of memories with replacement, unless the number of stored memories is less than the requested number,
 * in which case it will simply return all of the memories that it stores. Optionally, this memory may be set
 * so that the the {@link #sampleExperiences(int)} method always includes the most recent memory, which is useful
 * when experience replay is used to augment more standard Q-learning approaches.
 * @author James MacGlashan.
 */
public class FixedSizeMemory implements ExperienceMemory {

	protected int next = 0;
	protected EnvironmentOutcome[] memory;
	protected int size = 0;

	protected boolean alwaysIncludeMostRecent;


	/**
	 * Initializes with the size of the memory. The sampling method is *not* guaranteed to always include the most
	 * recent memory, and is a uniform random sampling from the stored memories.
	 * @param size the number of experiences to store
	 */
	public FixedSizeMemory(int size) {
		this(size, false);
	}

	/**
	 * Initializes with the size of the memory and whether the most recent memory should always be included
	 * in the returned results from the sampling memory.
	 * @param size the number of experiences to store
	 * @param alwaysIncludeMostRecent if true, then the result of the {@link #sampleExperiences(int)}} will always include the most recent experience and is a uniform random sampling for the n-1 samples.
	 *                                   If false, then it is a pure random sample with replacement.
	 */
	public FixedSizeMemory(int size, boolean alwaysIncludeMostRecent) {
		if(size < 1){
			throw new RuntimeException("FixedSizeMemory requires memory size > 0; was request size of " + size);
		}
		this.alwaysIncludeMostRecent = alwaysIncludeMostRecent;
		this.memory = new EnvironmentOutcome[size];
	}

	/**
	 * If true, then the result of the {@link #sampleExperiences(int)}} will always include the most recent experience
	 * and is a uniform random sampling for the n-1 samples. If false, then it is a pure random sample with replacement.
	 * @return true or false
	 */
	public boolean alwaysIncludeMostRecent() {
		return alwaysIncludeMostRecent;
	}

	/**
	 * Sets whether to always include the most recent experience in the {@link #sampleExperiences(int)} method.
	 * @param alwaysIncludeMostRecent If true, then the result of the {@link #sampleExperiences(int)}} will always include the most recent experience
	 *                                   and is a uniform random sampling for the n-1 samples. If false, then it is a pure random sample with replacement.
	 */
	public void setAlwaysIncludeMostRecent(boolean alwaysIncludeMostRecent) {
		this.alwaysIncludeMostRecent = alwaysIncludeMostRecent;
	}

	@Override
	public void addExperience(EnvironmentOutcome eo) {
		memory[next] = eo;
		next = (next+1) % memory.length;
		size = Math.min(size+1, memory.length);
	}

	@Override
	public List<EnvironmentOutcome> sampleExperiences(int n) {

		List<EnvironmentOutcome> samples;

		if(this.size == 0){
			return new ArrayList<EnvironmentOutcome>();
		}

		if(this.alwaysIncludeMostRecent){
			n--;
		}

		if(this.size < n){
			samples = new ArrayList<EnvironmentOutcome>(this.size);
			for(int i = 0; i < this.size; i++){
				EnvironmentOutcome eo = this.memory[i];
				samples.add(eo);
			}
			return samples;
		}
		else{
			samples = new ArrayList<EnvironmentOutcome>(Math.max(n, 1));
			Random r = RandomFactory.getMapped(0);
			for(int i = 0; i < n; i++) {
				int sind = r.nextInt(this.size);
				EnvironmentOutcome eo = this.memory[sind];
				samples.add(eo);
			}
		}
		if(this.alwaysIncludeMostRecent){
			EnvironmentOutcome eo;
			if(next > 0) {
				eo = this.memory[next - 1];
			}
			else if(size > 0){
				eo = this.memory[this.memory.length-1];
			}
			else{
				throw new RuntimeException("FixedSizeMemory getting most recent fails because memory is size 0.");
			}
			samples.add(eo);
		}

		return samples;
	}

	@Override
	public void resetMemory() {
		this.size = 0;
		this.next = 0;
	}


}
