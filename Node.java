import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Custom made Node class for the decision tree building
 */
public abstract class Node
{
    abstract public String getAnswer(HashMap<String, String> inputData);
}
class AttributeNode extends Node
{
    public String name;
    public int depth;
    public String indentation = "";
    public HashMap<String, Node> nodeMap; //Each possibility is an edge that points to another node
    public AttributeNode(String name, HashMap<String, Node> nodeMap, int depth)
    {
        this.name = name;
        this.nodeMap = nodeMap;
        this.depth = depth;
    }
    public String getAnswer(HashMap<String, String> inputData)
    {
        return nodeMap.get(inputData.get(name)).getAnswer(inputData);
    }
    @Override
    public String toString()
    {
        for(int i = 0; i < depth; i++)
            indentation += "      ";
        String output = "\n" + indentation + "<" + name + ">\n";
        for(Entry<String, Node> entry: nodeMap.entrySet())
        {
            output+= indentation + "   " + entry.getKey() + ": ";
            output+= entry.getValue().toString();
        }
        return output;
    }
}
class LeafNode extends Node
{
    String answer;
    public int depth;
    public String indentation = "";
    public int entries;
    public LeafNode(String answer, int depth, int entries)
    {
        this.answer = answer;
        this.depth = depth;
        this.entries = entries;
    }
    public String getAnswer(HashMap<String, String> inputData)
    {
        return answer;
    }
    @Override
    public String toString()
    {
        return indentation + answer + " (" + entries + ")" + "\n";
    }
}