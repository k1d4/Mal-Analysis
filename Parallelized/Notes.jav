// Returns an ArrayList<String> that contains the similar code between the code
// !!NEED TO REVIEW THIS AGAIN!!
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
		FileOutputStream fos = new FileOutputStream(input);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(graph);
        oos.close();
		return input;
	}

	// Load in an existing binary filter representation
	static BitSet loadFilter(String filter) throws Exception
	{
		FileInputStream fis = new FileInputStream(filter);
        ObjectInputStream ois = new ObjectInputStream(fis);
        return (BitSet) ois.readObject();
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

	// Save the code with a given name
	static String saveCode(ArrayList<String> code, String id) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(id);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(code);
        oos.close();
        return id;
	}

	// Update family when new nodes have been added
	static void updateFamily(FamilyNode family) throws Exception
	{
		// The code for the family
		ArrayList<String> updater = family.getCode();

		// Update the familys aggragate filter
		BitSet updatefilter = family.getFilter();

		// Iterate over each of the sample nodes
		for(BinaryNode node : family.samples)
		{

			// Iterate over each of the edges
			for (BinaryEdge b : node.edges)
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

import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.util.concurrent.TimeUnit;

// Head machine, where graph analysis and construction occurs
public class HeadMachine
{
	// Global list of all analysis machines
	static ArrayList<ObjectOutputStream> onlineSockets = new ArrayList<ObjectOutputStream>();

	// List of machines that are currently available
	static ArrayList<ObjectOutputStream> availableSockets = new ArrayList<ObjectOutputStream>();

	// The graph that is created to represent our ecosystem.
	static Graph graph = new Graph();

	// Get input file list
	static ArrayList<File> binaries;

	// Used to check whether a family already exists
	static FamilyNode append = null;

	// Check if adding family or unknown sample
	static String binaryType = null;

	public static void main(String [] args)
	{
		// Check for the correct arguments
		if (args.length != 2)
		{
			System.out.println("Please input host and port of Analysis Machine 1...");
			System.exit(0);
		}

		// Get the host and port
		String host = args[0];
		int port = Integer.parseInt(args[1]);

		try
		{
			// We need to open N connections with all servers... Ideally leave it open
			Socket socket = new Socket(InetAddress.getByName(host), port);

			// Fork a listener for that socket
			HeadMachineListener listener = new HeadMachineListener(socket);

			// Add the analysis machine to the online sockets and available list
			onlineSockets.add(listener.outputStream);
			availableSockets.add(listener.outputStream);

			// Start the listener
			listener.start();

			// Scanner used to get input from the user
			Scanner in = new Scanner(System.in);

			// Continually loop to send input (listener will handle responses)
			int select = -1;

			while(select != 0)
			{
				// Ask the user for input
				System.out.println("Select an option:");
				System.out.println("\t0. Exit");
				System.out.println("\t1. Load graph");
				System.out.println("\t2. Save graph");
				System.out.println("\t3. Add family to graph");
				System.out.println("\t4. Add unknown samples");
				System.out.println("\t5. Print graph");
				System.out.println("\t6. Add analysis machine");

				// Get which option the user wants
				try
				{
					select = in.nextInt();
				}

				catch(Exception e)
				{
					System.out.println("");
					System.out.println("***** Error! *****");
					System.out.print("* ");
					System.out.println("Please input a valid option...");
					System.out.println("******************");
					in.nextLine();
					continue;
				}

				// Do heavy lifting
				parseInput(select);

				// Need to do something better than this
				switch(select)
				{
					case 3: case 4: case 6:
						// Wait on the child
						synchronized(listener)
						{
							listener.wait();
						}
					default:
				}
			}
		}

		// Print exception if one occurred
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

    // Handle input from user and send tasks to analysis machines
	public static void parseInput(int select)
	{
		// Scanner used to get input from the user
		Scanner in = new Scanner(System.in);
		String input = "";
		File inputFile;

		// Switch on the users selection
		switch(select)
		{
			// Load a saved graph
			case 1:

				// try
				// {
				// 	// Get the saved file name from the user
				// 	System.out.print("Name of saved graph: ");
				// 	input = in.next();
				// 	graph = Graph.loadGraph(input);
				// }

				// catch (Exception e)
				// {
				// 	e.printStackTrace();
				// }

				break;

			// Save the graph to a file
			case 2:
				// try
				// {
				// 	// Get the saved name file
				// 	System.out.print("Name of file: ");
				// 	input = in.next();
				// 	graph.saveGraph(input, graph);
				// }
				// catch (Exception e)
				// {
				// 	e.printStackTrace();
				// }

				break;

			// Get a malware family from the user
			// distributed function
			case 3:
				binaryType = "family";

				// Get the malware family directory
				System.out.print("Input Directory: ");
				input = in.next();
				inputFile = new File(input);

				// Name to be associated with the family
				System.out.print("Family: ");
				String family = in.next();

				append = null;

				// Add the family to the graph
				try
				{
					// Checks if the input is a directory
					if (inputFile.isDirectory())
					{
						samples = new ArrayList<File>(Arrays.asList(inputFile.listFiles()));
					}

					// Else check if it is a file
					else if (inputFile.exists())
					{
						samples = new ArrayList<File>();
						samples.add(inputFile);
					}

					// Else it doesn't exist
					else
					{
						System.out.println("File does not exist");
						return;
					}

					// For each FamilyNode in the graph, check if the Family we are adding already exists
					for(FamilyNode i : graph.nodes)
					{
						if (i.name.equals(family))
						{
							append = i;
							break;
						}
					}


					// If the family does not already exist, create a new one
					if (append == null)
					{
						append = new FamilyNode(family);
						// append.edges = Graph.familyEdges(append);
						graph.nodes.add(append);
					}

					// Send to available machine
					while (samples.size() != 0)
					{
						if (availableSockets.size() != 0)
						{
							ObjectOutputStream send_socket = availableSockets.remove(0);
							File send_sample = samples.remove(0);
							HeadMachineSend.sendBinary(send_socket, send_sample);
						}
					}
				}
				catch (Exception e)
				{
					System.out.println(e);
				}
				break;

			// Add unknown files
			case 4:
				binaryType = "unknown";

				System.out.print("Input Binary Path: ");
				input = in.next();
				inputFile = new File(input);

				append = null;

				try
				{
					// Check if the file to be added exists
					if (inputFile.exists())
					{
						samples = new ArrayList<File>();
						samples.add(inputFile);
					}

					// The file doesn't exist
					else
					{
						System.out.println("File does not exist");
						return;
					}

					while (samples.size() != 0)
					{
						if (availableSockets.size() != 0)
						{
							ObjectOutputStream send_socket = availableSockets.get(0);
							File send_sample = samples.get(0);
							HeadMachineSend.sendBinary(send_socket, send_sample);
							availableSockets.remove(send_socket);
							samples.remove(send_sample);
						}
					}
				}
				catch (Exception e)
				{
					System.out.println(e);
				}

				break;

			// Print out the graph
			case 5:

				// Iterate over each family node
				for(FamilyNode i : graph.nodes)
				{
					System.out.println(i.name);

					System.out.print("\n\n");

					// Iterate over family edges
					for(FamilyEdge j : i.edges)
					{
						System.out.println("\t" + j.similarity);
						System.out.println("\t" + j.dest.name);
					}

					System.out.print("\n\n");

					// Iterate over the Sample Nodes
					for(BinaryNode j : i.binaries)
					{
						System.out.println("\t" + j.name);

						// Iterate over the sample edges
						for(BinaryEdge k : j.edges)
						{
							System.out.println("\t\t" + k.dest.name);
							System.out.println("\t\t" + k.similarity);
							System.out.println("\n");
						}
					}

					System.out.print("\n\n\n\n-----------------------------------------------");
				}

				break;

			case 6:
			// Presumably sets up another server
			// Get the host and port
				System.out.print("Input Host: ");
				String host = in.next();
				System.out.print("Input Port: ");
				int port = Integer.parseInt(in.next());

				try
				{
					Socket socket = new Socket(InetAddress.getByName(host), port);
					onlineSockets.add(new ObjectOutputStream(socket.getOutputStream()));
					availableSockets.add(new ObjectOutputStream(socket.getOutputStream()));

					// Fork the listener
					HeadMachineListener listener = new HeadMachineListener(socket);
					listener.start();
				}

				// Print exception if one occurred
				catch(Exception e)
				{
					System.out.println(e);
				}

				break;
		}
	}
}

/**
 * HeadMachineListener constants.
 * <p>
 * A HeadMachineListener takes in a
 * <ul>
 * <li>(Socket) socket
 * </ul>
 * <p>
 * HeadMachineListener is the listener established to handle data packets received
 * from the server. Opens an input stream on the socket to receive packet data,
 * unpacks it, and based on the op codes runs the appropriate success of failure
 * method in HeadMachineReceive.
 */
class HeadMachineListener extends Thread
{
	// Need the socket to listen on
	Socket socket;
	ObjectOutputStream outputStream;
	ObjectInputStream inputStream;

	// Constructor HeadMachineListener
	HeadMachineListener(Socket socket)
	{
		socket = socket;
		try
		{
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
		}

		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Run the newly forked thread. Handle data packets received from the server.
	 * Opens an input stream on the socket to receive packet data, unpacks it,
	 * and based on the op codes runs the appropriate success or failure
	 * method in HeadMachineReceive.
	 * Additionally, check if there's a heartbeat from the server to check that
	 * the server is still connected.
	 */
	public void run()
	{
		// Keep the connection open
		try
		{

			while(true)
			{
				Object data = inputStream.readObject();

				// Check if the data is a string
				if(data instanceof String)
				{
					switch((String) data)
					{
						// Just send a heartbeat back
						case "HEARTBEAT": HeadMachineSend.heartbeat(outputStream);

						// Send an error, unknown string
						default: HeadMachineSend.error(outputStream);
					}
				}

				else if (data instanceof BinaryNode)
				{
					HeadMachineReceive.addNode(outputStream, (BinaryNode) data);
				}

				// If it's any other object, send back an error
				else
				{
					HeadMachineSend.error(outputStream);
				}

				synchronized(this)
				{
					this.notify();
				}
			}
		}
    		

		// Print the exception
		catch(Exception e)
		{
			// Just exit quietly...
		}
	}
}
