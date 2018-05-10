import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.*;

// Head machine, where graph analysis and construction occurs
public class HeadMachine
{
	// Global list of all analysis machines
	static ArrayList<ObjectOutputStream> onlineSockets = new ArrayList<ObjectOutputStream>();

	// List of machines that are currently available
	static ArrayList<ObjectOutputStream> availableSockets = new ArrayList<ObjectOutputStream>();

	// The graph that is created to represent our ecosystem.
	static Graph graph;

	// Just a big 'ole lock...
	static Semaphore lock = new Semaphore(1);

	public static void main(String [] args)
	{
		if(args.length == 0)
		{
			try
			{
				// Scanner used to get input from the user
				Scanner in = new Scanner(System.in);

				// Continually loop to send input (listener will handle responses)
				int select = -1;

				while(select != 0)
				{
					// Ask the user for input
					System.out.println("Select an option:");
					System.out.println("\t0. Exit");
					System.out.println("\t1. Add family to graph");
					System.out.println("\t2. Add unknown binaries");
					System.out.println("\t3. Print graph");
					System.out.println("\t4. Output classifications to csv");
					System.out.println("\t5. Add analysis machine");

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
					parseInput(in, select);
				}

				// Exit
				System.exit(0);
			}

			// Print exception if one occurred
			catch(Exception e)
			{
				System.out.println(e);
			}
		}

		// Config file has been provided
		else
		{
			try
			{	
				// Create a scanner on the file
				Scanner configReader = new Scanner(new File(args[0]));

				// Reading in the config file
				HashMap<String, String> setup = new HashMap<String, String>();
				ArrayList<String> families = new ArrayList<String>();
				ArrayList<String> machines = new ArrayList<String>();

				// Sender thread
				HeadMachineSender sender;

				// Read in the setup args
				while(configReader.hasNext())
				{
					switch(configReader.nextLine())
					{
						case "<Setup>": 
							while(configReader.hasNext())
							{
								String in = configReader.nextLine();

								if(in.equals("</Setup>"))
								{
									break;
								}

								String [] parsing = in.split(":");
								setup.put(parsing[0], parsing[1]);
							}

							break;
						case "<Families>":
							while(configReader.hasNext())
							{
								String in = configReader.nextLine();

								if(in.equals("</Families>"))
								{
									break;
								}

								// Add each of them to the families class
								families.add(in);
							}

							break;
						case "<Machines>":
							while(configReader.hasNext())
							{
								String in = configReader.nextLine();

								if(in.equals("</Machines>"))
								{
									break;
								}

								// Add each of them to the families class
								machines.add(in);
							}

							break;

						default: configReader.nextLine();
					}
				}

				System.out.println(setup);
				System.out.println(families);
				System.out.println(machines);

				// Setup the Graph
				graph = new Graph(Integer.parseInt(setup.get("EDGE_SIMILARITY_THRESHOLD")), Integer.parseInt(setup.get("WINDOW_SIZE")), Integer.parseInt(setup.get("FILTER_SIZE")));
				
				// Setup the Machines
				for(String machine : machines)
				{
					// parse host / port
					String [] hostPort = machine.split(":");

					try
					{
						// We need to open N connections with all servers... Ideally leave it open
						Socket socket = new Socket(InetAddress.getByName(hostPort[0]), Integer.parseInt(hostPort[1]));

						// Create and start the new listener
						HeadMachineListener listener = new HeadMachineListener(socket);

						// Acquire the big 'ole lock
						lock.acquire();

						// Add the analysis machine to the online sockets and available list
						onlineSockets.add(listener.outputStream);
						availableSockets.add(listener.outputStream);

						// Release the big 'ole lock
						lock.release();

						// Start the listener
						listener.start();
					}

					// Print exception if one occurred
					catch(Exception e)
					{
						System.out.println(e);
					}
				}

				// Start analyzing families
				for(String family : families)
				{
					// Create a sender for that socket
					sender = new HeadMachineSender(family, family);

					// Start the sender
					sender.start();
				}

				// Check to see if executiong has finished
				while(true)
				{
					// Sleep for a bit
					TimeUnit.SECONDS.sleep(5);

					// Check if all the sockets are just chillin
					if(availableSockets.size() == onlineSockets.size())
					{
						break;
					}
				}

				// Create a printwriter to output with
				PrintWriter pw = new PrintWriter(new File("test-classifications.csv"));

				// Create a StringBuilder for speed stuff
				StringBuilder sb = new StringBuilder();

				// Iterate over each family node
				for(FamilyNode i : graph.nodes)
				{
					sb.append(i.name + ",");

					// Iterate over the Sample Nodes
					for(BinaryNode j : i.binaries)
					{
						sb.append(j.name + ",");
					}

					// New row
					sb.append("\n");
				}

				// Write out the string
				pw.write(sb.toString());

				// Close the printwriter
				pw.close();

				System.out.println("Finished!");
				System.exit(0);
			}

			catch(Exception e)
			{
				System.out.println("Incorrectly formmatted config file...");
				System.exit(0);
			}
		}
	}

