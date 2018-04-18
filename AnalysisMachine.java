import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.*;

/**
 * Server constants.
 * <p>
 * A Server object consists of
 * <ul>
 * <li>(ArrayList) users
 * <li>(Semaphore) lock
 *
 * </ul>
 * <p>
 * The Server class handles the operations from the server side of the chat application.
 * First, the server opens a connection to each client to handle user input. Throughout each client-server session,
 * the server also maintains a heartbeat via Heartbeat, which pings each client repeatedly to check whether
 * the connection is still good. In other words, if the client fails or terminates, the heartbeat allows the server
 * to check whether the client is still reachable, and if not, the server closes the socket.
 * <p>
 * The server then listens for client requests, and handles them via ClientHandler.
 */
public class Server
{
	// Global list of all users
	static ArrayList<User> users = new ArrayList<User>();

	// Used for locking the user list
	static Semaphore lock = new Semaphore(1);

	/**
	 * Server forks a new thread for each client who connects successfully.
	 * Sets up a ClientHandler to process the requests from each client.
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

		// Create socket port and fork threads for each client
		try
		{
			ServerSocket server = new ServerSocket(port);

			while(true)
			{
				Socket client = server.accept();
				ClientHandler handler = new ClientHandler(client);
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
 * ClientHandler constants.
 * <p>
 * A ClientHandler takes in a
 * <ul>
 * <li>(Socket) socket
 * <li>(boolean) heartbeat
 * <li>(String) account
 * </ul>
 * <p>
 * ClientListener is the listener established to handle data packets received
 * from the server. Opens an input stream on the socket to receive packet data,
 * unpacks it, and based on the op codes runs the appropriate success of failure
 * method in ClientReceive.
 */
class ClientHandler extends Thread
{
	// Need the new socket
	Socket socket;

	// Boolean for whether heartbeat has been received
	boolean heartbeat;

	// Account that is accociated with this thread
	String account = "";

	// Constructor for ClientHandler
	ClientHandler(Socket socket)
	{
		this.socket = socket;
		heartbeat = true;
	}

	/**
	 * Run the newly forked thread. Handle data packets received from the client.
	 * Opens an input stream on the socket to receive packet data, unpacks it,
	 * and based on the op codes processes the packet payload using methods in SenderReceive.
	 * Additionally, check if there's a heartbeat from the client to check that
	 * the client is still connected.
	 */
	public void run()
	{

		// Fork the heartbeat thread
		Heartbeat check = new Heartbeat(socket, this);
		check.start();

		// Keep the connection with the client open
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

					// Check the header, call server receive based on request
					switch(header[1])
					{
						case ChatProtocol.CREATE_ACCOUNT_REQUEST:	ServerReceive.createAccount(socket, data, this);
									break;
						case ChatProtocol.LOGIN_REQUEST:	ServerReceive.login(socket, data, this);
									break;
						case ChatProtocol.DELETE_ACCOUNT_REQUEST:	ServerReceive.deleteAccount(socket, data);
									break;
						case ChatProtocol.LIST_ALL_ACCOUNTS_REQUEST:	ServerReceive.listAllAccounts(socket, data);
									break;
						case ChatProtocol.SEND_MESSAGE_REQUEST:	ServerReceive.sendMessage(socket, data);
									break;
						case ChatProtocol.PULL_ALL_MESSAGES_REQUEST:	ServerReceive.pullAllMessages(socket, data);
									break;
						case ChatProtocol.END_SESSION_REQUEST:	ServerReceive.endSession(socket, data);
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
 * <li>(ClientHandler) handle
 * <li>(Socket) socket
 * </ul>
 * <p>
 *
 * <p>
 * Throughout each client-server session,
 * the server also maintains a heartbeat via Heartbeat, which pings each client repeatedly to check whether
 * the connection is still good. In other words, if the client fails or terminates, the heartbeat allows the server
 * to check whether the client is still reachable, and if not, the server closes the socket.
 */
class Heartbeat extends Thread
{
	// Need the new socket
	Socket socket;
	ClientHandler handle;

	// Constructor for Heartbeat
	Heartbeat(Socket socket, ClientHandler handle)
	{
		this.socket = socket;
		this.handle = handle;
	}

	/**
	 * Run the newly forked thread.
	 * Heartbeat is sent to the client, then the server waits for a period of 3 seconds.
	 * If the server does not receive a heartbeat from the client in that period,
	 * the server ends the client session and closes the socket.
	 */
	public void run()
	{
		// Send heartbeat to client
		try
		{
			while(true)
			{
				// Send the heartbeat
				ServerSend.heartbeat(socket);

				// Sleep and wait for heartbeat response from client
				Thread.sleep(3000);

				if(!handle.heartbeat)
				{
					// Tell the client thread to quit
					ServerReceive.endSession(socket, handle.account.getBytes("UTF-8"));
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
 * be stored on the server side. Provides a convenient way to store the user
 * information, so that it can be retrieved by various server operations.
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
	// Date the server received the message
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
