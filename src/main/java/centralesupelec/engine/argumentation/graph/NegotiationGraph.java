package centralesupelec.engine.argumentation.graph;

import java.util.ArrayList;
import java.util.Optional;

import centralesupelec.engine.agents.EngineerAgent;
import centralesupelec.engine.argumentation.Argument;
import centralesupelec.engine.argumentation.EnumCriterion;
import centralesupelec.engine.argumentation.EnumValue;
import centralesupelec.engine.argumentation.Item;

/** A graph object that represents the argumentation for and against item proposals
 *
 * @author Thomas Lamson
 */
public class NegotiationGraph {
    private ArrayList<ProposalNode> proposals = new ArrayList<>();
    
    public NegotiationGraph () {}
    
    /** Clones a graph by copying every node without keeping references to original nodes.
     * It can be useful to test things on a graph without actually changing it. */
    public NegotiationGraph clone() {
        NegotiationGraph cloned = new NegotiationGraph();
        
        for (ProposalNode proposal : proposals) {
            // We clone every proposal
            cloned.addProposal(proposal.getItem(), proposal.isInitiatedBySelf());
            
            // We clone the branch of every proposal
            ArgumentNode argument = proposal.getDefendedBy();
            while (argument != null) {
                cloned.addArgument(argument.getArgument());
                argument = argument.getAttackedBy();
            }
        }
        
        return cloned;
    }
    
    /** Registers a new proposal as initiated by current agent */
    public void initiateProposal(Item item) {
        addProposal(item, true);
    }
    
    /** Registers a new proposal as initiated by the another agent */
    public void receiveProposal(Item item) {
        addProposal(item, false);
    }
    
    /** Registers a new proposal */
    private void addProposal(Item item, boolean initiatedBySelf) {
        proposals.add(new ProposalNode(item, initiatedBySelf));
    }
    
    /** Checks that an argument can be added to the tree under a certain proposal */
    public boolean canAddArgument(Argument argument) {
        Item argumentItem = argument.getItem();
        for (ProposalNode proposal : proposals) {
            if (proposal.getItem().equals(argumentItem)) {
                return proposal.canAddArgument(argument);
            }
        }
        return false;
    }
    
    /** Adds an argument to the correct branch of the node, based on the item this argument is defending or attacking */
    public void addArgument(Argument argument) {
        for (ProposalNode proposal : proposals) {
            if (proposal.getItem().equals(argument.getItem())) {
                proposal.addArgument(argument);
            }
        }
    }

    /** Retrieves all items that were proposed until now */
    public ArrayList<Item> getProposedItems() {
        ArrayList<Item> items = new ArrayList<>();
        for (ProposalNode proposal : proposals) {
            items.add(proposal.getItem());
        }
        return items;
    }
    
    /** Retrieves all items that were proposed by other agent until now */
    public ArrayList<Item> getItemsProposedByOther() {
        ArrayList<Item> items = new ArrayList<>();
        for (ProposalNode proposal : proposals) {
            if (!proposal.isInitiatedBySelf()) {
                items.add(proposal.getItem());
            }
        }
        return items;
    }
    
    /** Boolean check to see if a given item proposal is supported or attacked in the current state of the graph */
    public boolean isItemWinning(Item item) {
        for (ProposalNode proposal : proposals) {
            if (proposal.getItem().equals(item)) {
                return proposal.isWinning();
            }
        }
        return false;
    }

    /** Tries to generate an argument for a given item and some agent preferences. Returns empty Optional if no valid argument could be formed */
    public Optional<Argument> generateBestArgumentForItem(Item item, EngineerAgent agent) {
        for (ProposalNode proposal : proposals) {
            if (proposal.getItem().equals(item)) {
                
                // Should the argument support or attack the item
                boolean canAccept = agent.getPreferences().canAccept(item, agent.getItems());

                // We only keep values that are consistent with agent's will.
                ArrayList<EnumValue> eligibleValues = new ArrayList<>();
                if (canAccept) {
                    eligibleValues.add(EnumValue.GOOD);
                    eligibleValues.add(EnumValue.VERY_GOOD);
                } else {
                    eligibleValues.add(EnumValue.BAD);
                    eligibleValues.add(EnumValue.VERY_BAD);
                }
                
                ArrayList<EnumCriterion> excludedCriteria = new ArrayList<>();
                Optional<ArgumentNode> optLeaf = proposal.getLeafArgument();
                if (optLeaf.isPresent()) {
                    EnumCriterion leafCriterion = optLeaf.get().getArgument().getValuePremise().getCriterion();
                    
                    // We're attacking another argument
                    Optional<EnumCriterion> optCriterion;
                    while ((optCriterion = agent.getPreferences().getBestCriterionExcept(excludedCriteria)).isPresent()) {
                        
                        // If the leaf's criterion is better than current criterion, we can stop: we won't find another valid criterion
                        if (agent.getPreferences().isCriterionBetter(leafCriterion, optCriterion.get())) {
                            break;
                        }
                        
                        Optional<EnumValue> optValue = agent.getPreferences().getScoreAtCriterion(item, optCriterion.get());
                        if (optValue.isPresent() && eligibleValues.contains(optValue.get())) {
                            
                            // We found a valid argument
                            Argument argument = new Argument(item, canAccept);
                            argument.addValuePremise(optCriterion.get(), optValue.get());
                            
                            // We need to justify that the new criterion is better than the previous argument's criterion
                            argument.addCriterionPreference(optCriterion.get(), leafCriterion);
                            
                            // Check if the argument is actually able to be added on the branch (stronger than any previous argument and attacking the last one)
                            if (agent.getGraph().canAddArgument(argument)) {
                                
                                // We found a valid argument
                                return Optional.of(argument);
                            }
                        }
                        
                        // We try with next criterion available
                        excludedCriteria.add(optCriterion.get());
                    }
                    
                } else if (canAccept) {
                    
                    // We're in an undefended proposal case, and we assert that we should defend it
                    Optional<EnumCriterion> optCriterion;
                    while ((optCriterion = agent.getPreferences().getBestCriterionExcept(excludedCriteria)).isPresent()) {
                        
                        // We check if the item has a positive value on current criterion
                        Optional<EnumValue> optValue = agent.getPreferences().getScoreAtCriterion(item, optCriterion.get());
                        if (optValue.isPresent() && eligibleValues.contains(optValue.get())) {
                            
                            // We found a valid argument
                            Argument argument = new Argument(item, true);
                            argument.addValuePremise(optCriterion.get(), optValue.get());
                            return Optional.of(argument);
                        }
                        
                        // We try with next criterion available
                        excludedCriteria.add(optCriterion.get());
                    }
                }
            }
        }
        
        // For any reason, a valid argument couldn't be generated to support agent's preferences on this item.
        return Optional.empty();
    }
    
    public String toString() {
        String result = "-- NEGOTIATION GRAPH --";
        
        for (ProposalNode proposal : proposals) {
            result += "\n" + proposal.toString() + "\n";
        }
        
        result += "\n-----------------------";
        return result;
    }
}
