import java.io.*;
import java.util.*;

// Edge between each of the samples
class SampleEdge implements Serializable
{
	// The source node
	SampleNode source;

	// Dest node
	SampleNode dest;

	// ID for the code file
	String codeID;

	// Similarity between two binaries
	double similarity;

	// Check whether the edges has been updated
	boolean updated;

	// Constructor for the sample edge
	SampleEdge(SampleNode source, SampleNode dest) throws Exception
	{
		this.dest = dest;
		this.source = source;
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