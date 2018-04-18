import java.io.*;
import java.util.*;

// Node for each of the binaries
class SampleNode implements Serializable
{

	// ID for the filter
	String filterID;

	// Code ID
	String codeID;

	// Name of the binary
	String name;

	// Similarity edges
	ArrayList<SampleEdge> edges;

	// Node Constructor for each binary
	SampleNode(ArrayList<String> code) throws Exception
	{
		this.name = code.get(0);
		this.codeID = Graph.saveCode(code);
		this.filterID = Graph.saveFilter(Graph.filter(code));
		this.edges = new ArrayList<SampleEdge>();
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