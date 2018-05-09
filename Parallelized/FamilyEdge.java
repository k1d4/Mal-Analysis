import java.io.*;
import java.util.*;

// Represents an edge between two families
class FamilyEdge implements Serializable
{
	// Family dest node
	FamilyNode dest;

	// Percentage similarity
	double similarity;
	
	FamilyEdge(FamilyNode dest)
	{
		// The dest FamilyNode for this edge
		this.dest = dest;

		// the similarity score between families
		this.similarity = -1;
	}
}