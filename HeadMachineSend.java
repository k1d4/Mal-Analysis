import java.net.Socket;
import java.io.*;
import java.util.*;
import java.util.regex.*;

/**
 * ClientSend contains methods that sends messages to the server,
 * corresponding to user operations.
 * <p>
 * Sends packets to server correspond to user operations including:
 * <ul>
 * <li> Delete Account
 * <li> Send Message
 * <li> Check Inbox
 * <li> List Users
 * <li> End Session
 * </ul>
 */
public abstract class ClientSend
{
	/**
	 * Request to create an account.
	 * Asks user for an account name to create a new account
	 * and checks if it's valid.
	 * Account name must be unique and at least one character.
	 * Sends a packet containing the username to the server.
	 * If successful, account will be created on the server end.
	 */
	public static void createAccount(Socket socket)
	{
		try
		{
			// Get name from user
			Scanner input = new Scanner(System.in);
			System.out.print("Please enter account name: ");

			// Server uses ";" to parse so don't allow user to input strings with it
			byte[] output = input.nextLine().replace(";", "").getBytes("UTF-8");

			// Get output stream from socket
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// Create header
			byte[] header = new byte[ChatProtocol.HEADER_SIZE];
			header[0] = ChatProtocol.VERSION;
			header[1] = ChatProtocol.CREATE_ACCOUNT_REQUEST;

			// Add the payload length
			header[2] = (byte) (output.length >>> 24);
			header[3] = (byte) (output.length >>> 16);
			header[4] = (byte) (output.length >>> 8);
			header[5] = (byte) output.length;

			// Put together final array to send
			byte[] send = new byte[header.length + output.length];
			System.arraycopy(header, 0, send, 0, header.length);
			System.arraycopy(output, 0, send, header.length, output.length);

			// Write to socket
			out.write(send);
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Request to log in.
	 * Asks user for an account name for logging in and checks if it's valid.
	 * Account name is valid if the account exists under that name, and if that
	 * account is not currently logged in elsewhere.
	 * Sends a packet containing the username to the server.
	 */
	public static void logIn(Socket socket)
	{
		try
		{
			// Get name from user
			Scanner input = new Scanner(System.in);
			System.out.print("Please enter account name: ");
			byte[] output = input.nextLine().replace(";", "").getBytes("UTF-8");

			// Get output stream from socket
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// Create header
			byte[] header = new byte[ChatProtocol.HEADER_SIZE];
			header[0] = ChatProtocol.VERSION;
			header[1] = ChatProtocol.LOGIN_REQUEST;

			// Add the payload length
			header[2] = (byte) (output.length >>> 24);
			header[3] = (byte) (output.length >>> 16);
			header[4] = (byte) (output.length >>> 8);
			header[5] = (byte) output.length;

			// Put together final array to send
			byte[] send = new byte[header.length + output.length];
			System.arraycopy(header, 0, send, 0, header.length);
			System.arraycopy(output, 0, send, header.length, output.length);

			// Write to socket
			out.write(send);
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Request to list all accounts, or a subset of them.
	 * Asks user to input either 'all' to see all users stored on the server,
	 * or to input a Java regular expression to pattern match usernames, and
	 * only display those. Sends a packet to the server containing the regular
	 * expression (regex) so that the server can put together the appropriate list.
	 */
	public static void listAllAccounts(Socket socket)
	{
		try
		{
			// Get option to use regular expression or simply print all users
			String regex = "";
			Scanner input = new Scanner(System.in);
			System.out.println("Enter \"all\" to list all accounts, ");
			System.out.println("or \"regex\" to list a subset of users ");
			System.out.print("matching a Java regular expression: ");
			String option = input.nextLine();

			// handle user input
			if (option.equals("all"))
			{
				regex = ".*";
			}

			else if (option.equals("regex"))
			{
				// get and verify regular expression
				System.out.print("");
				System.out.print("Please enter regular expression: ");
				regex = input.nextLine();

				// catch invalid regular expressions
				try
				{
					Pattern.compile(regex);
				}

				catch (PatternSyntaxException e)
				{
  					regex = "";
  					System.out.println("");
  					System.out.println("Make sure to enter a valid Java regular expression!");
				}
			}

			else
			{
				System.out.println("");
				System.out.println("Make sure to enter \"all\" or \"regex\"!");
			}


			// Create the string to send, users are not allowed to use ";" in name creation
			byte[] output = (regex).getBytes("UTF-8");

			// Get output stream from socket
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// Create header
			byte[] header = new byte[ChatProtocol.HEADER_SIZE];
			header[0] = ChatProtocol.VERSION;
			header[1] = ChatProtocol.LIST_ALL_ACCOUNTS_REQUEST;

			// Add payload length
			header[2] = (byte) (output.length >>> 24);
			header[3] = (byte) (output.length >>> 16);
			header[4] = (byte) (output.length >>> 8);
			header[5] = (byte) output.length;

			// Put together final array to send
			byte[] send = new byte[header.length + output.length];
			System.arraycopy(header, 0, send, 0, header.length);
			System.arraycopy(output, 0, send, header.length, output.length);

			// Write to socket
			out.write(send);
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Request to send message.
	 * Asks user for to input a recipient username and the message the user would like to
	 * send to the recipient. Sends a packet to the server containing the recipient name,
	 * message, and sender name.
	 */
	public static void sendMessage(Socket socket)
	{
		try
		{
			// Get recipient and message from user
			Scanner input = new Scanner(System.in);
			System.out.print("Please enter recipient: ");
			String recipient = input.nextLine().replace(";", "");
			System.out.print("Please enter message: ");
			String msg = input.nextLine().replace(";", "");

			// Create the string to send, users are not allowed to use ";" in name creation
			byte[] output = (Client.user + ";" + recipient + ";" + msg).getBytes("UTF-8");

			// Get output stream from socket
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// Create header
			byte[] header = new byte[ChatProtocol.HEADER_SIZE];
			header[0] = ChatProtocol.VERSION;
			header[1] = ChatProtocol.SEND_MESSAGE_REQUEST;

			// Add payload length
			header[2] = (byte) (output.length >>> 24);
			header[3] = (byte) (output.length >>> 16);
			header[4] = (byte) (output.length >>> 8);
			header[5] = (byte) output.length;

			// Put together final array to send
			byte[] send = new byte[header.length + output.length];
			System.arraycopy(header, 0, send, 0, header.length);
			System.arraycopy(output, 0, send, header.length, output.length);

			// Write to socket
			out.write(send);
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Request to pull all messages.
	 * Client request to the server to pull all messages, or view inbox and all
	 * current messages. Sends a packet to the server containing the client username,
	 * so that the server can grab all the messages stored under that user.
	 */
	public static void pullAllMessages(Socket socket)
	{
		try
		{
			// Server uses ";" to parse so don't allow user to input strings with it
			byte[] output = Client.user.getBytes("UTF-8");

			// Get output stream from socket
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// Create header
			byte[] header = new byte[ChatProtocol.HEADER_SIZE];
			header[0] = ChatProtocol.VERSION;
			header[1] = ChatProtocol.PULL_ALL_MESSAGES_REQUEST;

			// Add the payload length
			header[2] = (byte) (output.length >>> 24);
			header[3] = (byte) (output.length >>> 16);
			header[4] = (byte) (output.length >>> 8);
			header[5] = (byte) output.length;

			// Put together final array to send
			byte[] send = new byte[header.length + output.length];
			System.arraycopy(header, 0, send, 0, header.length);
			System.arraycopy(output, 0, send, header.length, output.length);

			// Write to socket
			out.write(send);
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Request to delete account.
	 * Checks if the user has unread messages; if so, asks for confirmation
	 * that the user would like to delete the account. If user has no unread messages
	 * or confirms deletion, account will successfully be deleted from the server and the
	 * session will end. Otherwise, the session resumes and the account is not deleted.
	 * Sends a message to the server either containing the client username to be deleted,
	 * or an empty string if the account shouldn't be deleted.
	 */
	public static void deleteAccount(Socket socket)
	{
		try
		{
			// default output will be empty string;
			// account names must be at least one character
			byte[] output = Client.user.getBytes("UTF-8");

			// ask if user wants to delete if they have unread messages
			if (Client.unreadMessages)
			{
				Scanner input = new Scanner(System.in);
				System.out.println("");
				System.out.println("*****************************");
				System.out.println("* You have unread messages! *");
				System.out.println("*****************************");
				System.out.println("");
				System.out.print("Are you sure you want to delete your account? (y/n): ");
				String option = input.nextLine();

				// handle user input
				if (option.equals("y"))
				{
					// do nothing! keep deleting account
				}

				else
				{
					// for anything else, don't delete! need client confirmation
					if (!option.equals("n"))
					{
						System.out.println("");
						System.out.println("Make sure to enter \"y\" or \"n\"!");
					}

					// pass empty string to Server to reject the deletion request
					output = "".getBytes("UTF-8");
				}
			}


			// Get output stream from socket
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// Create header
			byte[] header = new byte[ChatProtocol.HEADER_SIZE];
			header[0] = ChatProtocol.VERSION;
			header[1] = ChatProtocol.DELETE_ACCOUNT_REQUEST;

			// Add the payload length
			header[2] = (byte) (output.length >>> 24);
			header[3] = (byte) (output.length >>> 16);
			header[4] = (byte) (output.length >>> 8);
			header[5] = (byte) output.length;

			// Put together final array to send
			byte[] send = new byte[header.length + output.length];
			System.arraycopy(header, 0, send, 0, header.length);
			System.arraycopy(output, 0, send, header.length, output.length);

			// Write to socket
			out.write(send);
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Request to end the current user session. Sends a packet to the server to close socket.
	 */
	public static void endSession(Socket socket)
	{
		try
		{
			// Get user to end session for
			byte [] output = Client.user.getBytes("UTF-8");

			// Get output stream from socket
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// Create header
			byte[] header = new byte[ChatProtocol.HEADER_SIZE];
			header[0] = ChatProtocol.VERSION;
			header[1] = ChatProtocol.END_SESSION_REQUEST;

			// Add the payload length
			header[2] = (byte) (output.length >>> 24);
			header[3] = (byte) (output.length >>> 16);
			header[4] = (byte) (output.length >>> 8);
			header[5] = (byte) output.length;

			// Put together final array to send
			byte[] send = new byte[header.length + output.length];
			System.arraycopy(header, 0, send, 0, header.length);
			System.arraycopy(output, 0, send, header.length, output.length);

			// Write to socket
			out.write(send);

			System.exit(0);
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Sends a heartbeat to the server, in order for the server and client to ensure
	 * they are both connected to each other. If the heartbeat is not returned,
	 * the session will end.
	 */
	public static void heartbeat(Socket socket)
	{
		try
		{
			// Get output stream from socket
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// Create header
			byte[] header = new byte[ChatProtocol.HEADER_SIZE];
			header[0] = ChatProtocol.VERSION;
			header[1] = ChatProtocol.HEARTBEAT;

			// Write to socket
			out.write(header);
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
