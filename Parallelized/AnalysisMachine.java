import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.*;

// Code to be ran on the analysis machine
public class AnalysisMachine
{
	// Create a directory where all the files will be stored
	static File directory = new File("AnalysisMachine");

	public static void main(String [] args)
	{
		// Check for correct number of args
		if (args.length != 1)
		{
			System.out.println("Please input port...");
			System.exit(0);
		}

		// Get port number from user
		int port = Integer.parseInt(args[0]);

		// Make the directory a directory
		directory.mkdir();

		// Create socket port and fork threads for the HeadMachine Request
		try
		{
			// Create a new server socket waiting on the specified port
			ServerSocket AnalysisMachine = new ServerSocket(port);

			// Continually loop
			while(true)
			{
				// Accept the connection
				Socket HeadMachine = AnalysisMachine.accept();

				// Create and start the handler thread
				AnalysisHandler handler = new AnalysisHandler(HeadMachine);
				handler.start();
			}
		}

		// Print exception
		catch(Exception e)
		{
			System.out.println(e);
		}
	}
}

// Handler that is spawned when request is received
class AnalysisHandler extends Thread
{
	// The socket, input and output streams used for the connection
	Socket socket;
	ObjectOutputStream outputStream;
	ObjectInputStream inputStream;

	// Constructor for HeadMachineHandler
	AnalysisHandler(Socket socket)
	{
		// Set the socket for the thread
		this.socket = socket;

		try
		{
			// Create the input and output stream, make sure output created first!
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
		}

		catch(Exception e)
		{
			System.out.println(e);
		}
	}

	// Run the newly forked thread
	public void run()
	{
		// Keep the connection open
		try
		{
			while(true)
			{
				// Read an object from the input stream
				Object data = inputStream.readObject();

				// Check if the read data is a string
				if(data instanceof String)
				{
					switch((String) data)
					{
						// Just send a heartbeat back
						case "HEARTBEAT": AnalysisMachineSend.heartbeat(this.outputStream);

						// Send an error, unknown string
						default: AnalysisMachineSend.error(this.outputStream);
					}
				}

				// If it is a file, then analyze it
				else if (data instanceof byte[])
				{
					// Read in the filename then analyze
					AnalysisMachineReceive.fileAnalysis(this.outputStream, (byte[]) data, (String) inputStream.readObject());
				}

				// If it's any other object, send back an error
				else
				{
					AnalysisMachineSend.error(this.outputStream);
				}
			}
		}

		// Print the exception
		catch(Exception e)
		{
			// Just exit quietly...
		}
	}
}
