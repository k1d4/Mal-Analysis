import java.net.*;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.util.concurrent.TimeUnit;

/**
 * Client constants.
 * <p>
 * A Client object consists of
 * <ul>
 * <li>(boolean) loggedIn
 * <li>(boolean) unreadMessages
 * <li>(String) user
 *
 * </ul>
 * <p>
 * The Client class handles the operations from the client side of the chat application.
 * First, the client opens a connection to the server to handle user input, then forks a new
 * thread to continuously listen for any new messages or server pings.
 * <p>
 * The user is shown textual-based menus to navigate the application. The basic functionality is
 * if a user is not logged in, they may either create an account or log in.
 * <p>
 * Once a user is logged in, they may either send a message to another user, check their own
 * messages, list either all users or a subset according to a Java regular expression, delete their
 * own account, or simply end the session.
 */
public class HeadMachine
{
	// Bool to tell whether the client has logged in or not
	static boolean loggedIn;

	// Bool to tell whether the client has unread messages
	static boolean unreadMessages;

	// user account name
	static String user;

	/**
	 * Client opens a connection to the server via a socket to handle input requests by the user.
	 * The client also forks another thread to run concurrently, which continually listens
	 * for pings from the server. This enables the Client to display a pop-up notification if
	 * any new messages arrive while the user is still logged in.
	 */
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
			ClientListener listener = new ClientListener(socket);
			listener.start();

			// Continually loop to send input (listener will handle responses)
			while(true)
			{
				getInput(socket);

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

	/**
	 * The getInput method receives the {@code socket} connection and
	 * handles user input.
	 * <p>
	 * Prints a simple textual menu, from which the user selects operations.
	 * Users select options by entering the corresponding integer for each menu
	 * option.
	 * If the user is not logged in, the "Log In Menu" is displayed, from
	 * which users may create a new account or log in to an existing account.
	 * Users may not create an account with the same name as an already existing
	 * account, nor may users log into an account that either does not exist or
	 * is already logged in.
	 * <p>
	 * After successful log in, the "User Menu" is displayed. This gives the options
	 * <ul>
	 * <li> Delete Account
	 * <li> Send Message
	 * <li> Check Inbox
	 * <li> List Users
	 * <li> End Session
	 * </ul>
	 * <p>
	 * These options are handled by the subsequent methods in class ClientSend,
	 * which help send the data packets to the server to be processed.
	 */
	// Gets input from user and maps to opcode
	public static void getInput(Socket socket)
	{
		Scanner input = new Scanner(System.in);
		int in;

		if(loggedIn)
		{
			scan: while(true)
			{
				//System.out.println("--------------------");
				System.out.println("");
				System.out.println("--- "+user+"'s User Menu ---");
				System.out.println("(1) Delete Account");
				System.out.println("(2) Send Message");
				System.out.println("(3) Check Inbox");
				System.out.println("(4) List Users");
				System.out.println("(5) End Session");
				System.out.print("> ");

				try
				{
					in = input.nextInt();
				}

				catch(Exception e)
				{
					System.out.println("");
					System.out.println("***** Error! *****");
					System.out.print("* ");
					System.out.println("Please input a valid option...");
					System.out.println("******************");
					input.nextLine();
					continue;
				}

				switch(in)
				{
					case(1):	ClientSend.deleteAccount(socket);
								break scan;
					case(2):	ClientSend.sendMessage(socket);
								break scan;
					case(3):	ClientSend.pullAllMessages(socket);
								break scan;
					case(4):	ClientSend.listAllAccounts(socket);
								break scan;
					case(5):	ClientSend.endSession(socket);
								break scan;
					default:	System.out.println("");
								System.out.println("***** Error! *****");
								System.out.print("* ");
								System.out.println("Please input a valid integer...");
								System.out.println("******************");
				}
			}
		}

		else
		{
			scan: while(true)
			{
				//System.out.println("--------------------");
				System.out.println("");
				System.out.println("--- Log In Menu ---");
				System.out.println("(1) Create Account");
				System.out.println("(2) Log In");
				System.out.print("> ");

				try
				{
					in = input.nextInt();
				}

				catch(Exception e)
				{
					System.out.println("");
					System.out.println("***** Error! *****");
					System.out.print("* ");
					System.out.println("Please input a valid option...");
					System.out.println("******************");
					input.nextLine();
					continue;
				}

				switch(in)
				{
					case(1):	ClientSend.createAccount(socket);
								break scan;
					case(2):	ClientSend.logIn(socket);
								break scan;
					default:	System.out.println("");
								System.out.println("***** Error! *****");
								System.out.print("* ");
								System.out.println("Please input a valid integer...");
								System.out.println("******************");
				}
			}
		}
	}
}

/**
 * ClientListener constants.
 * <p>
 * A ClientListener takes in a
 * <ul>
 * <li>(Socket) socket
 * </ul>
 * <p>
 * ClientListener is the listener established to handle data packets received
 * from the server. Opens an input stream on the socket to receive packet data,
 * unpacks it, and based on the op codes runs the appropriate success of failure
 * method in ClientReceive.
 */
class ClientListener extends Thread
{
	// Need the socket to listen on
	Socket socket;

	// Constructor ClientListener
	ClientListener(Socket socket)
	{
		this.socket = socket;
	}

	/**
	 * Run the newly forked thread. Handle data packets received from the server.
	 * Opens an input stream on the socket to receive packet data, unpacks it,
	 * and based on the op codes runs the appropriate success or failure
	 * method in ClientReceive.
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
					ClientSend.heartbeat(socket);
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
					case(ChatProtocol.CREATE_ACCOUNT_SUCCESS):	ClientReceive.createAccountSuccess(socket, data);
								break;
					case(ChatProtocol.PUSH_MESSAGE_NOTIFICATION):	ClientReceive.pushMessageSuccess(socket, data);
								break;
					case(ChatProtocol.CREATE_ACCOUNT_FAILURE):	ClientReceive.createAccountFailure(socket, data);
								break;
					case(ChatProtocol.LOGIN_SUCCESS):	ClientReceive.loginSuccess(socket, data);
								break;
					case(ChatProtocol.LOGIN_FAILURE):	ClientReceive.loginFailure(socket, data);
								break;
					case(ChatProtocol.DELETE_ACCOUNT_SUCCESS): ClientReceive.deleteAccountSuccess(socket, data);
								break;
					case(ChatProtocol.LIST_ALL_ACCOUNTS_SUCCESS): ClientReceive.listAllAccountsSuccess(socket, data);
								break;
					case(ChatProtocol.SEND_MESSAGE_SUCCESS): ClientReceive.sendMessageSuccess(socket, data);
								break;
					case(ChatProtocol.PULL_ALL_MESSAGES_SUCCESS): ClientReceive.pullAllMessagesSuccess(socket, data);
								break;
					default: ClientReceive.generalFailure(socket, data);
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
