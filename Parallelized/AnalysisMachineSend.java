import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

// Methods for sending data back to the HeadMachine
public class AnalysisMachineSend
{
	public static void sendBinaryNode(ObjectOutputStream outputStream, BinaryNode node, String family)
	{
		try
		{
			// Send back the binary node
			outputStream.writeObject(node);
			outputStream.flush();

			// Send back the family name
			outputStream.writeObject(family);
			outputStream.flush();

			// Just to make sure things are working
			System.out.println("Sent node, part of " + family + ".");
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	// Send a heartbeat back to the HeadMachine
	public static void heartbeat(ObjectOutputStream outputStream)
	{
		try
		{
			// Send back the heartbeat
			outputStream.writeObject("HEARTBEAT");
			outputStream.flush();

			// Just to make sure things are working
			System.out.println("Sent heartbeat.");
		}

		// Something Failed
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	// Send a heartbeat back to the HeadMachine
	public static void error(ObjectOutputStream outputStream)
	{
		try
		{
			// Send back the heartbeat
			outputStream.writeObject("ERROR-MESSAGE");
			outputStream.flush();

			// Just to make sure things are working
			System.out.println("Sent error.");
		}

		// Something Failed
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
