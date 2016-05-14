package burlap.shell.command.world;

import burlap.mdp.core.state.State;
import burlap.mdp.stochasticgames.JointAction;
import burlap.mdp.stochasticgames.SGAgent;
import burlap.mdp.stochasticgames.SGAgentType;
import burlap.mdp.stochasticgames.agentactions.SGAgentAction;
import burlap.mdp.stochasticgames.agentactions.SGAgentActionType;
import burlap.shell.BurlapShell;
import burlap.shell.SGWorldShell;
import burlap.shell.command.ShellCommand;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.PrintStream;
import java.util.*;

/**
 * A controller for a set of {@link burlap.shell.command.ShellCommand} objects. These commands including
 * registering manually controlled agents with a {@link burlap.mdp.stochasticgames.World}
 * that can play games in the world. Listing
 * the manual agents. Setting the action selections of manual agents, and listing the currently selected
 * actions of the manual agents. Use the -h option for help information.
 * @author James MacGlashan.
 */
public class ManualAgentsCommands {


	protected Map<String, ManualSGAgent> manualAgents = new HashMap<String, ManualSGAgent>();

	protected RegisterAgentCommand regCommand = new RegisterAgentCommand();
	protected ListManualAgents lsAgents = new ListManualAgents();
	protected SetAgentAction setAction = new SetAgentAction();
	protected LSManualAgentActionsCommands lsActions = new LSManualAgentActionsCommands();

	public void setManualAgents(Map<String, ManualSGAgent> agents){
		this.manualAgents = agents;
	}

	public Map<String, ManualSGAgent> getManualAgents() {
		return manualAgents;
	}

	public ManualSGAgent getManualAgent(String name){
		return this.manualAgents.get(name);
	}

	public void setManualAgent(String name, ManualSGAgent agent){
		this.manualAgents.put(name, agent);
	}

	public RegisterAgentCommand getRegCommand() {
		return regCommand;
	}

	public ListManualAgents getLsAgents() {
		return lsAgents;
	}

	public SetAgentAction getSetAction() {
		return setAction;
	}

	public LSManualAgentActionsCommands getLsActions() {
		return lsActions;
	}

	public class RegisterAgentCommand implements ShellCommand{


		protected OptionParser parser = new OptionParser("r:h*");


		@Override
		public String commandName() {
			return "reg";
		}

		@Override
		public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

			int times = 1;
			OptionSet oset = this.parser.parse(argString.split(" "));
			List<String> args = (List<String>)oset.nonOptionArguments();


			if(oset.has("h")){
				os.println("[-r times] objectClass [actionName*]\n" +
						"Creates an agent that can be controlled by the shell, and has it join the world. objectClass" +
						"is the name of the OO-MDP object class with which the agent is associated. It is followed" +
						"by an optional list of names of SGAgentAction that define the actions that agent can take." +
						"If this list of action names is not provided, it will be assumed that all SGAgentActions defined in the" +
						"world's corresponding domain will selectable by the agent.\n\n" +
						"-r times: creates 'times' different agents that joint the world.");
				return 0;
			}


			if(oset.has("r")){
				String sval = (String)oset.valueOf("r");
				times = Integer.parseInt(sval);
				if(times <= 0){
					os.println("Cannot create " + times + " number of manual agents. Must be positive value.");
					return 0;
				}
			}

			String aclass = args.get(0);
			List<String> actionNames = args.subList(1, args.size());
			List<SGAgentActionType> actions = new ArrayList<SGAgentActionType>();

			if(actionNames.isEmpty()){
				actions = shell.getDomain().getAgentActions();
			}
			else{
				for(String aname : actionNames){
					SGAgentActionType action = shell.getDomain().getSGAgentAction(aname);
					if(action != null){
						actions.add(action);
					}
				}
			}

			SGAgentType type = new SGAgentType("manual", actions);

			for(int i = 0; i < times; i++){
				ManualSGAgent agent = new ManualSGAgent();
				agent.joinWorld(((SGWorldShell)shell).getWorld(), type);
				manualAgents.put(agent.getAgentName(), agent);
				os.println("Created manual agent named: " + agent.getAgentName());
			}




			return 0;
		}
	}


	public class ListManualAgents implements ShellCommand{


		protected OptionParser parser = new OptionParser("h*");

		@Override
		public String commandName() {
			return "lsAgents";
		}

		@Override
		public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

			OptionSet oset = this.parser.parse(argString.split(" "));

			if(oset.has("h")){
				os.println("Prints the names of manual agents that have been created.");
				return 0;
			}

			for(String agentName : manualAgents.keySet()){
				os.println(agentName);
			}

			return 0;
		}
	}


	public class SetAgentAction implements ShellCommand{

		protected OptionParser parser = new OptionParser("h*");

		@Override
		public String commandName() {
			return "sa";
		}

		@Override
		public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

			OptionSet oset = this.parser.parse(argString.split(" "));
			if(oset.has("h")){
				os.println("agentName actionName [actionParam*]\n" +
						"Sets the action for manual agent named agentName to the action with the name actionName. If the action" +
						"is a parameterized action, then the parameters must also be specified.");
				return 0;
			}

			List<String> args = (List<String>)oset.nonOptionArguments();

			if(args.size() < 2){
				return -1;
			}

			String agentName = args.get(0);

			String aname = args.get(1);

			SGAgentActionType action = shell.getDomain().getSGAgentAction(aname);
			if(action == null){
				os.println("Cannot set action to " + aname + " because that action name is not known.");
				return 0;
			}

			SGAgentAction ga = action.associatedAction(agentName, this.actionArgs(args));

			ManualSGAgent agent = manualAgents.get(agentName);
			if(agent == null){
				os.println("No manual agent named " + agentName);
				return 0;
			}

			agent.setNextAction(ga);

			return 0;
		}

		protected String actionArgs(List<String> commandArgs){
			StringBuilder buf = new StringBuilder();
			for(int i = 1; i < commandArgs.size(); i++){
				if(i > 1){
					buf.append(" ");
				}
				buf.append(commandArgs.get(i));
			}
			return buf.toString();
		}
	}

	public class LSManualAgentActionsCommands implements ShellCommand{

		protected OptionParser parser = new OptionParser("h*");

		@Override
		public String commandName() {
			return "lsActions";
		}

		@Override
		public int call(BurlapShell shell, String argString, Scanner is, PrintStream os) {

			OptionSet oset = this.parser.parse(argString.split(" "));

			if(oset.has("h")){
				os.println("Lists the current action selection of all manually controlled agents.");
				return 0;
			}

			for(ManualSGAgent agent : manualAgents.values()){
				os.println(agent.getAgentName() + " " + agent.getNextAction());
			}

			return 0;
		}
	}


	public static class ManualSGAgent extends SGAgent{


		protected volatile SGAgentAction nextAction = null;

		@Override
		public void gameStarting() {
			//do nothing
		}

		@Override
		public SGAgentAction getAction(State s) {

			synchronized(this){
				while(this.nextAction == null){
					try {
						this.wait();
					} catch(InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			SGAgentAction toTake = this.nextAction;
			this.nextAction = null;
			return toTake;
		}

		@Override
		public void observeOutcome(State s, JointAction jointAction, Map<String, Double> jointReward, State sprime, boolean isTerminal) {
			//do nothing
		}

		@Override
		public void gameTerminated() {
			//do nothing
		}

		public void setNextAction(SGAgentAction nextAction){
			synchronized(this){
				this.nextAction = nextAction;
				this.notifyAll();
			}
		}

		protected SGAgentAction getNextAction(){
			return this.nextAction;
		}

	}



}
