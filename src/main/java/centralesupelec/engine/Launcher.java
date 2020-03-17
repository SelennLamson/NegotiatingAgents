package centralesupelec.engine;

import centralesupelec.engine.agents.EngineerAgent;
import centralesupelec.engine.agents.ManagerAgent;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.io.Serializable;

/**
 * This class implements the main class of the practical work 3 project: <code>Engine</code>.
 *
 * @author E. Hermellin
 * @version 1.0
 */
public class Launcher implements Serializable {

    /**
     * The serial id of the class.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The main method.
     * @param args the arguments of the program.
     */
    public static void main(String[] args) throws ControllerException {
        
        // Initializing Jade
        Runtime rt = Runtime.instance();
        rt.setCloseVM(true);
        Profile pMain = new ProfileImpl("localhost", 8889, null);
        AgentContainer mc = rt.createMainContainer(pMain);
        
        // Data files
        String itemsPath = "data/items.txt";
        String preferencesPath1 = "data/preferences1.txt";
        String preferencesPath2 = "data/preferences2.txt";
        
        try {
            // Creating and starting two engineer agents with preferences
            mc.createNewAgent("engineer1", EngineerAgent.class.getName(), new Object[] {itemsPath, preferencesPath1}).start();
            mc.createNewAgent("engineer2", EngineerAgent.class.getName(), new Object[] {itemsPath, preferencesPath2}).start();
            
            // Creating and starting a manager agent
            mc.createNewAgent("manager", ManagerAgent.class.getName(), new Object[] {itemsPath}).start();
            
        } catch(StaleProxyException e) {
            e.printStackTrace();
        }
    }
}
