package centralesupelec.engine.argumentation;

/** An item evaluation on a criterion
 *
 * @author Thomas Lamson
 */
public class CriterionValue {
    private Item item;
    private EnumCriterion criterion;
    private EnumValue value;
    
    public CriterionValue(Item itemIn, EnumCriterion criterionIn, EnumValue valueIn) {
        item = itemIn;
        criterion = criterionIn;
        value = valueIn;
    }
    
    public Item getItem() {
        return item;
    }
    
    public EnumCriterion getCriterion() {
        return criterion;
    }
    
    public EnumValue getValue() {
        return value;
    }
}
