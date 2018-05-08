import java.net.Socket;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;


public abstract class HeadMachineSend
{

	public static void sendBinary(ObjectOutputStream outputStream, File sample)
	{
		try
		{
			byte[] data = Files.readAllBytes(sample.toPath());

			// Send send the file
			outputStream.writeObject(data);
			System.out.println("Finishe3!");
			outputStream.flush();
			System.out.println("Finishe4!");
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Sends a heartbeat to the server, in order for the server and HeadMachine to ensure
	 * they are both connected to each other. If the heartbeat is not returned,
	 * the session will end.
	 */
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

	// Send a heartbeat back to the AnalysisMachine
	// We don't necessarily need this, but maybe we do
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
