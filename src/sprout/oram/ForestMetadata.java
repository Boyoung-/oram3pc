package sprout.oram;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import sprout.util.RC;
import sprout.util.Util;

public class ForestMetadata implements Serializable
{
	public static final String CONFIG_FILE = "forest.cfg";
	public static final String FANOUT_NAME = "fanout";
	public static final String LEVELS_NAME = "levels";
	public static final String TUPLEBITSN_NAME = "tupleBitsN";
	public static final String BUCKETDEPTH_NAME = "bucketDepth";
	public static final String ADDRSPACE_NAME = "addressSpace";
	public static final String DATASIZE_NAME = "dataSize";
	public static final String LEAFEXPANSION_NAME = "leafExpansion";
	public static final String TAU_NAME = "tau";
	
	/**
	 * Class version ID.
	 */
	private static final long serialVersionUID = 1L;
	
	// Fanout for each tree in the forest
	private int fanout;
	
	// Number of ORAM hierarchy levels (number of trees, really)
	private int levels;
	
	// Tuple tag bit width
	private int baseTupleBitsL;
	private int baseTupleBitsN;
	private int[] lBits;
	private int[] lBytes;
	private int[] nBits;
	private int[] nBytes;
	private int[] tupleSizeInBytes;
	
	// Tau, the reduction factor
	private int tau;
	
	// Bucket depth (number of tuples per bucket)
	private int bucketDepth;
	
	// Number of leaves in the ORAM - N
	private long addressSpaceSize;
	private long baseNumLeaves;
	private long[] numLeaves;
	
	// Expansion factor for leaves
	private int leafExpansion;
	
	// Size of each data element
	private int dataSize;
	
	// Size of the entire DB
	private long totalSizeInBytes;
	
	/**
	 * Basic constructor - only called once when the database is being created. 
	 * Parameters are self-explanatory.
	 * 
	 * @param fanout
	 * @param levels
	 * @param tupleBitsN
	 * @param bucketDepth
	 * @param numLeaves
	 * @param dataSize
	 */
	public ForestMetadata(int fanout, int levels, int tupleBitsN,  int bucketDepth, long numLeaves, int dataSize)
	{
		this.fanout = fanout;
		this.levels = levels;
		this.baseTupleBitsN = tupleBitsN;
		this.bucketDepth = bucketDepth;
		this.baseNumLeaves = numLeaves;
		this.dataSize = dataSize;
		init();
	}
	
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
		Map<String, Object> configMap = (Map<String, Object>)yaml.load(input);
		
		// Retrieve all of the required parameters
		this.fanout = Integer.parseInt(configMap.get(FANOUT_NAME).toString());
		this.levels = Integer.parseInt(configMap.get(LEVELS_NAME).toString());
		this.baseTupleBitsN = Integer.parseInt(configMap.get(TUPLEBITSN_NAME).toString());
		this.bucketDepth = Integer.parseInt(configMap.get(BUCKETDEPTH_NAME).toString());
		this.addressSpaceSize = Long.parseLong(configMap.get(ADDRSPACE_NAME).toString());
		this.dataSize = Integer.parseInt(configMap.get(DATASIZE_NAME).toString());
		this.leafExpansion = Integer.parseInt(configMap.get(LEAFEXPANSION_NAME).toString());
		this.tau = Integer.parseInt(configMap.get(TAU_NAME).toString());
		
		// Figure out how many bits are needed to store the tuple leaf element
		BigInteger bi = new BigInteger("" + (baseNumLeaves - 1));
		this.baseTupleBitsL = bi.bitLength();
		
