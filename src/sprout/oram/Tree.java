package sprout.oram;

import java.util.Arrays;

import sprout.util.Util;
import sprout.util.RC;

public class Tree
{	
	private int index;
	private long offset;
	
	public Tree(int index, long offset)
	{
		this.index = index;
		this.offset = offset;
	}
	
	public int getTreeIndex() 
	{
		return index;
	}
	
	public long getDataOffset() 
	{
		return offset;
	}
	
	// TODO: overflow??
	public byte[] readTuple(long tupleNum)
	{
		int realTupleBytes = getRealTupleBytes();
		long start = offset + tupleNum * realTupleBytes;
		long end = start + realTupleBytes;
		return Arrays.copyOfRange(Forest.getForestData(), (int) start, (int) end);
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
	public RC writeTuple(byte[] tuple, long tupleNum)
	{
		if (tupleNum > 0 && tupleNum < getNumberOfTuples())
		{
			int realTupleBytes = getRealTupleBytes();
			if (realTupleBytes != tuple.length)
			{
				return RC.INVALID_TUPLE_SIZE;
			}
			long start = offset + tupleNum * realTupleBytes;
			System.arraycopy(tuple, 0, data, (int) start, realTupleBytes);
		}
		return RC.TREE_INVALID_SLOT_INDEX;
	}
	
	public RC initialInsertTuple(Tuple t, long n) throws TreeException 
	{
		Util.disp("ORAM-" + index + " inserting: " + t);		
		byte[] raw = t.toArray();
		long base = (long) (Math.pow(2, lBits) - 1) * w;
		RC ret = writeTuple(raw, base + n);
		if (ret != RC.SUCCESS)
		{
			System.out.println(ret.toString());
			throw new TreeException("Failed to write tuple at number " + (base+n));
		}
		
		return RC.SUCCESS;
	}
	
	/*
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
	*/
	
}
