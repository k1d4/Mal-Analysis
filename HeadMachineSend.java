import java.net.Socket;
import java.io.*;
import java.util.*;
import java.util.regex.*;


public abstract class HeadMachineSend
{
	
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
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// Create header
			byte[] header = new byte[ChatProtocol.HEADER_SIZE];
			header[0] = ChatProtocol.VERSION;
			header[1] = ChatProtocol.HEARTBEAT;

			// Write to socket
			out.write(header);
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
