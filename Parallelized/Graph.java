import java.util.*;
import java.io.*;
import java.nio.*;
import java.security.*;
import java.text.*;

// Object the represents the graph
class Graph implements Serializable
{
	// Minimum amount of similar code to consider it a similar segment
	static final int MIN_CODE_THRESHOLD = 64;

	// The percentage similarity an edge must be for it to be considered within a family
	static final double EDGE_SIMILARITY_THRESHOLD = 25;

	// The size of the filter used to hash the malware
	static final int FILTER_SIZE = (int) Math.pow(2, 28);

	// The window size used for the hash
	static final int WINDOW_SIZE = 128;

	// Nodes in the graph
	static ArrayList<FamilyNode> nodes;

	// Unknown nodes in the graph
	static ArrayList<SampleNode> unknown;

	// Constructor for the graph
	Graph()
	{
		this.nodes = new ArrayList<FamilyNode>();
		this.unknown = new ArrayList<SampleNode>();
	}

	// Create a filter for the node
	static BitSet filter(ArrayList<String> code) throws Exception
	{
		// This will contain the n-grams from the doc
		ArrayList<String> grams = new ArrayList<String>();

		// Create and store the n-gram strings
		for(int i = 0; i < code.size(); i++)
		{
			// Check if we are at the end of the file
			if(i + WINDOW_SIZE >= code.size()) break;

			// Initialize n-gram as empty string
			String gram = "";

			// Create grams
			for(int k = 0; k < WINDOW_SIZE; k++) gram += code.get(i + k);

			// Add gram to list
			grams.add(gram);
		}

		// Initialize a binary array, size of 2^28
		BitSet filter = new BitSet(FILTER_SIZE);

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

			// Truncate to 28 bit value
			index = index & 0x0FFFFFFF;

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

		// Return the percentage
		return (denominator != 0) ? (numerator / denominator * 100.0) : 0;
	}

	// Returns an ArrayList<String> that contains the similar code between the code
	static ArrayList<String> sampleCompare(ArrayList<String> source, ArrayList<String> dest) throws Exception
	{
		// List of intersections before filtering for threshold
		ArrayList<ArrayList<String>> copies = new ArrayList<ArrayList<String>>();

		// List of intersections after filtering for threshold
		ArrayList<String> output = new ArrayList<String>();

		// String that will be checked
		ArrayList<String> copy1 = new ArrayList<String>();
		ArrayList<String> copy2 = dest;

		// Strings to be compared
		String s1 = null;
		String s2 = null;

		// Index to check if a sequence is found
		int index = -1;

		// load line into s1
		if (source.size() != 0)
		s1 = source.get(0);

		// while(fileScan1.hasNext())
		for(String instruction : source)
		{
			// Make sure copy2 is not empty
			if(index >= copy2.size())
			{
				if (!copy1.isEmpty() && copy1.size() >= MIN_CODE_THRESHOLD)
				{
					String outputString = "";
					for(String i : copy1) outputString += (i + "\n");
					output.add(outputString);
				}
				break;
			}

			// No previous match has been found
			if(index == -1)
			{
				for(int k = 0; k < copy2.size(); k++)
				{
					s2 = copy2.get(k);

					if(s2.equals(s1))
					{
						copy1.add(s1);
						index = k + 1;
						break;
					}
					index = -1;
				}
			}

			// More code comparison stuff...
			else
			{
				s2 = copy2.get(index);

				if(s2.equals(s1))
				{
					copy1.add(s1);
					index++;
				}

				else
				{
					if(copy1.size() >= MIN_CODE_THRESHOLD)
					{
						String outputString = "";
						for(String i : copy1) outputString += (i + "\n");
						output.add(outputString);
					}

					index = -1;
					copy1.clear();
					continue;
				}
			}

			s1 = instruction;
		}

		// Check last line
		for(int k = 0; k < copy2.size(); k++)
		{
			s2 = copy2.get(k);

			if(s2.equals(s1))
			{
				copy1.add(s1);
				break;
			}
		}

		// If there are any similar segments, add them to the output List
		if (!copy1.isEmpty() && copy1.size() >= MIN_CODE_THRESHOLD)
		{
			String outputString = "";
			for(String i : copy1) outputString += (i + "\n");
			output.add(outputString);
		}

		return output;
	}

	// Read in a saved graph
	static Graph loadGraph(String graph) throws Exception
	{
		FileInputStream fis = new FileInputStream(graph);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return (Graph) ois.readObject();
	}

	// Write out the existing graph
	static String saveGraph(String input, Graph graph) throws Exception
	{
		// String id = uniqueID();
		// FileOutputStream fos = new FileOutputStream(id);
		FileOutputStream fos = new FileOutputStream(input);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(graph);
        oos.close();
        // return id;
		return input;
	}

	// Load in an existing binary filter representation
	static BitSet loadFilter(String filter) throws Exception
	{
		FileInputStream fis = new FileInputStream(filter);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return (BitSet) ois.readObject();
	}

