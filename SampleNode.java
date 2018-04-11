import java.io.*;
import java.util.*;

class SampleNode implements Serializable
{

	String filterID;
	String codeID;
	String name;
	ArrayList<SampleEdge> edges;

	SampleNode(ArrayList<String> code) throws Exception
	{
		this.name = code.get(0);
		this.codeID = Graph.saveCode(code);
		this.filterID = Graph.saveFilter(Graph.filter(code));
		this.edges = new ArrayList<SampleEdge>();
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