import java.io.*;
import java.util.*;

// Represents an edge between two families
class FamilyEdge implements Serializable
{
	// Family source node 
	FamilyNode source;

	// Family dest node
	FamilyNode dest;

	// The saving of the code
	String codeID;

	// Percentage similarity
	double similarity;
	
	FamilyEdge(FamilyNode source, FamilyNode dest) throws Exception
	{
		this.dest = dest;
		this.source = source;

		// Save and load from disk
		this.codeID = Graph.saveCode(new ArrayList<String>());

		// Save and load from disk
		this.similarity = -1;

		// Add the edge to the dest node
		dest.edges.add(this);
	}

	ArrayList<String> getCode() throws Exception
	{
		return Graph.loadCode(codeID);
	}
}