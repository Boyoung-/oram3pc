package sprout.oram;

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
