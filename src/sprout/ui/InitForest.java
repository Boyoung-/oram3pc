package sprout.ui;

import sprout.oram.Forest;
import sprout.oram.ForestMetadata;
import sprout.util.StopWatch;

public class InitForest {
	public static void main(String[] args) throws Exception {
		ForestMetadata.setup("config/newConfig.yaml");
		// String[] defaultFilenames = ForestMetadata.getDefaultForestNames();

		StopWatch sw = new StopWatch("Forest Setup");
		sw.start();

		new Forest("init");
		// new Forest("restore", defaultFilenames[0], null);
		// new Forest("restore", defaultFilenames[1], null);
		// System.out.println("16 bits: " + Util.addZero(new BigInteger(1,
		// Forest.getForestData(0, 2)).toString(2), 16));

		sw.stop();
		System.out.println(sw);

		/*
		 * new Forest("restore", defaultFilenames[2], null); BigInteger Li =
		 * BigInteger.ZERO; //BigInteger Li = BigInteger.ONE; //BigInteger Li =
		 * new BigInteger("0101010", 2);
		 * 
		 * Bucket[] sD_buckets = null; try { sD_buckets = new
		 * Tree(1).getBucketsOnPath(Li); } catch (TreeException |
		 * BucketException e) { e.printStackTrace(); } for (int j = 0; j <
		 * sD_buckets.length; j++) { Tuple[] tuples = sD_buckets[j].getTuples();
		 * for (int k=0; k<tuples.length; k++) { System.out.println(j+"-"+k+": "
		 * + tuples[k]); } }
		 */
	}

}
