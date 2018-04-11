import java.io.*;
import java.util.*;

class SampleEdge implements Serializable
{

	SampleNode source;
	SampleNode dest;
	String codeID;
	double similarity;
	boolean updated;

	SampleEdge(SampleNode source, SampleNode dest) throws Exception
	{
		this.dest = dest;
		this.source = source;
		this.updated = false;

		// Save and load from disk
		this.similarity = Graph.filterCompare(source.getFilter(), dest.getFilter());

		// Save and load from disk
		this.codeID = Graph.saveCode(Graph.sampleCompare(source.getCode(), dest.getCode()));
	}

	ArrayList<String> getCode() throws Exception
	{
		return Graph.loadCode(codeID);
	}
}