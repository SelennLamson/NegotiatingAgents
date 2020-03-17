package centralesupelec.engine.state_machine.actions;

import centralesupelec.engine.agents.EngineerAgent;
import centralesupelec.engine.state_machine.State;

/** An action to perform when a state is chosen and transitioned to.
 * Static generator allow to create actions for different states at different points in time.
 *
 * @author Thomas Lamson
 */
public class Action {
    // Constant policy values
    public static final float UNACCEPTABLE = -100.0f;
    public static final float CANCEL_VALUE = -99.0f;
    
    /** State initiating the action */
    private State state;
    private float policyValue;
    /** Content of message to send once state is reached, if any */
    private String content;
    
    /** Creates an action with empty message */
    public Action(float policyValueIn) {
        this(policyValueIn, "");
    }
    
    public Action(float policyValueIn, String contentIn) {
        policyValue = policyValueIn;
        content = contentIn;
    }
    
    /** Performs action once state is chosen and reached */
    public void execute(EngineerAgent agent) {
        // Do nothing for basic actions (overridden by more complex actions)
    }
    
    // ------- SIMPLE GENERATORS ------- //
    
    public static Action generateWaitAction(EngineerAgent agent) {
        return new Action(0);
    }
    
    public static Action generateAskWhyAction(EngineerAgent agent) {
        return new Action(CANCEL_VALUE, agent.currentItem.getName());
    }
    
    public static Action generateCommitAction(EngineerAgent agent) {
        return new Action(0, agent.currentItem.getName());
    }
    
    public static Action generateTakeAction(EngineerAgent agent) {
        return new Action(0, agent.currentItem.getName());
    }
    
    public static Action generateCancelAction(EngineerAgent agent) {
        return new Action(CANCEL_VALUE);
    }
    
    // ------- GETTERS ------- // ------- SETTERS ------- //
    
    public void setState(State stateIn) {
        state = stateIn;
    }
    
    public State getState() {
        return state;
    }
    
    public float getPolicyValue() {
        return policyValue;
    }
    
    public String getContent() {
        return content;
    }
}
