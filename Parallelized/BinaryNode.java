import java.io.*;
import java.util.*;

// Node for each of the binaries
class BinaryNode implements Serializable
{

	// File name for the bloom filter representing the binary
	String filterID;

	BitSet filter;

	// File name for the code that is extracted from the binary
	String codeID;

	ArrayList<String> code;

	// Name of the binary
	String name;

	// Similarity edges
	ArrayList<BinaryEdge> edges;

	// Node constructor for each binary
	BinaryNode(ArrayList<String> code) throws Exception
	{
		this.name = code.get(0);
		this.code = code;
		this.codeID = Graph.saveCode(this.code);
		this.filter = Graph.filter(this.code);
		this.filterID = Graph.saveFilter(this.filter);
		this.edges = new ArrayList<BinaryEdge>();
	}

	// these are likely unnecessary for binarynode
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
