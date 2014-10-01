package sprout.oram;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import sprout.crypto.Random;
import sprout.util.Util;
import sprout.util.RC;

public class Forest implements Iterator<Tree> 
{	
	public static class TreeZero {
		public BigInteger nonce = BigInteger.ZERO;
		public byte[] initialEntry;
		
		TreeZero() {}
		
		TreeZero(BigInteger n, byte[] E) {
			nonce = n;
			initialEntry = E.clone();
		}
	}
	
	private ForestMetadata metadata;
	private ArrayList<Tree> trees;
	private TreeZero OT0 = new TreeZero();
	//private byte[] initialEntry;
	private int initialEntryTupleSize;
	private int currTreeIndex;
	
	/**
	 * Load an ORAM hierarchy (forest) from the specified file.
	 * 
	 * @param file - file from which to load the ORAM hierarchy.
	 * @return RC.SUCCESS on success, something else otherwise
	 * @throws FileNotFoundException 
	 * @throws NumberFormatException 
	 */
	public RC loadFile(String file) throws NumberFormatException, FileNotFoundException
	{
		metadata = new ForestMetadata(file);
		return RC.NOT_YET_IMPLEMENTED;
	}
	
	/**
	 * Initialize an ORAM hierarchy (forest) from the specified configuration file
	 * and data file.
	 * 
	 * @param cfgFile - file from which to get the ORAM parameters.
	 * @param dataFile - file containing the data to be used during initialization.
	 * @return RC.SUCCESS on success, something else otherwise
	 * @throws NumberFormatException 
	 * @throws ForestException 
	 * @throws IOException 
	 */
	public RC buildFromFile(String cfgFile, String dataFile, String dbFile) throws NumberFormatException, ForestException, IOException
	{
		metadata = new ForestMetadata(cfgFile);
		return buildFromFile(dataFile, dbFile);
	}
	
