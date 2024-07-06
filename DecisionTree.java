import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class DecisionTree
{
    /**
     * Esta constante serve para utilizar no compareTo em que tem que retornar um inteiro
     * para transformar  o double em um inteiro grande o suficiente para n ser arredondado pelo (int).
     */
    public final static int DOUBLE_TO_INT_CONSTANT = 64;
    public final static int DISCRETIZATION_THRESHOLD = 10;
    public final static float INTERVAL_NUMBER = 10.0f;
    public static void main(String args[]) throws Exception
    {
        if(args.length == 0 || args.length > 2)
            throw new Exception("Wrong number of arguments");
        /**
         * Input fields
         */
        FileReader input = new FileReader(args[0]);
        BufferedReader csvFile = new BufferedReader(input);

        /**
         * Data storage field initialization
         */
        ArrayList<String> results = new ArrayList<>(); //results
        Set<String> labelSet = new HashSet<>();
        HashMap<String, ArrayList<String>> exampleMap = new HashMap<>();
        HashMap<String, HashSet<String>> possibilityMap = new HashMap<>(); // Cada atributo (String) possui um conjunto de possibilidades (HashSet)

        /**
         * Attribute storage
         */
        String stLine = csvFile.readLine();
        String[] attributeArray = stLine.split(",");
        ArrayList<String> attributeList = new ArrayList<>();
        for(int i = 1; i < attributeArray.length - 1; i++)
            attributeList.add(attributeArray[i]);


        /**
         * Map's lists initialization
         */

        for(int i = 0; i < attributeList.size(); i++)
        {
            ArrayList<String> currAtribute = new ArrayList<>();
            exampleMap.put(attributeList.get(i), currAtribute);
            HashSet<String> currPossibility = new HashSet<>();
            possibilityMap.put(attributeList.get(i), currPossibility);
        }

        /**
         * Data processing and insertion on respective Map Lists
         */
        String line = csvFile.readLine();
        while(line != null)
        {
            String[] splittedLine = line.split(",");
            int lineSize = splittedLine.length;
            for(int i = 0; i < splittedLine.length - 2; i++) //- 2 porque ignora ID e class
            {
                ArrayList<String> currAtribute = exampleMap.get(attributeList.get(i));
                currAtribute.add(splittedLine[i + 1]); //+1 para passar à frente o id
                HashSet<String> currPossibility = possibilityMap.get(attributeList.get(i));
                currPossibility.add(splittedLine[i + 1]);
            }
            results.add(splittedLine[lineSize - 1]);
            labelSet.add(splittedLine[lineSize - 1]);
            line = csvFile.readLine();
        }

        csvFile.close(); //file close to prevent data leaks

        /**
         * Discretization of the data values
         */
        for(String attribute : attributeList)
        {
            ArrayList<String> exampleList = exampleMap.get(attribute);
            HashSet<String> possibilityList = possibilityMap.get(attribute);
            if(possibilityList.size() >= DISCRETIZATION_THRESHOLD)
                discretization(exampleList, possibilityList);
        }

        /**
         * Classification is not null when entropy = 0 and there is a definitive answer in which case
         * that is the classification being passed.
        */ 
        Node root = buildTree(attributeList, exampleMap, possibilityMap, labelSet, results, "", 0);
        System.out.println(root.toString());
        if(args.length == 2)
        {
            ArrayList<LineData> testData = readInputData(args[1], attributeList, root, possibilityMap);
            System.out.println(getAnswer(testData, root));
        }
    }

    /**
     * Funtion that inputs the examples and possibilities of a certain attribute and divides them into intervals for more concise readings
     */

    public static void discretization(ArrayList<String> exampleList, HashSet<String> possibilityList)
    {
        for(int i = 0; i < exampleList.get(0).length(); i++)
        {
            char curChar = exampleList.get(0).charAt(i);
            if(!Character.isDigit(curChar) && curChar != '.')
            return;
        }
        Collections.sort(exampleList);
        possibilityList.clear();

        float smallest = Float.parseFloat(exampleList.get(1));
        float biggest = Float.parseFloat(exampleList.get(exampleList.size() - 1));
        float amplitude = biggest - smallest;
        float step = amplitude / INTERVAL_NUMBER;
        float counter = smallest;
        int i = 0;
        while(counter <= biggest)
        {
            String lowerBound = String.format("%.2f", counter);
            String higherBound = String.format("%.2f", counter + step);
            lowerBound = lowerBound.replace(',', '.');
            higherBound = higherBound.replace(',', '.');
            String interval = "[" + lowerBound + ", " + higherBound + "]";
            possibilityList.add(interval);
            while(i < exampleList.size() && Float.parseFloat(exampleList.get(i)) < (counter + step))
            {
                exampleList.set(i, interval);
                i++;
            }
            counter += step;
        }

        ArrayList<String> sortedList = new ArrayList<>(possibilityList);
        Collections.sort(sortedList);

        

        String lowerInterval = "< " + String.format("%.2f", (smallest + step));
        String biggerInterval = "> " + String.format("%.2f", (biggest));
        possibilityList.clear();
        possibilityList.add(lowerInterval);

        for(int j = 1; j < sortedList.size() - 1; j++)
            possibilityList.add(sortedList.get(j));
        possibilityList.add(biggerInterval);

        String firstValue = exampleList.get(0);
        String lastValue = exampleList.get(exampleList.size() - 1);
        for(int j = 0; j < exampleList.size(); j++)
        {
            String example = exampleList.get(j);
            if(example.equals(firstValue))
                exampleList.set(j, lowerInterval);
            if(example.equals(lastValue))
                exampleList.set(j, biggerInterval);
        }
    }
    /**
     * 
     * 
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * END OF MAIN 
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * 
     * 
     */


    /**
     * Decision Tree building function using ID3 algorythm
     * @param attributeList
     * @param exampleMap
     * @param possibilityMap
     * @param labelSet
     * @param results
     * @param mostFrequentLabelParent
     * @param depth
     * @return
     */
    public static Node buildTree(ArrayList<String> attributeList, HashMap<String, ArrayList<String>> exampleMap, HashMap<String,HashSet<String>> possibilityMap ,Set<String> labelSet, ArrayList<String> results, String mostFrequentLabelParent, int depth)
    {
        if(isExampleMapEmpty(exampleMap))
            return new LeafNode(mostFrequentLabelParent, depth, results.size());

        String sameClassification = hasSameClassification(results);
        if(sameClassification != null)
            return new LeafNode(sameClassification, depth, results.size());
        if(attributeList.isEmpty())
            return new LeafNode(mostFrequentLabel(results), depth, results.size());
        /*
            Basicamente crio todos os atributos possiveis e vejo a entropia
            de todos e organizo numa arvore, depois vou buscar o primeiro (mais a esquerda) que é a menor entropia
        */
        double totalEntropy = calculateTotalEntropy(results, labelSet);

        TreeSet<Attribute> attributeTree = new TreeSet<>(new AttributeComparator());
        for(String attribute : attributeList)
        {
            Attribute curAttribute = new Attribute(attribute, possibilityMap.get(attribute), exampleMap.get(attribute), labelSet, results, totalEntropy);
            attributeTree.add(curAttribute);
        }
            
        //First element removed = least entropy / most information gain
        Attribute bestAttribute = attributeTree.first();
        String attribute = bestAttribute.getName();
        HashMap<String,Node> nodeMap = new HashMap<>();

        //Recursive function call
        for(String possibility: bestAttribute.getPossibilityNameSet())
        {
            //Filtering all of the data
            ArrayList<String> filteredArrayList = filterAttributeList(attributeList,attribute);
            HashMap<String,ArrayList<String>> filteredExampleMap = filterExampleMap(exampleMap,attribute,possibility);
            HashMap<String,HashSet<String>> filteredPossibilityMap = filterPossibilityMap(possibilityMap,attribute);
            ArrayList<String> filteredResults = filterResults(results,exampleMap.get(attribute),possibility);
            
            nodeMap.put(possibility, buildTree(filteredArrayList, filteredExampleMap , filteredPossibilityMap ,labelSet, filteredResults ,mostFrequentLabel(results), depth + 1));
        }
        return new AttributeNode(bestAttribute.getName(), nodeMap, depth);
    }
    /**
     * 
     * 
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * END OF MAIN ID3 FUNCTION
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * 
     * 
     */



    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * AUXILIARY FUNCTIONS
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     */
    private static String getAnswer(ArrayList<LineData> testData, Node root)
    {
        String output = "";
        for(LineData lineData: testData)
            output+= lineData.toString() + "\n";
        return output;
    }
    public static boolean isExampleMapEmpty(HashMap<String,ArrayList<String>> map)
    {
        for(Entry<String,ArrayList<String>> entry: map.entrySet())
            if(entry.getValue().size() != 0)
                return false;
        return true;
    }

    /**
     * Simple helper function to filter an element from a list
     */
    public static ArrayList<String> filterAttributeList(ArrayList<String> list, String valueToRemove)
    {
        ArrayList<String> newList = new ArrayList<>();
        for(String value: list)
            if(!value.equals(valueToRemove))
                newList.add(value);
        return newList;
    }

    public static HashMap<String,ArrayList<String>> filterExampleMap(HashMap<String,ArrayList<String>> map, String attributeToRemove, String possibility)
    {
        HashMap<String,ArrayList<String>> newMap = new HashMap<String,ArrayList<String>>();
        ArrayList<String> columnToRemove = map.get(attributeToRemove);
        for(Entry<String,ArrayList<String>> entry: map.entrySet())
        {
            String attribute = entry.getKey();
            ArrayList<String> column = entry.getValue();
            if(attribute.equals(attributeToRemove))
                continue;
            ArrayList<String> newColumn = new ArrayList<>();
            for(int i = 0; i < column.size(); i++)
            {
                if(columnToRemove.get(i).equals(possibility))
                    newColumn.add(column.get(i));
            }
            newMap.put(attribute,newColumn); 
        }
        return newMap;
    }

    public static HashMap<String,HashSet<String>> filterPossibilityMap(HashMap<String,HashSet<String>> map, String valueToRemove)
    {
        HashMap<String,HashSet<String>> newMap = new HashMap<String,HashSet<String>>();
        for(String value: map.keySet())
            if(!value.equals(valueToRemove))
                newMap.put(value,map.get(value));
        return newMap;
    }

    public static ArrayList<String> filterResults(ArrayList<String> results, ArrayList<String> examples, String possibility)
    {
        ArrayList<String> newList = new ArrayList<>();
        for(int i = 0; i< examples.size(); i++)
            if(examples.get(i).equals(possibility))
                newList.add(results.get(i));
        return newList;
    }

    public static String mostFrequentLabel(ArrayList<String> results)
    {
        return mostFrequentLabel(listToMap(results));
    }

    public static String mostFrequentLabel(HashMap<String,Integer> labelMap)
    {
        String mostFrequentLabel = "";
        int max = Integer.MIN_VALUE;
        for(Entry<String,Integer> entry: labelMap.entrySet())
        {
            if(entry.getValue() > max)
            {
                max = entry.getValue();
                mostFrequentLabel = entry.getKey();
            }
        }
        return mostFrequentLabel;
    }

    public static HashMap<String,Integer> listToMap(ArrayList<String> list)
    {
        HashMap<String,Integer> labelMap = new HashMap<>();
        for(String label: list)
            labelMap.compute(label, (k,v) -> v == null ? 1 : v + 1);
        return labelMap;
    }
    
    /**
     * To be used in one of the leaf cases
     * @param results - list of results
     * @return null if has multiple different labels as results; that label otherwise.
     */
    public static String hasSameClassification(ArrayList<String> results)
    {
        String classification = results.get(0);
        for(String label: results)
            if(!label.equals(classification))
                return null;
        return classification;
    }

    private static double calculateTotalEntropy(ArrayList<String> results, Set<String> labelSet)
    {
        double entropy = 0;
        HashMap<String,Integer> labelMap = new HashMap<>();
        for(String label: results)
            labelMap.compute(label, (k,v) -> v == null ? 1 : v + 1);
        for(String label: labelSet)
        {
            Integer ocur = labelMap.get(label);
            if(ocur == null)
                continue;
            double p = (double) ocur / results.size();
            entropy += p * customLog(2,p);
        }
        return -entropy;
    }
    
    public static double customLog(double base, double logNumber)
    {
        return Math.log(logNumber) / Math.log(base);
    }

    private static ArrayList<LineData> readInputData(String arg, ArrayList<String> attributeList, Node root, HashMap<String, HashSet<String>> possibilityMap) throws Exception
    {
        //Read file and turn csv into array or list of (list of strings)
        //Convert that String matrix into a list of HashMaps<String,String>

        /**
         * Input fields
         */
        FileReader fReader = new FileReader(arg);
        BufferedReader testFile = new BufferedReader(fReader);

        /**
         * Structures initialization
         */
        ArrayList<LineData> fileData = new ArrayList<>();

        /**
         * Data processing and insertion on respective structures
         */
        String line = testFile.readLine();
        while(line != null)
        {
            HashMap<String,String> lineDataMap = new HashMap<>();
            String[] splittedLine = line.split(",");
            int lineSize = splittedLine.length;
            for(int i = 0; i < lineSize - 1; i++)
            {
                lineDataMap.put(attributeList.get(i),splittedLine[i + 1]);
            }
            for (HashMap.Entry<String, String> entry : lineDataMap.entrySet()) 
            {
                HashSet<String> possibilityList = possibilityMap.get(entry.getKey());
                inputDiscretization(lineDataMap, entry, possibilityList);
            }
            LineData lineData = new LineData(splittedLine[0], lineDataMap, root);
            fileData.add(lineData);
            line = testFile.readLine();
        }
        testFile.close(); //file close to prevent data leaks
        return fileData;
    }

    public static void inputDiscretization(HashMap<String,String> data, HashMap.Entry<String, String> entry, HashSet<String> possibilityList)
    {
        for(int i = 0; i < entry.getValue().length(); i++)
        {
            char curChar = entry.getValue().charAt(i);
            if(!Character.isDigit(curChar) && curChar != '.')
                return;
        }

        if(entry.getValue().charAt(0) != '[' && entry.getValue().charAt(0) == '<' && entry.getValue().charAt(0) == '>')
            return;

        float max = Float.MIN_VALUE;
        float min = Float.MAX_VALUE;
        for(String possibility : possibilityList)
        {
            if(possibility.charAt(0) != '[')
                continue;
            float smallestVal = stToValue(possibility);
            float biggestVal = lastToValue(possibility);    
            max = Math.max(max, biggestVal);
            min = Math.min(min, smallestVal);

            String valueString = entry.getValue();

            float value = Float.parseFloat(valueString);
            if(value >= smallestVal && value < biggestVal)
            {
                data.replace(entry.getKey(), possibility);
                break;
            }
        }
        if(entry.getValue().charAt(0) != '[')
        {
            if(Float.parseFloat(entry.getValue()) < min)
            {
                data.replace(entry.getKey(), "< " + String.format("%.2f", min));
                possibilityList.add("< " + String.format("%.2f", min));
            }
            else
            {
                data.replace(entry.getKey(), "> " + String.format("%.2f", max));
                possibilityList.add("> " + String.format("%.2f", max));
            }
        }
    }

    public static float stToValue(String interval)
    {
        String valueString = "";
        char curChar = interval.charAt(1);
        int i = 1;
        while(curChar != ',')
        {
            valueString += curChar;
            i++;
            curChar = interval.charAt(i);
        }
        float value = Float.parseFloat(valueString);
        return value;
    }

    public static float lastToValue(String interval)
    {
        String valueString = "";
        char curChar = interval.charAt(1);
        int i = 1;
        while(curChar != ',')
        {
            i++;
            curChar = interval.charAt(i);
        }
        i++;
        curChar = interval.charAt(i);
        while(curChar != ']')
        {
            if(curChar != ' ')
                valueString += curChar;
            i++;
            curChar = interval.charAt(i);
        }
        float value = Float.parseFloat(valueString);
        return value;
    }
    /**
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     * END AUXILIARY FUNCTIONS
     * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     */

}

/**
 * 
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * AUXILIRY CLASSES
 * ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 * 
 */

/**
 * Used to create the attribute tree set
 */
class AttributeComparator implements Comparator<Attribute>
{
    @Override
    public int compare(Attribute a, Attribute b)
    {
        return a.compareTo(b);
    }
}

/**
 * Used as the object that stores the information of a given line from the second file argument
 */
class LineData
{
    private String id;
    private String answer = null;
    LineData(String id, HashMap<String,String> data, Node root)
    {
        this.id = id;
        answer = root.getAnswer(data);
    }
    @Override
    public String toString()
    {
        if(answer == null)
            return "Answer unavailable!";
        return id + ": " + answer;
    }
}
