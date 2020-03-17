package centralesupelec.engine.state_machine.actions;

import java.util.ArrayList;

import centralesupelec.engine.agents.EngineerAgent;
import centralesupelec.engine.argumentation.Item;

/** An action performed when accepting a proposal
 *
 * @author Thomas Lamson
 */
public class AcceptAction extends Action {
    private Item item;
    
    public AcceptAction(float policyValueIn, String contentIn, Item itemIn) {
        super(policyValueIn, contentIn);
        item = itemIn;
    }
    
    @Override
    public void execute(EngineerAgent agent) {
        // Makes sure that current remembered item is the one to accept
        agent.currentItem = item;
    }
    
    /** Generating an AcceptAction that accepts a proposal directly after it's emitted */
    public static Action generateAcceptAction(EngineerAgent agent) {
        
        // Accepting a proposal directly is only ACCEPTABLE if the proposed item can be accepted
        if (agent.getPreferences().canAccept(agent.currentItem, agent.getItems())) {
            return new AcceptAction(
                    agent.getPreferences().computeScore(agent.currentItem), 
                    agent.currentItem.getName(), 
                    agent.currentItem);
        } else {
            return new Action(UNACCEPTABLE);
        }
    }

    /** Generating an AcceptAction that accepts a proposal after negotiation */
    public static Action generateAcceptAnyAction(EngineerAgent agent) {
        
        // We can only accept items proposed by other agents
        ArrayList<Item> extProposedItems = agent.getGraph().getItemsProposedByOther();
        
        float bestPolicy = UNACCEPTABLE;
        Item bestItem = null;
        
        // We can only accept items that are in winning state in the graph, or that we can genuinely accept with our preferences
        for (Item item : extProposedItems) {
            if (agent.getGraph().isItemWinning(item) || agent.getPreferences().canAccept(item, agent.getItems())) {
                float policy = agent.getPreferences().computeScore(item);
                
                // Among acceptable items, we only accept the best one in terms of score
                if (bestPolicy < policy) {
                    bestPolicy = policy;
                    bestItem = item;
                }
            }
        }
        
        // Construct an AcceptAnyAction only if there is an item to accept
        if (bestItem != null) {
            return new AcceptAction(
                    bestPolicy,
                    bestItem.getName(),
                    bestItem);
        } else {
            return new Action(UNACCEPTABLE);
        }
    }
}
