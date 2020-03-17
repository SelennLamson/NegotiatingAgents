package centralesupelec.engine.argumentation;

import java.util.Optional;
import java.util.Random;

/** A value an item can have on a criterion
 *
 * @author Thomas Lamson
 */
public enum EnumValue {
    VERY_BAD(0, "VERY_BAD"),
    BAD(1, "BAD"),
    GOOD(2, "GOOD"),
    VERY_GOOD(3, "VERY_GOOD");
    
    private int value;
    private String name;
    
    private EnumValue(int valueIn, String nameIn) {
        value = valueIn;
        name = nameIn;
    }
    
    public int getValue() {
        return value;
    }
    
    public String toString() {
        return name;
    }
    
    /** Picks a random value amongst existing */
    public static EnumValue pickRandom() {
        return EnumValue.values()[new Random().nextInt(EnumValue.values().length)];
    }

    /** Tries to retrieve a value from its integer value. Returns an empty Optional if not recognized */
    public static Optional<EnumValue> findByValue(int value) {
        for (EnumValue val : values()) {
            if (val.value == value) {
                return Optional.of(val);
            }
        }
        return Optional.empty();
    }

    /** Tries to retrieve a value from its name. Returns an empty Optional if not recognized */
    public static Optional<EnumValue> findByName(String name) {
        for (EnumValue val : values()) {
            if (val.name.equals(name)) {
                return Optional.of(val);
            }
        }
        return Optional.empty();
    }
}
