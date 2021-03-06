package sprout.oram;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import sprout.util.Util;

// TODO: change bucketTupleBytes to tupleBytes

public class ForestMetadata implements Serializable {
	public static final String CONFIG_FILE = "newConfig.yaml";
	public static final String TAU_NAME = "tau";
	public static final String NBITS_NAME = "nBits";
	public static final String W_NAME = "w";
	public static final String E_NAME = "e";
	// public static final String LEVELS_NAME = "levels";
	public static final String DBYTES_NAME = "dBytes";
	// public static final String NONCEBITS_NAME = "nonceBits";
	public static final String INSERT_NAME = "insert";

	private static final long serialVersionUID = 1L;

	private static String[] defaultForestNames;
	private static String[] defaultPathNames;

	// Whether ForestMetadata is configured
	private static boolean status = false;

	// Tau in the write-up
	private static int tau;
	private static int twoTauPow; // 2^tau

	// bits of N in the record
	private static int lastNBits;

	// Bucket depth (number of tuples per bucket)
	private static int w;

	// Leaf expansion (number of buckets in each leaf)
	private static int e;

	// Number of trees (including tree 0)
	private static int levels;

	// Size of each data element in the last tree h
	private static int dBytes;

	// Tuple info
	private static int[] lBits;
	private static int[] nBits;
	private static int[] aBits;
	private static int[] tupleBits;

	// Bucket info
	// private static int nonceBits;

	// Tree info
	private static long[] offset;
	private static long[] numLeaves;
	private static long[] numBuckets;
	private static long[] treeBytes;

	// Size of the entire DB
	private static long forestBytes;

	// Max number of records can be inserted
	private static long addressSpace;

	// Number of records we want to initially insert
	private static long numInsert;
	
	// for loadPathCheat
	private static long[] pathOffset;
	private static long[] pathNumBuckets;
	private static long pathSize;

	public static void setup(String filename) throws FileNotFoundException {
		setup(filename, true);
	}

	public static void setup(String filename, boolean ifPrint)
			throws FileNotFoundException {
		Yaml yaml = new Yaml();
		InputStream input = new FileInputStream(new File(filename));
		@SuppressWarnings("unchecked")
		Map<String, Object> configMap = (Map<String, Object>) yaml.load(input);

		// Retrieve all of the required parameters
		tau = Integer.parseInt(configMap.get(TAU_NAME).toString());
		lastNBits = Integer.parseInt(configMap.get(NBITS_NAME).toString());
		w = Integer.parseInt(configMap.get(W_NAME).toString());
		e = Integer.parseInt(configMap.get(E_NAME).toString());
		// levels = Integer.parseInt(configMap.get(LEVELS_NAME).toString());
		dBytes = Integer.parseInt(configMap.get(DBYTES_NAME).toString());
		// nonceBits =
		// Integer.parseInt(configMap.get(NONCEBITS_NAME).toString());
		numInsert = Long.parseLong(configMap.get(INSERT_NAME).toString(), 10);

		init(ifPrint);
		setDefaultForestNames();
	}

	private static void setDefaultForestNames() {
		int t = tau;
		int n = lastNBits;
		// int w = ForestMetadata.getBucketDepth();
		int d = dBytes;
		long r = numInsert;

		defaultForestNames = new String[2];
		defaultForestNames[0] = "files/forest_t" + t + "n" + n + "w" + w + "d"
				+ d + "_r" + r + "_share1.bin";
		defaultForestNames[1] = "files/forest_t" + t + "n" + n + "w" + w + "d"
				+ d + "_r" + r + "_share2.bin";
		
		// for cheat mode: only load one path of each tree
		defaultPathNames = new String[2];
		defaultPathNames[0] = "files/path_t" + t + "n" + n + "w" + w + "d"
				+ d + "_r" + r + "_share1.bin";
		defaultPathNames[1] = "files/path_t" + t + "n" + n + "w" + w + "d"
				+ d + "_r" + r + "_share2.bin";

	}

	private static void init(boolean ifPrint) {
		twoTauPow = (int) Math.pow(2, tau);
		levels = (lastNBits + tau - 1) / tau + 1;

		lBits = new int[levels];
		nBits = new int[levels];
		aBits = new int[levels];
		tupleBits = new int[levels];
		numBuckets = new long[levels];
		treeBytes = new long[levels];
		offset = new long[levels];
		numLeaves = new long[levels];

		forestBytes = 0L;

		// Compute the values for each of the ORAM levels
		int h = levels - 1;
		int logW = (int) (Math.log(w) / Math.log(2));

		for (int i = h; i >= 0; i--) {
			if (i == 0) {
				nBits[i] = 0;
				lBits[i] = 0;
				numLeaves[i] = 0;
				numBuckets[i] = 1;
			} else {
				if (i == h)
					nBits[i] = lastNBits;
				else
					nBits[i] = i * tau;
				lBits[i] = Math.max(nBits[i] - logW, 1);
				numLeaves[i] = (long) Math.pow(2, lBits[i]);
				numBuckets[i] = numLeaves[i] * e + numLeaves[i] - 1;
			}

			if (i == h) {
				aBits[i] = dBytes * 8;

				addressSpace = (long) Math.pow(2, nBits[i]);
			} else {
				aBits[i] = twoTauPow * lBits[i + 1];
			}

			if (i == 0)
				tupleBits[i] = aBits[i];
			else
				tupleBits[i] = 1 + nBits[i] + lBits[i] + aBits[i];
			treeBytes[i] = getBucketBytes(i) * numBuckets[i];
			forestBytes += treeBytes[i];
		}

		// calculate tree offsets
		long os = 0L;
		for (int i = 0; i < levels; i++) {
			offset[i] = os;
			os += treeBytes[i];
		}

		status = true;

		if (ifPrint)
			printInfo();
		
		
		// for loadPathCheat
		pathOffset = new long[levels];
		pathNumBuckets = new long[levels];
		pathOffset[0] = 0;
		pathNumBuckets[0] = 1;
		pathSize = pathNumBuckets[0] * getBucketBytes(0);
		for (int i = 1; i < levels; i++) {
			pathOffset[i] = pathSize;
			pathNumBuckets[i] = lBits[i] + e;
			pathSize += pathNumBuckets[i] * getBucketBytes(i);
		}
	}

