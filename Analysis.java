import java.util.*;
import java.io.*;
import java.nio.*;
import java.security.*;

public class Analysis
{
	// Presents options to the user
	static void menu()
	{
		int select = 0;
		String input = null;
		String output = null;
		Scanner in = new Scanner(System.in);
		Graph graph = new Graph();

		do
		{
			System.out.println("Select an option:");
			System.out.println("\t0. Exit");
			System.out.println("\t1. Load graph");
			System.out.println("\t2. Save graph");
			System.out.println("\t3. Add family to graph");
			System.out.println("\t4. Add unknown samples");
			System.out.println("\t5. Print Graph");

			select = in.nextInt();

			switch(select)
			{
				case 1:

					try
					{
						System.out.print("Name of saved graph: ");
						input = in.next();
						graph = Graph.loadGraph(input);
					}

					catch (Exception e)
					{
						e.printStackTrace();
					}

					break;

				case 2:
					try
					{
						System.out.print("Name of file: ");
						input = in.next();
						graph.saveGraph(graph);
					}
					catch (Exception e)
					{	
						e.printStackTrace();
					}

					break;

				case 3:
					System.out.print("Input Directory: ");
					input = in.next();
					System.out.print("Family: ");
					String family = in.next();

					try
					{
						graph.addFamily(new File(input), family);
					}
					catch (Exception e)
					{
						System.out.println(e);
					}
					break;

				case 4:
					System.out.print("Input Directory: ");
					input = in.next();
					graph.addSample(new File(input));
					break;

				case 5:
					for(FamilyNode i : graph.nodes)
					{
						System.out.println(i.name);

						System.out.print("\n\n");

						for(FamilyEdge j : i.edges)
						{
							System.out.println("\t" + j.similarity);
							System.out.println("\t" + j.source.name);
							System.out.println("\t" + j.dest.name);
						}

						System.out.print("\n\n");

						for(SampleNode j : i.samples)
						{
							System.out.println("\t" + j.name);

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

		} while(select != 0);
	}

	public static void main(String [] args)
	{
		menu();
	}
} 