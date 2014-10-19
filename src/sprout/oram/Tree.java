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
	
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	public int getTreeIndex() 
	{
		return index;
	}
	
	private byte[] getByteBucket(long bucketNum) throws TreeException
	{
		if (bucketNum < 0 || bucketNum >= ForestMetadata.getNumBuckets(index))
			throw new TreeException("Bucket number error");
		
		int bucketBytes = ForestMetadata.getBucketBytes(index);
		long start = ForestMetadata.getTreeOffset(index) + bucketNum * bucketBytes;
		return Forest.getForestData(start, bucketBytes);
	}
	
	public Bucket getBucket(long bucketNum) throws BucketException, TreeException
	{
		return new Bucket(index, getByteBucket(bucketNum));
	}
	
	private void setByteBucket(byte[] bucket, long bucketNum) throws TreeException
	{
		if (bucketNum < 0 || bucketNum >= ForestMetadata.getNumBuckets(index))
			throw new TreeException("Bucket number error");
		
		int bucketBytes = ForestMetadata.getBucketBytes(index);
		if (bucket.length != bucketBytes)
			throw new TreeException("Bucket length error");
		
		long start = ForestMetadata.getTreeOffset(index) + bucketNum * bucketBytes;
		Forest.setForestData(bucket, start);
	}
	
	public void setBucket(Bucket bucket, long bucketNum) throws TreeException 
	{
		setByteBucket(bucket.toByteArray(), bucketNum);
	}
	
	private List<Long> getBucketIndicesOnPath(long L) throws TreeException
	{
		List<Long> indices = new ArrayList<Long>();
		if (index == 0) {
			indices.add(0L);
			return indices;
		}			
		
		if (L < 0 || L >= ForestMetadata.getNumLeaves(index))
			throw new TreeException("Invalid path");
		
		int w = ForestMetadata.getBucketDepth();
		int e = ForestMetadata.getLeafExpansion();
		int lBits = ForestMetadata.getLBits(index);
		
		for (int i=0; i<lBits; i++) {
			long bucketIndex = (L >> (lBits-i)) + (long) Math.pow(2, i) - 1;
			indices.add(bucketIndex);
		}
		
		long bucketIndex = ForestMetadata.getNumLeaves(index)-1 + L*e;
		for (int i=0; i<w; i++)
			indices.add(bucketIndex+i);
		
		return indices;
	}
	
	public List<Bucket> getBucketsOnPath(long L) throws TreeException, BucketException
	{
		List<Bucket> path = new ArrayList<Bucket>();
		for (long bucketIndex : getBucketIndicesOnPath(L))
			path.add(getBucket(bucketIndex));
		
		return path;
	}
	
	public void setBucketsOnPath(List<Bucket> buckets, long L) throws TreeException
	{
		List<Long> indices = getBucketIndicesOnPath(L);
		if (indices.size() != buckets.size())
			throw new TreeException("Number of buckets is not correct");
		
		for (int i=0; i<indices.size(); i++)
			setBucket(buckets.get(i), indices.get(i));
	}
	
}