	// Save an existing binary filter representation
	static String saveFilter(BitSet filter) throws Exception
	{
		String id = uniqueID();
		FileOutputStream fos = new FileOutputStream(id);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(filter);
        oos.close();
        return id;
	}

	// Save the filter along with its name
	static String saveFilter(BitSet filter, String id) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(id);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(filter);
        oos.close();
        return id;
	}

	// Load in the saved code for a node
	static ArrayList<String> loadCode(String code) throws Exception
	{
		FileInputStream fis = new FileInputStream(code);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return (ArrayList<String>) ois.readObject();
	}

	// Save the code for a node
	static String saveCode(ArrayList<String> code) throws Exception
	{
		String id = uniqueID();
		FileOutputStream fos = new FileOutputStream(id);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(code);
        oos.close();
        return id;
	}

	// Save the code with a given name
	static String saveCode(ArrayList<String> code, String id) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(id);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(code);
        oos.close();
        return id;
	}

	// Create sample edges for a family
	static ArrayList<SampleEdge> sampleEdges(SampleNode source, FamilyNode family)
	{
		// Create new arraylist
		ArrayList<SampleEdge> edges = new ArrayList<SampleEdge>();

		// Iterate over the nodes already in the graph
		for(SampleNode dest : family.samples)
		{
			try
			{
				// Create and add new edge to the graph
				SampleEdge newEdge = new SampleEdge(source, dest);
				dest.edges.add(newEdge);
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

	// Create the edges for a family
	static ArrayList<FamilyEdge> familyEdges(FamilyNode source)
	{
		// Create new arraylist
		ArrayList<FamilyEdge> edges = new ArrayList<FamilyEdge>();

		// Iterate over the nodes already in the graph
		for(FamilyNode dest : nodes)
		{
			try
			{
				// Create and add new edge to the graph
				edges.add(new FamilyEdge(source, dest));
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
	static FamilyNode familyCheck(SampleNode node) throws Exception
	{
		for(FamilyNode i : nodes)
		{
			if(filterCompare(node.getFilter(), i.getFilter()) >= 25.0)
			{
				System.out.println(node.name + " is likely part of " + i.name);
				return i;
			}
		}

		System.out.println("Family not found for " + node.name);
		return null;
	}

	// Updates the family when new nodes have been added
	static void updateFamily(FamilyNode family) throws Exception
	{
		// The code for the family
		ArrayList<String> updater = family.getCode();

		// Update the familys aggragate filter
		BitSet updatefilter = family.getFilter();

		// Iterate over each of the sample nodes
		for(SampleNode node : family.samples)
		{

			// Iterate over each of the edges
			for (SampleEdge b : node.edges)
			{

				// Check out whether the edge has been updated
				if (!b.updated)
				{

					// Get the code from each of the edges
					for (String z : b.getCode())
					{
						updater.add(z);
					}

					// Set updated to true
					b.updated = true;

					// The similar code from the edge
					ArrayList<String> code = b.getCode();
					ArrayList<String> grams = new ArrayList<String>();
					ArrayList<String> lines = new ArrayList<String>();

					// Array to contain the strings as we scan
					for(String i : code)
					{
						String [] split = i.split("\n");
						for (String x : split)
						{
							lines.add(x);
						}
					}

					// Create and store the n-gram strings
					for(int i = 0; i < lines.size(); i++)
					{
						// Check if we are at the end of the file
						if(i + WINDOW_SIZE >= lines.size()) break;

						// Initialize n-gram as empty string
						String gram = "";

						// Create grams
						for(int k = 0; k < WINDOW_SIZE; k++) gram += lines.get(i + k);

						// Add gram to list
						grams.add(gram);
					}

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

						// Truncate to 28 bit value
						index = index & 0x0FFFFFFF;

						// Set index in filter
						updatefilter.set(index);
					}
				}
			}

			// Write the code to file
			Graph.saveCode(updater, family.codeID);

			// Write the filter to file
			Graph.saveFilter(updatefilter, family.filterID);
		}
	}

	// Update the family edges
	static void updateFamilyEdges(FamilyNode family)
	{
		// Iterate over each of the family edges
		for(FamilyEdge i : family.edges)
		{
			try
			{
				// Check if the source is equal to the family name
				if(i.source.name.equals(family.name))
				{
					i.similarity = filterCompare(family.getFilter(), i.dest.getFilter());
				}

				else
				{
					i.similarity = filterCompare(family.getFilter(), i.source.getFilter());
				}
			}

			catch(Exception e)
			{
				System.out.println(e);
			}
		}
	}

	// Generate a unique ID based upon date and time
	static String uniqueID()
	{
		SimpleDateFormat gen = new SimpleDateFormat("ddMMyy-hhmmss.SSS");
		return gen.format(new Date());
	}
}
