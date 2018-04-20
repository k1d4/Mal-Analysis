import java.io.*;
import java.util.*;

// Node representation for each family
class FamilyNode implements Serializable
{
	// Saved file for the filter
	String filterID;

	// Name of the code file
	String codeID;

	// The binaries associated with the family
	ArrayList<SampleNode> samples;

	// Name of the family
	String name;

	// Edges to the other families
	ArrayList<FamilyEdge> edges;

	// Constructor for the family node
	FamilyNode(String name) throws Exception
	{
		this.name = name;

		// Save and load from disk
		this.filterID = Graph.saveFilter(new BitSet(Graph.FILTER_SIZE));

		// Save and load from disk
		this.codeID = Graph.saveCode(new ArrayList<String>());

		// Each binary in the family
		this.samples = new ArrayList<SampleNode>();

		// Edges to other families
		this.edges = new ArrayList<FamilyEdge>();
	}

	ArrayList<String> getCode() throws Exception
	{
		return Graph.loadCode(codeID);
	}

	BitSet getFilter() throws Exception
	{
		return Graph.loadFilter(filterID);
	}
}