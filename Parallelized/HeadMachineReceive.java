import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.util.concurrent.*;

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
	public static void addNode(ObjectOutputStream conn, BinaryNode node, String family)
	{
		
		// Try to acquire the lock
		try
		{
			// Release the lock
			Graph.lock.acquire();
		}

		catch(Exception e)
		{
			System.out.println(e);
			System.out.println("Locking Error!");
		}

		// Just to make sure things are working
		System.out.println("Received " + node.name + ".");

		// Free the machine for analysis of anther binary
		HeadMachine.availableSockets.add(conn);

		// Testing which family the node is a part of
		FamilyNode test = null;

		try
		{
			// Check if the family is unknown
			if(family.equals("unknown"))
			{
				test = Graph.familyCheck(node);
			}

			// if the family is not unknown
			else
			{
				// Find the family associated with the name
				for(FamilyNode famNode : Graph.nodes)
				{
					// If we have found the correct node
					if(famNode.name.equals(family))
					{
						// Set test to this family node
						test = famNode;
						break;
					}
				}
			}

			// If family is still unknown, we have to add the node to the unknown list
			if(test == null)
			{
				Graph.unknown.add(node);
			}

			// Else add the node to the family
			else
			{
				Graph.familyAddBinary(test, node);
			}
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}

		// Release the lock
		Graph.lock.release();
	}

	public static void generalFailure(ObjectOutputStream conn, byte [] buffer)
	{
		System.out.println("");
		System.out.println("***** Operation Failed! *****");
		System.out.print("* ");
		System.out.println(new String(buffer));
		System.out.println("*****************************");
	}
}
