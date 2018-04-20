import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

// Methods for sending data back to the HeadMachine
public class AnalysisMachineSend
{
	// Send a heartbeat back to the HeadMachine
	public static void heartbeat(Socket socket)
	{
		try
		{
			// Get output stream from socket
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// Create header
			byte[] header = new byte[ChatProtocol.HEADER_SIZE];
			header[0] = ChatProtocol.VERSION;
			header[1] = ChatProtocol.HEARTBEAT;

			// Write to socket
			out.write(header);
		}

		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
