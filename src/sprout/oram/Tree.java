package sprout.oram;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sprout.crypto.Random;
import sprout.util.Util;
import sprout.util.RC;

public class Tree
{	
	// Tree parameters 
	private int level;
	private int fanout;
	private int bucketSize;
	private int bucketDepth;
	private int tupleSize;
	private int numTuples;
	private int numLevels;
	private int leafExpansion;
	private long N;
	
	// Tuple params for the tree
	private int lBits;
	private int lBytes;
	private int nBytes;
	private int dBytes;
	
	// Disk stuff
	private String dbfile = null;
	private int offset = 0;
	
	/**
	 * Create an empty tree with the specified number of leaves.
	 * 
	 * @param numLeaves
	 */
	public Tree(long offset, int level, String dbfile, ForestMetadata metadata)
	{
		this.level = level;
		this.offset = (int)offset;
		this.dbfile = dbfile;
		this.fanout = metadata.getFanout();
		this.bucketSize = metadata.getTupleSizeInBytes(level) * metadata.getBucketDepth();
		this.bucketDepth = metadata.getBucketDepth();
		this.tupleSize = metadata.getTupleSizeInBytes(level);
		this.numLevels = (int)(Math.log(metadata.getNumLeaves(level)) / Math.log(fanout));
		this.N = metadata.getNumLeaves(level);
		this.leafExpansion = metadata.getLeafExpansion(); // this is the factor 4, for example
		this.lBits = metadata.getTupleBitsL(level);
		this.lBytes = metadata.getTupleBytesL(level);
		this.nBytes = metadata.getTupleBytesN(level);
		this.dBytes = metadata.getDataSize();
		this.numTuples = bucketDepth * (int)((N * leafExpansion) + (N - 1)); // complete, balanced k-ary tree with leaves expanded (e.g., to 4)
		
		Util.disp("Tuple size = " + tupleSize);
		Util.disp("Tree #tuples = " + numTuples);
		Util.disp("Tree size in bytes = " + (tupleSize * numTuples));
		Util.disp("Num levels = " + numLevels);
		Util.disp("Database file offset = " + offset);
	}
	
	public int getNumberOfNonExpandedBuckets()
	{
		return bucketDepth * (int)((N + (N - 1)));
	}
	
	public int getNumberOfBuckets()
	{
		return (numTuples / bucketDepth);
	}
	
	public int getNumberOfTuples()
	{
		return numTuples;
	}
	
	public int getBucketDepth()
	{
		return bucketDepth;
	}
	
	public int getBucketSize() {
		return bucketSize;
	}
	
	public int getFanout()
	{
		return fanout;
	}
	
	public int getNumLevels()
	{
		return numLevels;
	}
	
	public long getNumLeaves()
	{
		return N;
	}
	
	public int getLeafExpansion()
	{
		return leafExpansion;
	}
	
	public boolean getSlotStatus(int slot) throws TreeException
	{
		if (slot < 0 || slot >= numTuples)
		{
			throw new TreeException("Slot index out of bounds: " + slot + " > " + numTuples);
		}
		byte[] raw = new byte[tupleSize];
		RC ret = readTuple(raw, slot);
		if (ret != RC.SUCCESS)
		{
			throw new TreeException("Error reading tuple.");
		}
		Tuple t = new Tuple(raw, lBytes, nBytes, dBytes);
		return t.isOccupied();
	}
	
	/**
	 * Retrieve the size of the tree in bytes.
	 * 
	 * @return tree size in bytes
	 */
	public long getSizeInBytes()
	{
		return numTuples * tupleSize;
	}
	
	public int getLBytes() {
		return lBytes;
	}
	
	public int getNBytes() {
		return nBytes;
	}
	
	public int getDBytes() {
		return dBytes;
	}
	
	/**
	 * Find the tuple in the tree that matches the specified tag.
	 * @param tag
	 * @return
	 * @throws TreeException 
	 */
	public Tuple findTupleByTag(byte[] tag) throws TreeException
	{
		for (int i = 0; i < numTuples; i++)
		{
			byte[] raw = new byte[tupleSize];
			RC ret = readTuple(raw, i);
			if (ret != RC.SUCCESS)
			{
				throw new TreeException("Error reading tuple.");
			}
			Tuple other = new Tuple(raw, lBytes, nBytes, dBytes);
			
			if (level == 1 && raw[0] == 1)
			{
//				System.out.println("Comparing with: " + other);
			}
			if (other.isOccupied() && other.matchesRawTag(tag))
			{
				return other;
			}
		}
		return null;
	}
	
