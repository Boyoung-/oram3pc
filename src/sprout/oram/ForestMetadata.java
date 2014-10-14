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

import sprout.util.RC;
import sprout.util.Util;

public class ForestMetadata implements Serializable
{
	public static final String CONFIG_FILE 			= "forest.cfg";
	public static final String TAU_NAME 			= "tau";
	public static final String W_NAME 				= "w";
	public static final String E_NAME 				= "e";
	public static final String LEVELS_NAME 			= "levels";
	public static final String DBYTES_NAME 			= "dBytes";
	public static final String NONCEBITS_NAME		= "nonceBits";
	
	public static final String INSERT_NAME			= "insert";
	
	/**
	 * Class version ID.
	 */
	private static final long serialVersionUID = 1L;
	
	// status
	private static boolean status = false;
	
	// Tau in the write-up
	private static int tau;
	private static int twoTauPow;
		
	// Bucket depth (number of tuples per bucket)
	private static int w;
	
	// Number of buckets in each leaf
	private static int e;
	
	// Number of trees
	private static int levels;
	// Largest tree index
	private static int h;
	
	// Size of each data element in the last tree
	private static int dBytes;
	
	// nonce size
	private static int nonceBits;
	
	// Tuple info
	private static int[] lBits;
	private static int[] lBytes;
	private static int[] nBits;
	private static int[] nBytes;
	private static int[] aBits;
	private static int[] aBytes;
	private static int[] tupleBits;
	private static int[] tupleBytes;
	
	// Tree info
	private static long[] offset;
	private static long[] numLeaves;
	private static long[] treeBytes;
	
	// Number of buckets in the ORAM
	private static long[] numBuckets;
	
	// Max number of records
	private static long addressSpace;
	
	// Size of the entire DB
	private static long forestBytes;
	
	// Number of records we want to initially insert
	private static long numInsert;
	
	/**
	 * Construct the forest metadata from a previously generated config file.
	 * 
	 * @param filename - config file name
	 * @return 
	 * @throws FileNotFoundException
	 * @throws NumberFormatException
	 */
	public static void setup(String filename) throws FileNotFoundException
	{
		Yaml yaml = new Yaml();
		InputStream input = new FileInputStream(new File(filename));
		@SuppressWarnings("unchecked")
		Map<String, Object> configMap = (Map<String, Object>) yaml.load(input);
		
		// Retrieve all of the required parameters
		tau = Integer.parseInt(configMap.get(TAU_NAME).toString());
		w = Integer.parseInt(configMap.get(W_NAME).toString());
		e = Integer.parseInt(configMap.get(E_NAME).toString());
		levels = Integer.parseInt(configMap.get(LEVELS_NAME).toString());
		dBytes = Integer.parseInt(configMap.get(DBYTES_NAME).toString());
		nonceBits = Integer.parseInt(configMap.get(NONCEBITS_NAME).toString());
		
		numInsert = Integer.parseInt(configMap.get(INSERT_NAME).toString());
		
		init();
	}
	
	/**
	 * Perform extra initialization of parameter values after loading 
	 * from the config file.
	 */
	private static void init()
	{
		twoTauPow = (int) Math.pow(2, tau);
		h = levels - 1;
		
		lBytes = new int[levels];
		lBits = new int[levels];
		nBytes = new int[levels];
		nBits = new int[levels];
		aBytes = new int[levels];
		aBits = new int[levels];
		tupleBits = new int[levels];
		tupleBytes = new int[levels];
		numBuckets = new long[levels];
		treeBytes = new long[levels];
		offset = new long[levels];
		numLeaves = new long[levels];
		
		forestBytes = 0L;
		
		// Compute the values for each of the ORAM levels
		for (int i = h; i >= 0; i--)
		{			
			if (i == 0) {
				nBits[i] = 0;
				nBytes[i] = 0;
				lBits[i] = 0;
				lBytes[i] = 0;
				numLeaves[i] = 1;
				numBuckets[i] = 1;
			}
			else {
				nBits[i] = i * tau;
				nBytes[i] = (nBits[i] + 7) / 8;
				int leastNumLeaves = (int) Math.ceil(Math.pow(2, nBits[i]) / (w * e));
				lBits[i] = Math.max((int) Math.ceil(Math.log(leastNumLeaves) / Math.log(2)), 1);
				lBytes[i] = (lBits[i] + 7 ) / 8;
				numLeaves[i] = (long) Math.pow(2, lBits[i]);
				numBuckets[i] = numLeaves[i] * e + numLeaves[i] - 1;
			}
			
			if (i == h) {
				aBits[i] = dBytes * 8;
				aBytes[i] = dBytes;
				
				addressSpace = (long) Math.pow(2, nBits[i]);
			}
			else {
				aBits[i] = twoTauPow * lBits[i+1];
				aBytes[i] = (aBits[i] + 7 ) / 8;
			}
			
			if (i == 0) {
				tupleBits[i] = aBits[i];
				tupleBytes[i] = aBytes[i];
			}
			else {
				tupleBits[i] = 1 + nBits[i] + lBits[i] + aBits[i];
				tupleBytes[i] = (tupleBits[i] + 7) / 8;
			}
			treeBytes[i] = (tupleBytes[i] + getNonceBytes()) * getNumTuples(i);
			forestBytes += treeBytes[i];
		}
		
		// calculate tree offsets
		long os = 0L;
		for (int i=0; i<levels; i++) {
			offset[i] = os;
			os += treeBytes[i];
		}
		
		status = true;
		
		printInfo();
	}
	
