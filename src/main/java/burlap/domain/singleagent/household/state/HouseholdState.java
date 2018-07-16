package Household.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import household.Household;

import java.util.*;

public class HouseholdState implements MutableOOState {

    private static final int DEFAULT_MIN_X = 0;
    private static final int DEFAULT_MIN_Y = 0;

    private int width;
    private int height;
    private HouseholdAgent agent;
    private Map<String, HouseholdPerson> people;
    private Map<String, HouseholdRoom> rooms;
    private Map<String, HouseholdDoor> doors;

    public HouseholdState(int width,
			  int height,
			  HouseholdAgent agent,
			  List<HouseholdPerson> people,
			  List<HouseholdRoom> rooms,
			  List<HouseholdDoors> doors) {
	this.width = width;
	this.height = height;
	this.agent = agent;
	this.people = new HashMap<String, HouseholdPerson>();
	for(HouseholdPerson p : people) {
	    this.people.put(p.name(), p);
	}

	this.rooms = new HashMap<String, HouseholdRoom>();
	for(HouseholdRoom r : rooms) {
	    this.rooms.put(r.name(), r);
	}

	this.doors = new Hashmap<String, HouseholdDoor>();
	for(HouseholdDoor d : doors) {
	    this.doors.put(d.name(), d);
	}
    }

    public HouseholdState(HouseholdAgent a,
			  Map<String, HouseholdPerson> people,
			  Map<String, HouseholdRoom> rooms,
			  Map<String, HouseholdDoor> doors) {
	this.agent = a;
	this.people = people;
	this.rooms = rooms;
	this.doors = doors;
    }

    @Override
    public MutableOOState addObject(ObjectInstance obInst) {
	if(obInst instanceof HouseholdAgent ||
	   obInst.className().equals(Household.CLASS_AGENT)) {
	    touchAgent();
	    agent = (HouseholdAgent) obInst;
	} else if(obInst instanceof HouseholdPerson ||
		  obInst.className().equals(Household.CLASS_PERSON)) {
	    touchPeople().put(obInst.name(), (HouseholdPerson) obInst);
        } else if(obInst instanceof HouseholdRoom ||
		  obInst.className().equals(Household.CLASS_ROOM)) {
	    touchRooms().put(obInst.name(), (HouseholdRoom) obInst);
	} else if(obInst instanceof HouseholdDoor ||
		  obInst.className().equals(Household.CLASS_DOOR)) {
	    touchDoors().put(obInst.name(), (HouseholdDoor) obInst);
	} else {
	    throw new RuntimeException("Can only add certain objects.");
	}
	return this;
    }

    @Override
    public MutableOOState removeObject(String name) {
	throw new RuntimeException("Remove not implemented");
    }

    @Override
    public MutableOOState renameObject(String currentName, String newName) {
	throw new RuntimeException("Rename not implemented");
    }
	
    @Override
    public int numObjects() {
	return 1 + people.size() + rooms.size() + doors.size();
    }

    @Override
    public ObjectInstance object(String name) {
	if(agent.name().equals(name)) {
	    return agent;
	}

	ObjectInstance o = people.get(name);
	if(o != null) {
	    return o;
	}

	o = rooms.get(name);
	if(o != null) {
	    return o;
	}

	o = doors.get(name);
	if(o != null) {
	    return o;
	}

	return null;
    }

    @Override
    public List<ObjectInstance> objects() {
	List<ObjectInstance> obs = newArrayList<ObjectInstance>();
	objs.add(agent);
	objs.addAll(people.values());
	objs.addAll(rooms.values());
	objs.addAll(doors.values());
	return objs;
    }

    @Override
    public List<ObjectInstance> objectsOfClass(String className) {
	if(className.equals(Household.CLASS_AGENT)) {
	    return Arrays.asList(agent);
	} else if(className.equals(Household.CLASS_PERSON)) {
	    return new ArrayList<ObjectInstance>(people.values());
	} else if(className.equals(Household.CLASS_ROOM)) {
	    return new ArrayList<ObjectInstance>(rooms.values());
	} else if(className.equals(Household.CLASS_DOOR)) {
	    return new ArrayList<ObjectInstance>(doors.values());
	}
	throw new RuntimeException("No object class " + className);
    }
	
	
    
    public HouseholdAgent getAgent() {
	return agent;
    }

    public void setAgent(HouseholdAgent agent) {
	this.agent = agent;
    }

    public Map<String, HouseholdPerson> getPeople() {
	return people;
    }
    
    public void setPeople(Map<String, HouseholdPerson> people) {
	this.people = people;
    }

    public Map<String, HouseholdRoom> getRooms() {
	return rooms;
    }
    
    public void setRooms(Map<String, HouseholdRoom> rooms) {
	this.rooms = rooms;
    }

    public Map<String, HouseholdDoor> getDoors() {
	return doors;
    }

    public void setDoors(Map<String, HouseholdDoor> doors) {
	this.doors = doors;
    }

}
