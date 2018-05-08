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
	public static void fileAnalysis(ObjectOutputStream outputStream, byte [] data)
	{
		// String path for the file
		String path = Graph.uniqueID();

		// Create a file at that path
		File binary = new File(path);

		BinaryNode newNode = null;

		try
		{
			// Write the file at that path
			Files.write(binary.toPath(), data);

			// Run the file through RetDec
			ArrayList<String> decompiledCode = decompile(binary);

			// Create a binary node based upon that new file
			newNode = new BinaryNode(decompiledCode);
		}

		catch(Exception e)
		{
			System.out.println(e);
		}

		// Send the node back to the Head Machine
		AnalysisMachineSend.sendBinaryNode(outputStream, newNode);
	}

	// Uses RetDec to deobfuscate a file
	static ArrayList<String> decompile(File inFile)
	{
		// Create a new process to run RetDec
		Process p;

		// Create a new array to store the code in
		ArrayList<String> codeOutput = new ArrayList<String>();

		// Add the name of the input file
		codeOutput.add(inFile.getName());

		try
		{
			// Create log file !!TESTING!!
			File log = new File("Log.txt");

			// Call retdec-decompiler.sh on the file
			ProcessBuilder builder = new ProcessBuilder("retdec-decompiler.sh", inFile.toString());

			// Output to the log file !!TESTING!!
			builder.redirectOutput(log);

			// Start process
			p = builder.start();

			// Wait until thread has terminated
        	p.waitFor();

        	// Read the output file into an arraylist
			Scanner asmScan = null;

			// Initialize IO
			try
			{
				asmScan = new Scanner(new BufferedReader(new FileReader(inFile + ".c")));
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

				// Print to output file
				if(nextLine.length() != 0)
				{
					codeOutput.add(nextLine);
				}
			}

			// Close the scanning on the retdec output
			asmScan.close();
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
