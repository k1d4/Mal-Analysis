import java.io.*;
import java.util.*;

// Node for each of the binaries
class BinaryNode implements Serializable
{

	// File name for the bloom filter representing the binary
	String filterID;

	// File name for the code that is extracted from the binary
	String codeID;

	// Name of the binary
	String name;

	// Similarity edges
	ArrayList<BinaryEdge> edges;

	// Node constructor for each binary
	BinaryNode(ArrayList<String> code) throws Exception
	{
		this.name = code.get(0);
		this.codeID = Graph.saveCode(code);
		this.filterID = Graph.saveFilter(Graph.filter(code));
		this.edges = new ArrayList<BinaryEdge>();
	}

	// Loads the code for the node
	ArrayList<String> getCode() throws Exception
	{
		return Graph.loadCode(codeID);
	}

	// Loads the filter for the code
	BitSet getFilter() throws Exception
	{
		return Graph.loadFilter(filterID);
	}
}