	/**
	 * Read/return the bytes in the specified bucket.
	 * 
	 * @param bucketNum
	 * @return bucket bytes
	 */
	public byte[] readBucket(int bucketNum)
	{
		byte[] buffer = new byte[bucketSize];
		try
		{
			RandomAccessFile ro = new RandomAccessFile(dbfile, "r");
			ro.seek(offset + (bucketNum * bucketSize));
			ro.read(buffer, 0, bucketSize);
			ro.close();
			return buffer;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return buffer;
	}
	
	/**
	 * Write the new contents of a bucket to disk.
	 * 
	 * @param newBucket
	 * @param pos
	 * @return RC.SUCCESS on success, something else otherwise
	 */
	public RC writeBucket(byte[] bucket, int pos)
	{
		if (pos > 0 && pos < N)
		{
			if (bucket.length != bucketSize)
			{
				return RC.INVALID_BUCKET_SIZE;
			}
			try
			{
				RandomAccessFile ro = new RandomAccessFile(dbfile, "rw");
				ro.seek(offset + (pos * bucketSize));
				ro.write(bucket, 0, bucket.length);
				ro.close();
				return RC.SUCCESS;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			
			return RC.IO_ERROR;
		}
		return RC.TREE_INVALID_BUCKET_INDEX;
	}
	
	/**
	 * Read/return the bytes in the specified tuple.
	 * 
	 * @param bucketNum
	 * @return bucket bytes
	 */
	public RC readTuple(byte[] buffer, int tupleNum)
	{
		RC ret = RC.SUCCESS;
		try
		{	
			RandomAccessFile ro = new RandomAccessFile(dbfile, "r");
			ro.seek(offset + (tupleNum * tupleSize));
			ro.read(buffer, 0, tupleSize);
			ro.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
			ret = RC.IO_ERROR;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			ret = RC.IO_ERROR;
		}
		return ret;
	}
	
	public Tuple getTuple(int tupleNum) throws TreeException {
		byte[] buffer = new byte[tupleSize];
		RC ret = readTuple(buffer, tupleNum);
		if (ret != RC.SUCCESS)
		{
			throw new TreeException("Error getting tuple " + tupleNum);
		}
		return new Tuple(buffer, lBytes, nBytes, dBytes);
	}
	
	/**
	 * Write the specified tuple in place in the tree.
	 * 
	 * @param tuple - tuple to save
	 * @param slot - slot index in the tree (NOT THE INDEX WITHIN A BUCKET)
	 * @return
	 */
	public RC writeTuple(byte[] tuple, int slot)
	{
		if (slot > 0 && slot < numTuples)
		{
			if (tupleSize != tuple.length)
			{
				return RC.INVALID_TUPLE_SIZE;
			}
			try
			{
				RandomAccessFile ro = new RandomAccessFile(dbfile, "rw");
				ro.seek(offset + (slot * tupleSize));
				ro.write(tuple, 0, tuple.length);
				ro.close();
				return RC.SUCCESS;
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return RC.TREE_INVALID_SLOT_INDEX;
	}
	
	/**
	 * Determine if the bucket at position pos is full.
	 * (Loop over every tuple in the bucket and check to see if it's in use)
	 * 
	 * @param pos - target bucket
	 * @return true if full, false otherwise
	 * @throws TreeException 
	 */
	private boolean isBucketFull(int pos) throws TreeException
	{
		int start = pos * bucketDepth;
		int len = bucketDepth;
		for (int i = start; i < start + len; i++)
		{
			byte[] raw = new byte[tupleSize];
			RC ret = readTuple(raw, i);
			if (ret != RC.SUCCESS)
			{
				throw new TreeException("Error reading tuple.");
			}
			Tuple t = new Tuple(raw, lBytes, nBytes, dBytes);
			if (!t.isOccupied()) return false; // found an empty
		}
		return true;
	}
	
	/**
	 * Retrieve a list of tuple indices that are full in the specified bucket.
	 * 
	 * @param bucketPos - target bucket
	 * @return
	 * @throws TreeException 
	 */
	private List<Integer> getFullSlots(int bucketPos) throws TreeException
	{
		List<Integer> indices = new ArrayList<Integer>();
		int start = bucketPos * bucketDepth;
		int len = bucketDepth;
 		for (int i = start; i < start + len; i++)
		{
 			byte[] raw = new byte[tupleSize];
			RC ret = readTuple(raw, i);
			if (ret != RC.SUCCESS)
			{
				throw new TreeException("Error reading tuple.");
			}
 			Tuple t = new Tuple(raw, lBytes, nBytes, dBytes);
 			if (t.isOccupied()) indices.add(i);
		}
		return indices;
	}
	
	/**
	 * Retrieve a list of tuple indices that are empty in the specified bucket.
	 * 
	 * @param bucketPos - target bucket
	 * @return
	 * @throws TreeException 
	 */
	private List<Integer> getEmptySlots(int bucketPos) throws TreeException
	{
		List<Integer> indices = new ArrayList<Integer>();
		int start = bucketPos * bucketDepth;
		int len = bucketDepth;
 		for (int i = start; i < start + len; i++)
		{
 			byte[] raw = new byte[tupleSize];
			RC ret = readTuple(raw, i);
			if (ret != RC.SUCCESS)
			{
				throw new TreeException("Error reading tuple.");
			}
 			Tuple t = new Tuple(raw, lBytes, nBytes, dBytes);
 			if (!t.isOccupied()) indices.add(i);
		}
		return indices;
	}
	
	/**
	 * Retrieve the indices of non-empty buckets at the specified level in the tree.
	 * 
	 * @param level
	 * @return
	 * @throws TreeException 
	 */
	private List<Integer> getNonEmptyBucketsAtLevel(int level) throws TreeException
	{
		List<Integer> nonempty = new ArrayList<Integer>();
		
		int len = (int)Math.pow(fanout, level);
		int low = len - 1;
		for (int i = low; i < low + len; i++)
		{
			if (!getFullSlots(i).isEmpty())
			{
				nonempty.add(i);
			}
		}
		
		return nonempty;
	}
	
	/**
	 * Retrieve the tuple slots that are not occupied (empty) at the given level in the tree. 
	 * 
	 * @param level
	 * @return
	 * @throws TreeException 
	 */
	private List<Integer> getEmptyTuplesAtLevel(int level) throws TreeException
	{
		List<Integer> empties = new ArrayList<Integer>();
		
		int len = (int)Math.pow(fanout, level);
		int low = len - 1;
		for (int i = low; i < low + len; i++)
		{
			empties.addAll(getEmptySlots(i));
		}
		
		return empties;
	}
	
	/**
	 * Retrieve the tuple slots that are occupied (full) at the given level in the tree.
	 * 
	 * @param level
	 * @return
	 * @throws TreeException 
	 */
	private List<Integer> getFullTuplesAtLevel(int level) throws TreeException
	{
		List<Integer> filled = new ArrayList<Integer>();
		
		int len = (int)Math.pow(fanout, level);
		int low = len - 1;
		for (int i = low; i < low + len; i++)
		{
			filled.addAll(getFullSlots(i));
		}
		
		return filled;
	}

	/**
	 * Determine the child index at the next lower layer in the tree
	 * based on the leaf encoding L and the specified level in the tree.
	 * 
	 * @param t - tuple under consideration
	 * @param level - current level in the tree
	 * @return the child index in the next level of the tree
	 */
	private int nextChildrenIndices(Tuple t, int level)
	{
		String rep = t.getKAryRep(fanout, lBits);
		Util.debug(rep + ", " + level);
		int child = Integer.parseInt("" + rep.charAt(level)); // levels are 0-based	
		return child;
	} 
	
	/**
	 * Select a random non-empty tuple from some node in the level and push it
	 * down towards the right child.
	 * 
	 * @param level (!= last level of leaves)
	 * @return success or error
	 * @throws TreeException 
	 */
	private RC pushDown(int level) throws TreeException
	{
		List<Integer> fullIndices = getFullTuplesAtLevel(level);
		if (fullIndices.isEmpty())
		{
			// If there are no full tuples at this level (rare), do nothing
			Util.disp("Nothing to move at level " + level);
			return RC.SUCCESS;
		}
		
		// Select random full tuple at level and empty tuple at level+1
		List<Integer> nonEmptyBuckets = getNonEmptyBucketsAtLevel(level);
		if (nonEmptyBuckets.isEmpty())
		{
			return RC.SUCCESS;
		}
		int sourceBucket = nonEmptyBuckets.get(Random.generateRandomInt(0,  nonEmptyBuckets.size() - 1));
		List<Integer> filled = getFullSlots(sourceBucket);
		if (filled.isEmpty())
		{
			return RC.SUCCESS;
		}
		int sourceTupleIndex = filled.get(Random.generateRandomInt(0,  filled.size() - 1));
		byte[] src = new byte[tupleSize];
		RC ret = readTuple(src, sourceTupleIndex);
		if (ret != RC.SUCCESS)
		{
			throw new TreeException("Error reading tuple.");
		}
		Tuple srcTuple = new Tuple(src, lBytes, nBytes, dBytes);
		if (srcTuple.isOccupied() == false)
		{
			throw new TreeException("Tuple state corrupt: " + sourceTupleIndex);
		}
		
		// Move to an empty tuple slot in the appropriate bucket (based on leaf address)
		int child = nextChildrenIndices(srcTuple, level + 1);
		int targetBucket = (fanout * sourceBucket) + child + 1;
		List<Integer> empties = null;
		
		// If we've overflown into the leaves, take the leaf expansion into account
		// Choose a random bucket in the leaf to which this tuple will be placed
		if (targetBucket >= (N - 1)) 
		{
			int base = (int) (N + ((targetBucket - N) * leafExpansion));
			int offset = Random.generateRandomInt(0, leafExpansion);
			targetBucket = base + offset;
			empties = getEmptySlots(targetBucket);
			while (empties.isEmpty())
			{
				offset = Random.generateRandomInt(0, leafExpansion);
				targetBucket = base + offset;
				empties = getEmptySlots(targetBucket);
			}
		}
		else
		{
			empties = getEmptySlots(targetBucket);
		}
		if (empties.isEmpty())
		{
			throw new TreeException("No room in bucket " + targetBucket + " to move (" + sourceBucket + "," + sourceTupleIndex + ")");
		}
		int targetTupleIndex = empties.get(Random.generateRandomInt(0, empties.size() - 1));;
		
		// Push to level+1 (copy into buffer) and overwrite the destination tuple
		writeTuple(src, targetTupleIndex);
		byte[] zeros = new byte[tupleSize];
		Arrays.fill(zeros, (byte)0); 
		writeTuple(zeros, sourceTupleIndex); // this will zero out the full bit for the source tuple
		
		// TODO: replace this complete zero-out with a single byte write that zeros out the prepended tuple metadata 
		
		return RC.SUCCESS;
	}

	/**
	 * Insert a new tuple into the tree during initialization.
	 * 
	 * NOTE: ONLY INVOKED DURING TREE CREATION!
	 * 
	 * @param t - tuple to be inserted
	 * @return SUCCESS if successful, something else otherwise 
	 * @throws TreeException 
	 */
	public RC initialInsertTuple(Tuple t) throws TreeException 
	{
		RC ret = RC.SUCCESS;
		
		Util.disp("ORAM-" + level + " inserting: " + t);
		
		// Put in one children of the root, and then call push down on every level
		// => child c is at index: 1 + c
		int targetBucketIndex = nextChildrenIndices(t, 1) + 1;
		
		// Pick a random tuple in the target bucket
		List<Integer> emptySpots = getEmptySlots(targetBucketIndex);
		if (emptySpots.isEmpty())
		{
			throw new TreeException("No empty spots in the target bucket: " + targetBucketIndex);
		}
		int targetTupleIndex = emptySpots.get(Random.generateRandomInt(0,  emptySpots.size() - 1));
		
		// Store the tuple and mark the slot as in use
		byte[] raw = t.toArray();
		ret = writeTuple(raw, targetTupleIndex);
		if (ret != RC.SUCCESS)
		{
			System.out.println(ret.toString());
			throw new TreeException("Failed to write tuple at slot (" + targetBucketIndex + "," + targetTupleIndex + ")");
		}
		
		// Sanity check...
		raw = new byte[tupleSize];
		ret = readTuple(raw, targetTupleIndex);
		if (ret != RC.SUCCESS)
		{
			return ret;
		}
		Tuple copy = new Tuple(raw, lBytes, nBytes, dBytes);
		if (!copy.isOccupied())
		{
			Util.disp(t.toString());
			Util.disp(copy.toString());
			throw new TreeException("Write error.");
		}
		
		// Fetch the number of occupied entries to perform sanity check after push
		int numOccupied = getNumOccupiedTuples();
		
		// Call push down at every level (in reverse order)
		for (int i = numLevels - 1; i >= 1; i--)
		{
			pushDown(i);
		}
		
		// Post-push down occupancy check
		if (numOccupied != getNumOccupiedTuples())
		{
			ret = RC.TREE_PUSH_DOWN_ERROR;
		}
		
		return ret;
	}
	
	private List<Integer> getTupleIndicesOnPathToLeaf(long leafNum)
	{
		List<Integer> indices = new ArrayList<Integer>();
		
		// The root is always included in the path
		for (int i = 0; i < this.bucketDepth; i++)
		{
			indices.add(i);  
		}
		
		// Add the tuple indices in the bucket based on the leaf k-ary representation
		for (int l = 1; l < numLevels; l++)
		{
			String rep = Util.toKaryString(leafNum, fanout, lBits);
			int bucketPos = (fanout * l) + 1 + Integer.parseInt("" + rep.charAt(l)); // levels are 0-based
			int start = bucketPos * bucketDepth;
			int len = bucketDepth;
	 		for (int i = start; i < start + len; i++)
			{
	 			indices.add(i);
			}
		}
		
		return indices;
	}
	
	public List<Integer> getBucketIndicesOnPathToLeaf(long leafNum)
	{
		List<Integer> indices = new ArrayList<Integer>();
		
		// The root is always included in the path
		indices.add(0);  
		
		// TODO: leaf level 4 buckets???
		for (int l = 1; l < numLevels; l++)
		{
			String rep = Util.toKaryString(leafNum, fanout, lBits);
			int bucketPos = (fanout * l) + 1 + Integer.parseInt("" + rep.charAt(l)); // levels are 0-based
	 		indices.add(bucketPos);
		}
		
		return indices;
	}
	
	/**
	 * Retrieve a list of tuples along the path from the root to the leaf.
	 * 
	 * @param leafNum
	 * @return
	 * @throws TreeException 
	 */
	public List<Tuple> getPathToLeaf(long leafNum) throws TreeException
	{
		List<Tuple> path = new ArrayList<Tuple>();
		
		for (Integer i : getTupleIndicesOnPathToLeaf(leafNum))
		{
			byte[] buffer = new byte[tupleSize];
			RC ret = readTuple(buffer, i);
			if (ret != RC.SUCCESS)
			{
				throw new TreeException("Error extracting root-to-leaf " + leafNum + " tuples given tuple " + i);
			}
			path.add(new Tuple(buffer, lBytes, nBytes, dBytes));
		}
		
		return path;
	}
	
	/**
	 * Blindly update the root-to-leaf path with the new set of tuples.
	 * 
	 * @param tuples
	 * @param leafNum
	 * @return
	 */
	public RC updatePathToLeaf(List<Tuple> tuples, long leafNum)
	{
		RC ret = RC.SUCCESS;
		
		int tupleIndex = 0;
		List<Integer> indices = getTupleIndicesOnPathToLeaf(leafNum);
		if (indices.size() != tuples.size())
		{
			return RC.TREE_INVALID_PATH_LENGTH;
		}
		for (Integer i : getTupleIndicesOnPathToLeaf(leafNum))
		{
			ret = writeTuple(tuples.get(tupleIndex++).toArray(), i);
			if (ret != RC.SUCCESS)
			{
				return ret;
			}
		}
		
		return ret;
	}
	
	/**
	 * Display all information about the tree
	 * 
	 * @return human-readable (?) string representation
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < numTuples; i++)
		{
			byte[] raw = new byte[tupleSize];
			RC ret = readTuple(raw, i);
			if (ret != RC.SUCCESS)
			{
				try
				{
					throw new TreeException("Error reading tuple.");
				}
				catch (TreeException e)
				{
					e.printStackTrace();
				}
			}
 			Tuple t = new Tuple(raw, lBytes, nBytes, dBytes);
			builder.append(i + ": " + t.isOccupied() + "\n");
		}
		
		return builder.toString();
	}
	
	/**
	 * Retrieve a list of the tuples currently in use.
	 * 
	 * @return in-use tuples
	 * @throws TreeException 
	 */
	public List<Integer> inUseList() throws TreeException
	{
		List<Integer> tuples = new ArrayList<Integer>();
		for (int i = 0; i < numTuples; i++)
		{
			byte[] raw = new byte[tupleSize];
			RC ret = readTuple(raw, i);
			if (ret != RC.SUCCESS)
			{
				throw new TreeException("Error reading tuple.");
			}
 			Tuple t = new Tuple(raw, lBytes, nBytes, dBytes);
 			if (t.isOccupied()) tuples.add(i);
		}
		return tuples;
	}
	
	/**
	 * Retrieve the number of occupied tuples in the tree.
	 * @return
	 * @throws TreeException 
	 */
	public int getNumOccupiedTuples() throws TreeException
	{
		int count = 0;
		for (int i = 0; i < numTuples; i++)
		{
			byte[] raw = new byte[tupleSize];
			RC ret = readTuple(raw, i);
			if (ret != RC.SUCCESS)
			{
				throw new TreeException("Error reading tuple.");
			}
			Tuple other = new Tuple(raw, lBytes, nBytes, dBytes);
			if (other.isOccupied())
			{
				count++;
			}
		}
		return count;
	}
}
