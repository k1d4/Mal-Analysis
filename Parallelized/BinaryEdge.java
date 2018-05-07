import java.io.*;
import java.util.*;

// Edge between each of the samples
class BinaryEdge implements Serializable
{
	// Dest node
	SampleNode dest;

	// ID for the similar code between two nodes
	String codeID;

	// Similarity percantage between two binaries
	double similarity;

	// Check whether the edge has been updated
	boolean updated;

	// Constructor for the sample edge
	// !!NEED TO MAKE SURE THIS OPERATION ISN'T OCCURING TWICE!!
	BinaryEdge(SampleNode source, SampleNode dest) throws Exception
	{
		this.dest = dest;
		this.updated = false;

		// Save and load from disk
		this.similarity = Graph.filterCompare(source.getFilter(), dest.getFilter());

		// Save and load from disk
		this.codeID = Graph.saveCode(Graph.sampleCompare(source.getCode(), dest.getCode()));
	}

	ArrayList<String> getCode() throws Exception
	{
		return Graph.loadCode(codeID);
	}
}
