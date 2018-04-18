import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.*;

/**
 * AnalysisMachine constants.
 * <p>
 * A AnalysisMachine object consists of
 * <ul>
 * <li>(ArrayList) users
 * <li>(Semaphore) lock
 *
 * </ul>
 * <p>
 * The AnalysisMachine class handles the operations from the AnalysisMachine side of the chat application.
 * First, the AnalysisMachine opens a connection to each HeadMachine to handle user input. Throughout each HeadMachine-AnalysisMachine session,
 * the AnalysisMachine also maintains a heartbeat via Heartbeat, which pings each HeadMachine repeatedly to check whether
 * the connection is still good. In other words, if the HeadMachine fails or terminates, the heartbeat allows the AnalysisMachine
 * to check whether the HeadMachine is still reachable, and if not, the AnalysisMachine closes the socket.
 * <p>
 * The AnalysisMachine then listens for HeadMachine requests, and handles them via HeadMachineHandler.
 */
public class AnalysisMachine
{
	/**
	 * AnalysisMachine forks a new thread for each HeadMachine who connects successfully.
	 * Sets up a HeadMachineHandler to process the requests from each HeadMachine.
	 */
	public static void main(String [] args)
	{
		// Check for correct number of args
		if (args.length != 1)
		{
			System.out.println("Please input port...");
			System.exit(0);
		}

		// Get port number from user
		int port = Integer.parseInt(args[0]);

		// Create socket port and fork threads for each HeadMachine
		try
		{
			AnalysisMachineSocket AnalysisMachine = new AnalysisMachineSocket(port);

			while(true)
			{
				Socket HeadMachine = AnalysisMachine.accept();
				HeadMachineHandler handler = new HeadMachineHandler(HeadMachine);
				handler.start();
			}
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}

/**
 * HeadMachineHandler constants.
 * <p>
 * A HeadMachineHandler takes in a
 * <ul>
 * <li>(Socket) socket
 * <li>(boolean) heartbeat
 * <li>(String) account
 * </ul>
 * <p>
 * HeadMachineListener is the listener established to handle data packets received
 * from the AnalysisMachine. Opens an input stream on the socket to receive packet data,
 * unpacks it, and based on the op codes runs the appropriate success of failure
 * method in HeadMachineReceive.
 */
class HeadMachineHandler extends Thread
{
	// Need the new socket
	Socket socket;

	// Boolean for whether heartbeat has been received
	boolean heartbeat;

	// Account that is accociated with this thread
	String account = "";

	// Constructor for HeadMachineHandler
	HeadMachineHandler(Socket socket)
	{
		this.socket = socket;
		heartbeat = true;
	}

	/**
	 * Run the newly forked thread. Handle data packets received from the HeadMachine.
	 * Opens an input stream on the socket to receive packet data, unpacks it,
	 * and based on the op codes processes the packet payload using methods in SenderReceive.
	 * Additionally, check if there's a heartbeat from the HeadMachine to check that
	 * the HeadMachine is still connected.
	 */
	public void run()
	{

		// Fork the heartbeat thread
		Heartbeat check = new Heartbeat(socket, this);
		check.start();

		// Keep the connection with the HeadMachine open
		try
		{
			synchronized(this)
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
						heartbeat = true;
						continue;
					}

					// Get the payload length
					int payloadLen = ByteBuffer.wrap(Arrays.copyOfRange(header, 2, 6)).getInt();
					byte[] data = new byte[payloadLen];

					read = -1;

					while((read = stream.read(data)) != payloadLen)
					{
						// Read payload
					}

					// Check the header, call AnalysisMachine receive based on request
					switch(header[1])
					{
						case ChatProtocol.CREATE_ACCOUNT_REQUEST:	AnalysisMachineReceive.createAccount(socket, data, this);
									break;
						case ChatProtocol.LOGIN_REQUEST:	AnalysisMachineReceive.login(socket, data, this);
									break;
						case ChatProtocol.DELETE_ACCOUNT_REQUEST:	AnalysisMachineReceive.deleteAccount(socket, data);
									break;
						case ChatProtocol.LIST_ALL_ACCOUNTS_REQUEST:	AnalysisMachineReceive.listAllAccounts(socket, data);
									break;
						case ChatProtocol.SEND_MESSAGE_REQUEST:	AnalysisMachineReceive.sendMessage(socket, data);
									break;
						case ChatProtocol.PULL_ALL_MESSAGES_REQUEST:	AnalysisMachineReceive.pullAllMessages(socket, data);
									break;
						case ChatProtocol.END_SESSION_REQUEST:	AnalysisMachineReceive.endSession(socket, data);
									break;
					}
				}
      		}
		}