    // Handle input from user and send tasks to analysis machines
	public static void parseInput(Scanner in, int select)
	{
		// Used to check whether a family already exists
		FamilyNode append = null;

		// Get input file list
		ArrayList<File> binaries;

		// The input from the user
		String input;

		// The input file
		File inputFile;

		// Sender thread
		HeadMachineSender sender;

		// Switch on the users selection
		switch(select)
		{
			// Get a malware family from the user
			case 1:

				// Get the malware family directory
				System.out.print("Input Directory: ");
				input = in.next();

				// Name to be associated with the family
				System.out.print("Family: ");
				String family = in.next();

				// Create a sender for that socket
				sender = new HeadMachineSender(input, family);

				// Start the sender
				sender.start();

				break;

			// Add unknown files
			case 2:

				System.out.print("Input Binary Path: ");
				input = in.next();

				// Create a sender for that socket
				sender = new HeadMachineSender(input, "unknown");

				// Start the sender
				sender.start();

				break;

			// Print out the graph
			case 3:

				// Might get a null pointer exception, it's okay! Just fail and try again
				try
				{
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
								// Make sure its not the same node
								System.out.println("\t\t" + k.dest.name);
								System.out.println("\t\t" + k.similarity);
								System.out.println("\n");
							}
						}

						System.out.print("\n-----------------------------------------------\n");
					}
				}

				// If a null pointer occurs
				catch(Exception e)
				{
					System.out.println(e);
					break;
				}

				break;

			// Print out the graph
			case 4:

				// Might get a null pointer exception, it's okay! Just fail and try again
				try
				{
					// Create a printwriter to output with
					PrintWriter pw = new PrintWriter(new File("test-classifications.csv"));

					// Create a StringBuilder for speed stuff
					StringBuilder sb = new StringBuilder();

					// Iterate over each family node
					for(FamilyNode i : graph.nodes)
					{
						sb.append(i.name + ",");

						// Iterate over the Sample Nodes
						for(BinaryNode j : i.binaries)
						{
							sb.append(j.name + ",");
						}

						// New row
						sb.append("\n");
					}

					// Write out the string
					pw.write(sb.toString());

					// Close the printwriter
					pw.close();
				}

				// If a null pointer occurs
				catch(Exception e)
				{
					System.out.println(e);
					break;
				}

				break;

			case 5:
				// Get the host and port
				System.out.print("Input Host: ");
				String host = in.next();
				System.out.print("Input Port: ");
				int port = Integer.parseInt(in.next());

				try
				{
					// We need to open N connections with all servers... Ideally leave it open
					Socket socket = new Socket(InetAddress.getByName(host), port);

					// Create and start the new listener
					HeadMachineListener listener = new HeadMachineListener(socket);

					// Acquire the big 'ole lock
					lock.acquire();

					// Add the analysis machine to the online sockets and available list
					onlineSockets.add(listener.outputStream);
					availableSockets.add(listener.outputStream);

					// Release the big 'ole lock
					lock.release();

					// Start the listener
					listener.start();
				}

				// Print exception if one occurred
				catch(Exception e)
				{
					System.out.println(e);
				}

				break;

			// If the user enters anything else, just exit
			default: break;
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

		// Print connection status
		System.out.println(socket);
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

				// If a node is received
				if (data instanceof BinaryNode)
				{
					// Must have received a node, read it in
					HeadMachineReceive.addNode(outputStream, (BinaryNode) data, (String) inputStream.readObject());
				}

				// If it's any other object, see what it is
				else
				{
					System.out.println(data);
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

class HeadMachineSender extends Thread
{
	// The input file name
	String input;

	// The family name
	String family;

	// Constructor HeadMachineListener
	HeadMachineSender(String input, String family)
	{
		this.input = input;
		this.family = family;
	}

	public void run()
	{
		// Create a new file for the input
		File inputFile = new File(input);

		// Used to check whether a family already exists
		FamilyNode append = null;

		// Get input file list
		ArrayList<File> binaries;

		// See whether the family already exists
		append = null;

		// Add the family to the graph
		try
		{
			// Checks if the input is a directory
			if (inputFile.isDirectory())
			{
				binaries = new ArrayList<File>(Arrays.asList(inputFile.listFiles()));
			}


			// Else check if it is a file
			else if (inputFile.exists())
			{
				binaries = new ArrayList<File>();
				binaries.add(inputFile);
			}

			// Else the file doesn't exist
			else
			{
				System.out.println("File does not exist!");
				return;
			}

			// Check if the family is unknown
			if(!family.equals("unknown"))
			{
				// For each FamilyNode in the graph, check if the family we are adding already exists
				for(FamilyNode node : HeadMachine.graph.nodes)
				{
					if (node.name.equals(family))
					{
						append = node;
						break;
					}
				}

				// If the family does not already exist, create a new one
				if (append == null)
				{
					// Create the new family node
					append = new FamilyNode(family);

					// Add the family node to the family list
					Graph.addFamily(append);
				}

				// The the family value
				family = append.name;
			}

			// Send binaries to analysis machines
			while (binaries.size() != 0)
			{
				// Acquire the big 'ole lock
				HeadMachine.lock.acquire();

				if (HeadMachine.availableSockets.size() != 0)
				{
					// Get an available socket
					ObjectOutputStream send_socket = HeadMachine.availableSockets.get(0);

					// Get a binary
					File send_binary = binaries.get(0);

					// Make sure we actually got a socket and a file
					if(send_binary != null && send_socket != null)
					{
						// Remove the socket from the list
						HeadMachine.availableSockets.remove(0);

						// Remove the binary from the list
						binaries.remove(0);

						// Send the binary to the analysis machine
						HeadMachineSend.sendBinary(send_socket, send_binary, family);

						// Release the big 'ole lock
						HeadMachine.lock.release();
					}
				}

				else
				{
					// Release the big 'ole lock
					HeadMachine.lock.release();
				}
			}
		}

		// Some exception has occurred
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}
