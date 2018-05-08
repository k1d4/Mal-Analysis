import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.*;

// Code to be ran on the analysis machine
public class AnalysisMachine
{
	// Used for locking RetDec
	static Semaphore lock = new Semaphore(1);

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

		// Create socket port and fork threads for the HeadMachine Request
		try
		{
			ServerSocket AnalysisMachine = new ServerSocket(port);

			while(true)
			{
				Socket HeadMachine = AnalysisMachine.accept();
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
	// Need the new socket
	Socket socket;
	ObjectOutputStream outputStream;
	ObjectInputStream inputStream;

	// Constructor for HeadMachineHandler
	AnalysisHandler(Socket socket)
	{
		this.socket = socket;

		try
		{
			System.out.println("FUCK1");
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			inputStream = new ObjectInputStream(socket.getInputStream());
			System.out.println("FUCK2");
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
			synchronized(this)
			{
				while(true)
				{
					// Create an ObjectInputStream and read an object from it
					System.out.println("Finished1!");
					Object data = inputStream.readObject();
					System.out.println("Finished2!");
					

					// Check if the data is a string
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
		
						AnalysisMachineReceive.fileAnalysis(this.outputStream, (byte[]) data);
					}

					// If it's any other object, send back an error
					else
					{
		
						AnalysisMachineSend.error(this.outputStream);
					}
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
