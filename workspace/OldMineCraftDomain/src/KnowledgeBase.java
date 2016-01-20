package burlap.domain.singleagent.minecraft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import burlap.oomdp.core.Domain;

/**
 * The knowledge-base is a wrapper around a collection of objects of type T that are used
 * to encoode knowledge in the minecraft world (ie. affordances, subgoals, etc...)
 * @author dabel
 *
 * @param <T> The type of knowledge (affordance, subgoal, etc...)
 */
public class KnowledgeBase<T> {
	private List<T>				kb;
	private Class<T>			kbclass;
	private String				kbName;
	private String				basePath = System.getProperty("user.dir") + "/kb";
	
	
	public KnowledgeBase(Class<T> kbtype) {
		this.kb = new ArrayList<T>();
		this.kbclass = kbtype;
		this.init();
	}

	public KnowledgeBase(Class<T> kbtype, List<T> kb) {
		this.kb = new ArrayList<T>(kb);
		this.kbclass = kbtype;
		this.init();
	}
	
	private void init() {
		this.kbName = this.kbclass.getSimpleName().toLowerCase();
	}
	
	public void add(T t) {
		this.kb.add(t);
	}
	
	public List<T> getAll() {
		return this.kb;
	}
	
	public void save(String filename) {
		String fpath = basePath + "/" + kbName + "/" + filename;
		
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fpath)));
			
			for (T t: this.kb) {
				bw.write(t.toString());
			}
			
			bw.close();
		} catch (IOException e) {
			System.out.println("ERROR");
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void load(Domain d, String filename) {
		T t = null;
		try {
			Scanner scnr = new Scanner(new File(basePath + "/" + kbName + "/" + filename));
			while (scnr.hasNextLine()) {

				if (this.kbclass == Affordance.class) {
					t = (T) Affordance.load(d, scnr);
				}
				
				this.kb.add(t);

			}

			scnr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void process() {
		for(T t : kb) {
			if (this.kbclass == Affordance.class) {
				((Affordance) t).postProcess();
			}
		}
	}

}
