import java.net.Socket;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;

class Normalization
{
	public static void main(String [] args)
	{
		// Get the input file from the first input arg
		File inFile = new File(args[0]);

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

			// Need to split the string
			String [] parts = inFile.toString().split("\\.");

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
				scan = new Scanner(new BufferedReader(new FileReader(parts[0] + ".py")));
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

			PrintWriter writer = new PrintWriter("Output.py");

			for(String x : codeOutput)
			{
				writer.println(x);
			}

			writer.close();
		}

		// Print exception if unable to objdump
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}