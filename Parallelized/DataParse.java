// Will parse the input class give a directory that contains correct classifications by folder
import java.io.*;
import java.util.*;

class DataParse
{
	public static void main(String [] args)
	{
		// Create a new file for the input
		File inputFile = new File(args[0]);

		// Get input file list
		ArrayList<File> families;

		// Get input file list
		ArrayList<File> binaries;

		// Add the family to the graph
		try
		{
			// Create a printwriter to output with
			PrintWriter pw = new PrintWriter(new File("correct-classifications.csv"));

			// Create a StringBuilder for speed stuff
			StringBuilder sb = new StringBuilder();

			// Checks if the input is a directory
			if (inputFile.isDirectory())
			{
				families = new ArrayList<File>(Arrays.asList(inputFile.listFiles()));
			}

			// Else the file doesn't exist
			else
			{
				System.out.println("File does not exist!");
				return;
			}

			// Iterate over each of the subdirectories
			for(File i : families)
			{
				// Get the sub directories
				binaries = new ArrayList<File>(Arrays.asList(i.listFiles()));

				// Append the family onto the string
				sb.append(i.toString().substring(i.toString().lastIndexOf("/") + 1) + ",");

				for(File j : binaries)
				{
					// Append the family onto the string
					sb.append(j.toString().substring(j.toString().lastIndexOf("/") + 1) + ",");
				}

				// New row
				sb.append("\n");
			}

			// Write out the string
			pw.write(sb.toString());

			// Close the printwriter
			pw.close();
		}

		// Some exception has occurred
		catch (Exception e)
		{
			System.out.println(e);
		}
	}
}