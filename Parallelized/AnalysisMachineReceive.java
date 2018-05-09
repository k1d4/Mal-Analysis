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
		// Get the familyname from the fileName
		String [] parts = fileName.split("%");

		// Exit if there is not a filename and a family name
		if(parts.length != 2)
		{
			AnalysisMachineSend.error(outputStream);
			return;
		}

		// String filename for the file
		String name = Graph.uniqueID(parts[0]);

		// Create a file at that path
		File binary = new File(AnalysisMachine.directory + "/" + Graph.uniqueID(parts[0]));

		// Creating a new node for the graph
		BinaryNode newNode = null;

		try
		{
			// Write the file at that path
			Files.write(binary.toPath(), data);

			// Run the file through RetDec
			ArrayList<String> decompiledCode = decompile(new File(name));

			// Create a binary node based upon that new file
			newNode = new BinaryNode(decompiledCode);
		}

		// Just in case there was an error
		catch(Exception e)
		{
			System.out.println(e);
		}

		// Send the node back to the Head Machine
		AnalysisMachineSend.sendBinaryNode(outputStream, newNode, parts[1]);
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
			// File log = new File(AnalysisMachine.directory + "/Log.txt");

			// Call retdec-decompiler.sh on the file
			ProcessBuilder builder = new ProcessBuilder("retdec-decompiler.sh", "-l", "py", inFile.toString());

			// Change the directory of the process
			builder.directory(AnalysisMachine.directory);

			// Output to parent process System.out
			builder.inheritIO();

			// Start process
			p = builder.start();

			// Wait until thread has terminated
        	p.waitFor();

        	// Read the output file into an arraylist
			Scanner scan = new Scanner(new BufferedReader(new FileReader(AnalysisMachine.directory + "/" + inFile.toString() + ".py")));

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
						// // Generalize Functions
						// outString = outString.replaceAll("[a-zA-Z_0-9]+\\(", "func(");

						// // Generalize Globals 
						// outString = outString.replaceAll("g[0-9]+", "g");

						// // Generalize Arguments 
						// outString = outString.replaceAll("a[0-9]+", "a");

						// // Generalize Struct Vars 
						// outString = outString.replaceAll("e[0-9]+", "e");

						// // Generalize Local Vars 
						// outString = outString.replaceAll("v[0-9]+", "v");

						// // Remove whitespace
						// outString = outString.replaceAll("\\s", "");

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

		// Print exception if unable to retdec
		catch(Exception e)
		{
			System.out.println(e);
		}

		// Return the created file
		return codeOutput;
	}
}
