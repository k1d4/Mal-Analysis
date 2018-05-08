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
	public static void fileAnalysis(ObjectOutputStream outputStream, byte [] data, String fileName)
	{
		// String path for the file
		String path = Graph.uniqueID(fileName);

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
			// Create log file
			File log = new File("Log.txt");

			// Call retdec-decompiler.sh on the file
			ProcessBuilder builder = new ProcessBuilder("retdec-decompiler.sh", "-l", "py", inFile.toString());

			// Output to the log file
			builder.redirectOutput(log);

			// Start process
			p = builder.start();

			// Wait until thread has terminated
        	p.waitFor();

        	// Read the output file into an arraylist
			Scanner scan = null;

			// Initialize IO
			try
			{
				scan = new Scanner(new BufferedReader(new FileReader(inFile.toString() + ".py")));
			}

			// Some IO exception occurred, close files
			catch(Exception e)
			{
				System.out.println(e);
			}

			while(scan.hasNext())
			{
				String nextLine = scan.nextLine();

				if(nextLine.contains("- Functions -"))
				{
					break;
				}
			}

			// Normalize input and write to output
			while(scan.hasNext())
			{
				// Get next line
				String nextLine = scan.nextLine();

				if(nextLine.contains("- Dynamically Linked Functions -"))
				{
					break;
				}

				// Remove comments
				String [] string_parsing = nextLine.split("#");

				// If there are comments, remove the comment part
				if(string_parsing.length != 0)
				{
					String outString = string_parsing[0];

					// Don't add the line if it is just the definition of a global
					if(outString.contains("global"))
					{
						continue;
					}

					// If there is a part that is not a comment
					if(outString.length() != 0)
					{
						// Generalize Functions
						outString = outString.replaceAll("[a-zA-Z_0-9]+\\(", "func(");

						// Generalize Globals 
						outString = outString.replaceAll("g[0-9]+", "g");

						// Generalize Arguments 
						outString = outString.replaceAll("a[0-9]+", "a");

						// Generalize Struct Vars 
						outString = outString.replaceAll("e[0-9]+", "e");

						// Generalize Local Vars 
						outString = outString.replaceAll("v[0-9]+", "v");

						// Remove whitespace
						outString = outString.replaceAll("\\s", "");

						if(outString.length() != 0)
						{
							// Normalization occurs here
							codeOutput.add(outString);
						}
					}
				}
			}

			// Close the scanning on the retdec output
			scan.close();
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
