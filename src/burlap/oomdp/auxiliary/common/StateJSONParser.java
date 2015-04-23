package burlap.oomdp.auxiliary.common;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.auxiliary.StateParser;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Attribute.AttributeType;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.objects.MutableObjectInstance;
import burlap.oomdp.core.objects.ObjectInstance;
import burlap.oomdp.core.states.MutableState;
import burlap.oomdp.core.states.State;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;



/**
 * A StateParser class that uses the JSON file format and can can convert states to JSON strings (and back from them) for any possible input domain. Use of
 * this class requires the Jackson java library. This class also provides methods for preparing an OO-MDP {@link State} into a datastructure that can be easily
 * parsed into an JSON representation and for taking a datastructure preapred for a JSON representation and converting it into an OO-MDP {@link State}.
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
				else if(a.type == AttributeType.STRING || a.type == AttributeType.INTARRAY || a.type == AttributeType.DOUBLEARRAY){
					objectData.put(a.name, o.getStringValForAttribute(a.name));
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
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return writer.toString();
	}

	@Override
	public State stringToState(String str) {
		
		JsonFactory jsonFactory = new JsonFactory();
		List<Map<String, Object>> objects = new ArrayList<Map<String, Object>>();
		try {
			ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
			TypeReference<List<Map<String, Object>>> listTypeRef = 
					new TypeReference<List<Map<String, Object>>>() {};
			objects = objectMapper.readValue(str, listTypeRef);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return this.JSONPreparedToState(objects);
	}
	
	
	/**
	 * Takes a JSON prepared datastructure representation of a state and turns it into an actual state object. The JSON
	 * prepared version is a list of maps. Each map represents an object instance which stores the objects name, name
	 * of the object's class, and the value for each attribute.
	 * @param objects the list of OO-MDP object instances
	 * @return and OO-MDP {@link State} object.
	 */
	public State JSONPreparedToState(List<Map<String, Object>> objects){
		
		State s = new MutableState();
		
		for(Map<String, Object> oMap : objects){
			String obName = (String)oMap.get("name");
			String className = (String)oMap.get("class");
			ObjectInstance ob = new MutableObjectInstance(this.domain.getObjectClass(className), obName);
			for(Attribute a : this.domain.getObjectClass(className).attributeList){

				Object mapVal = oMap.get(a.name);
				if(mapVal instanceof Boolean){
					boolean bval = (Boolean)oMap.get(a.name);
					if(bval){
						ob.setValue(a.name, 1);
					}
					else{
						ob.setValue(a.name, 0);
					}

				}
				else if(mapVal instanceof Integer){
					ob.setValue(a.name, (Integer)mapVal);
				}
				else if(mapVal instanceof Double){
					ob.setValue(a.name, (Double)mapVal);
				}
				else if(mapVal instanceof String){
					ob.setValue(a.name, (String)mapVal);
				}

			}
			s.addObject(ob);
		}
		
		return s;
	}

}
