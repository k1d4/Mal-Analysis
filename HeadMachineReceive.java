import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * ClientReceive contains methods that handle messages from the server,
 * corresponding to user operations.
 * <p>
 * Handles server messages after processing user operations including:
 * <ul>
 * <li> Delete Account
 * <li> Send Message
 * <li> Check Inbox
 * <li> List Users
 * <li> End Session
 * </ul>
 */
public class ClientReceive
{
	/**
	 * Prints a create account success message to the client.
	 * Takes in Socket conn and byte [] buffer,
	 * which contains the username of the created account.
	 */
	public static void createAccountSuccess(Socket conn, byte [] buffer)
	{
		Client.user = new String(buffer);
		Client.loggedIn = true;

		System.out.println("");
		System.out.println("An account with username " + Client.user + " has been successfully created!");
	}

	/**
	 * Prints a create account failure message to the client.
	 * Takes in Socket conn and byte [] buffer,
	 * which contains the reason that the account was not created successfully.
	 */
	public static void createAccountFailure(Socket conn, byte [] buffer)
	{
		Client.loggedIn = false;

		System.out.println("");
		System.out.println("***** Operation Failed! *****");
		System.out.print("* ");
		System.out.println(new String(buffer));
		System.out.println("*****************************");
	}

	/**
	 * Prints a log in success message to the client.
	 * Takes in Socket conn and byte [] buffer,
	 * which contains the username of the created account
	 * and whether the user has any unread messages according
	 * to the server. The server updates the status of unread
	 * messages for each user, and if the user logging in has any,
	 * they are greeted with a message to check their inbox for the
	 * unread messages sent while they were offline.
	 */
	public static void loginSuccess(Socket conn, byte [] buffer)
	{
		// Initialize user log in parameters
		String [] input = new String(buffer).split(";");
		Client.user = input[0];
		Client.unreadMessages = Boolean.valueOf(input[1]);
		Client.loggedIn = true;

		System.out.println("");
		System.out.println("Logged in successfully to account " + Client.user + "!");

		if(Client.unreadMessages)
		{
			System.out.println("");
			System.out.println("You received a new message!");
			System.out.println("Please check your inbox.");
		}
	}

	/**
	 * Prints a log ins failure message to the client.
	 * Takes in Socket conn and byte [] buffer,
	 * which contains the reason that the account was not created successfully.
	 */
	public static void loginFailure(Socket conn, byte [] buffer)
	{
		Client.loggedIn = false;

		System.out.println("");
		System.out.println("***** Operation Failed! *****");
		System.out.print("* ");
		System.out.println(new String(buffer));
		System.out.println("*****************************");
	}

	/**
	 * Prints a list all accounts success message to the client.
	 * Takes in Socket conn and byte [] buffer,
	 * which contains the queried list of users. The list is
	 * then printed to screen.
	 */
	public static void listAllAccountsSuccess(Socket conn, byte [] buffer)
	{
		System.out.println("");
		System.out.println("Queried users:");
		System.out.println(new String(buffer));
	}

	/**
	 * Prints a send message success message to the client.
	 * Takes in Socket conn and byte [] buffer.
	 * Simply prints that the message was successfully sent.
	 */
	public static void sendMessageSuccess(Socket conn, byte [] buffer)
	{
		System.out.println("");
		System.out.println("Message to " + new String(buffer) + " was sent successfully!");
	}

	/**
	 * Prints a pull all messages success message to the client.
	 * Takes in Socket conn and byte [] buffer,
	 * which contains all of the messages in the inbox of the user.
	 * Prints all messages to screen, separated by whitespace.
	 */
	public static void pullAllMessagesSuccess(Socket conn, byte [] buffer)
	{
		Client.unreadMessages = false;

		System.out.println("");
		System.out.println("Your messages:");
		System.out.println(new String(buffer));
	}

	/**
	 * Prints a push message success message to the client,
	 * alerting the user that they have received a new message.
	 * Takes in Socket conn and byte [] buffer,
	 * which contains the username of the message sender.
	 */
	public static void pushMessageSuccess(Socket conn, byte [] buffer)
	{
		Client.unreadMessages = true;

		System.out.println("");
		System.out.println("You received a message from " + new String(buffer) + "!");
		System.out.println("Please check your inbox.");
	}

	/**
	 * Prints a delete account success message to the client.
	 * Takes in Socket conn and byte [] buffer,
	 * which contains the username of the deleted account.
	 */
	public static void deleteAccountSuccess(Socket conn, byte [] buffer)
	{
		System.out.println("");
		System.out.println("Account for " + new String(buffer) + " was deleted!");
		System.exit(0);
	}

	/**
	 * Prints a general failure message to the client,
	 * to communicate to the user that the prior action was not completed
	 * successfully.
	 * Takes in Socket conn and byte [] buffer,
	 * which contains the reason that the account was not created successfully.
	 */
	public static void generalFailure(Socket conn, byte [] buffer)
	{
		System.out.println("");
		System.out.println("***** Operation Failed! *****");
		System.out.print("* ");
		System.out.println(new String(buffer));
		System.out.println("*****************************");
	}
}
