import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


/**
 * Possibility class to simplify entropy calculations
 */
public class Possibility
{
    private String name;
    public int numberOfEntries; //Exemplo size total tipo nPessoas que têm esta possibilidade no atributo em questao
    private Set<String> labelSet;
    private Map<String,Integer> labelMap;
    private double leafEntropy;
    private ArrayList<String> results; //Só os desta possibilidade


    Possibility(String name, ArrayList<String> results, Set<String> labelSet)
    {
        this.name = name;
        this.results = results;
        this.labelSet = labelSet;
        numberOfEntries = results.size();
        labelMap = new HashMap<>();
        separateLabels();
        leafEntropy = calculateEntropy();
    }
    private void separateLabels()
    {
        for(String label: results)
            labelMap.compute(label,(k,v) -> v == null ? 1 : v + 1);
    }
    private double calculateEntropy()
    {
        double entropy = 0;
        for(Entry<String,Integer> entry: labelMap.entrySet())
        {
            int v = entry.getValue();
            double p = (double) v / (double) numberOfEntries;
            entropy += p * DecisionTree.customLog(2,p);
        }
        return -entropy;
    }
    public double getEntropy()
    {
        return leafEntropy;
    }
    public String getName()
    {
        return name;
    }
    @Override
    public String toString()
    {
        return "Name: " + name + " (" + numberOfEntries + ")\n" + 
            "   Label Set: " + labelSet.toString() + "\n" +
            "   Results: " + results.toString() + "\n" +
            "   Entropy: " + leafEntropy + "\n" +
            "   Label map " + labelMap.toString() + "\n\n";
    }
}
