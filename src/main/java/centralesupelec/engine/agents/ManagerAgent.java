package centralesupelec.engine.agents;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import centralesupelec.engine.argumentation.Item;
import jade.core.AID;
import jade.core.Agent;

/** A agent managing the negotiation between engineer agents
 *
 * @author Thomas Lamson
 */
public class ManagerAgent extends Agent {
    private static final long serialVersionUID = 1L;
    
    /** All the items that still need to be negotiated */
    private ArrayList<Item> items = new ArrayList<>();
    /** Items that we already selected, in order of their selection */
    private ArrayList<Item> selectedItems = new ArrayList<>();
    
    /** References to the engineer agents negotiating */
    private AID[] engineers = {new AID("engineer1", AID.ISLOCALNAME), new AID("engineer2", AID.ISLOCALNAME)};
    
    /** Starts and initializes the agent */
    protected void setup() {
        // Printout a welcome message
        System.out.println("Hello! Manager-agent " + getAID().getName() + " is ready.");
        
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
        }
        
        // Initializes the manager behavior that will handle message exchanges
        addBehaviour(new ManagerBehaviour());
    }
    
    /** Selects a given item and remove it from the items to negotiate */
    public void selectItem(Item item) {
        selectedItems.add(item);
        items.remove(item);
    }

    /** Tries to select an item through its name. Can fail but no warning */
    public void selectItemByName(String itemName) {
        for (Item item : items) {
            if (item.getName().equals(itemName)) {
                selectItem(item);
                return;
            }
        }
    }
    
    // ------- GETTERS ------- // ------- SETTERS ------- //
    
    public ArrayList<Item> getItems() {
        return items;
    }

    public AID[] getEngineers() {
        return engineers;
    }
}
