import java.io.*;
import java.util.*;

class FamilyNode implements Serializable
{
	String filterID;
	String codeID;
	ArrayList<SampleNode> samples;
	String name;
	ArrayList<FamilyEdge> edges;

	FamilyNode(String name) throws Exception
	{
		this.name = name;

		// Save and load from disk
		this.filterID = Graph.saveFilter(new BitSet(Graph.FILTER_SIZE));

		// Save and load from disk
		this.codeID = Graph.saveCode(new ArrayList<String>());
		this.samples = new ArrayList<SampleNode>();
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