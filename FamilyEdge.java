import java.io.*;
import java.util.*;

class FamilyEdge implements Serializable
{
	FamilyNode source;
	FamilyNode dest;
	String codeID;
	double similarity;
	
	FamilyEdge(FamilyNode source, FamilyNode dest) throws Exception
	{
		this.dest = dest;
		this.source = source;

		// Save and load from disk
		this.codeID = Graph.saveCode(new ArrayList<String>());

		// Save and load from disk
		this.similarity = -1;

		dest.edges.add(this);
	}

	ArrayList<String> getCode() throws Exception
	{
		return Graph.loadCode(codeID);
	}
}