	public static void printInfo() 
	{
		Util.disp("===== ForestMetadata =====");
		Util.disp("tau:\t" + tau);
		Util.disp("w:\t" + w);
		Util.disp("e:\t" + e);
		Util.disp("trees:\t" + levels);
		Util.disp("D bytes:\t" + dBytes);
		Util.disp("nonce bits:\t" + nonceBits);
		Util.disp("max # records:\t" + addressSpace);
		Util.disp("forest bytes:\t" + forestBytes);
		Util.disp("");
		
		for (int i=0; i<levels; i++) {
			Util.disp("[Level " + i + "]");
			Util.disp("    lBits          => " + lBits[i]);
			Util.disp("    lBytes         => " + lBytes[i]);
			Util.disp("    nBits          => " + nBits[i]);
			Util.disp("    nBytes         => " + nBytes[i]);
			Util.disp("    aBits          => " + aBits[i]);
			Util.disp("    aBytes         => " + aBytes[i]);
			Util.disp("    tupleBits      => " + tupleBits[i]);
			Util.disp("    tupleBytes     => " + tupleBytes[i]);
			Util.disp("    numLeaves      => " + numLeaves[i]);
			Util.disp("    numBuckets     => " + numBuckets[i]);
			Util.disp("    numTuples      => " + getNumTuples(i));
			Util.disp("    treeOffset     => " + offset[i]);
			Util.disp("    treeBytes      => " + treeBytes[i]);
			Util.disp("");
		}
		Util.disp("=============");
		Util.disp("");
	}
	
	/**
	 * Dump the metadata to the default config name.
	 * 
	 * @return success, else an exception is thrown
	 * @throws IOException
	 */
	public static RC write() throws IOException
	{
		return write(CONFIG_FILE);
	}
	
	/**
	 * Dump the metadata to the specified config name.
	 * 
	 * @return success, else an exception is thrown
	 * @throws IOException
	 */
	public static RC write(String filename) throws IOException
	{	
		Yaml yaml = new Yaml();
	    FileWriter writer = new FileWriter(filename);
	    
	    // Cached configuration map
		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put(TAU_NAME, "" + tau);
		configMap.put(W_NAME, "" + w);
		configMap.put(E_NAME, "" + e);
		configMap.put(LEVELS_NAME, "" + levels);
		configMap.put(DBYTES_NAME, "" + dBytes);
		configMap.put(NONCEBITS_NAME, "" + nonceBits);
		
		configMap.put(INSERT_NAME, "" + numInsert);
	    
	    yaml.dump(configMap, writer);
	    
		return RC.SUCCESS;
	}
	
	///// ACCESSORS
	public static boolean getStatus()
	{
		return status;
	}
	
	public static int getLevels()
	{
		return levels;
	}
	
	public static int getLBits(int level)
	{
		return lBits[level];
	}
	
	public static int getNBits(int level)
	{
		return nBits[level];
	}

	public static int getLBytes(int level)
	{
		return lBytes[level];
	}
	
	public static int getNBytes(int level)
	{
		return nBytes[level];
	}
	
	public static int getABits(int level)
	{
		return aBits[level];
	}
	
	public static int getABytes(int level)
	{
		return aBytes[level];
	}
	
	public static int getTupleBits(int level)
	{
		return tupleBits[level];
	}
	
	public static int getTupleBytes(int level)
	{
		return tupleBytes[level];
	}
	
	public static int getWholeTupleBytes(int level)
	{
		return getNonceBytes() + getTupleBytes(level);
	}
	
	public static long getTreeOffset(int level)
	{
		return offset[level];
	}
	
	public static long getTreeBytes(int level)
	{
		return treeBytes[level];
	}
	
	public static int getBucketDepth()
	{
		return w;
	}
	
	public static long getNumBuckets(int level) 
	{
		return numBuckets[level];
	}
	
	public static long getNumTuples(int level)
	{
		if (level == 0)
			return 1;
		else
			return numBuckets[level] * w;
	}
	
	public static long getNumLeaves(int level)
	{
		return numLeaves[level];
	}
	
	public static int getDataSize()
	{
		return dBytes;
	}
	
	public static int getLeafExpansion()
	{
		return e;
	}
	
	public static int getTau()
	{
		return tau;
	}
	
	public static int getTwoTauPow()
	{
		return twoTauPow;
	}
	
	public static long getForestBytes()
	{
		return forestBytes;
	}
	
	public static long getAddressSpace() 
	{
		return addressSpace;
	}
	
	public static int getNonceBits() 
	{
		return nonceBits;
	}
	
	public static int getNonceBytes() 
	{
		return (nonceBits + 7 ) / 8;
	}
	
	public static long getNumInsert() 
	{
		return numInsert;
	}
}
