package centralesupelec.engine.argumentation;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/** Represents the preferences of an agent for a list of items against a list of criterion
 *
 * @author Thomas Lamson
 */
public class Preferences {
    /** Ordered criteria, in descending order of importance (more important ones are at the beginning) */
    private ArrayList<EnumCriterion> criteria = new ArrayList<>();
    
    /** Values given to an item in a criterion */
    private ArrayList<CriterionValue> values = new ArrayList<>();
    
    /** Retrieves the most important criterion, excluding of list of them */
    public Optional<EnumCriterion> getBestCriterionExcept(ArrayList<EnumCriterion> blackList) {
        for (EnumCriterion criterion : criteria) {
            if (!blackList.contains(criterion)) {
                return Optional.of(criterion);
            }
        }
        return Optional.empty();
    }
    
    /** Retrieves an integer representation of a criterion importance */
    private int criterionImportance(EnumCriterion criterion) {
        int i = criteria.indexOf(criterion);
        return i == -1 ? 0 : criteria.size() - i;
    }
    
    /** Retrieves the score of an item at a given criterion. Returns an empty Optional if not value for this item and criterion */
    public Optional<EnumValue> getScoreAtCriterion(Item item, EnumCriterion criterion) {
        Optional<CriterionValue> critValue = values.stream().filter(cv -> cv.getItem().equals(item) && cv.getCriterion() == criterion).findFirst();
        if (critValue.isPresent()) {
            return Optional.of(critValue.get().getValue());
        } else {
            return Optional.empty();
        }
    }
    
    /** Computes the weighted sum score of an item */
    public int computeScore(Item item) {
        int score = 0;
        
        for (CriterionValue criterionValue : values) {
            if (criterionValue.getItem().equals(item)) {
                score += criterionValue.getValue().getValue() * criterionImportance(criterionValue.getCriterion());
            }
        }
        
        return score;
    }
    
    /** Retrieves the list of evaluated items */
    public ArrayList<Item> getItems() {
        return getItems(new ArrayList<Item>());
    }
    
    /** Retrieves the list of evaluated items, minus a given blacklist */
    public ArrayList<Item> getItems(ArrayList<Item> blackList) {
        ArrayList<Item> items = new ArrayList<>();
        
        for (CriterionValue val : values)
            if (!items.contains(val.getItem()) && !blackList.contains(val.getItem()))
                items.add(val.getItem());
        
        return items;
    }
    
    /** Find the best item (based on score) among a whitelist of items. Can return an empty Optional if no item in whitelist. */
    public Optional<Item> findBest(ArrayList<Item> whiteList) {
        ArrayList<Item> items = new ArrayList<>();
        
        for (Item item : getItems()) {
            if (whiteList.contains(item)) {
                items.add(item);
            }
        }
        
        if (items.size() == 0) {
            return Optional.empty();
        }
        
        int maxScore = 0;
        Item bestItem = null;
        for (Item item : items) {
            int score = computeScore(item);
            if (bestItem == null || score > maxScore) {
                maxScore = score;
                bestItem = item;
            }
        }
        
        return Optional.of(bestItem);
    }

    /** Checks if a given item is acceptable. The rule is that the item must be in the top 10% of the given item list */
    public boolean canAccept(Item item, ArrayList<Item> items) {
        
        // Computing scores of the list and sorting them
        ArrayList<Integer> scores = new ArrayList<>();
        for (Item it : items) {
            scores.add(computeScore(it));
        }
        Integer[] sortedScores = scores.toArray(new Integer[scores.size()]);
        Arrays.sort(sortedScores);
        
        // Extracting the top 10% rank score
        int position = sortedScores.length - Math.min((int) Math.ceil(0.1f * sortedScores.length), sortedScores.length);
        int minScore = sortedScores[position];
        
        // Check if given item scores better than top 10% rank score
        return computeScore(item) >= minScore;
    }

    
    /** Is criterionA more important than criterionB */
    public boolean isCriterionBetter(EnumCriterion criterionA, EnumCriterion criterionB) {
        return criterionImportance(criterionA) > criterionImportance(criterionB);
    }
    
    /** Initializes preferences for a given list of items randomly, using all known criteria */
    public void randomize(ArrayList<Item> items) {
        List<EnumCriterion> critList = Arrays.asList(EnumCriterion.values());
        Collections.shuffle(critList);
        criteria = new ArrayList<>(critList);
        
        for(Item it : items)
            for (EnumCriterion crit : criteria)
                values.add(new CriterionValue(it, crit, EnumValue.pickRandom()));
    }
    
