import java.net.Socket;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;


public abstract class HeadMachineSend
{

	public static void sendBinary(ObjectOutputStream outputStream, File binary, String family)
	{
		try
		{
			// Get the byte array from the binary
			byte[] data = Files.readAllBytes(binary.toPath());

			// Send send the file
			outputStream.writeObject(data);
			outputStream.flush();

			// Send the file name and family name
			outputStream.writeObject(binary.toString() + "%" + family);
			outputStream.flush();

			// Just to make sure things are working
			System.out.println("Sent " + binary + " of family " + family + ".");
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

			// Just to make sure things are working
			System.out.println("Sent heartbeat.");
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
