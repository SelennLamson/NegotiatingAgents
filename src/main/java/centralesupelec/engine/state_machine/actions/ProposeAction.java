package centralesupelec.engine.state_machine.actions;

import java.util.ArrayList;
import java.util.Optional;

import centralesupelec.engine.agents.EngineerAgent;
import centralesupelec.engine.argumentation.Item;
import centralesupelec.engine.argumentation.graph.NegotiationGraph;

/** An action to perform when reaching a PROPOSE state
 *
 * @author Thomas Lamson
 */
public class ProposeAction extends Action {
    private Item item;
    
    public ProposeAction(float policyValueIn, String contentIn, Item itemIn) {
        super(policyValueIn, contentIn);
        item = itemIn;
    }
    
    @Override
    public void execute(EngineerAgent agent) {
        // We register a new proposal initiated by the agent itself
        agent.getGraph().initiateProposal(item);
        
        // Make sure that proposed item becomes the new remembered current item
        agent.currentItem = item;
    }
    
    /** Generating a ProposeAction by proposing the best unproposed item that the agent can accept. */
    public static Action generateAction(EngineerAgent agent) {
        
        // Retrieving the non-proposed items
        ArrayList<Item> whiteList = new ArrayList<>();
        for (Item item : agent.getItems()) {
            if (!agent.getGraph().getProposedItems().contains(item)) {
                whiteList.add(item);
            }
        }
        
        // Computing the best item we can choose among the non-proposed items
        Optional<Item> optBest = agent.getPreferences().findBest(whiteList);
        
        // While there is still items to propose and while we can accept the current item...
        while (optBest.isPresent() && agent.getPreferences().canAccept(optBest.get(), agent.getItems())) {
            
            // Clone the negotiation graph to simulate what would happen when proposing the new item
            NegotiationGraph temporaryGraph = agent.getGraph().clone();
            temporaryGraph.initiateProposal(optBest.get());
            
            // Generating an ArgueAction in anticipation of an ASK_WHY request
            float arguePolicyValue = ArgueAction.generateActionWithGraph(agent, temporaryGraph, Optional.of(optBest.get())).getPolicyValue();
            
            // We check that it was actually possible to argue in favor of the proposed item
            if (arguePolicyValue > UNACCEPTABLE) {
                
                // As this item could be proposed and argued in favor of, and as we are testing them from best to worse, we can stop here and propose the item
                return new ProposeAction(agent.getPreferences().computeScore(optBest.get()),
                        optBest.get().getName(),
                        optBest.get());
            }
            
            // Remove the item from the list and retry with the remaining items
            whiteList.remove(optBest.get());
            optBest = agent.getPreferences().findBest(whiteList);
        }
        
        // No item were able to be proposed
        return new Action(UNACCEPTABLE);
    }
}
