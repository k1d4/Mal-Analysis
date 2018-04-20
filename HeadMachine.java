import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.util.concurrent.TimeUnit;


public class HeadMachine
{

	public static void main(String [] args)
	{
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
			// Open a socket with the host
			Socket socket = new Socket(InetAddress.getByName(host), port);

			// Fork the listener
			HeadMachineListener listener = new HeadMachineListener(socket);
			listener.start();

			// Continually loop to send input (listener will handle responses)
			int select = 0;
			do
			{
				select = getInput(socket);

				// Wait on the child
				synchronized(listener)
				{
					listener.wait();
				}
			}
			while(select != 0);
		}

		// Print exception if one occurred
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

    // Handle input from user and send tasks to analysis machines
	public static int getInput(Socket socket)
	{
        // Selection from the user
		int select = 0;

		// Input string from the user
		String input = null;

		// Output file from the user
		String output = null;

		// Scanner used to get input from the user
		Scanner in = new Scanner(System.in);

		// The graph that is created to represent our ecosystem.
		Graph graph = new Graph();

		scan: while(true)
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
						graph.addFamily(new File(input), family);
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
					graph.addSample(new File(input));
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
		return select;
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
		// Keep the connection with the client open
		try
		{
			while(true)
			{
				// Get input stream from the socket
				InputStream stream = socket.getInputStream();

				// Read the header
				byte[] header = new byte[ChatProtocol.HEADER_SIZE];
				int read = -1;

				while ((read = stream.read(header)) != ChatProtocol.HEADER_SIZE)
				{
					// Read header
				}

				// Check whether it's a heartbeat
				if (header[1] == ChatProtocol.HEARTBEAT)
				{
					HeadMachineSend.heartbeat(socket);
					continue;
				}

				// Get the payload length
				int payloadLen = ByteBuffer.wrap(Arrays.copyOfRange(header,  ChatProtocol.CODE_SIZE, ChatProtocol.HEADER_SIZE)).getInt();
				byte[] data = new byte[payloadLen];
				read = -1;

				while((read = stream.read(data)) != payloadLen)
				{
					// Read payload
				}

				switch(header[1])
				{
					case(ChatProtocol.CREATE_ACCOUNT_SUCCESS):	HeadMachineReceive.createAccountSuccess(socket, data);
								break;
					case(ChatProtocol.PUSH_MESSAGE_NOTIFICATION):	HeadMachineReceive.pushMessageSuccess(socket, data);
								break;
					case(ChatProtocol.CREATE_ACCOUNT_FAILURE):	HeadMachineReceive.createAccountFailure(socket, data);
								break;
					case(ChatProtocol.LOGIN_SUCCESS):	HeadMachineReceive.loginSuccess(socket, data);
								break;
					case(ChatProtocol.LOGIN_FAILURE):	HeadMachineReceive.loginFailure(socket, data);
								break;
					case(ChatProtocol.DELETE_ACCOUNT_SUCCESS): HeadMachineReceive.deleteAccountSuccess(socket, data);
								break;
					case(ChatProtocol.LIST_ALL_ACCOUNTS_SUCCESS): HeadMachineReceive.listAllAccountsSuccess(socket, data);
								break;
					case(ChatProtocol.SEND_MESSAGE_SUCCESS): HeadMachineReceive.sendMessageSuccess(socket, data);
								break;
					case(ChatProtocol.PULL_ALL_MESSAGES_SUCCESS): HeadMachineReceive.pullAllMessagesSuccess(socket, data);
								break;
					default: HeadMachineReceive.generalFailure(socket, data);
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
			System.out.println(e);
		}
	}
}
