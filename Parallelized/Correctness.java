// Will give the correctness metrics of two csv file
import java.io.*;
import java.util.*;

class Correctness
{
	public static void main(String [] args)
	{
			// Create a new file for the input
			File correctFile = new File(args[0]);

			// Create a new file for the input
			File testingFile = new File(args[1]);

			Scanner correctReader = null;
			Scanner testingReader = null;

			try
			{
				// Create the scanners
				correctReader = new Scanner(correctFile);
				testingReader = new Scanner(testingFile);
			}	

			catch(Exception e)
			{
				// do nothing...
			}


			// Create hashmaps for each of the inputs
			HashMap<String, String> correctHashmap = new HashMap<String, String>();
			HashMap<String, String> testingHashmap = new HashMap<String, String>();

			// Read in the inputs, putting them into a hashmap
			while(correctReader.hasNext())
			{
				// Read in the string
				String  read = correctReader.nextLine();
				String [] readArray = read.split(",");

				// Put it in the Hashmaps
				correctHashmap.put(readArray[0], read);
			}

			// Read in the inputs, putting them into a hashmap
			while(testingReader.hasNext())
			{
				// Read in the string
				String  read = testingReader.nextLine();
				String [] readArray = read.split(",");

				// Put it in the Hashmaps
				testingHashmap.put(readArray[0], read);
			}

			// Iterate over each set, check what is correct
			for(String key : testingHashmap.keySet())
			{
				String testString = testingHashmap.get(key);
				String correctString = correctHashmap.get(key);

				String [] testingArray = testString.split(",");

				// Some testing
				int sum = 0;

				if(correctString != null)
				{
					for(int i = 1; i < testingArray.length; i++)
					{
						if(correctString.contains(testingArray[i]))
						{
							sum++;
						}
					}
				}		

				System.out.println(key + ": " + (sum / (testingArray.length - 1)) * 100 + "% correct!");
			}
	}
}