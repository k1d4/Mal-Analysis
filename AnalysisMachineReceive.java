import java.net.Socket;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;

/**
 * ServerReceive contains methods that receives messages from the client,
 * and processes them appropriately.
 * <p>
 * Receives packets from client correspond to user operations including:
 * <ul>
 * <li> Delete Account
 * <li> Send Message
 * <li> Check Inbox
 * <li> List Users
 * <li> End Session
 * </ul>
 */
public class ServerReceive
{
	/**
	 * Handles request to create an account.
	 * Account creation fails if username is not at least one character
	 * or if account already exists under that username.
	 * If successful, account will be created on the server end.
	 * Sends a packet notifying the client on success or failure.
	 */
	public static void createAccount(Socket conn, byte[] data, ClientHandler handle)
	{
		// Get the account name
		String account = new String(data);

		try
		{
			// Acquire the lock
			Server.lock.acquire();

			// check that account name has at least one character
			if (account.length() == 0)
			{
				// Give reason for the failure, send to client
				String reason = "Account name must be at least one character.";
				ServerSend.generalFailure(conn, ChatProtocol.CREATE_ACCOUNT_FAILURE, reason.getBytes("UTF-8"));
				// Release lock
				Server.lock.release();
				return;
			}

			// Check whether user already exists
			for (User u : Server.users)
			{
				if (account.equals(u.account))
				{
					// Give reason for the failure, send to client
					String reason = "User " + u.account + " already exists in database.";
					ServerSend.generalFailure(conn, ChatProtocol.CREATE_ACCOUNT_FAILURE, reason.getBytes("UTF-8"));

					// Release lock
					Server.lock.release();

					// Print the double create has tried to occur
					System.out.println("User tried to double create account for " + account + ".");
					return;
				}
			}

			User u = new User(account, conn);
			u.loggedIn = true;

			// Otherwise, create account and login
			Server.users.add(u);

			// Print that the user has been created
			System.out.println("User " + account + " has been created.");

			// Set the account name in the thread
			handle.account = account;

			// Send success message to the user
			ServerSend.createAccountSuccess(conn, data);
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}

		// Release lock
		Server.lock.release();
	}

	/**
	 * Handles request to log in.
	 * Log in fails if account does not exist, or if the account is
	 * already logged in.
	 * If successful, user will be logged in on the client side.
	 * Sends a packet notifying the client on success or failure.
	 */
	public static void login(Socket conn, byte[] data, ClientHandler handle)
	{
		// Get the account name
		String account = new String(data);

		try
		{
			// Acquire the lock
			Server.lock.acquire();

			// Check whether user already exists
			for (User u : Server.users)
			{
				if (account.equals(u.account))
				{
					if (u.loggedIn)
					{
						// Give reason for the failure, send to client
						String reason = "User " + u.account + " is already logged in.";
						ServerSend.generalFailure(conn, ChatProtocol.LOGIN_FAILURE, reason.getBytes("UTF-8"));

						// Release lock
						Server.lock.release();

						// Print the login failure message
						System.out.println("Attempted to login to account " + account + ", which was already logged in.");
						return;
					}

					// Otherwise, send login success
					u.loggedIn = true;

					// include whether user has unread messages!
					byte[] login_info = (account + ";" + String.valueOf(u.unreadMessages)).getBytes("UTF-8");

					// Set the socket for updates
					u.socket = conn;
					ServerSend.loginSuccess(conn, login_info);

					// Release lock
					Server.lock.release();

					// Set the account name in the thread
					handle.account = account;

					// Print that the user has logged in
					System.out.println("User " + account + " has logged in.");
					return;
				}
			}

			// Send login failure to user
			ServerSend.generalFailure(conn, ChatProtocol.LOGIN_FAILURE, "Account not found!".getBytes("UTF-8"));

			// Print that the user has tried to login
			System.out.println("User " + account + " tried to log into account that doesn't exist.");
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}

		// Release lock
		Server.lock.release();
	}

	/**
	 * Handles request to list account names.
	 * Listing account name should not usually fail;
	 * if no account names pattern match to the Java
	 * regular expression inputted by the user, then simply
	 * indicate that no usernames match.
	 * Sends a packet containing the queried list, and
	 * notifies the client on success or failure.
	 */
	public static void listAllAccounts(Socket conn, byte[] data)
	{
		// Get the regular expression
		String regex = new String(data);
		Pattern p = Pattern.compile(regex);

		try
		{
			// Acquire the lock
			Server.lock.acquire();

			// Create string to build the user list
			String output = "";

			for (User u : Server.users)
			{
				if (p.matcher(u.account).matches())
				{
					output += u.account + "\n";
				}
			}

			// cut off extra newline
			if (output.length()>=1)
			{
				output = output.substring(0, output.length() - 1);
			}
			else
			{
				output = "No matching users!";
			}

			// Send users to the client
			ServerSend.listAllAccountsSuccess(conn, output.getBytes("UTF-8"));
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}

		// Print that the accounts have been listed
		System.out.println("Displayed queried accounts.");

		// Release lock
		Server.lock.release();
	}