	/**
	 * Initialize an ORAM hierarchy (forest) from the specified data file. 
	 * 
	 * @precondition - the metadata object is set.
	 * 
	 * @param dataFile - file containing the data to be used during initialization.
	 * @return RC.SUCCESS on success, something else otherwise
	 * @throws NumberFormatException 
	 * @throws ForestException 
	 * @throws IOException 
	 */
	public RC buildFromFile(String dataFile, String dbFile) throws NumberFormatException, ForestException, IOException
	{
		if (metadata == null)
		{
			throw new ForestException("Uninitialized metadata during ORAM creation");
		}
		
		RC ret = RC.SUCCESS; // assume success
		byte[] buffer = new byte[metadata.getDataSize()];
		FileInputStream is = new FileInputStream(dataFile);
		
		// Allocate space for the whole tree so that writing to disk actually works!
		long dbSize = metadata.getTotalSizeInBytes();
		Util.disp("Total database size = " + dbSize);
		RandomAccessFile ro = new RandomAccessFile(dbFile, "rw");
		for (long i = 0L; i < dbSize; i++)
		{
			ro.write((byte)0);
		}
		ro.close();
		
		// Create the base ORAM tree
		long offset = 0L;
		Tree base = new Tree(offset, 0, dbFile, metadata);
		try
		{
			// Recall: numLeaves is specified as k*2^k, so we need to multiply by k to get the actual number of data elements
			HashMap<Long, Integer> leaves = new HashMap<Long, Integer>();
			long address = 0;
			long baseNumEntries = metadata.getNumLeaves(0) * metadata.getLeafExpansion();
			while (address < baseNumEntries) 
			{
				int bytesRead = is.read(buffer, 0, metadata.getDataSize());
				if (bytesRead == 0)
				{
					is.close();
					throw new ForestException("Invalid data file contents (insufficient data).");
				}
				
				// 1. Encrypt the data
				// TODO
				
				// 2. Randomly choose a new (unique) leaf and tag
				long targetLeaf = Random.generateRandomLong(0, metadata.getNumLeaves(0) - 1);
				while (leaves.containsKey(targetLeaf) && leaves.get(targetLeaf) >= (metadata.getBucketDepth() * metadata.getLeafExpansion()))
				{
					targetLeaf = Random.generateRandomLong(0, metadata.getNumLeaves(0) - 1);
				}
				if (leaves.containsKey(targetLeaf))
				{
					leaves.put(targetLeaf, leaves.get(targetLeaf) + 1);
				}
				else
				{
					leaves.put(targetLeaf, 1);
				}
				
				// 3. Build the tuple raw bytes and create the tuple object
				byte[] leafBytes = Util.longToByteArray(targetLeaf);
				byte[] leaf = new byte[metadata.getTupleBytesL(0)];
				for (int i = leafBytes.length - 1, j = metadata.getTupleBytesL(0) - 1; j >= 0; i--, j--)
				{
					leaf[j] = leafBytes[i];
				}
				byte[] fullTag = Util.longToByteArray(address);
				byte[] tag = Arrays.copyOfRange(fullTag, fullTag.length - metadata.getTupleBytesN(0), fullTag.length);
				Tuple t = new Tuple(leaf, tag, buffer, metadata.getDataSize());
				
				// 4. insert tuple into root of base tree and then perform random eviction to push down
				ret = base.initialInsertTuple(t);
				if (ret != RC.SUCCESS)
				{
					is.close();
					throw new ForestException("Error inserting tuple into base ORAM tree: " + ret.toString());
				}
				
				// Increment address for the next data item
				address++;
			}
			is.close();
			
			// Sanity check: the number of full tuples should be exactly the size of the address space...
			if (base.getNumOccupiedTuples() != baseNumEntries)
			{
				throw new ForestException("ORAM-0 tree corrupt: " + baseNumEntries + "," + base.getNumOccupiedTuples());
			}
			
			// Save the tree in the forest
			trees = new ArrayList<Tree>();
			trees.add(base);
			offset += base.getSizeInBytes();
			
			// Encode the higher-order trees
			int entryBucketSize = 0;
			ArrayList<byte[]> lastLeaves = new ArrayList<byte[]>();
			for (int i = 1; i < metadata.getLevels(); i++)
			{
				address = 0L;
				System.out.println("ORAM-" + i + " numLeaves = " + metadata.getNumLeaves(i));
				Tree higherTree = new Tree(offset, i, dbFile, metadata);
				long numLeaves = metadata.getNumLeaves(i);
				leaves = new HashMap<Long, Integer>();
				for (long l = 0; l < numLeaves; l++)
				{
					// 1. randomly choose a new (unique) leaf in THIS tree 
					long targetLeaf = Random.generateRandomLong(0, numLeaves - 1);
					while (leaves.containsKey(targetLeaf) && leaves.get(targetLeaf) >= (metadata.getBucketDepth() * metadata.getLeafExpansion()))
					{
						targetLeaf = Random.generateRandomLong(0, numLeaves - 1);
					}
					if (leaves.containsKey(targetLeaf))
					{
						leaves.put(targetLeaf, leaves.get(targetLeaf) + 1);
					}
					else
					{
						leaves.put(targetLeaf, 1);
					}
					byte[] leafBytes = Util.longToByteArray(targetLeaf);
					byte[] leaf = Arrays.copyOfRange(leafBytes, leafBytes.length - metadata.getTupleBytesL(i - 1), leafBytes.length);
					
//					Util.disp("Picked random leaf: " + targetLeaf);
					
					// 2. Construct the tag by stripping the last log_2(tau) bits
					int bitsRemoved = metadata.getTauExponent() * i;
					byte[] fullTag = Util.longToByteArray(address >> bitsRemoved);
					byte[] tag = Arrays.copyOfRange(fullTag, fullTag.length - metadata.getTupleBytesN(i), fullTag.length);
					
					// 3. Construct the data item, which is the concatenation of \tau leaves 
					int packed = (int)(Math.pow(metadata.getTau(), i));
					bitsRemoved -= metadata.getTauExponent();
					buffer = new byte[metadata.getDataSize()];
//					System.out.println("Data size = " + buffer.length);
//					System.out.println("Packed = " + packed);
					StringBuilder dataBits = new StringBuilder();
					int bitCount = metadata.getTupleBitsL(i - 1);
					for (int t = 0; t < packed; t++) // packed should equal data size... might wanna assert that
					{
						// Build the tag and query the sub tree for the tuple associated with that tag
						byte[] fullSubTag = Util.longToByteArray((address++) >> bitsRemoved);
						byte[] subTag = Arrays.copyOfRange(fullSubTag, fullSubTag.length - metadata.getTupleBytesN(i - 1), fullSubTag.length);
						
//						Util.disp("Leaf bits we care about = " + bitCount);
//						Util.disp("Address = " + (address - 1));
//						Util.disp("Searching for tag: " + Util.byteArraytoKaryString(subTag, 2));
						
						Tuple subTuple = trees.get(i - 1).findTupleByTag(subTag);
						if (subTuple == null)
						{
							throw new ForestException("Failed finding matching sub-tuple in ORAM-" + (i-1));
						}
						
//						Util.disp("Found: " + subTuple);

						// Append the leaf of the resulting tuple to the data buffer for this new tuple
						byte[] subLeaf = subTuple.getRawLeaf();
						String leafBits = Util.byteArraytoKaryString(subLeaf, 2, bitCount);
						dataBits.append(leafBits);
					}
					
					// TODO: assert that length of string is (packed * bitCount)
					// TODO: inline assertions above
					
					// Convert appended leaf bits to a byte array
					byte[] subLeafBits = Util.bitsToByteArray(dataBits.toString());
					for (int li = 0; li < subLeafBits.length; li++)
					{
						buffer[li] = subLeafBits[li];
					}
					
					// 5. Create the tuple and insert it into the tree
					Tuple t = new Tuple(leaf, tag, buffer, buffer.length, metadata.getTupleSizeInBytes(i));
					ret = higherTree.initialInsertTuple(t);
					if (ret != RC.SUCCESS)
					{
						throw new ForestException("Error inserting tuple into ORAM- " + i + ": " + ret.toString());
					}
					
					// Ensure the tuple was inserted correctly
					if ((l + 1) != higherTree.getNumOccupiedTuples())
					{
						throw new TreeException("Tree corrupt: " + t + " not inserted properly. \nThere should be " + (l+1) + " tuple(s) occuped, but instead there is/are " + higherTree.getNumOccupiedTuples());
					}
					
					// If we're at the last tree before the "entry" ORAM bucket, add the leaf to the list of leaves
					if (i == metadata.getLevels() - 1)
					{
						lastLeaves.add(leaf);
						entryBucketSize += leaf.length;
					}
				}
				
				// Add the new tree to the list and adjust the database file offset
				trees.add(higherTree);
				offset += higherTree.getSizeInBytes();
				
				// Sanity check: the number of full tuples should be exactly the size of the address space...
				if (higherTree.getNumOccupiedTuples() != numLeaves)
				{
					throw new ForestException("ORAM-" + i + " tree corrupt: " + numLeaves + "," + higherTree.getNumOccupiedTuples());
				}
			}
			
			// Append entryBucketSize bytes to the ORAM database file
			ro = new RandomAccessFile(dbFile, "rw");
			ro.seek(ro.length()); // seek to the end
			for (long i = 0L; i < entryBucketSize; i++)
			{
				ro.write((byte)0);
			}
			ro.close();
			
			// Add the final "ORAM", which is just a single bucket
			// L1 || L2 || ... || Ln,
			// where n = #leaves in last tree and the tags are implicit!
			OT0.initialEntry = new byte[entryBucketSize];
			int entryOffset = 0;
			for (int i = 0; i < lastLeaves.size(); i++)
			{
				byte[] leaf = lastLeaves.get(i);
				Util.disp("Adding leaf to entry ORAM: " + Util.byteArraytoKaryString(leaf, 2, leaf.length * 8));
				Util.disp("" + leaf[0]);
				
				// Ensure the tuple entries are of the same size
				if (initialEntryTupleSize == 0)
				{
					initialEntryTupleSize = leaf.length;
				}
				else if (initialEntryTupleSize != leaf.length)
				{
					throw new ForestException("Initial ORAM tuple size mismatch");
				}
				
				// Save the tuple in the initial ORAM
				for (int j = 0; j < leaf.length; j++)
				{
					OT0.initialEntry[entryOffset++] = leaf[j];
				}
			}
		}
		catch (IOException e)
		{
			Util.error(e.getMessage());
			ret = RC.IO_ERROR;
		}
		catch (TreeException e)
		{
			Util.error(e.getMessage());
			ret = RC.GENERIC_ERROR;
		}
		
		return ret;
	}
	
