package centralesupelec.engine.argumentation;

import java.util.ArrayList;

/** An item that needs to be negotiated
 *
 * @author Thomas Lamson
 */ 
public class Item {
    private String name;
    private String description;
    
    public Item(String nameIn, String descriptionIn) {
        name = nameIn;
        description = descriptionIn;
    }
    
    @Override
    /** Reimplemented to still match items that are not the exact same instance */
    public int hashCode() {
        return name.hashCode();
    }
    
    @Override
    /** Reimplemented to still match items that are not the exact same instances */
    public boolean equals(Object other) {
        return other instanceof Item && name.equals(((Item) other).name);
    }
    
    public String toString() {
        return name + ";" + description;
    }
    
    /** Parse a String into a list of items. Format: ItemName;ItemDescription|OtherItem;OtherDescription|... */
    public static ArrayList<Item> parseItems(String content) {
        ArrayList<Item> items = new ArrayList<>();
        
        String[] itemStrings = content.split("\\|");
        for (String str : itemStrings) {
            String[] elts = str.split(";");
            items.add(new Item(elts[0], elts[1]));
        }
        
        return items;
    }
    
    // ------- GETTERS ------- // ------- SETTERS ------- //

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
