package Household.state;

import burlap.mdp.core.oo.state.MutableOOState;
import burlap.mdp.core.oo.state.OOStateUtilities;
import burlap.mdp.core.oo.state.OOVariableKey;
import burlap.mdp.core.oo.state.ObjectInstance;
import burlap.mdp.core.state.MutableState;
import household.Household;

import java.util.*;

public class HouseholdState implements MutableOOState {

    private HouseholdAgent agent;
    private Map<String, HouseholdPerson> people;
    private Map<String, HouseholdRoom> rooms;
    private Map<String, HouseholdDoor> doors;

    public HouseholdState(HouseholdAgent agent, List<HouseholdPerson> people,
			  List<HouseholdRoom> rooms, List<HouseholdDoors> doors) {
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

    public HouseholdState(HouseholdAgent a, Map<String, HouseholdPerson> people,
			  Map<String, HouseholdRoom> rooms, Map<String, HouseholdDoor> doors) {
	this.agent = a;
	this.people = people;
	this.rooms = rooms;
	this.doors = doors;
    }

    @Override
    public int numObjects() {
	return 1 + people.size() + rooms.size() + doors.size();
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
