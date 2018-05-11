import java.io.*;
import java.util.*;

// Node for each of the binaries
class BinaryNode implements Serializable
{

	// The actual filter
	BitSet filter;

	// The actual code
	ArrayList<String> code;

	// Name of the binary
	String name;

	// Similarity edges
	ArrayList<BinaryEdge> edges;

	// Node constructor for each binary
	BinaryNode(ArrayList<String> code, int window_size) throws Exception
	{
		// Name of the file
		this.name = code.get(0);

		// ArrayList of the decompiled code
		this.code = code;

		// The bitset representation of the decompiled code
		this.filter = Graph.filter(this.code, window_size);
	}
}
