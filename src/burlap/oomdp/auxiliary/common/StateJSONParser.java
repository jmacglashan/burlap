package burlap.oomdp.auxiliary.common;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;






//import org.yaml.snakeyaml.Yaml;
//import com.fasterxml.jackson.core.json.JsonParser;
import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Attribute.AttributeType;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


/**
 * A StateParser class that uses the JSON file format and can can convert states to JSON strings (and back from them) for any possible input domain. Use of
 * this class requires the Jackson java library
 * @author James MacGlashan, Stephen Brawner
 *
 */
public class StateJSONParser implements StateParser {

	/**
	 * The domain holding the object class definitions that states represented in JSON strings will be converted to.
	 * This is not necessary if parsing will only go from states to strings and not strings to states.
	 */
	protected Domain		domain;
	
	/**
	 * Initializes with a given domain object.
	 * The domain object is used for converting from strings to states. Specifically, the object instances will be assigned to the object class
	 * in the domain object with the given name in the state string.
	 * @param domain the domain object to which state objects will be associated.
	 */
	public StateJSONParser(Domain domain){
		this.domain = domain;
	}
	
	
	/**
	 * Returns the data structure of a state that is passed to JSON.
	 * @param s the input state to turn into a JSON prepared datastructure
	 * @return JSON prepared data structure representation of the state
	 */
	public List<Map<String, Object>> getJSONPrepared(State s){
		List<Map<String, Object>> jsonData = new ArrayList<Map<String,Object>>();
		for(ObjectInstance o : s.getAllObjects()){
			Map<String, Object> objectData = new HashMap<String, Object>();
			objectData.put("name", o.getName());
			objectData.put("class", o.getObjectClass().name);
			for(Attribute a : o.getObjectClass().attributeList){
				if(a.type == AttributeType.BOOLEAN){
					Boolean bval = new Boolean(o.getDiscValForAttribute(a.name) == 1);
					objectData.put(a.name, bval);
				}
				else if(a.type == AttributeType.DISC || a.type == AttributeType.INT){
					objectData.put(a.name, o.getDiscValForAttribute(a.name));
				}
				else if(a.type == AttributeType.REAL || a.type == AttributeType.REALUNBOUND){
					objectData.put(a.name, o.getRealValForAttribute(a.name));
				}
				else if(a.type == AttributeType.RELATIONAL){
					objectData.put(a.name, o.getStringValForAttribute(a.name));
				}
				else if(a.type == AttributeType.MULTITARGETRELATIONAL){
					objectData.put(a.name, o.getAllRelationalTargets(a.name));
				}
			}
			jsonData.add(objectData);
		}
		return jsonData;
	}
	
	@Override
	public String stateToString(State s) {
		JsonFactory jsonFactory = new JsonFactory();
		StringWriter writer = new StringWriter();
		JsonGenerator jsonGenerator;
		ObjectMapper objectMapper = new ObjectMapper();
		
		List<Map<String, Object>> data = this.getJSONPrepared(s);
		try {
			jsonGenerator = jsonFactory.createGenerator(writer);
			objectMapper.writeValue(jsonGenerator, data);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return writer.toString();
	}

	@Override
	public State stringToState(String str) {
		
		State s = new State();
		JsonFactory jsonFactory = new JsonFactory();
		List<Map<String, Object>> objects = new ArrayList<Map<String, Object>>();
		try {
			ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
			TypeReference<List<Map<String, Object>>> listTypeRef = 
					new TypeReference<List<Map<String, Object>>>() {};
			objects = objectMapper.readValue(str, listTypeRef);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		for(Map<String, Object> oMap : objects){
			String obName = (String)oMap.get("name");
			String className = (String)oMap.get("class");
			ObjectInstance ob = new ObjectInstance(this.domain.getObjectClass(className), obName);
			for(Attribute a : this.domain.getObjectClass(className).attributeList){
				if(a.type == AttributeType.BOOLEAN){
					Boolean bval = (Boolean)oMap.get(a.name);
					if(bval){
						ob.setValue(a.name, 1);
					}
					else{
						ob.setValue(a.name, 0);
					}
				}
				else if(a.type == AttributeType.DISC || a.type == AttributeType.INT){
					ob.setValue(a.name, (Integer)oMap.get(a.name));
				}
				else if(a.type == AttributeType.REAL || a.type == AttributeType.REALUNBOUND){
					ob.setValue(a.name, (Double)oMap.get(a.name));
				}
				else if(a.type == AttributeType.RELATIONAL){
					ob.setValue(a.name, (String)oMap.get(a.name));
				}
				else if(a.type == AttributeType.MULTITARGETRELATIONAL){
					Set<?> rset = (Set<?>)oMap.get(a.name);
					for(Object rtarget : rset){
						ob.addRelationalTarget(a.name, (String)rtarget);
					}
				}
			}
			s.addObject(ob);
		}
		
		return s;
	}

}
