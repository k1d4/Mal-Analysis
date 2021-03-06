import java.util.*;
import java.io.*;
import java.nio.*;
import java.security.*;
import java.text.*;
import java.util.concurrent.*;

// Object the represents the graph
class Graph implements Serializable
{
	// The percentage similarity an edge must be for it to be considered within a family
	double similarity;

	// The size of the filter used to hash the malware
	int filter_size = (int) Math.pow(2, 20);

	// The window size used for the hash
	int window_size;

	// Nodes in the graph
	ArrayList<FamilyNode> nodes;

	// Unknown nodes in the graph
	ArrayList<BinaryNode> unknown;

	// Constructor for the graph
	Graph(double similarity, int window_size)
	{
		this.nodes = new ArrayList<FamilyNode>();
		this.unknown = new ArrayList<BinaryNode>();
		this.similarity = similarity;
		this.window_size = window_size;
	}

	// Create a filter for the node
	static BitSet filter(ArrayList<String> code, int window_size) throws Exception
	{
		// This will contain the n-grams from the doc
		ArrayList<String> grams = new ArrayList<String>();

		// Create and store the n-gram strings
		for(int i = 0; i < code.size(); i++)
		{
			// Check if we are at the end of the file
			if(i + window_size >= code.size()) break;

			// Initialize n-gram as empty string
			String gram = "";

			// Create grams
			for(int k = 0; k < window_size; k++)
			{
				gram += code.get(i + k);
			} 

			// Add gram to list
			grams.add(gram);
		}

		// Initialize a binary array, size of 2^28
		BitSet filter = new BitSet(window_size);

		// Store to byte array
		byte[] bytesOfMessage;

		// Message digests are secure one-way hash functions that take arbitrary-sized
		// data and output a fixed-length hash value
		MessageDigest md;
		byte[] thedigest = null;

		for(String i : grams)
		{
			// Encodes this String into a sequence of bytes
			bytesOfMessage = i.getBytes("UTF-8");

			// MessageDigest object that implements MD5
			md = MessageDigest.getInstance("MD5");

			// Hashes the byte array
			thedigest = md.digest(bytesOfMessage);

			// Copies last 4 bytes of computation, as MD5 is 16 bytes
			byte [] trunc = Arrays.copyOfRange(thedigest, 11, 15);

			// Wraps a byte array into a buffer.
			int index = ByteBuffer.wrap(trunc).getInt();

			// Truncate to 20 bit value
			index = index & 0x000FFFFF;

			// Set index in filter
			filter.set(index);
		}

		return filter;
	}

	// Returns a percentage similarity between two nodes
	static double filterCompare(BitSet source, BitSet dest)
	{
		// Create new Bitset for intersection
		BitSet intersection = (BitSet) source.clone();

		// Get the intersection
		intersection.and(dest);

		// Size of the smallest set
		double denominator = (source.cardinality() < dest.cardinality()) ? source.cardinality() : dest.cardinality();

		// Get the size of the intersection
		double numerator = intersection.cardinality();

		System.out.println(numerator + "/" + denominator);

		// Return the percentage
		return (denominator != 0) ? (numerator / denominator * 100.0) : 0;
	}

	// Create binary edges for a family
	ArrayList<BinaryEdge> binaryEdges(BinaryNode source, FamilyNode family)
	{
		// Create new arraylist
		ArrayList<BinaryEdge> edges = new ArrayList<BinaryEdge>();

		// Iterate over the nodes already in the graph
		for(BinaryNode dest : family.binaries)
		{
			try
			{
				// Create and add new edge to the graph
				BinaryEdge newEdge = new BinaryEdge(dest);

				// Set the similarity for the new edge
				newEdge.similarity = Graph.filterCompare(source.filter, dest.filter);

				// Add to the edge array
				edges.add(newEdge);
			}

			catch(Exception e)
			{
				System.out.println(e);
			}
		}

		// Return the list of edges
		return edges;
	}

	// Do a comparision against each composite from each family, check the threshold
	FamilyNode familyCheck(BinaryNode node) throws Exception
	{
		// Iterate over each of the existing families
		for(FamilyNode family : nodes)
		{
			// Compare the binary to the family node
			if(filterCompare(node.filter, family.filter) >= similarity)
			{
				// Family has been found!
				return family;
			}
		}

		// Family has not been found
		return null;
	}

	// Add a binary to a family
	void familyAddBinary(FamilyNode family, BinaryNode node)
	{
		// Add the Binary to the family filter
		family.filter.or(node.filter);

		// Add the BinaryEdges to this node
		node.edges = binaryEdges(node, family);

		// Add the corresponding edges to the dest nodes
		for(BinaryEdge edge : node.edges)
		{
			// Create the new BinaryEdge
			BinaryEdge newEdge = new BinaryEdge(node);

			// Copy the similarity score
			newEdge.similarity = edge.similarity;

			// Add the symmetric edge to the dest node
			edge.dest.edges.add(newEdge);
		}

		// Add the binary to the BinaryNode list
		family.binaries.add(node);

		// Update family similarity
		updateFamilyEdges(family);
	}

	// Add the family to the graph
	void addFamily(FamilyNode family)
	{
		// Create an edge for each family
		for(FamilyNode node : nodes)
		{
			// Create two edges, add them to their respective families
			FamilyEdge dest = new FamilyEdge(node);
			family.edges.add(dest);

			FamilyEdge source = new FamilyEdge(family);
			node.edges.add(source);
		}

		// Add the family to the list
		nodes.add(family);
	}

	void updateFamilyEdges(FamilyNode family)
	{
		// Iterate over families edges, update the similarity scores
		for(FamilyEdge edge : family.edges)
		{
			// Update the edges similarity score
			edge.similarity = filterCompare(family.filter, edge.dest.filter);

			for(FamilyEdge symmetric : edge.dest.edges)
			{
				if (symmetric.dest == family)
				{
					symmetric.similarity = edge.similarity;
					break;
				}
			}
		}
	}
}
