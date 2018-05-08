import java.io.*;
import java.util.*;

// Node representation for each family
class FamilyNode implements Serializable
{
	// Saved file for the filter
	String filterID;

	// The actual filter
	BitSet filter;

	// Name of the code file
	String codeID;

	// The actual code
	ArrayList<String> code;

	// The binaries associated with the family
	ArrayList<BinaryNode> samples;

	// Name of the family
	String name;

	// Edges to the other families
	ArrayList<FamilyEdge> edges;

	// Constructor for the family node
	FamilyNode(String name) throws Exception
	{
		// Set the name for the family
		this.name = name;
	}
}