	/**
	 * Write the ORAM hierarchy tree to the specified file.
	 * 
	 * @param file - file to write tree contents to
	 * @return RC.SUCCESS on success, something else otherwise
	 * @throws IOException 
	 */
	public RC writeFile(String file) throws IOException
	{
		if (metadata != null)
		{
			metadata.write();
		}
		return RC.NOT_YET_IMPLEMENTED;
	}
	
	/**
	 * Set the metadata object for the forest.
	 * 
	 * @param data
	 */
	public void setMetaData(ForestMetadata data)
	{
		metadata = data;
	}
	
	public ForestMetadata getMetadata() {
		return metadata;
	}
	
	/**
	 * Retrieve the contents of the initial entry (the initial ORAM).
	 * @return
	 */
	public TreeZero getInitialORAM()
	{
		return OT0;
	}
	
	public byte[] getEntryInInitialORAM(int index)
	{
		int initialBytes = getInitialEntryTupleSize();
		int offset = ((int) index) * initialBytes;
		
		byte[] entry = new byte[initialBytes];
		for (int i = 0; i < initialBytes; i++)
		{
			entry[i] = OT0.initialEntry[offset + i];
		}
		return entry;
	}
	
	/**
	 * Retrieve the size of the tuples in the initial (entry) ORAM.
	 * @return
	 */
	public int getInitialEntryTupleSize()
	{
		return initialEntryTupleSize;
	}
	
