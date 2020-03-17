package centralesupelec.engine.agents;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.Optional;

import centralesupelec.engine.argumentation.Argument;
import centralesupelec.engine.argumentation.Item;
import centralesupelec.engine.state_machine.State;
import centralesupelec.engine.state_machine.actions.Action;

/** The behavior handling the messages that an EngineerAgent sends and receives during a negotiation protocol
 *
 * @author Thomas Lamson
 */
public class NegotiateBehaviour extends CyclicBehaviour {
    private static final long serialVersionUID = 1L;
    
    public void action() {
        boolean encounteredError = false;
        
        // Preparing some variables and remembering current state
        EngineerAgent engineerAgent = (EngineerAgent) myAgent;
        String name = engineerAgent.getDisplayName();
        State prevState = engineerAgent.getStateMachine().getState();
        
        // We first check if current states has direct transitions to next states (that doesn't require to wait for a message)
        State[] nextStates = engineerAgent.getStateMachine().getDirectOutcomes();
        if (nextStates.length == 0) {
            
            // If there is no direct outcome, we can safely retrieve the message from the stack
            ACLMessage msg = myAgent.receive();
            
            // If there is such message, handle the reception
            if (msg != null) {
                String content = msg.getContent();
                int performative = msg.getPerformative();
                
                // The received message's performative tells us what are the next possible transitions for our agent
                nextStates = engineerAgent.getStateMachine().getOutcomes(performative);
                
                
                // HANDLING: Received items data from manager
                //      - we register the new items to negotiate with
                //      - we reset the negotiation graph for a new round
                if (prevState == State.WAIT && performative == ACLMessage.INFORM_REF) {
                    engineerAgent.setItems(Item.parseItems(content));
                    engineerAgent.resetGraph();
                
                    
                // HANDLING: Received a new proposal
                //      - we parse and validate the new proposal
                //      - we register the proposal to the negotiation graph as an external proposal
                //      - we remember the new current item
                } else if (performative == ACLMessage.PROPOSE) {
                    Optional<Item> item = engineerAgent.getItemByName(content);
                    if (item.isPresent()) {
                        engineerAgent.getGraph().receiveProposal(item.get());
                        engineerAgent.currentItem = item.get();
                    } else {
                        encounteredError = true;
                    }
                
                    
                // HANDLING: Received a proposal accept message
                //      - we parse and validate the accepted item
                //      - we remember the new current item
                } else if (performative == ACLMessage.ACCEPT_PROPOSAL) {
                    Optional<Item> item = engineerAgent.getItemByName(content);
                    if (item.isPresent()) {
                        engineerAgent.currentItem = item.get();
                    } else {
                        encounteredError = true;
                    }
                
                
                // HANDLING: Received an argument
                //      - we parse and validate the new argument
                //      - we check that the argument can be added at a valid location in the graph
                //      - we add it to the graph
                } else if (performative == ACLMessage.INFORM) {
                    Optional<Argument> argument = Argument.parseArgument(content, engineerAgent);
                    if (argument.isPresent()) {
                        if (engineerAgent.getGraph().canAddArgument(argument.get())) {
                            engineerAgent.getGraph().addArgument(argument.get());
                        } else {
                            encounteredError = true;
                            System.out.println(name + ": cannot add argument.");
                        }
                    } else {
                        encounteredError = true;
                    }
                }
            }
        }
        
        // If we ever encountered an error during message parsing, we can only transit to the CANCEL state
        if (encounteredError) {
            System.out.println(name + ": encountered an error!");
            nextStates = new State[] {State.CANCEL};
        }
        
        // If we can perform a transition (a message was received, or there exist a direct outcome to current state)
        if (nextStates.length > 0) {
            
            // We evaluate the different transitions and select the best one through this call
            Action bestAction = State.chooseBetweenStates(engineerAgent, nextStates);
            
            // The returned best actions gives us the new state and the content of the message to send
            State selectedState = bestAction.getState();
            String content = bestAction.getContent();
            
            // Setting new state
            engineerAgent.getStateMachine().setState(selectedState);
            
            // Executing action (performing several tasks that depend on the type of action)
            bestAction.execute(engineerAgent);
    
            // Print the newly reached state, with the content of the message we're going to send
            // Note: we don't print if state is WAIT or WAIT_COMMIT for more clarity
            if (selectedState != State.WAIT && selectedState != State.WAIT_COMMIT) {
                System.out.println(name + ": " + selectedState.getName() + (content.length() == 0 ? "" : ": ") + content);
            }
            
            // Print the negotiation graph of the agent whenever an ACCEPT state was reached
//            if (selectedState == State.ACCEPT || selectedState == State.ACCEPT_ANY) {
//                System.out.println(name + ":\n" + engineerAgent.getGraph().toString() + "\n");
//            }
            
            // If there is a message to sent when reaching new state, send it
            if (selectedState.getPerformative() != -1) {
                ACLMessage reply = new ACLMessage(selectedState.getPerformative());
                reply.setContent(content);
                
                if (selectedState.getSendEngineers())
                    reply.addReceiver(engineerAgent.getOtherEngineer());
                if (selectedState.getSendManager())
                    reply.addReceiver(engineerAgent.getManager());
                
                myAgent.send(reply);
            }
        }
    }
}