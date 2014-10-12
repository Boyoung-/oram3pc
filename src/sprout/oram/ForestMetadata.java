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
	public static final String LEVELS_NAME 			= "levles";
	public static final String DBYTES_NAME 			= "DBYTES";
	public static final String NONCEBITS_NAME		= "nonceBits";
	
	/**
	 * Class version ID.
	 */
	private static final long serialVersionUID = 1L;
	
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
	
	// Tree sizes
	private static long[] treeBytes;
	
	// Number of buckets in the ORAM
	private static long[] numBuckets;
	
	// Max number of records
	private static long addressSpace;
	
	// Size of each data element
	private static int DBytes;
	
	// Size of the entire DB
	private static long totalSizeInBytes;
	
	/**
	 * Construct the forest metadata from a previously generated config file.
	 * 
	 * @param filename - config file name
	 * @throws FileNotFoundException
	 * @throws NumberFormatException
	 */
	public ForestMetadata(String filename) throws FileNotFoundException, NumberFormatException
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
		DBytes = Integer.parseInt(configMap.get(DBYTES_NAME).toString());
		nonceBits = Integer.parseInt(configMap.get(NONCEBITS_NAME).toString());
		
		init();
	}
	
	/**
	 * Perform extra initialization of parameter values after loading 
	 * from the config file.
	 */
	// TODO: change math.ceil to +7/8
	// TODO: remove bytes??
	private void init()
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
		
		totalSizeInBytes = 0L;
		
		// Compute the values for each of the ORAM levels
		for (int i = h; i >= 0; i--)
		{			
			if (i == 0) {
				nBits[i] = 0;
				nBytes[i] = 0;
				lBits[i] = 0;
				lBytes[i] = 0;
				numBuckets[i] = 1;
			}
			else {
				nBits[i] = i * tau;
				nBytes[i] = (int) Math.ceil((double) nBits[i] / 8);
				lBits[i] = nBits[i] - (int) Math.floor(Math.log(w * e) / Math.log(2));
				lBytes[i] = (int) Math.ceil((double) lBits[i] / 8);
				long numLeaves = (long) Math.pow(2, lBits[i]);
				numBuckets[i] = numLeaves * e + numLeaves - 1;
			}
			
			if (i == h) {
				aBits[i] = DBytes * 8;
				aBytes[i] = DBytes;
				
				addressSpace = (long) Math.pow(2, nBits[i]);
			}
			else {
				aBits[i] = twoTauPow * lBits[i+1];
				aBytes[i] = (int) Math.ceil((double) aBits[i] / 8);
			}
			
			if (i == 0) {
				tupleBits[i] = aBits[i];
				tupleBytes[i] = aBytes[i];
				treeBytes[i] = tupleBytes[i] + getNonceBytes();
				totalSizeInBytes += treeBytes[i];
			}
			else {
				tupleBits[i] = 1 + nBits[i] + lBits[i] + aBits[i];
				tupleBytes[i] = 1 + nBytes[i] + lBytes[i] + aBytes[i];
				treeBytes[i] = (tupleBytes[i] + getNonceBytes()) * w * numBuckets[i];
				totalSizeInBytes += treeBytes[i];
			}
			
			// debug info
			Util.disp("[Level " + i + "]");
			Util.disp("    lBits			=> " + lBits[i]);
			Util.disp("    lBytes			=> " + lBytes[i]);
			Util.disp("    nBits			=> " + nBits[i]);
			Util.disp("    nBytes			=> " + nBytes[i]);
			Util.disp("    aBits			=> " + aBits[i]);
			Util.disp("    aBytes			=> " + aBytes[i]);
			Util.disp("    tupleBits		=> " + tupleBits[i]);
			Util.disp("    tupleBytes		=> " + tupleBytes[i]);
			Util.disp("    numBuckets		=> " + numBuckets[i]);
			Util.disp("");
		}
		
		Util.disp("Total size (in bytes) => " + totalSizeInBytes);
		Util.disp("=====");
	}
	
	/**
	 * Dump the metadata to the default config name.
	 * 
	 * @return success, else an exception is thrown
	 * @throws IOException
	 */
	public RC write() throws IOException
	{
		return write(CONFIG_FILE);
	}
	
	/**
	 * Dump the metadata to the specified config name.
	 * 
	 * @return success, else an exception is thrown
	 * @throws IOException
	 */
	public RC write(String filename) throws IOException
	{	
		Yaml yaml = new Yaml();
	    FileWriter writer = new FileWriter(filename);
	    
	    // Cached configuration map
		Map<String, String> configMap = new HashMap<String, String>();
		configMap.put(TAU_NAME, "" + tau);
		configMap.put(W_NAME, "" + w);
		configMap.put(E_NAME, "" + e);
		configMap.put(LEVELS_NAME, "" + levels);
		configMap.put(DBYTES_NAME, "" + DBytes);
		configMap.put(NONCEBITS_NAME, "" + nonceBits);
	    
	    yaml.dump(configMap, writer);
	    
		return RC.SUCCESS;
	}
	
	///// ACCESSORS
	
	public static int getLevels()
	{
		return levels;
	}
	
	public static int getTupleBitsL(int level)
	{
		return lBits[level];
	}
	
	public static int getTupleBitsN(int level)
	{
		return nBits[level];
	}

	public static int getTupleBytesL(int level)
	{
		return lBytes[level];
	}
	
	public static int getTupleBytesN(int level)
	{
		return nBytes[level];
	}
	
	public static int getTupleBitA(int level)
	{
		return aBits[level];
	}
	
	public static int getTupleBytesA(int level)
	{
		return aBytes[level];
	}
	
	public static int getTupleSizeInBits(int level)
	{
		return tupleBits[level];
	}
	
	public static int getTupleSizeInBytes(int level)
	{
		return tupleBytes[level];
	}
	
	public static long getTreeBytes(int level)
	{
		return treeBytes[level];
	}
	
	public static int getBucketDepth()
	{
		return w;
	}
	
	public static long getNumBuckets(int level) {
		return numBuckets[level];
	}
	
	public static long getNumLeaves(int level)
	{
		return (long) Math.pow(2, lBits[level]);
	}
	
	public static int getDataSize()
	{
		return DBytes;
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
	
	public static long getTotalSizeInBytes()
	{
		return totalSizeInBytes;
	}
	
	public static long getAddressSpace() {
		return addressSpace;
	}
	
	public static int getNonceBits() {
		return nonceBits;
	}
	
	public static int getNonceBytes() {
		return (int) Math.ceil((double) nonceBits / 8);
	}
}
