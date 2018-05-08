import java.io.*;
import java.util.*;

// Edge between each of the samples
class BinaryEdge implements Serializable
{
	// Dest node
	BinaryNode dest;

	// ID for the similar code between two nodes
	String codeID;

	// Similarity percantage between two binaries
	double similarity;

	// Check whether the edge has been updated
	boolean updated;

	// Constructor for the sample edge
	BinaryEdge(BinaryNode dest) throws Exception
	{
		// Destination for this node
		this.dest = dest;

		// Whether the edge has been updated
		this.updated = false;

		// Similarity score between two nodes
		this.similarity = -1;

		// Save and load from disk
		this.codeID = null;
	}
}
