package sprout.visualization;

import java.io.PrintStream;
import sprout.oram.Tree;
import sprout.oram.TreeException;

public class ORAMVisualizer
{
	/**
	 * Generate a visualization of the ORAM tree.
	 * 
	 * @param tree
	 * @return
	 * @throws TreeException 
	 */
	public static String renderTree(Tree tree, PrintStream out) throws TreeException
	{
		StringBuilder builder = new StringBuilder();
		out.print("graph Tree {\n");
		out.print("    node [shape = record];\n");
		out.print("    rankdir=LR;\n");

		// Add the nodes 
		int tupleOffset = 0;
		long numLeaves = tree.getNumLeaves();
		for (int i = 0; i < tree.getNumberOfNonExpandedBuckets() && tupleOffset < tree.getNumberOfTuples(); i++)
		{
			if (i < numLeaves - 1)
			{
				out.print("    struct" + i + " [label=\"");
				for (int t = 0; t < tree.getBucketDepth() - 1; t++)
				{
					String status = tree.getSlotStatus(tupleOffset) ? "full" : "empty";
					out.print("<f" + (tupleOffset++) + "> " + status + "|");
				}
				String status = tree.getSlotStatus(tupleOffset) ? "full" : "empty";
				out.print("<f" + (tupleOffset++) + "> " + status + "\"];\n");
			}
			else
			{
				out.print("    struct" + i + " [label=\"");
				for (int t = 0; t < (tree.getBucketDepth() * tree.getLeafExpansion()) - 1; t++)
				{
					String status = tree.getSlotStatus(tupleOffset) ? "full" : "empty";
					out.print("<f" + (tupleOffset++) + "> " + status + "|");
				}
				String status = tree.getSlotStatus(tupleOffset) ? "full" : "empty";
				out.print("<f" + (tupleOffset++) + "> " + status + "\"];\n");
			}
		}
			
		// readability... 
		out.print("\n");

		// Add tree connections
		int cutoff = (int)Math.pow(tree.getFanout(), tree.getNumLevels() + 1);
		int upperCutoff = tree.getNumberOfTuples() / tree.getBucketDepth();
		for (int i = 0; i < tree.getNumberOfNonExpandedBuckets(); i++)
		{
			if (i < numLeaves - 1)
			{
				for (int f = 0; f < tree.getFanout(); f++)
				{
					int child = (tree.getFanout() * i) + 1 + f;
					if (child < cutoff) out.print("    struct" + i + " -- struct" + child + ";\n");
				}
			}
		}

		out.print("}\n");
		return builder.toString();
	}
}
