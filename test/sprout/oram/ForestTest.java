package sprout.oram;

import org.junit.Test;

import sprout.util.Util;

public class ForestTest {
	public Forest forest;

	public ForestTest() {
		try {
			ForestMetadata.setup("config/newConfig.yaml");
			// Create the tree so we can actually run some tests on it...
			forest = new Forest("init");
			// forest.buildFromFile("config/smallConfig.yaml",
			// "config/smallData.txt", "db.bin");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// @Test
	// public void testNumberOfTrees()
	// {
	// assertEquals(forest.getNumberOfTrees(), 3);
	// }

	@Test
	public void testForestTraversal() {
		long address = 0L;

		// byte[] entryBucket = forest.getInitialORAM().initialEntry;
		// int initialBytes = forest.getInitialEntryTupleSize();
		byte[] entryBucket = null;
		try {
			entryBucket = forest.getTree(0).getBucket(0).getByteTuple(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		int initialBytes = ForestMetadata.getBucketBytes(0);

		System.out.println("Searching for leaf: " + 0L);
		int offset = ((int) address) * initialBytes;

		byte[] initialLeaf = new byte[initialBytes];
		for (int index = 0; index < initialBytes; index++) {
			initialLeaf[index] = entryBucket[offset + index];
		}

		Util.disp(initialLeaf);
	}

}
