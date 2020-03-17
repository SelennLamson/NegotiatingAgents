package centralesupelec.engine.argumentation.graph;

import java.util.Optional;

import centralesupelec.engine.argumentation.Argument;
import centralesupelec.engine.argumentation.Item;

/** A node in the argumentation graph that represents a proposal and is at the root of a branch
 *
 * @author Thomas Lamson
 */
public class ProposalNode {
    private Item item;
    private ArgumentNode defendedBy = null;
    /** Was this proposal initiated by current agent? */
    private boolean initiatedBySelf;
    
    public ProposalNode(Item itemIn, boolean initiatedBySelfIn) {
        item = itemIn;
        initiatedBySelf = initiatedBySelfIn;
    }

    /** Is this proposal currently in a winning state (supported by a winning argument) */
    public boolean isWinning() {
        if (defendedBy == null) {
            return false;
        } else {
            return defendedBy.isWinning();
        }
    }
    
    /** Retrieves the last ArgumentNode of the branch, or empty Optional if proposal is not defended yet */
    public Optional<ArgumentNode> getLeafArgument() {
        if (defendedBy != null) {
            return defendedBy.getLeafArgument();
        }
        return Optional.empty();
    }
    
    /** Checks if a given argument can be added to the end of the branch */
    public boolean canAddArgument(Argument argIn) {
        if (defendedBy == null) {
            return true;
        } else {
            return defendedBy.canAddArgument(argIn);
        }
    }
    
    /** Adds the argument to the end of the branch. */
    public void addArgument(Argument argIn) {
        if (defendedBy == null) {
            defendedBy = new ArgumentNode(argIn, this);
        } else {
            defendedBy.addArgument(argIn);
        }
    }
    
    public String toString() {
        String result = "Proposal: " + item.getName();
        if (defendedBy != null) {
            result += "\n\t" + defendedBy.toString();
        }
        return result;
    }
    
    // ------- GETTERS ------- // ------- SETTERS ------- //

    public Item getItem() {
        return item;
    }
    
    public boolean isInitiatedBySelf() {
        return initiatedBySelf;
    }

    public ArgumentNode getDefendedBy() {
        return defendedBy;
    }
    
    public void setDefendedBy(ArgumentNode argument) {
        defendedBy = argument;
    }
}
