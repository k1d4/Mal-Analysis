import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

// Methods for sending data back to the HeadMachine
public class AnalysisMachineSend
{
	public static void sendBinaryNode(ObjectOutputStream outputStream, BinaryNode node)
	{
		try
		{
			System.out.println("Finish3!");
			// Send back the binary node
			outputStream.writeObject("HELP");
			outputStream.flush();
			System.out.println("Finishe4!");
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
			outputStream.writeObject("ERRORMESSAGE");
			outputStream.flush();
		}

		// Something Failed
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
