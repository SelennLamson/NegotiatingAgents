package centralesupelec.engine.argumentation.graph;

import java.util.Optional;

import centralesupelec.engine.argumentation.Argument;

/** A node in the argumentation graph that represents an argument and attacks or support another node
 *
 * @author Thomas Lamson
 */
public class ArgumentNode {
    private Argument argument;
    private ArgumentNode attackedBy = null;
    
    /** Create an ArgumentNode without parent */
    public ArgumentNode(Argument argumentIn) {
        argument = argumentIn;
    }
    
    /** Create an ArgumentNode attacking another ArgumentNode */
    public ArgumentNode(Argument argumentIn, ArgumentNode parentArgument) {
        argument = argumentIn;
        parentArgument.attackedBy = this;
    }
    
    /** Create an ArgumentNode supporting a ProposalNode */
    public ArgumentNode(Argument argumentIn, ProposalNode parentProposal) {
        argument = argumentIn;
        parentProposal.setDefendedBy(this);
    }
    
    /** Boolean check to see if this node is in winning state in its branch */
    public boolean isWinning() {
        if (attackedBy == null) {
            return true;
        } else {
            return !attackedBy.isWinning();
        }
    }
    
    /** Recursive getter of last node of the branch */
    public Optional<ArgumentNode> getLeafArgument() {
        if (attackedBy == null) {
            return Optional.of(this);
        } else {
            return attackedBy.getLeafArgument();
        }
    }
    
    /** Checks that a given argument can be added at the end of the branch.
     * It should always be stronger than any argument of the branch, and it should attack the last one. */
    public boolean canAddArgument(Argument argIn) {
        if (attackedBy == null) {
            // The argument must be stronger than and attack the last one of the branch to be added
            return argIn.isStrongerThan(argument) && argIn.isAttacking(argument);
        } else {
            // The argument must be stronger than every argument along the branch to be added
            return argIn.isStrongerThan(argument) && attackedBy.canAddArgument(argIn);
        }
    }
    
    /** Recursively pass an argument to the leaf node of the branch and add it there */
    public void addArgument(Argument argIn) {
        if (attackedBy == null) {
            attackedBy = new ArgumentNode(argIn, this);
        } else {
            attackedBy.addArgument(argIn);
        }
    }
    
    public String toString() {
        String result = argument.toString();
        if (attackedBy != null) {
            result += "\n\t" + attackedBy.toString();
        }
        return result;
    }
    
    // ------- GETTERS ------- // ------- SETTERS ------- //

    public Argument getArgument() {
        return argument;
    }

    public ArgumentNode getAttackedBy() {
        return attackedBy;
    }
}
