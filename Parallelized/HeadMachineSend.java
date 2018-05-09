import java.net.Socket;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.TimeUnit;

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

			// Trim the filename
			String fileName = binary.toString().substring(binary.toString().lastIndexOf("/") + 1);

			// Send the file name and family name
			outputStream.writeObject(fileName + "%" + family);
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
		}

		// Something Failed
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
