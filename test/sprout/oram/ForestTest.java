package sprout.oram;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import sprout.util.Util;

public class ForestTest 
{
	public Forest forest;
	
	public ForestTest()
	{
		try
		{
			// Create the tree so we can actually run some tests on it...
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

//	@Test
//	public void testNumberOfTrees() 
//	{
//		assertEquals(forest.getNumberOfTrees(), 3);
//	}
	
	@Test
	public void testForestTraversal()
	{
		long address = 0L;
		
		byte[] entryBucket = forest.getInitialORAM().initialEntry;
		int initialBytes = forest.getInitialEntryTupleSize();
		
		System.out.println("Searching for leaf: " + 0L);
		int offset = ((int) address) * initialBytes;
		
		byte[] initialLeaf = new byte[initialBytes];
		for (int index = 0; index < initialBytes; index++)
		{
			initialLeaf[index] = entryBucket[offset + index];
		}
		
		Util.disp(initialLeaf);
	}

}
