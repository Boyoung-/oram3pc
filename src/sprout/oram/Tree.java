package sprout.oram;

import java.util.ArrayList;
import java.util.List;

public class Tree
{	
	private int index;
	
	public Tree(int index)
	{
		this.index = index;
	}
	
	public int getTreeIndex() 
	{
		return index;
	}
	
	public byte[] readTuple(long tupleNum) throws TreeException
	{
		if (tupleNum < 0 || tupleNum >= ForestMetadata.getNumTuples(index))
			throw new TreeException("Tuple number error");
		
		int wholeTupleBytes = ForestMetadata.getWholeTupleBytes(index);
		long start = ForestMetadata.getTreeOffset(index) + tupleNum * wholeTupleBytes;
		return Forest.getForestData(start, wholeTupleBytes);
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
	
	public void writeTuple(byte[] tuple, long tupleNum) throws TreeException
	{
		if (tupleNum < 0 || tupleNum >= ForestMetadata.getNumTuples(index))
			throw new TreeException("Tuple number error");
		
		int wholeTupleBytes = ForestMetadata.getWholeTupleBytes(index);
		if (wholeTupleBytes != tuple.length)
			throw new TreeException("Tuple length error");
		
		long start = ForestMetadata.getTreeOffset(index) + tupleNum * wholeTupleBytes;
		Forest.setForestData(tuple, start);
	}
	
	public Tuple readLeafTuple(long n) throws TreeException, TupleException
	{
		if (n < 0 || n >= ForestMetadata.getNumLeaves(index)*ForestMetadata.getBucketDepth()*ForestMetadata.getLeafExpansion())
			throw new TreeException("Leaf tuple number error");
		
		long base = (ForestMetadata.getNumLeaves(index) - 1) * ForestMetadata.getBucketDepth();
		return new Tuple(index, readTuple(base + n));
	}
	
	public void writeLeafTuple(Tuple t, long n) throws TreeException 
	{
		if (n < 0 || n >= ForestMetadata.getNumLeaves(index)*ForestMetadata.getBucketDepth()*ForestMetadata.getLeafExpansion())
			throw new TreeException("Leaf tuple number error");
		
		byte[] raw = t.toByteArray();
		long base = (ForestMetadata.getNumLeaves(index) - 1) * ForestMetadata.getBucketDepth();
		writeTuple(raw, base + n);
	}
	
	private List<Long> getTupleIndicesOnPath(long L) throws TreeException
	{
		if (L < 0 || L >= ForestMetadata.getNumLeaves(index))
			throw new TreeException("Invalid path");
		
		int w = ForestMetadata.getBucketDepth();
		int e = ForestMetadata.getLeafExpansion();
		int lBits = ForestMetadata.getLBits(index);
		List<Long> indices = new ArrayList<Long>();
		
		for (int i=0; i<lBits; i++) {
			long bucketIndex = (L >> (lBits-i)) + (long) Math.pow(2, i) - 1;
			for (long j=bucketIndex*w; j<bucketIndex*w+w; j++)
				indices.add(j);
		}
		
		long startTuple = (ForestMetadata.getNumLeaves(index)-1 + L*e) * w;
		for (long j=startTuple; j<startTuple+w*e; j++)
			indices.add(j);
		
		return indices;
	}
	
	// TODO: test this function
	public List<Tuple> getTuplesOnPath(long L) throws TreeException, TupleException
	{
		if (L < 0 || L >= ForestMetadata.getNumLeaves(index))
			throw new TreeException("Invalid path");
		
		List<Tuple> path = new ArrayList<Tuple>();
		
		for (long tupleIndex : getTupleIndicesOnPath(L))
		{
			Tuple t = new Tuple(index, readTuple(tupleIndex));
			path.add(t);
		}
		
		return path;
	}
	
	// TODO: test this function
	public void updatePathToLeaf(List<Tuple> tuples, long L) throws TreeException
	{
		if (L < 0 || L >= ForestMetadata.getNumLeaves(index))
			throw new TreeException("Invalid path");
		List<Long> indices = getTupleIndicesOnPath(L);
		if (indices.size() != tuples.size())
			throw new TreeException("Number of tuples is not correct");
		
		for (int i=0; i<indices.size(); i++)
			writeTuple(tuples.get(i).toByteArray(), indices.get(i));
	}
	
}
