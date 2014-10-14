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
	
	private byte[] getTuple(long tupleNum) throws TreeException
	{
		if (tupleNum < 0 || tupleNum >= ForestMetadata.getNumTuples(index))
			throw new TreeException("Tuple number error");
		
		int wholeTupleBytes = ForestMetadata.getWholeTupleBytes(index);
		long start = ForestMetadata.getTreeOffset(index) + tupleNum * wholeTupleBytes;
		return Forest.getForestData(start, wholeTupleBytes);
	}
	
	public Tuple readTuple(int tupleNum) throws TreeException, TupleException 
	{
		return new Tuple(index, getTuple(tupleNum));
	}
	
	private void setTuple(byte[] tuple, long tupleNum) throws TreeException
	{
		if (tupleNum < 0 || tupleNum >= ForestMetadata.getNumTuples(index))
			throw new TreeException("Tuple number error");
		
		int wholeTupleBytes = ForestMetadata.getWholeTupleBytes(index);
		if (wholeTupleBytes != tuple.length)
			throw new TreeException("Tuple length error");
		
		long start = ForestMetadata.getTreeOffset(index) + tupleNum * wholeTupleBytes;
		Forest.setForestData(tuple, start);
	}
	
	public void writeTuple(Tuple tuple, int tupleNum) throws TreeException, TupleException 
	{
		setTuple(tuple.toByteArray(), tupleNum);
	}
	
	public Tuple readLeafTuple(long n) throws TreeException, TupleException
	{
		if (n < 0 || n >= ForestMetadata.getNumLeaves(index)*ForestMetadata.getBucketDepth()*ForestMetadata.getLeafExpansion())
			throw new TreeException("Leaf tuple number error");
		
		long base = (ForestMetadata.getNumLeaves(index) - 1) * ForestMetadata.getBucketDepth();
		return new Tuple(index, getTuple(base + n));
	}
	
	public void writeLeafTuple(Tuple t, long n) throws TreeException 
	{
		if (n < 0 || n >= ForestMetadata.getNumLeaves(index)*ForestMetadata.getBucketDepth()*ForestMetadata.getLeafExpansion())
			throw new TreeException("Leaf tuple number error");
		
		byte[] raw = t.toByteArray();
		long base = (ForestMetadata.getNumLeaves(index) - 1) * ForestMetadata.getBucketDepth();
		setTuple(raw, base + n);
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
	
	public List<Tuple> getTuplesOnPath(long L) throws TreeException, TupleException
	{
		if (L < 0 || L >= ForestMetadata.getNumLeaves(index))
			throw new TreeException("Invalid path");
		
		List<Tuple> path = new ArrayList<Tuple>();
		
		for (long tupleIndex : getTupleIndicesOnPath(L))
		{
			Tuple t = new Tuple(index, getTuple(tupleIndex));
			path.add(t);
		}
		
		return path;
	}
	
	public void setTuplesOnPath(List<Tuple> tuples, long L) throws TreeException
	{
		if (L < 0 || L >= ForestMetadata.getNumLeaves(index))
			throw new TreeException("Invalid path");
		List<Long> indices = getTupleIndicesOnPath(L);
		if (indices.size() != tuples.size())
			throw new TreeException("Number of tuples is not correct");
		
		for (int i=0; i<indices.size(); i++)
			setTuple(tuples.get(i).toByteArray(), indices.get(i));
	}
	
}
