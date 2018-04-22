import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.util.concurrent.TimeUnit;


public class HeadMachine
{
	// Global list of all analysis machines
	static ArrayList<AnalysisMachine> analysisMachines = new ArrayList<AnalysisMachine>();

	public static void main(String [] args)
	{
		// Selection from the user
		int select = -1;

		// Input string from the user
		String input = null;

		// Scanner used to get input from the user
		Scanner in = new Scanner(System.in);

		// The graph that is created to represent our ecosystem.
		Graph graph = new Graph();

		// Check for the correct arguments
		if (args.length != 2)
		{
			System.out.println("Please input host and port...");
			System.exit(0);
		}

		// Get the host and port
		String host = args[0];
		int port = Integer.parseInt(args[1]);

		try
		{
			// We need to open N connections with all servers.. ideally leave it open
			// Open a socket with the host
			Socket socket = new Socket(InetAddress.getByName(host), port);

			// Fork the listener
			HeadMachineListener listener = new HeadMachineListener(socket);
			listener.start();

			// Continually loop to send input (listener will handle responses)
			scan: while(select != 0)
			{
				// Ask the user for input
				System.out.println("Select an option:");
				System.out.println("\t0. Exit");
				System.out.println("\t1. Load graph");
				System.out.println("\t2. Save graph");
				System.out.println("\t3. Add family to graph");
				System.out.println("\t4. Add unknown samples");
				System.out.println("\t5. Print Graph");

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

				// do heavy lifting
				parseInput(select, graph, socket);

				// Wait on the child
				synchronized(listener)
				{
					listener.wait();
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
	public static void parseInput(int select, Graph graph, Socket socket)
	{
		// Scanner used to get input from the user
		Scanner in = new Scanner(System.in);

		// Switch on the users selection
		switch(select)
		{
			// Load a saved graph
			case 1:

				try
				{
					// Get the saved file name from the user
					System.out.print("Name of saved graph: ");
					input = in.next();
					graph = Graph.loadGraph(input);
				}

				catch (Exception e)
				{
					e.printStackTrace();
				}

				break scan;

			// Save the graph to a file
			case 2:
				try
				{
					// Get the saved name file
					System.out.print("Name of file: ");
					input = in.next();
					graph.saveGraph(input, graph);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				break scan;

			// Get a malware family from the user
			// distributed function
			case 3:
				// Get the malware family directory
				System.out.print("Input Directory: ");
				input = in.next();

				// Name to be associated with the family
				System.out.print("Family: ");
				String family = in.next();

				// Add the family to the graph
				try
				{
					// graph.addFamily(new File(input), family);

					// Get input file list
					ArrayList<File> samples;

					// Used to check whether a family already exists
					FamilyNode append = null;

					// Checks if the input is a directory
					if (input.isDirectory())
					{
						samples = new ArrayList<File>(Arrays.asList(input.listFiles()));
					}

					// Else check if it is a file
					else if (input.exists())
					{
						samples = new ArrayList<File>();
						samples.add(input);
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
						if (i.name.equals(name))
						{
							append = i;
							break;
						}
					}

					// If the family does not already exist, create a new one
					if (append == null)
					{
						append = new FamilyNode(name);
						append.edges = familyEdges(append);
						graph.nodes.add(append);
					}

					//
					// ALLOCATE MACHINES TO HANDLE SAMPLES
					// SEND REQUESTS TO DEOBFUSCATE ETC
					//
					// Loop over the samples, deobfuscate
					// for (File i : samples)
					// {
					// 	try
					// 	{
					// 		ArrayList<String> code = deobfuscate(i);
					// 		SampleNode newNode = new SampleNode(code);
					// 		newNode.edges = sampleEdges(newNode, append);
					// 		append.samples.add(newNode);
					// 	}
					//
					// 	catch(Exception e)
					// 	{
					// 		System.out.println(e);
					// 	}
					//
					// }

					// Update family
					try
					{
						graph.updateFamily(append);
					}

					catch(Exception e)
					{
						System.out.println(e);
					}

					graph.updateFamilyEdges(append);
				}
				catch (Exception e)
				{
					System.out.println(e);
				}
				break scan;

			// Add unknown files
			// distributed function
			case 4:
				System.out.print("Input Directory: ");
				input = in.next();

				// graph.addSample(new File(input));
				// Get input file list
				ArrayList<File> samples;
				FamilyNode append = null;

				// Check if the file to be added exists
				if (input.exists())
				{
					samples = new ArrayList<File>();
					samples.add(input);
				}

				// The file doesn't exist
				else
				{
					System.out.println("File does not exist");
					return;
				}


				//
				// ALLOCATE MACHINES TO HANDLE SAMPLES
				// SEND REQUESTS TO DEOBFUSCATE ETC
				//
				// try
				// {
				// 	// Deobfuscate the input file
				// 	ArrayList<String> code = deobfuscate(input);
				// 
				// 	// Create a new node for the binary
				// 	SampleNode newNode = new SampleNode(code);
				//
				// 	// Check what family the newNode is most similar to
				// 	append = familyCheck(newNode);
				//
				// 	// If it is similar to a certain family, add it to that family
				// 	if(append != null)
				// 	{
				// 		newNode.edges = sampleEdges(newNode, append);
				// 		append.samples.add(newNode);
				// 		updateFamily(append);
				// 		updateFamilyEdges(append);
				// 	}
				//
				// 	// Else throw it in the unknown bin
				// 	else
				// 	{
				// 		unknown.add(newNode);
				// 	}
				// }
				//
				// // Catch any errors
				// catch (Exception e)
				// {
				// 	System.out.println(e);
				// }

				break scan;

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
						System.out.println("\t" + j.source.name);
						System.out.println("\t" + j.dest.name);
					}

					System.out.print("\n\n");

					// Iterate over the Sample Nodes
					for(SampleNode j : i.samples)
					{
						System.out.println("\t" + j.name);

						// Iterate over the sample edges
						for(SampleEdge k : j.edges)
						{
							System.out.println("\t\t" + k.source.name);
							System.out.println("\t\t" + k.dest.name);
							System.out.println("\t\t" + k.similarity);
							System.out.println("\n");
						}
					}

					System.out.print("\n\n\n\n-----------------------------------------------");
					break scan;
				}
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

	// Constructor HeadMachineListener
	HeadMachineListener(Socket socket)
	{
		this.socket = socket;
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
			synchronized(this)
			{
        		while(true)
				{
					// Create an ObjectInputStream and read an object from it
					inputStream = new ObjectInputStream(socket.getInputStream());
					Object data = inputStream.readObject();

					// Check if the data is a string
					if(data instanceof String)
					{
						switch(data)
						{
							// Just send a heartbeat back
							case "HEARTBEAT": HeadMachineSend.heartbeat();

							// Send an error, unknown string
							default: HeadMachineSend.error();
						}
					}

					// If it's any other object, send back an error
					else
					{
						HeadMachineSend.error();
					}
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
