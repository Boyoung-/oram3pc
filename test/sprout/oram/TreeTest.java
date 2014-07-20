package sprout.oram;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import sprout.util.Util;

public class TreeTest
{
	public Forest forest;
	
	public TreeTest()
	{
		// create the tree so we can actually run some tests on it...
		try
		{
			forest = new Forest();
			forest.buildFromFile("config/smallConfig.yaml", "config/smallData.txt", "db.bin");
		}
		catch (NumberFormatException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ForestException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testLevels()
	{
		for (int i = 0; i < forest.getNumberOfTrees(); i++)
		{
			try
			{
				Util.disp("Working on ORAM-" + i);
				Tree t = forest.getTree(i);
				long numLeaves = t.getNumLeaves();
				for (long l = 0; l < numLeaves; l++)
				{
					Util.disp("\tFetch leaf path: " + l);
					List<Tuple> tuples = t.getPathToLeaf(l);
				}
			}
			catch (ForestException e)
			{
				e.printStackTrace();
				assertEquals(false, true); // this won't happen
			}
			catch (TreeException e)
			{
				e.printStackTrace();
				assertEquals(false, true); // this won't happen
			}
		}
	}

}
