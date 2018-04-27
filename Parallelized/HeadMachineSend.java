import java.net.Socket;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;


public abstract class HeadMachineSend
{

	public static void sendBinary(Socket socket, File sample)
	{
		try
		{
			byte[] data = Files.readAllBytes(sample.toPath());

			// Get output stream from socket
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

			// Send back the heartbeat
			outputStream.write(data);
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
	public static void heartbeat(Socket socket)
	{
		try
		{
			// Get output stream from socket
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

			// Send back the heartbeat
			outputStream.writeBytes("HEARTBEAT");
		}

		// Something Failed
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	// Send a heartbeat back to the AnalysisMachine
	// We don't necessarily need this, but maybe we do
	public static void error(Socket socket)
	{
		try
		{
			// Get output stream from socket
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

			// Send back the heartbeat
			outputStream.writeBytes("ERRORMESSAGE");
		}

		// Something Failed
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
