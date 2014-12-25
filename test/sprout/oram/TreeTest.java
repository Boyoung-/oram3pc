package sprout.oram;

import static org.junit.Assert.*;

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
			ForestMetadata.setup("config/newConfig.yaml");
			forest = new Forest();
			//forest.buildFromFile("config/smallConfig.yaml", "config/smallData.txt", "db.bin");
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testLevels()
	{
		for (int i = 0; i < ForestMetadata.getLevels(); i++)
		{
			try
			{
				Util.disp("Working on ORAM-" + i);
				Tree t = forest.getTree(i);
				long numLeaves = ForestMetadata.getNumLeaves(i);
				for (long l = 0; l < numLeaves; l++)
				{
					Util.disp("\tFetch leaf path: " + l);
					//Bucket[] buckets = t.getBucketsOnPath(l);
					t.getBucketsOnPath(l);
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				assertEquals(false, true); // this won't happen
			}
		}
	}

}
