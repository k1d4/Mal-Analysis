import java.net.Socket;
import java.util.concurrent.TimeUnit;

/**
 * HeadMachineReceive contains methods that handle messages from the server,
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
public class HeadMachineReceive
{
	
	public static void generalFailure(Socket conn, byte [] buffer)
	{
		System.out.println("");
		System.out.println("***** Operation Failed! *****");
		System.out.print("* ");
		System.out.println(new String(buffer));
		System.out.println("*****************************");
	}
}
