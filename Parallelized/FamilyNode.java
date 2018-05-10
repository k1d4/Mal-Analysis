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
	FamilyNode(String name)
	{
		// Set the name for the family
		this.name = name;

		// Create a blank filter for the family
		this.filter = new BitSet(Graph.FILTER_SIZE);

		// Edges to the other families
		edges = new ArrayList<FamilyEdge>();

		// The binaries associated with the family
		binaries = new ArrayList<BinaryNode>();
	}
}