	/**
	 * Handles request to pull all messages, or check inbox.
	 * Checking inbox should not usually fail;
	 * if a user has no messages, the client will see a message
	 * indicating there are no messages.
	 * Sends a packet containing all user messages, and
	 * notifies the client on success or failure.
	 */
	public static void pullAllMessages(Socket conn, byte[] data)
	{
		// Get the account name
		String account = new String(data);

		try
		{
			// Acquire the lock
			Server.lock.acquire();

			// Create string to build message list
			String output = "";

			// Find user
			for (User u : Server.users)
			{
				if (u.account.equals(account))
				{
					// Append on the messages
					for(Message m : u.messages)
					{
						output += m.date + "\n" + m.sender + "\n" + m.message + "\n\n";
					}

					// cut off extra newline
					if (output.length()>=2)
					{
						output = output.substring(0, output.length()-2);
					}
					else
					{
						output = "No messages in inbox!";
					}

					// now user has no unread messages!
					u.unreadMessages = false;

					// Send the messages back to the user
					ServerSend.pullAllMessagesSuccess(conn, output.getBytes("UTF-8"));

					// Print that the user has requested inbox
					System.out.println("User " + account + " requested inbox.");

					// Release lock
					Server.lock.release();

					return;
				}
			}

			// Otherwise user account is incorrect
			ServerSend.generalFailure(conn, ChatProtocol.PULL_ALL_MESSAGES_FAILURE, "No account name found!".getBytes("UTF-8"));

			// Print that the user has logged in
			System.out.println("User " + account + " requested inbox that doesn't exist.");
		}

		catch(Exception e)
		{
			System.out.println(e);
		}

		// Release lock
		Server.lock.release();
	}

	/**
	 * Handles request to send message.
	 * Send message fails if recipient username does not exist.
	 * If successful, message will be successfully sent from sender to recipient.
	 * Sends a packet notifying the sender on success or failure.
	 */
	public static void sendMessage(Socket conn, byte[] data)
	{
		// Parse the input
		String [] input = new String(data).split(";");
		String sender = input[0];
		String recipient = input[1];
		String msg = input[2];

		try
		{
			// Acquire the lock
			Server.lock.acquire();

			// Find the user
			for(User u : Server.users)
			{
				// Once the user is found
				if (u.account.equals(recipient))
				{
					// Create new message with date
					Message message = new Message(new SimpleDateFormat("dd-MM-yyyy").format(new Date()), sender, msg);

					// Add the message to the users inbox
					u.messages.add(message);

					// Send notification to the user if they are logged in
					if(u.loggedIn)
					{
						ServerSend.pushMessageNotification(u.socket, sender.getBytes("UTF-8"));
					}

					// now user has unread messages!
					u.unreadMessages = true;

					// Send a success to the user
					ServerSend.sendMessageSuccess(conn, recipient.getBytes("UTF-8"));

					// Print that the user has sent message
					System.out.println("User " + sender + " sent message to " + recipient + ".");

					// Release lock
					Server.lock.release();

					return;
				}
			}


			// Otherwise recipient is incorrect
			ServerSend.generalFailure(conn, ChatProtocol.SEND_MESSAGE_FAILURE, "Recipient username not found!".getBytes("UTF-8"));

			// Print that the user failed to send message
			System.out.println("User " + sender + " failed to send message to " + recipient + ".");
		}

		catch(Exception e)
		{
			System.out.println(e);
		}

		// Release lock
		Server.lock.release();
	}

	/**
	 * Handles request to delete account.
	 * Delete account fails if user has unread messages and fails
	 * to confirm deletion.
	 * If successful, account will be deleted and the client session
	 * will end.
	 * Sends a packet notifying the client on success or failure.
	 */
	public static void deleteAccount(Socket conn, byte[] data)
	{
		// Get the account name
		String account = new String(data);

		try
		{
			// Acquire the lock
			Server.lock.acquire();

			// client passes along empty string to halt account deletion
			if (account.length()==0)
			{
				// Give reason for the failure, send to client
				String reason = "Client has unread messages and did not confirm account deletion.";
				ServerSend.generalFailure(conn, ChatProtocol.DELETE_ACCOUNT_FAILURE, reason.getBytes("UTF-8"));
				// Release lock
				Server.lock.release();
				return;
			}



			// Find user
			for (User u : Server.users)
			{
				if (account.equals(u.account))
				{
					// Remove the user from the list
					Server.users.remove(u);
					ServerSend.deleteAccountSuccess(conn, data);

					// Release lock
					Server.lock.release();

					// Print that the user has deleted account
					System.out.println("Account for " + account + " has been deleted.");

					return;
				}
			}

			// Otherwise account is found
			ServerSend.generalFailure(conn, ChatProtocol.DELETE_ACCOUNT_FAILURE, "No account name found!".getBytes("UTF-8"));

			// Print that the user has deleted account
			System.out.println("User tried to delete for " + account + " that doesn't exist.");
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}

		// Release lock
		Server.lock.release();
	}

	/**
	 * Handles request to end session.
	 * Closes the socket.
	 * Sends a packet notifying the client on success or failure.
	 */
	public static void endSession(Socket conn, byte[] data)
	{

		// Get the account name
		String account = new String(data);
		try
		{
			// Find the user
			for (User u : Server.users)
			{
				if (account.equals(u.account))
				{
					u.loggedIn = false;
				}
			}

			// Close the connection
        	conn.close();

        	// Print that the user has ended session
			System.out.println("User " + account + " has ended session.");

    	}

    	// Print exception
    	catch (Exception e)
    	{
    		System.out.println(e);
    	}
	}
}
