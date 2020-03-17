package centralesupelec.engine.argumentation;

import java.util.Optional;

import centralesupelec.engine.agents.EngineerAgent;

/** An argument structure that defends or attacks a proposal or another argument
 *
 * @author Thomas Lamson
 */
public class Argument {
    
    /** Internal class that expresses a preference between two criteria */
    public class CriterionPreference {
        private EnumCriterion superiorCriterion;
        private EnumCriterion inferiorCriterion;
        
        public CriterionPreference(EnumCriterion superiorCriterionIn, EnumCriterion inferiorCriterionIn) {
            superiorCriterion = superiorCriterionIn;
            inferiorCriterion = inferiorCriterionIn;
        }
        
        public EnumCriterion getSuperiorCriterion() {
            return superiorCriterion;
        }
        
        public EnumCriterion getInferiorCriterion() {
            return inferiorCriterion;
        }
        
        public String toString() {
            return superiorCriterion.getName() + " > " + inferiorCriterion.getName();
        }
    }
    
    /** Internal class that expresses a value given to an item on a certain criterion */
    public class ItemValue {
        private EnumCriterion criterion;
        private EnumValue value;
        
        public ItemValue(EnumCriterion criterionIn, EnumValue valueIn) {
            criterion = criterionIn;
            value = valueIn;
        }

        public EnumCriterion getCriterion() {
            return criterion;
        }

        public EnumValue getValue() {
            return value;
        }
        
        public String toString() {
            return criterion.getName() + " = " + value.toString();
        }
    }
    
    /** The item the argument is defending or attacking */
    private Item item;
    
    /** Is the argument for or against the item? */
    private boolean isPro;
    
    /** An optional criterion preference premise */
    private CriterionPreference criterionPremise;
    
    /** A criterion value premise */
    private ItemValue valuePremise;

    /** Creates an argument we no premise. Warning: a criterion value premise should always be added before using the argument */
    public Argument(Item itemIn, boolean isProIn) {
        item = itemIn;
        isPro = isProIn;
    }
    
    /** Creates and registers a criterion preference premise to the argument */
    public void addCriterionPreference(EnumCriterion superiorCriterion, EnumCriterion inferiorCriterion) {
        criterionPremise = new CriterionPreference(superiorCriterion, inferiorCriterion);
    }
    
    /** Creates and registers a criterion value premise to the argument */
    public void addValuePremise(EnumCriterion criterion, EnumValue value) {
        valuePremise = new ItemValue(criterion, value);
    }
    
    /** Computes if this argument can be added deeper in the branch than a given other argument. */
    public boolean isStrongerThan(Argument other) {
        if (criterionPremise == null) {
            // An argument always requires a criterion premise to be stronger than another argument
            return false;
        } else if (other.criterionPremise == null) {
            // An argument with criterion premise is always stronger than an argument without
            return true;
        } else {
            // If a given criterion was considered less important than another one in the branch, it cannot be used again
            return other.criterionPremise.inferiorCriterion != criterionPremise.superiorCriterion;
        }
    }
    
    /** Computes if this argument is attacking another given argument, and therefore can be added in the branch directly below it. */
    public boolean isAttacking(Argument other) {
        // An argument always require a criterion premise that supports its value premise to attack another argument
        if (criterionPremise == null || criterionPremise.superiorCriterion != valuePremise.criterion)
            return false;
        
        // If both arguments talk about the same criterion, they are not attacking each other
        if (other.valuePremise.criterion == valuePremise.criterion)
            return false;
        
        // To attack an argument, our criterion preference should state that our criterion is better than the other argument's criterion
        return other.valuePremise.criterion == criterionPremise.inferiorCriterion;
    }
    
    /** Converts an argument to String in order to send it to another agent */
    public String toString() {
        String result = "";
        
        if (!isPro)
            result += "not ";
        result += item.getName();
        result += " <= ";
        
        if (valuePremise != null) {
            result += valuePremise.toString() + ", ";
        }
        
        if (criterionPremise != null) {
            result += criterionPremise.toString() + ", ";
        }
        
        result = result.substring(0, result.length() - 2);
        
        return result;
    }
    
    /** Tries to parse an argument in a String. Returns an empty Optional if not possible to parse */
    public static Optional<Argument> parseArgument(String content, EngineerAgent agent) {
        
        // CHECK: a statement and a premises part
        String[] mainElements = content.split(" <= ");
        if (mainElements.length == 2) {
            String itemElement = mainElements[0];

            // Is is an argument for or against an item?
            boolean isPro;
            if (itemElement.startsWith("not ")) {
                isPro = false;
                itemElement = itemElement.substring(4);
            } else {
                isPro = true;
            }
            
            // CHECK: the item is known
            Optional<Item> item = agent.getItemByName(itemElement);
            if (item.isPresent()) {
                Argument argument = new Argument(item.get(), isPro);
                
                // CHECK: there is at least one premise
                String[] premisesElements = mainElements[1].split(", ");
                if (premisesElements.length > 0) {
                    
                    // CHECK: first premise is a value premise
                    String[] valuePremiseElements = premisesElements[0].split(" = ");
                    if (valuePremiseElements.length == 2) {
                        String criterionName = valuePremiseElements[0];
                        String valueName = valuePremiseElements[1];
                        
                        Optional<EnumCriterion> criterion = EnumCriterion.findByName(criterionName);
                        Optional<EnumValue> value = EnumValue.findByName(valueName);
                        
                        // CHECK: criterion and value are recognized
                        if (criterion.isPresent() && value.isPresent()) {
                            argument.addValuePremise(criterion.get(), value.get());
                            
                            // If there is only a value premise, we stop here and return a valid argument
                            if (premisesElements.length == 1) {
                                return Optional.of(argument);
                            }
                            
                            // CHECK: second premise is a criterion preference
                            String[] criterionPremiseElements = premisesElements[1].split(" > ");
                            if (criterionPremiseElements.length == 2) {
                                Optional<EnumCriterion> superiorCriterion = EnumCriterion.findByName(criterionPremiseElements[0]);
                                Optional<EnumCriterion> inferiorCriterion = EnumCriterion.findByName(criterionPremiseElements[1]);
                                
                                // CHECK: superior and inferior criteria are recognized
                                if (superiorCriterion.isPresent() && inferiorCriterion.isPresent()) {
                                    argument.addCriterionPreference(superiorCriterion.get(), inferiorCriterion.get());
                                    
                                    // CHECK: there is not more than two premises
                                    if (premisesElements.length == 2) {
                                        
                                        // We return a valid argument
                                        return Optional.of(argument);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return Optional.empty();
    }
    
    // ------- GETTERS ------- // ------- SETTERS ------- //

    public Item getItem() {
        return item;
    }

    public boolean isPro() {
        return isPro;
    }

    public CriterionPreference getCriterionPremise() {
        return criterionPremise;
    }

    public ItemValue getValuePremise() {
        return valuePremise;
    }
}
