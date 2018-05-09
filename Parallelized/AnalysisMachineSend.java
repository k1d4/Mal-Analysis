import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.TimeUnit;

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
		}

		// Print exception
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
		}

		// Something Failed
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
