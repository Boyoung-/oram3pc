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
	private int index;
	private int w;
	private int e;
	private long numBuckets;
	private long treeBytes;
	
	// Tuple params for the tree
	private int lBits;
	private int lBytes;
	private int nBits;
	private int nBytes;
	private int aBits;
	private int aBytes;
	private int tupleBits;
	private int tupleBytes;
	
	// Disk stuff
	private byte[] data;
	private long offset;
	
	// other info
	ForestMetadata metadata;
	
	/**
	 * Create an empty tree with the specified number of leaves.
	 * 
	 * @param numLeaves
	 */
	public Tree(int index, byte[] data, long offset, ForestMetadata metadata)
	{
		this.index = index;
		this.w = metadata.getBucketDepth();
		this.e = metadata.getLeafExpansion();
		this.numBuckets = metadata.getNumBuckets(index);
		this.treeBytes = metadata.getTreeBytes(index);
		
		this.lBits = metadata.getTupleBitsL(index);
		this.lBytes = metadata.getTupleBytesL(index);
		this.nBits = metadata.getTupleBitsN(index);
		this.nBytes = metadata.getTupleBytesN(index);
		this.aBits = metadata.getTupleBitA(index);
		this.aBytes = metadata.getTupleBytesA(index);
		this.tupleBits = metadata.getTupleSizeInBits(index);
		this.tupleBytes = metadata.getTupleSizeInBytes(index);
		
		this.data = data;
		this.offset = offset;
		
		this.metadata = metadata;
	}
	
	public int getTreeIndex() {
		return index;
	}
	
	public long getNumberOfBuckets()
	{
		return numBuckets;
	}
	
	public long getNumberOfTuples()
	{
		return numBuckets * w;
	}
	
	public int getBucketDepth()
	{
		return w;
	}
	
	public int getNumLevels()
	{
		return lBits;
	}
	
	public long getNumLeaves()
	{
		return (long) Math.pow(2, lBits);
	}
	
	public int getLeafExpansion()
	{
		return e;
	}
	
	/**
	 * Retrieve the size of the tree in bytes.
	 * 
	 * @return tree size in bytes
	 */
	public long getSizeInBytes()
	{
		return treeBytes;
	}
	
	public int getLBytes() {
		return lBytes;
	}
	
	public int getNBytes() {
		return nBytes;
	}
	
	public int getABytes() {
		return aBytes;
	}
	
	// TODO: overflow??
	public byte[] readTuple(int tupleNum)
	{
		long start = offset + tupleNum * tupleBytes;
		long end = start + tupleBytes;
		return Arrays.copyOfRange(data, (int) start, (int) end);
	}
	
	/*
	public Tuple getTuple(int tupleNum) throws TreeException {
		byte[] buffer = new byte[tupleSize];
		RC ret = readTuple(buffer, tupleNum);
		if (ret != RC.SUCCESS)
		{
			throw new TreeException("Error getting tuple " + tupleNum);
		}
		return new Tuple(buffer, lBytes, nBytes, dBytes);
	}
	*/
	
	// TODO: overflow??
	public RC writeTuple(byte[] tuple, int tupleNum)
	{
		if (tupleNum > 0 && tupleNum < getNumberOfTuples())
		{
			if (tupleBytes != tuple.length)
			{
				return RC.INVALID_TUPLE_SIZE;
			}
			long start = offset + tupleNum * tupleBytes;
			System.arraycopy(tuple, 0, data, (int) start, tupleBytes);
		}
		return RC.TREE_INVALID_SLOT_INDEX;
	}
	

	
	public RC initialInsertTuple(Tuple t, long n) throws TreeException 
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
	
}