	public static void printInfo() {
		Util.disp("===== ForestMetadata =====");
		Util.disp("tau:\t" + tau);
		Util.disp("N bits:\t" + lastNBits);
		Util.disp("w:\t" + w);
		Util.disp("e:\t" + e);
		Util.disp("trees:\t" + levels);
		Util.disp("D bytes:\t" + dBytes);
		// Util.disp("nonce bits:\t" + nonceBits);
		Util.disp("max # records:\t" + addressSpace);
		Util.disp("forest bytes:\t" + forestBytes);
		Util.disp("");

		for (int i = 0; i < levels; i++) {
			Util.disp("[Level " + i + "]");
			Util.disp("    nBits             => " + nBits[i]);
			Util.disp("    lBits             => " + lBits[i]);
			Util.disp("    aBits             => " + aBits[i]);
			Util.disp("    tupleBits         => " + tupleBits[i]);
			Util.disp("    bucketTupleBytes  => " + getBucketTupleBytes(i));
			Util.disp("    bucketBytes       => " + getBucketBytes(i));
			Util.disp("    numLeaves         => " + numLeaves[i]);
			Util.disp("    numBuckets        => " + numBuckets[i]);
			Util.disp("    numTuples         => " + getNumTuples(i));
			Util.disp("    treeOffset        => " + offset[i]);
			Util.disp("    treeBytes         => " + treeBytes[i]);
			Util.disp("");
		}
		Util.disp("");
	}

	public static void write() throws IOException {
		write(CONFIG_FILE);
	}

	public static void write(String filename) throws IOException {
		Yaml yaml = new Yaml();
		FileWriter writer = new FileWriter(filename);

		// Cached configuration map
		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put(TAU_NAME, "" + tau);
		configMap.put(NBITS_NAME, "" + lastNBits);
		configMap.put(W_NAME, "" + w);
		configMap.put(E_NAME, "" + e);
		// configMap.put(LEVELS_NAME, "" + levels);
		configMap.put(DBYTES_NAME, "" + dBytes);
		// configMap.put(NONCEBITS_NAME, "" + nonceBits);
		configMap.put(INSERT_NAME, "" + numInsert);

		yaml.dump(configMap, writer);
	}

	// /// ACCESSORS
	public static boolean getStatus() {
		return status;
	}

	public static int getLastNBits() {
		return lastNBits;
	}

	public static int getLevels() {
		return levels;
	}

	public static int getLBits(int level) {
		return lBits[level];
	}

	public static int getLBytes(int level) {
		return (lBits[level] + 7) / 8;
	}

	public static int getNBits(int level) {
		return nBits[level];
	}

	public static int getNBytes(int level) {
		return (nBits[level] + 7) / 8;
	}

	public static int getABits(int level) {
		return aBits[level];
	}

	public static int getABytes(int level) {
		if (level == (levels - 1))
			return dBytes;
		else
			return (aBits[level] + 7) / 8;
	}

	public static int getTupleBits(int level) {
		return tupleBits[level];
	}

	public static int getTupleBytes(int level) {
		return (tupleBits[level] + 7) / 8;
	}

	public static long getTreeOffset(int level) {
		return offset[level];
	}

	public static long getTreeBytes(int level) {
		return treeBytes[level];
	}

	public static int getBucketDepth() {
		return w;
	}

	public static long getNumBuckets(int level) {
		return numBuckets[level];
	}

	public static long getNumTuples(int level) {
		if (level == 0)
			return 1;
		else
			return numBuckets[level] * w;
	}

	public static long getNumLeaves(int level) {
		return numLeaves[level];
	}

	public static int getDataSize() {
		return dBytes;
	}

	public static int getLeafExpansion() {
		return e;
	}

	public static int getTau() {
		return tau;
	}

	public static int getTwoTauPow() {
		return twoTauPow;
	}

	public static long getForestBytes() {
		return forestBytes;
	}

	public static long getAddressSpace() {
		return addressSpace;
	}

	/*
	 * public static int getNonceBits() { return nonceBits; }
	 * 
	 * public static int getNonceBytes() { return (nonceBits + 7) / 8; }
	 */

	public static long getNumInsert() {
		return numInsert;
	}

	public static int getBucketTupleBits(int level) {
		if (level == 0)
			return tupleBits[level];
		else
			return tupleBits[level] * w;
	}

	public static int getBucketTupleBytes(int level) {
		return (getBucketTupleBits(level) + 7) / 8;
	}

	public static int getBucketBytes(int level) {
		// return getNonceBytes() + getBucketTupleBytes(level);
		return getBucketTupleBytes(level);
	}

	public static long getNumLeafTuples(int level) {
		if (level == 0)
			return (long) twoTauPow;
		else
			return numLeaves[level] * w * e;
	}

	public static String[] getDefaultForestNames() {
		return defaultForestNames;
	}
	
	public static String[] getDefaultPathNames() {
		return defaultPathNames;
	}
	
	public static long getPathNumBuckets(int level) {
		return pathNumBuckets[level];
	}
	
	public static long getPathOffset(int level) {
		return pathOffset[level];
	}
	
	public static long getPathSize() {
		return pathSize;
	}
}
