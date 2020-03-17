package centralesupelec.engine.argumentation;

import java.util.Optional;

/** A criterion on which items are evaluated
 *
 * @author Thomas Lamson
 */
public enum EnumCriterion {
    POWER("Power"),
    COST("Cost"),
    CONSUMPTION("Consumption"),
    DURABILITY("Durability"),
    ENVIRONMENT("Environment"),
    NOISE("Noise");
    
    private String name;
    private EnumCriterion(String nameIn) {
        name = nameIn;
    }
    
    public String getName() {
        return name;
    }
    
    /** Tries to find the criterion corresponding to the given name, returns empty Optional if not recognized */
    public static Optional<EnumCriterion> findByName(String name) {
        for (EnumCriterion crit : values()) {
            if (crit.name.equals(name)) {
                return Optional.of(crit);
            }
        }
        return Optional.empty();
    }
}
