import java.io.*;
import java.util.*;

// Represents an edge between two families
class FamilyEdge implements Serializable
{
	// Family dest node
	FamilyNode dest;

	// The saving of the code
	String codeID;

	// Percentage similarity
	double similarity;
	
	FamilyEdge(FamilyNode dest) throws Exception
	{
		// The dest FamilyNode for this edge
		this.dest = dest;

		// Save and load from disk
		this.similarity = -1;
	}
}