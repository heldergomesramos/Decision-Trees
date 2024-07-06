import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * This class represents and stores information about a specific attribute of the entire input
 */
public class Attribute implements Comparable<Attribute>
{
    private String name; /* Name of the attribute. E.g. Pat */
    private Set<String> possibilityNameSet; /* The possible values of the attribute. E.g. Full, Some, None */
    private Set<Possibility> possibilitySet; /* The possible values of the attribute as Possibility objects. */
    private ArrayList<String> examples; /* The examples given by input */
    private Set<String> labelSet; /* The possible results the attribute can give for its values */
    private ArrayList<String> results; /* The result given by each value, in the same order as examples */
    private int size; /* The total number of values */
    private double attributeEntropy; /* The entropy calculated for this attribute */
    private double informationGain;

    public Attribute(String name, Set<String> possibilityNameSet, ArrayList<String> examples, Set<String> labelSet, ArrayList<String> results, double totalEntropy)
    {
        this.name = name;
        this.possibilityNameSet = possibilityNameSet;
        this.examples = examples;
        this.labelSet = labelSet;
        this.results = results;
        this.size = examples.size();
        possibilitySet = new HashSet<>();
        for(String possibilityName : possibilityNameSet)
            possibilitySet.add(new Possibility(possibilityName, filter(examples, results, possibilityName), labelSet));
        attributeEntropy = calculateEntropy();
        informationGain = totalEntropy - attributeEntropy;
    }

    /**
     * Filters the examples for the ones we want in possibility
     * @param examples - the set of all examples
     * @param results - the result for each example (also a set)
     * @param possibility - the possibility we want to filter
     * @return ArrayList of examples filtered
     */
    private ArrayList<String> filter(ArrayList<String> examples, ArrayList<String> results, String possibility)
    {
        ArrayList<String> filtered = new ArrayList<>();
        for (int i = 0; i < size; i++)
            if(examples.get(i).equals(possibility))
                filtered.add(results.get(i));
        return filtered;
    }

    /**
     * The total entropy, given all the examples, for this attribute
     * @return total entropy
     */
    private double calculateEntropy()
    {
        double entropy = 0;
        for(Possibility possibility: possibilitySet) 
            entropy += (double) possibility.numberOfEntries / size * possibility.getEntropy();
        return entropy;
    }
    public double getEntropy()
    {
        return attributeEntropy;
    }
    public double getInformationGain()
    {
        return informationGain;
    }
    public String getName()
    {
        return name;
    }
    public Set<String> getPossibilityNameSet()
    {
        return possibilityNameSet;
    }
    public ArrayList<String> getExamples()
    {
        return examples;
    }

    @Override
    public String toString()
    {
        return "Name: " + name + " (" + size + ")\n" + 
            "Possibility name set: " + possibilityNameSet.toString() + "\n" +
            "Examples: " + examples.toString() + "\n" +
            "Label Set: " + labelSet.toString() + "\n" +
            "Results: " + results.toString() + "\n" +
            "Entropy: " + attributeEntropy + "\n" + 
            "Information Gain: " + informationGain + "\n";
            //"Possibility objects: \n" + possibilitySet.toString();
    }
    @Override
    public int compareTo(Attribute other)
    {
        if(informationGain == other.getInformationGain())
            return name.compareTo(other.getName());
        return -(int) ((informationGain - other.getInformationGain()) * DecisionTree.DOUBLE_TO_INT_CONSTANT);
    }
    @Override
    public boolean equals(Object other)
    {
        return ((Attribute) other).getName().equals(name);
    }
}