    /** Initializes preferences for a given list of items from a given preference file.
     * Returns false if any problem was encountered during parsing and prints a detailed error message. */
    public boolean loadFromFile(String filePath, ArrayList<Item> items) {
        Scanner scanner = null;
        try {
            File file = new File(filePath);
            scanner = new Scanner(file);
            int line = 0;
            
            while (scanner.hasNextLine()) {
                line++;
                
                // Removing spaces and tabulations
                String cleanLine = scanner.nextLine().replace(" ", "").replace("\t", "");
                
                // Ignoring commented lines and empty lines
                if (!cleanLine.startsWith("#") && !cleanLine.contentEquals("")) {
                    
                    // If line is describing criteria ordering
                    if (cleanLine.contains(">")) {
                        String[] splitCriterions = cleanLine.split(">");
                        for (String splitCrit : splitCriterions) {
                            Optional<EnumCriterion> optCrit = EnumCriterion.findByName(splitCrit);
                            if (optCrit.isPresent()) {
                                criteria.add(optCrit.get());
                            } else {
                                // ERROR: Criterion not recognized
                                System.out.println("Criterion \"" + splitCrit + "\" at line " + Integer.toString(line) + " of preference file \"" + filePath + "\" was not recognized.");
                                return false;
                            }
                        }
                    
                    // If line is describing item evaluations
                    } else {
                        String[] elements = cleanLine.split(":");
                        if (elements.length == 2) {
                            
                            String itemName = elements[0];
                            Item item = null;
                            for (Item it : items) {
                                if (it.getName().equals(itemName)) {
                                    item = it;
                                    break;
                                }
                            }
                            
                            if (item != null) {
                                String[] critValues = elements[1].split(",");
                                if (critValues.length == criteria.size()) {
                                    ArrayList<EnumCriterion> alreadyGraded = new ArrayList<>();
                                    
                                    for (String critValue : critValues) {
                                        String[] critValueElements = critValue.split("=");
                                        if (critValueElements.length == 2) {
                                            String critName = critValueElements[0];
                                            String valueName = critValueElements[1];
                                            
                                            Optional<EnumCriterion> optCrit = EnumCriterion.findByName(critName);
                                            if (!optCrit.isPresent()) {
                                                
                                                // ERROR: Criterion not recognized
                                                System.out.println("Criterion \"" + critName
                                                        + "\" in item \"" + itemName
                                                        + "\" at line " + Integer.toString(line)
                                                        + " of preference file \"" + filePath
                                                        + "\" was not recognized.");
                                                return false;
                                            } else if (alreadyGraded.contains(optCrit.get())) {
                                                
                                                // ERROR: Criterion evaluated twice
                                                System.out.println("Criterion \"" + critName
                                                        + "\" in item \"" + itemName
                                                        + "\" at line " + Integer.toString(line)
                                                        + " of preference file \"" + filePath
                                                        + "\" was graded twice.");
                                                return false;
                                            }
                                            
                                            alreadyGraded.add(optCrit.get());
                                            
                                            Optional<EnumValue> optValue = EnumValue.findByName(valueName);
                                            if (!optValue.isPresent()) {
                                                
                                                // ERROR: Value not recognized
                                                System.out.println("Value \"" + valueName
                                                        + "\" in item \"" + itemName
                                                        + "\" at line " + Integer.toString(line)
                                                        + " of preference file \"" + filePath
                                                        + "\" was not recognized.");
                                                return false;
                                            }
                                            
                                            // Valid criterion value parsed
                                            values.add(new CriterionValue(item, optCrit.get(), optValue.get()));
                                            
                                        } else {
                                            // ERROR: Syntax error on criterion value
                                            System.out.println("Item \"" + itemName
                                                    + "\" at line " + Integer.toString(line)
                                                    + " of preference file \"" + filePath
                                                    + "\" has a syntax error at criterion value " + Integer.toString(alreadyGraded.size() + 1) + ".");
                                            return false;
                                        }
                                    }
                                    
                                } else {
                                    // ERROR: Wrong number of criteria
                                    System.out.println("Item \"" + itemName
                                            + "\" at line " + Integer.toString(line)
                                            + " of preference file \"" + filePath
                                            + "\" doesn't have the right number of criterion values.");
                                    return false;
                                }
                            } else {
                                // ERROR: Item not recognized
                                System.out.println("Item \"" + itemName
                                        + "\" at line " + Integer.toString(line)
                                        + " of preference file \"" + filePath
                                        + "\" was not recognized.");
                                return false;
                            }
                        } else {
                            // ERROR: Incorrect format
                            System.out.println("Didn't find \"ItemName: Crit=VALUE,...\" format at line "
                                    + Integer.toString(line) + " of preference file \""
                                    + filePath + "\".");
                            return false;
                        }
                    }
                }
            }
            
            // Successfully parsed the preferences file
            return true;
            
        } catch(FileNotFoundException e) {
            // ERROR: File not found
            System.out.println("Preference file at path \"" + filePath + "\" was not found.");
            return false;
            
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }
    
    /** Builds a printable String to represent the preferences (not valid for file saving!) */
    public String toString() {
        String result = "--- PREFERENCES ---\n";
        for (EnumCriterion criterion : criteria) {
            result += criterion.getName() + " > ";
        }
        result = result.substring(0, result.length() - 3) + "\n";
        
        for (Item item : getItems()) {
            result += "\n" + item.getName() + ":\n";
            for (EnumCriterion criterion : criteria) {
                result += "\t" + criterion.getName() + " = " + getScoreAtCriterion(item, criterion).get() + "\n";
            }
        }
        result += "\n-------------------";
        return result;
    }
}
