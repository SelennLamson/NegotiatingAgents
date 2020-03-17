package centralesupelec.engine.agents;

import java.util.ArrayList;
import java.util.Random;

import centralesupelec.engine.argumentation.Item;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/** The behavior handling the messages that manage a negotiation protocol
 *
 * @author Thomas Lamson
 */
public class ManagerBehaviour extends CyclicBehaviour {
    private static final long serialVersionUID = 1L;
    
    /** Should we start a new negotiation on next step? */
    private boolean beginNegotiation = true;
    
    public void action() {
        ManagerAgent managerAgent = (ManagerAgent) myAgent;
        ACLMessage msg = myAgent.receive();
        
        if (beginNegotiation) {
            
            // Checking that there are still items to negotiate with
            if (managerAgent.getItems().size() == 0) {
                System.out.println("ALL ITEMS WERE SELECTED.");
                myAgent.doDelete();
                return;
            }
            
            beginNegotiation = false;
            
            // Send the remaining items to all engineer agents
            msg = new ACLMessage(ACLMessage.INFORM_REF);
            for (AID aid : managerAgent.getEngineers()) {
                msg.addReceiver(aid);
            }
            msg.setContent(buildItemsString(managerAgent.getItems()));
            System.out.println("------------------------------\nManager: beginning of cycle - sending items.");
            myAgent.send(msg);
            
            // Send a request to a randomly selected engineer agent to make it start the negotiations
            msg = new ACLMessage(ACLMessage.QUERY_REF);
            AID receiver = managerAgent.getEngineers()[new Random().nextInt(managerAgent.getEngineers().length)];
            msg.addReceiver(receiver);
            msg.setContent("");
            System.out.println("Manager: sending query to " + receiver.getLocalName() + ".");
            myAgent.send(msg);
            
        } else if (msg != null) {
            
            // Receiving a message
            String content = msg.getContent();
            int performative = msg.getPerformative();
            
            // If message is a TAKE order, negotiation concluded on a item. We can select it.
            if (performative == ACLMessage.INFORM_REF) {
                System.out.println("Manager: end of cycle - selected item: " + content + "\n------------------------------\n");
                
                managerAgent.selectItemByName(content);
                beginNegotiation = true;
                
            // If message is a CANCEL order, negotiation couldn't conclude. We stop the process.
            } else if (performative == ACLMessage.CANCEL) {
                System.out.println("NEGOTIATION WAS CANCELLED.");
            }
        }
    }
    
    private String buildItemsString(ArrayList<Item> items) {
        String result = "";
        for(Item item : items)
            result += item.toString() + "|";
        return result.substring(0, result.length() - 1);
    }
}
