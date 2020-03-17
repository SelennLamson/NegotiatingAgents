package centralesupelec.engine.state_machine;

import java.util.function.Function;
import jade.lang.acl.ACLMessage;

import centralesupelec.engine.agents.EngineerAgent;
import centralesupelec.engine.state_machine.actions.AcceptAction;
import centralesupelec.engine.state_machine.actions.Action;
import centralesupelec.engine.state_machine.actions.ArgueAction;
import centralesupelec.engine.state_machine.actions.ProposeAction;

/** A state in the finite state-machine of the negotiation protocol
 *
 * @author Thomas Lamson
 */
public enum State {
    
    // STATE    PRINT_NAME      PERFORMATIVE_TO_SEND        TO_ENGIN.   TO_MANAGER  ACTION_GENERATOR
    WAIT        ("WAIT",        -1,                         false,      false,      Action::generateWaitAction),        // No message on WAIT
    WAIT_COMMIT ("WAIT",        -1,                         false,      false,      Action::generateWaitAction),         // No message on WAIT_COMMIT
    PROPOSE     ("PROPOSE",     ACLMessage.PROPOSE,         true,       false,      ProposeAction::generateAction),
    ASK_WHY     ("ASK_WHY",     ACLMessage.REQUEST,         true,       false,      Action::generateAskWhyAction),
    ACCEPT      ("ACCEPT",      ACLMessage.ACCEPT_PROPOSAL, true,       false,      AcceptAction::generateAcceptAction),
    ACCEPT_ANY  ("ACCEPT",      ACLMessage.ACCEPT_PROPOSAL, true,       false,      AcceptAction::generateAcceptAnyAction),
    ARGUE_PROP  ("ARGUE",       ACLMessage.INFORM,          true,       false,      ArgueAction::generateArgueProposalAction),
    ARGUE       ("ARGUE",       ACLMessage.INFORM,          true,       false,      ArgueAction::generateArgueAction),
    COMMIT      ("COMMIT",      ACLMessage.CONFIRM,         true,       false,      Action::generateCommitAction),
    COMMIT_TAKE ("COMMIT",      ACLMessage.CONFIRM,         true,       false,      Action::generateCommitAction),
    TAKE        ("TAKE",        ACLMessage.INFORM_REF,      true,       true,       Action::generateTakeAction),         // TAKE is also sent to manager
    CANCEL      ("CANCEL",      ACLMessage.CANCEL,          true,       true,       Action::generateCancelAction);       // CANCEL is also sent to manager
    
    // ------------------------------- Short description of states --------------------------------
    
    // WAIT         >   Wait for a request from the manager to begin negotiation, or for a proposal from the other engineer agent
    // WAIT_COMMIT  >   Wait for the other engineer agent to commit after it accepted a proposal
    // PROPOSE      >   Propose a new item to the negotiation
    // ASK_WHY      >   Request an argument that defends the proposal that was just made by the other agent
    // ACCEPT       >   Accept a proposal that was just emitted by the other agent
    // ACCEPT_ANY   >   Accept an old proposal that was emitted by the other agent, maybe after some negotiation
    // ARGUE_PROP   >   Argue to defend the proposal that was just made
    // ARGUE        >   Find the argument that leads the best item possible to a negotiation winning state
    // COMMIT       >   Commit to an item after having accepted it
    // COMMIT_TAKE  >   Commit to an item after it was accepted and commited by the other agent, preparing to take it
    // TAKE         >   Inform all agents that an item was chosen after the negotiation
    // CANCEL       >   Inform all agents that the negotiation was cancelled

    // --- See the Action generators of each state to see the transition policies of each state ---
    // ---------- See the StateMachine class to see the transition rules between states -----------
    
    /** The name that is printed when reaching that state */
    private String name;
    
    /** The performative to use when sending this state's message. -1 if no message to send */
    private int performative;
    
    /** Should this message be sent to other engineers? */
    private boolean sendEngineers;
    
    /** Should this message be sent to the manager? */
    private boolean sendManager;
    
    /** Action generation function that will be used to generate the current policy value of that state */
    private Function<EngineerAgent, Action> actionFunction;
    
    private State(String nameIn, int performativeIn, boolean sendEngineersIn, boolean sendManagerIn, Function<EngineerAgent, Action> policyFunctionIn) {
        name = nameIn;
        performative = performativeIn;
        sendEngineers = sendEngineersIn;
        sendManager = sendManagerIn;
        actionFunction = policyFunctionIn;
    }
    
    /** Applies the Action generator to compute the specific action this state will perform if transitionned to. */
    public Action getAction(EngineerAgent agent) {
        return actionFunction.apply(agent);
    }
    
    /** Selects the best Action to perform given a list of possible states that can be reached. */
    public static Action chooseBetweenStates(EngineerAgent agent, State[] states) {
        float maxVal = 0;
        State bestState = null;
        Action bestAction = null;
        
        for (State s : states) {
            // Generating action for current next state
            Action action = s.getAction(agent);
            
            // Selecting the action with greatest policy value
            float val = action.getPolicyValue();
            if (bestAction == null || val > maxVal) {
                maxVal = val;
                bestState = s;
                bestAction = action;
            }
        }
        
        // Registering best state in best action and returning it
        bestAction.setState(bestState);
        return bestAction;
    }
    
    // ------- GETTERS ------- // ------- SETTERS ------- //
    
    public String getName() {
        return name;
    }
    
    public int getPerformative() {
        return performative;
    }
    
    public boolean getSendEngineers() {
        return sendEngineers;
    }
    
    public boolean getSendManager() {
        return sendManager;
    }
}