	/**
	 * Retrieve the number of trees in this forest.
	 * 
	 * NOTE: this is the number of levels in the ORAM
	 * 
	 * @return
	 */
	public int getNumberOfTrees()
	{
		return trees.size();
	}
	
	/**
	 * Return a reference to the i-th ORAM tree in the hierarchy.
	 *  
	 * @param index
	 * @return i-th tree in hierarchy
	 * @throws ForestException
	 */
	public Tree getTree(int index) throws ForestException
	{
		if (index < 0 || index >= trees.size())
		{
			throw new ForestException("Invalid tree index: " + index);
		}
		return trees.get(index);
	}

	/**
	 * Determine if there is a next tree after the current one.
	 * 
	 * @return true if currentTreeIndex != 0
	 */
	public boolean hasNext()
	{
		return (currTreeIndex == 0);
	}

	/**
	 * Return the next tree and advance the tree index. 
	 * 
	 * @return next tree
	 */
	public Tree next()
	{
		return trees.get(currTreeIndex--);
	}

	/**
	 * Remove the tree at the current tree index
	 */
	public void remove()
	{
		trees.remove(currTreeIndex);
	}
	
	/**
	 * Reset the iterator to start at index #trees(the initial tree), which
	 * isn't really a tree
	 */
	public void restart()
	{
		currTreeIndex = trees.size() - 1;
	}
	
	public String getInitialORAMTreeString() {
		long n = Util.byteArrayToLong(OT0.initialEntry);
		return Util.toKaryString(n, 2, OT0.initialEntry.length * 8);
	}
}