		// Print the exception
		catch(Exception e)
		{
			// Do nothing
		}
	}
}

/**
 * Heartbeat constants.
 * <p>
 * <ul>
 * <li>(HeadMachineHandler) handle
 * <li>(Socket) socket
 * </ul>
 * <p>
 *
 * <p>
 * Throughout each HeadMachine-AnalysisMachine session,
 * the AnalysisMachine also maintains a heartbeat via Heartbeat, which pings each HeadMachine repeatedly to check whether
 * the connection is still good. In other words, if the HeadMachine fails or terminates, the heartbeat allows the AnalysisMachine
 * to check whether the HeadMachine is still reachable, and if not, the AnalysisMachine closes the socket.
 */
class Heartbeat extends Thread
{
	// Need the new socket
	Socket socket;
	HeadMachineHandler handle;

	// Constructor for Heartbeat
	Heartbeat(Socket socket, HeadMachineHandler handle)
	{
		this.socket = socket;
		this.handle = handle;
	}

	/**
	 * Run the newly forked thread.
	 * Heartbeat is sent to the HeadMachine, then the AnalysisMachine waits for a period of 3 seconds.
	 * If the AnalysisMachine does not receive a heartbeat from the HeadMachine in that period,
	 * the AnalysisMachine ends the HeadMachine session and closes the socket.
	 */
	public void run()
	{
		// Send heartbeat to HeadMachine
		try
		{
			while(true)
			{
				// Send the heartbeat
				AnalysisMachineSend.heartbeat(socket);

				// Sleep and wait for heartbeat response from HeadMachine
				Thread.sleep(3000);

				if(!handle.heartbeat)
				{
					// Tell the HeadMachine thread to quit
					AnalysisMachineReceive.endSession(socket, handle.account.getBytes("UTF-8"));
					break;
				}

				else
				{
					// Heartbeat has been received, check again
					handle.heartbeat = false;
				}
			}
		}

		// Print the exception
		catch(Exception e)
		{
			// Do nothing, exit
		}
	}
}

/**
 * User constants.
 * <p>
 * <ul>
 * <li>(String) account
 * <li>(boolean) loggedIn
 * <li>(boolean) unreadMessages
 * <li>(Arraylist) messages
 * <li>(Socket) socket
 * </ul>
 * <p>
 *
 * <p>
 * Defines a User object, which contains necessary information that should
 * be stored on the AnalysisMachine side. Provides a convenient way to store the user
 * information, so that it can be retrieved by various AnalysisMachine operations.
 */
class User
{
	// Account name
	String account;

	// Whether the user is currently logged in
	boolean loggedIn;

	// Whether the user has unread messages
	boolean unreadMessages;

	// List of messages the user has
	ArrayList<Message> messages;

	// Socket associated with user for updates
	Socket socket;

	// Constructor for the user
	User(String account, Socket socket)
	{
		this.account = account;
		this.loggedIn = true;
		this.unreadMessages = false;
		this.socket = socket;
		messages = new ArrayList<Message>();
	}
}

/**
 * Message constants.
 * <p>
 * <ul>
 * <li>(String) date
 * <li>(String) sender
 * <li>(String) message
 * </ul>
 * <p>
 *
 * <p>
 * Defines a message object, containing the data, sender, and message itself.
 * Simply a way to lump the three important parts of the message into one data object.
 */
class Message
{
	// Date the AnalysisMachine received the message
	String date;

	// Sender of the message
	String sender;

	// The message content
	String message;

	// Constructor for the message class
	Message(String date, String sender, String message)
	{
		this.date = date;
		this.sender = sender;
		this.message = message;
	}
}
