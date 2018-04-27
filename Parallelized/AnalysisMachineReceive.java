import java.net.Socket;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;

// Methods for receiving and handling data
public class AnalysisMachineReceive
{
	// Take in the file to analyze, run it through RetDec
	public static void fileAnalysis(Socket socket, byte [] data)
	{
		String path = Graph.uniqueID();
		// Convert the byte array into a file object, write it to memory
		//Files.write(f.toPath(), (byte []) data);
		Files.write(path, data);
		File binary = new File(path);

		// Run the file through RetDec
		ArrayList<String> decompiledCode = decompile(binary);

		BinaryNode newNode = new BinaryNode(decompiledCode);

		AnalysisMachineSend.sendBinaryNode(socket, newNode);

		// THIS WILL SEND BACK TO THE HEAD A BINARYNODE
		// OBJECT, WITHOUT ANY EDGES

	}

	// Uses RetDec to deobfuscate a file
	static ArrayList<String> decompile(File inFile)
	{
		// Create a new process to run RetDec
		Process p;

		// Create a new array to store the code in
		ArrayList<String> output = new ArrayList<String>();

		// Add the name of the input file
		output.add(inFile.getName());

		try
		{
			// Create output file
			File output = new File(inFile.getName() + ".TEMP");

			// Call objdump to get asm
			ProcessBuilder builder = new ProcessBuilder("retdec", "something", inFile);

			// Should have this maybe redirected into a log?
			builder.redirectOutput(output);

			// Start process
			p = builder.start();

			// Wait until thread has terminated
        	p.waitFor();

			Scanner asmScan = null;

			// Initialize IO
			try
			{
				asmScan = new Scanner(new BufferedReader(new FileReader(output)));
			}

			// Some IO exception occurred, close files
			catch(Exception e)
			{
				System.out.println(e);
			}

			// Normalize input and write to output
			while(asmScan.hasNext())
			{
				// Get next line
				String nextLine = asmScan.nextLine();

				try
				{
					nextLine = nextLine.substring(0, nextLine.indexOf("\t"));
				}

				catch (Exception e)
				{
					// Ignore
				}

				// Print to output file
				if(nextLine.length() != 0)
				{
					codeOutput.add(nextLine);
				}
			}

			asmScan.close();

			// Delete the original file
			output.delete();
		}

		// Print exception if unable to objdump
		catch(Exception e)
		{
			e.printStackTrace();
		}

		// Return the created file
		return codeOutput;
	}
}
