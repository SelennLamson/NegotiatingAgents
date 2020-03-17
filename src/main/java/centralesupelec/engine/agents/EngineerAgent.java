package centralesupelec.engine.agents;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Scanner;

import centralesupelec.engine.argumentation.Item;
import centralesupelec.engine.argumentation.Preferences;
import centralesupelec.engine.argumentation.graph.NegotiationGraph;
import centralesupelec.engine.state_machine.State;
import centralesupelec.engine.state_machine.StateMachine;
import jade.core.AID;
import jade.core.Agent;

/** An agent able to negotiate about items with another EngineerAgent
 *
 * @author Thomas Lamson
 */
public class EngineerAgent extends Agent {
    private static final long serialVersionUID = 1L;

    /** Reference to the manager agent */
    private AID manager = new AID("manager", AID.ISLOCALNAME);
    /** References to the engineers negotiating */
    private AID[] engineers = {new AID("engineer1", AID.ISLOCALNAME), new AID("engineer2", AID.ISLOCALNAME)};
    /** Reference to the other engineer */
    private AID otherEngineer;
    
    private Preferences preferences = new Preferences();
    private ArrayList<Item> items = new ArrayList<>();
    
    /** A finite state machine describing the protocol by actions we can perform upon receiving performatives */
    private StateMachine stateMachine = new StateMachine();
    /** A negotiation graph that handles the argumentation storage, generation and solving */
    private NegotiationGraph graph = new NegotiationGraph();
    
    /** A simple placeholder to remember the item being discussed currently */
    public Item currentItem;
    
    /** Starts and initializes the agent */
    protected void setup() {
        // Printout a welcome message
        System.out.println("Hello! Engineer-agent " + getAID().getName() + " is ready.");
        
        // We need the AID of the other EngineerAgent we're negotiating with
        if (getAID().equals(engineers[0])) {
            otherEngineer = engineers[1];
        } else {
            otherEngineer = engineers[0];
        }
        
        // Initializes the state machine at the initial state "WAIT"
        stateMachine.setState(State.WAIT);

        // Parsing arguments
        Object[] args = getArguments();
        
        if (args == null || args.length == 0) {
            System.out.println("Didn't receive an item list path at setup. Shutting down...");
            doDelete();
        } else {
            
            // Parsing items in file
            String itemsPath = (String) args[0];
            
            try {
                File file = new File(itemsPath);
                Scanner scanner = new Scanner(file);
                
                while (scanner.hasNextLine()) {
                    Item item = Item.parseItems(scanner.nextLine()).get(0);
                    items.add(item);
                }
                
                scanner.close();
            } catch(FileNotFoundException e) {
                System.out.println("Item list file was not found. Shutting down...");
                doDelete();
            }
            
            if (args.length > 1) {
                
                // We received a preferences file, we try to load it but it can also fail for many reasons
                String preferencesPath = (String) args[1];
                System.out.println("Loading preferences file: " + preferencesPath);
                if (!preferences.loadFromFile(preferencesPath, items)) {
                    doDelete();
                }
            } else {
                
                // We didn't receive a preferences file, we simply randomize them
                System.out.println("Randomizing preferences.");
                preferences.randomize(items);
            }
        }
        
        // Initializes the negotiation behavior that will handle messages and state transitions
        addBehaviour(new NegotiateBehaviour());
    }
    
    /** Resets the negotiation graph for the next negotiation round */
    public void resetGraph() {
        graph = new NegotiationGraph();
    }
    
    /** Retrieves an item by its name, useful for parsing. Optional is empty if name wasn't recognized as an item */
    public Optional<Item> getItemByName(String itemName) {
        for (Item item : items) {
            if (item.getName().equals(itemName)) {
                return Optional.of(item);
            }
        }
        return Optional.empty();
    }
    
    /** Stops the agent */
    protected void takeDown() {
        System.out.println("Engineer-agent " + getAID().getName() + " terminating.");
    }
    
    // ------- GETTERS ------- // ------- SETTERS ------- //
    
    public StateMachine getStateMachine() {
        return stateMachine;
    }

    public AID getOtherEngineer() {
        return otherEngineer;
    }

    public AID getManager() {
        return manager;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> itemsIn) {
        items = itemsIn;
    }
    
    public String getDisplayName() {
        return getAID().getLocalName().split("@")[0];
    }

    public NegotiationGraph getGraph() {
        return graph;
    }

    public Preferences getPreferences() {
        return preferences;
    }
}
