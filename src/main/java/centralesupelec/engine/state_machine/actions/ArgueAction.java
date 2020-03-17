package centralesupelec.engine.state_machine.actions;

import java.util.ArrayList;
import java.util.Optional;

import centralesupelec.engine.agents.EngineerAgent;
import centralesupelec.engine.argumentation.Argument;
import centralesupelec.engine.argumentation.Item;
import centralesupelec.engine.argumentation.graph.NegotiationGraph;

/** An action to perform when reaching the ARGUE state
 *
 * @author Thomas Lamson
 */
public class ArgueAction extends Action {
    private Argument argument;
    
    public ArgueAction(float policyValueIn, String contentIn, Argument argumentIn) {
        super(policyValueIn, contentIn);
        argument = argumentIn;
    }
    
    @Override
    public void execute(EngineerAgent agent) {
        // Add the produced argument to the negotiation graph
        agent.getGraph().addArgument(argument);
    }
    
    /** Generating an ArgueAction for any item in a given agent's negotiation graph */
    public static Action generateArgueAction(EngineerAgent agent) {
        return generateActionWithGraph(agent, agent.getGraph(), Optional.empty());
    }
    
    /** Generating an ArgueAction for the currently proposed item and using agent's negotiation graph */
    public static Action generateArgueProposalAction(EngineerAgent agent) {
        return generateActionWithGraph(agent, agent.getGraph(), Optional.of(agent.currentItem));
    }
    
    /** Generating an ArgueAction based on a given negotiation graph, can be useful to simulate what an argue would produce with any given graph.
     * An optional forced item can be indicated to limit the argumentation to one specific item. Used when answering an ASK_WHY request. */
    public static Action generateActionWithGraph(EngineerAgent agent, NegotiationGraph graph, Optional<Item> optForcedItem) {
        
        // Initialization
        Argument bestArgument = null;
        float bestArgumentScore = UNACCEPTABLE;
        ArrayList<Item> proposedItems = graph.getProposedItems();

        // Generate the best argument for each proposed item (either defending or attacking)
        for (Item item : proposedItems) {
            // Next iteration if there is a forced item and it's not the current one
            if (optForcedItem.isPresent() && !optForcedItem.get().equals(item)) {
                continue;
            }
            
            // Try to generate the best argument for current item
            Optional<Argument> optArgument = graph.generateBestArgumentForItem(item, agent);
            
            // If a valid argument was generated...
            if (optArgument.isPresent()) {
                Argument argument = optArgument.get();
                
                // Compute what will be the best item after the application of such argument
                float newBestItemScore = UNACCEPTABLE;
                
                // Cloning the agent's graph to simulate the effect of the generated argument
                NegotiationGraph temporaryGraph = graph.clone();
                temporaryGraph.addArgument(argument);
                
                for (Item finalItem : proposedItems) {
                    // Is an item winning when we add the new argument?
                    if (temporaryGraph.isItemWinning(finalItem)) {
                        
                        // It is winning so we try to remember the best winning item so far in terms of score
                        float itemScore = agent.getPreferences().computeScore(finalItem);
                        
                        if (newBestItemScore < itemScore) {
                            newBestItemScore = itemScore;
                        }
                    }
                }
                
                // We only consider the argument acceptable if it changed the best item of the graph (or its score)
                // With considered arguments, we select the one that makes the most interesting item winning
                if (newBestItemScore > bestArgumentScore) {
                    bestArgumentScore = newBestItemScore;
                    bestArgument = argument;
                }
            }
        }
        
        // If we were able to generate a valid and acceptable argument, we produce an ArgueAction
        if (bestArgument != null) {
            return new ArgueAction(bestArgumentScore,
                                   bestArgument.toString(),
                                   bestArgument);
        } else {
            return new Action(UNACCEPTABLE);
        }
    }
}
