package burlap.behavior.singleagent.learning.experiencereplay;

import burlap.debugtools.RandomFactory;
import burlap.oomdp.singleagent.environment.EnvironmentOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author James MacGlashan.
 */
public class FixedSizeMemory implements ExperiencesMemory {

	protected int next = 0;
	protected EnvironmentOutcome [] memory;
	protected int size = 0;

	protected boolean alwaysIncludeMostRecent;


	public FixedSizeMemory(int size) {
		this(size, false);
	}

	public FixedSizeMemory(int size, boolean alwaysIncludeMostRecent) {
		if(size < 1){
			throw new RuntimeException("FixedSizeMemory requires memory size > 0; was request size of " + size);
		}
		this.alwaysIncludeMostRecent = alwaysIncludeMostRecent;
		this.memory = new EnvironmentOutcome[size];
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
