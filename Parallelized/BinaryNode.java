import java.io.*;
import java.util.*;

// Node for each of the binaries
class BinaryNode implements Serializable
{

	// File name for the bloom filter representing the binary
	String filterID;

	// The actual filter
	BitSet filter;

	// File name for the code that is extracted from the binary
	String codeID;

	// The actual code
	ArrayList<String> code;

	// Name of the binary
	String name;

	// Similarity edges
	ArrayList<BinaryEdge> edges;

	// Node constructor for each binary
	BinaryNode(ArrayList<String> code) throws Exception
	{
		// Name of the file
		this.name = code.get(0);

		// ArrayList of the decompiled code
		this.code = code;

		// This will need to be used once the head machine gets the node
		this.codeID = null;

		// The bitset representation of the decompiled code
		this.filter = Graph.filter(this.code);

		// Will need to be used at the head machine to save memory
		this.filterID = null;

		// Initialize at the head machine when constructing the graph
		this.edges = null;
	}
}
