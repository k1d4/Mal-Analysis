import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

// Methods for sending data back to the HeadMachine
public class AnalysisMachineSend
{
	public static void sendBinaryNode(Socket socket, BinaryNode node)
	{
		try
		{
			// Get output stream from socket
			ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());

			// Send back the heartbeat
			outputStream.writeObject(node);
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	// Send a heartbeat back to the HeadMachine
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

	// Send a heartbeat back to the HeadMachine
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
