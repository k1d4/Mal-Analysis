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
		}

		// Print exception
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
