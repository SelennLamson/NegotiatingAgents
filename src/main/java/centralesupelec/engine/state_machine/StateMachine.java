package centralesupelec.engine.state_machine;

import java.util.ArrayList;
import java.util.Optional;

import jade.lang.acl.ACLMessage;

/** A Finite State-Machine that describes the transitions between states in the negotiation protocol
 *
 * @author Thomas Lamson
 */
public class StateMachine {
    
    /** Internal class that represents a transition rule from one state to a list of outcomes, given a received performative */
    private class Rule {
        /** The source state */
        private State state;
        
        /** The performative that triggers this rule. -1 means that this rule is automatically triggered */
        private int performative;
        
        /** The possible destination states */
        private State[] outcomes;
        
        private Rule(State stateIn, int performativeIn, State[] outcomesIn) {
            state = stateIn;
            performative = performativeIn;
            outcomes = outcomesIn;
        }
    }
    
    /** Current state */
    private State state;
    
    /** List of all transition rules */
    private ArrayList<Rule> rules;
    
    /** Builds an Engineer's agent State Machine with state transition rules corresponding to negotiation protocol */
    public StateMachine() {
        rules = new ArrayList<>();
        
        // --------------- FROM STATE: WAIT ---------------
        
        // We receive an INFORM_REF, telling us what items will be negotiated (stay in WAIT state)
        rules.add(new Rule(State.WAIT,          ACLMessage.INFORM_REF,      new State[] {State.WAIT}));
        
        // We receive a QUERY_REF, telling us to start the negotiation with a PROPOSE or to CANCEL it
        rules.add(new Rule(State.WAIT,          ACLMessage.QUERY_REF,       new State[] {State.PROPOSE, State.CANCEL}));
        
        // We receive a PROPOSE, telling us that the other agent started the negotiation. We can directly ACCEPT or we can ASK_WHY
        rules.add(new Rule(State.WAIT,          ACLMessage.PROPOSE,         new State[] {State.ASK_WHY, State.ACCEPT}));
        
        // We receive a CANCEL, indicating that the other agent stopped the negotiation, we stay in state WAIT
        rules.add(new Rule(State.WAIT,          ACLMessage.CANCEL,          new State[] {State.WAIT}));
        // ------------------------------------------------
        
        
        // --------------- FROM STATE: PROPOSE ------------
        
        // We receive an ACCEPT_PROPOSAL, indicating that our proposal was accepted. We go in WAIT_COMMIT
        rules.add(new Rule(State.PROPOSE,       ACLMessage.ACCEPT_PROPOSAL, new State[] {State.WAIT_COMMIT}));
        
        // We receive a REQUEST, asking us to justify our proposal. We ARGUE_PROP or we CANCEL if we can't
        rules.add(new Rule(State.PROPOSE,       ACLMessage.REQUEST,         new State[] {State.ARGUE_PROP, State.CANCEL}));
        // ------------------------------------------------

        
        // --------------- FROM STATE: ARGUE --------------
        
        // We receive an ACCEPT_PROPOSAL, indicating that one of our previous proposals was accepted. We go in WAIT_COMMIT
        rules.add(new Rule(State.ARGUE,         ACLMessage.ACCEPT_PROPOSAL, new State[] {State.WAIT_COMMIT}));
        
        // We receive an INFORM, giving us a new argument. We can either: ARGUE again, PROPOSE a new item, ACCEPT_ANY of the previous proposals or CANCEL the negotiation
        rules.add(new Rule(State.ARGUE,         ACLMessage.INFORM,          new State[] {State.ARGUE, State.PROPOSE, State.ACCEPT_ANY, State.CANCEL}));
        
        // We receive a PROPOSE, indicating a new proposal. We can directly ACCEPT or we can ASK_WHY
        rules.add(new Rule(State.ARGUE,         ACLMessage.PROPOSE,         new State[] {State.ASK_WHY, State.ACCEPT}));
        
        // We receive a CANCEL, indicating that the negotiation is cancelled. We just WAIT
        rules.add(new Rule(State.ARGUE,         ACLMessage.CANCEL,          new State[] {State.WAIT}));
        // ------------------------------------------------

        
        // --------------- FROM STATE: ARGUE_PROP ---------
        // Same rules as ARGUE
        rules.add(new Rule(State.ARGUE_PROP,    ACLMessage.ACCEPT_PROPOSAL, new State[] {State.WAIT_COMMIT}));
        rules.add(new Rule(State.ARGUE_PROP,    ACLMessage.INFORM,          new State[] {State.ARGUE, State.PROPOSE, State.ACCEPT_ANY, State.CANCEL}));
        rules.add(new Rule(State.ARGUE_PROP,    ACLMessage.PROPOSE,         new State[] {State.ASK_WHY, State.ACCEPT}));
        rules.add(new Rule(State.ARGUE_PROP,    ACLMessage.CANCEL,          new State[] {State.WAIT}));
        // ------------------------------------------------

        
        // --------------- FROM STATE: ASK_WHY ------------
        
        // We receive an INFORM, giving us an argument to defend the previous proposal. We can either: ARGUE back, PROPOSE a new item, ACCEPT_ANY of the previous proposals or CANCEL the negotiation
        rules.add(new Rule(State.ASK_WHY,       ACLMessage.INFORM,          new State[] {State.ARGUE, State.PROPOSE, State.ACCEPT_ANY, State.CANCEL}));
        
        // We receive a CANCEL, indicating that previous proposal couldn't be justified. We stop the negotiation protocol and WAIT
        rules.add(new Rule(State.ASK_WHY,       ACLMessage.CANCEL,          new State[] {State.WAIT}));
        // ------------------------------------------------

        
        // --------------- FROM STATE: ACCEPT -------------
        
        // We just accepted a proposal, we directly COMMIT_TAKE to commit to take it
        rules.add(new Rule(State.ACCEPT,        -1,                         new State[] {State.COMMIT_TAKE}));
        // ------------------------------------------------

        
        // --------------- FROM STATE: ACCEPT_ANY ---------
        
        // We just accepted a proposal, we directly COMMIT_TAKE to commit to take it
        rules.add(new Rule(State.ACCEPT_ANY,    -1,                         new State[] {State.COMMIT_TAKE}));
        // ------------------------------------------------

        
        // --------------- FROM STATE: WAIT_COMMIT --------
        
        // We receive a CONFIRM, indicating that the proposal that was just accepted is not committed. We can COMMIT too
        rules.add(new Rule(State.WAIT_COMMIT,   ACLMessage.CONFIRM,         new State[] {State.COMMIT}));
        
        // We receive a CANCEL, indicating that the agreement was cancelled. We just WAIT
        rules.add(new Rule(State.WAIT_COMMIT,   ACLMessage.CANCEL,          new State[] {State.WAIT}));
        // ------------------------------------------------

        
        // --------------- FROM STATE: COMMIT_TAKE --------
        
        // We receive a CONFIRM, indicating that the other agent also committed to the proposal we accepted. We TAKE it
        rules.add(new Rule(State.COMMIT_TAKE,   ACLMessage.CONFIRM,         new State[] {State.TAKE}));
        
        // We receive a CANCEL, indicating that the other agent cancelled the agreement. We just WAIT
        rules.add(new Rule(State.COMMIT_TAKE,   ACLMessage.CANCEL,          new State[] {State.WAIT}));
        // ------------------------------------------------

        
        // --------------- FROM STATE: COMMIT -------------
        
        // We receive an INFORM_REF, indicating that the other agent took the committed item. We WAIT for next negotiation cycle
        rules.add(new Rule(State.COMMIT,        ACLMessage.INFORM_REF,      new State[] {State.WAIT}));
        // ------------------------------------------------

        
        // --------------- FROM STATE: TAKE ---------------
        
        // We just took an item, we directly WAIT for next negotiation cycle
        rules.add(new Rule(State.TAKE,          -1,                         new State[] {State.WAIT}));
        // ------------------------------------------------
        
        
        // --------------- FROM STATE: CANCEL -------------
        
        // We just cancelled the negotiation so we directly WAIT for next instructions
        rules.add(new Rule(State.CANCEL,        -1,                         new State[] {State.WAIT}));
        // ------------------------------------------------
    }
    
    /** Returns current state's outcomes that doens't require a performative and therefore must be transitioned to immediately (there should be only one such outcome, if any) */
    public State[] getDirectOutcomes() {
        return getOutcomes(-1);
    }
    
    /** Returns current state's outcomes that can be reached after a certain performative */
    public State[] getOutcomes(int performative) {
        Optional<Rule> optRule = rules.stream().filter(r -> r.state == state && r.performative == performative).findFirst();
        if (optRule.isPresent()) {
            return optRule.get().outcomes;
        } else {
            return new State[0];
        }
    }
    
    // ------- GETTERS ------- // ------- SETTERS ------- //
    
    public void setState(State stateIn) {
        state = stateIn;
    }
    
    public State getState() {
        return state;
    }
}
