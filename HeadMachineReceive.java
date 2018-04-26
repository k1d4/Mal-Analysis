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
	public static void addNode(Socket conn, BinaryNode node)
	{
		// Free socket for other binaries
		HeadMachine.availableSockets.add(conn);

		try
		{
			if (HeadMachine.binaryType.equals("unknown"))
			{
				// Check what family the newNode is most similar to
				HeadMachine.append = Graph.familyCheck(node);
			}

			if(append != null)
			{
				newNode.edges = sampleEdges(node, HeadMachine.append);
				HeadMachine.append.samples.add(node);
				HeadMachine.graph.updateFamily(HeadMachine.append);
				HeadMachine.graph.updateFamilyEdges(HeadMachine.append);
			}
			else
			{
				HeadMachine.graph.unknown.add(node);
			}
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	public static void generalFailure(Socket conn, byte [] buffer)
	{
		System.out.println("");
		System.out.println("***** Operation Failed! *****");
		System.out.print("* ");
		System.out.println(new String(buffer));
		System.out.println("*****************************");
	}
}
