import java.io.*;
import java.util.*;

// Node representation for each family
class FamilyNode implements Serializable
{
	// The actual filter
	BitSet filter;

	// The binaries associated with the family
	ArrayList<BinaryNode> binaries;

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