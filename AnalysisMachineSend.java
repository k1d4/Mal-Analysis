import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * AnalysisMachineSend contains methods that sends messages to the client,
 * indicating whether corresponding methods in SenderReceive processed
 * user inputs successfully.
 * <p>
 * Sends client messages after completing sender operations concerning:
 * <ul>
 * <li> Delete Account
 * <li> Send Message
 * <li> Check Inbox
 * <li> List Users
 * <li> End Session
 * </ul>
 */
public class AnalysisMachineSend
{
	

	/**
	 * Sends a general failure message to the client.
	 * Takes in Socket conn, byte [] opcode, and byte [] buffer,
	 * which contains the reason for failure.
	 * Sends the reason and appropriate failure opcode to client.
	 */
	// Send back a general failure
	public static void generalFailure(Socket socket, byte opcode, byte [] buffer)
	{
		try
		{
			// Get output stream from socket
			DataOutputStream out = new DataOutputStream(socket.getOutputStream());

			// Create header
			byte[] header = new byte[ChatProtocol.HEADER_SIZE];
			header[0] = ChatProtocol.VERSION;
			header[1] = opcode;

			// Add the payload length
			header[2] = (byte) (buffer.length >>> 24);
			header[3] = (byte) (buffer.length >>> 16);
			header[4] = (byte) (buffer.length >>> 8);
			header[5] = (byte) buffer.length;

			// Put together final array to send
			byte[] send = new byte[header.length + buffer.length];
			System.arraycopy(header, 0, send, 0, header.length);
			System.arraycopy(buffer, 0, send, header.length, buffer.length);

			// Write to socket
			out.write(send);
		}

		// Print excption
		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	/**
	 * Sends a heartbeat message to the client.
	 * Takes in Socket conn along which to send the heartbeat.
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

		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}
