import java.io.*;
import java.util.*;

// Edge between each of the samples
class BinaryEdge implements Serializable
{
	// Dest node
	BinaryNode dest;

	// Similarity percantage between two binaries
	double similarity;

	// Constructor for the sample edge
	BinaryEdge(BinaryNode dest)
	{
		// Destination for this node
		this.dest = dest;

		// Similarity score between two nodes
		this.similarity = -1;
	}
}
