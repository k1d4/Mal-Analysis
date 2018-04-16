import java.util.*;
import java.io.*;
import java.nio.*;
import java.security.*;

// Class that takes in input from the user, passes data to graph
public class Analysis
{
	// Presents options to the user
	static void menu()
	{
		// Selection from the user
		int select = 0;

		// Input string from the user
		String input = null;

		// Output file from the user
		String output = null;

		// Scanner used to get input from the user
		Scanner in = new Scanner(System.in);

		// The graph that is created to represent our ecosystem.
		Graph graph = new Graph();

		do
		{
			// Ask the user for input
			System.out.println("Select an option:");
			System.out.println("\t0. Exit");
			System.out.println("\t1. Load graph");
			System.out.println("\t2. Save graph");
			System.out.println("\t3. Add family to graph");
			System.out.println("\t4. Add unknown samples");
			System.out.println("\t5. Print Graph");

			// Get which option the user wants
			select = in.nextInt();

			// Switch on the users selection
			switch(select)
			{
				// Load a saved graph
				case 1:

					try
					{
						// Get the saved file name from the user
						System.out.print("Name of saved graph: ");
						input = in.next();
						graph = Graph.loadGraph(input);
					}

					catch (Exception e)
					{
						e.printStackTrace();
					}

					break;

				// Save the graph to a file
				case 2:
					try
					{
						// Get the saved name file
						System.out.print("Name of file: ");
						input = in.next();
						graph.saveGraph(input, graph);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					break;

				// Get a malware family from the user
				case 3:
					// Get the malware family directory
					System.out.print("Input Directory: ");
					input = in.next();

					// Name to be associated with the family
					System.out.print("Family: ");
					String family = in.next();

					// Add the family to the graph
					try
					{
						graph.addFamily(new File(input), family);
					}
					catch (Exception e)
					{
						System.out.println(e);
					}
					break;

				// Add unknown files
				case 4:
					System.out.print("Input Directory: ");
					input = in.next();
					graph.addSample(new File(input));
					break;

				// Print out the graph
				case 5:

					// Iterate over each family node
					for(FamilyNode i : graph.nodes)
					{
						System.out.println(i.name);

						System.out.print("\n\n");

						// Iterate over family edges
						for(FamilyEdge j : i.edges)
						{
							System.out.println("\t" + j.similarity);
							System.out.println("\t" + j.source.name);
							System.out.println("\t" + j.dest.name);
						}

						System.out.print("\n\n");

						// Iterate over the Sample Nodes
						for(SampleNode j : i.samples)
						{
							System.out.println("\t" + j.name);

							// Iterate over the sample edges
							for(SampleEdge k : j.edges)
							{
								System.out.println("\t\t" + k.source.name);
								System.out.println("\t\t" + k.dest.name);
								System.out.println("\t\t" + k.similarity);
								System.out.println("\n");
							}
						}

						System.out.print("\n\n\n\n-----------------------------------------------");
					}
			}

		// As long as the input isn't 0
		} while(select != 0);
	}

	// Run the program
	public static void main(String [] args)
	{
		menu();
	}
}