		init();
	}
	
	/**
	 * Perform extra initialization of parameter values after loading 
	 * from the config file.
	 */
	private void init()
	{
		lBytes = new int[levels];
		lBits = new int[levels];
		nBytes = new int[levels];
		nBits = new int[levels];
		tupleSizeInBytes = new int[levels];
		totalSizeInBytes = 0L;
		numLeaves = new long[levels];
		dataSize = getDataSize();
		int tauExp = (int)(Math.log(tau) / Math.log(2));
		
		// TODO: some error checking here, if needed
		
		// Compute the values for each of the ORAM levels
		Util.disp("=====\nORAM parameters:");
		Util.disp("tau = " + tau + ", e = " + leafExpansion + "\n");
		for (int i = 0; i < levels; i++)
		{
			int tauPow = (int)(Math.pow(tau, i));
			int tauExpPow = tauExp * i;
			
			// Compute the number of leaves
			if (i == 0) // #leaves => (2^k) / e
			{
				numLeaves[i] = addressSpaceSize / leafExpansion; 
			}
			else // #leaves => (2^k) / (e * \tau*i)
			{
				numLeaves[i] = addressSpaceSize / (leafExpansion * tauPow); 
			}
			
			// Extract the number of bits for leaves/tags
			nBits[i] = baseTupleBitsN - tauExpPow;
			lBits[i] = (int)(Math.ceil(Math.log(numLeaves[i]) / Math.log(2)));
			
			// Convert to bytes and sum for the total tuple size
			nBytes[i] = (int)Math.ceil(((nBits[i] + 7) / 8));
			lBytes[i] = (int)Math.ceil(((lBits[i] + 7) / 8));
			
			// Determine max data size
			if (i > 0)
			{
				int subDataSize = getTupleBytesL(i - 1) * (int)(Math.pow(getTau(), i)); 
				if (subDataSize > dataSize)
				{
					dataSize = subDataSize;
				}
			}
		}
		
		// Update data taking into account length of D entry in tuples
		for (int i = 0; i < levels; i++)
		{
			tupleSizeInBytes[i] = lBytes[i] + nBytes[i] + dataSize + 1; // additional 1 is for tuple metadata (e.g., full bit and whatnot)
			
			// Accumulate the total size of the DB
			long treeSize = (bucketDepth * (int)((numLeaves[i] * leafExpansion) + (numLeaves[i] - 1))) * tupleSizeInBytes[i];
			totalSizeInBytes += treeSize;
			
			// debug
			Util.disp("    [Level " + i + "]");
			Util.disp("    #leaves            => " + numLeaves[i]);
			Util.disp("    tag bits           => " + nBits[i]);
			Util.disp("    leaf bits          => " + lBits[i]);
			Util.disp("    data size (bytes)  => " + dataSize);
			Util.disp("    tuple size (bytes) => " + tupleSizeInBytes[i]);
			Util.disp("    tree size (bytes)  => " + treeSize);
			Util.disp("");
		}
		
		Util.disp("   Total size (in bytes) => " + totalSizeInBytes);
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
		configMap.put(FANOUT_NAME, "" + fanout);
		configMap.put(LEVELS_NAME, "" + levels);
		configMap.put(TUPLEBITSN_NAME, "" + baseTupleBitsN);
		configMap.put(BUCKETDEPTH_NAME, "" + bucketDepth);
		configMap.put(ADDRSPACE_NAME, "" + addressSpaceSize);
		configMap.put(DATASIZE_NAME, "" + dataSize);
	    
	    yaml.dump(configMap, writer);
	    
		return RC.SUCCESS;
	}
	
	///// ACCESSORS
	
	public int getFanout()
	{
		return fanout;
	}
	
	public int getLevels()
	{
		return levels;
	}
	
	public int getTupleBitsL(int level)
	{
		return lBits[level];
	}
	
	public int getTupleBitsN(int level)
	{
		return nBits[level];
	}

	public int getTupleBytesL(int level)
	{
		return lBytes[level];
	}
	
	public int getTupleBytesN(int level)
	{
		return nBytes[level];
	}
	
	public int getTupleBytesD()
	{
		return dataSize;
	}
	
	public int getTupleSizeInBytes(int level)
	{
		return tupleSizeInBytes[level];
	}
	
	public int getBucketDepth()
	{
		return bucketDepth;
	}
	
	public long getNumLeaves(int level)
	{
		return numLeaves[level];
	}
	
	public int getDataSize()
	{
		return dataSize;
	}
	
	public int getLeafExpansion()
	{
		return leafExpansion;
	}
	
	public int getTau()
	{
		return tau;
	}
	
	public int getTauExponent()
	{
		return (int)(Math.log(tau) / Math.log(2));
	}
	
	public long getTotalSizeInBytes()
	{
		return totalSizeInBytes;
	}
}
