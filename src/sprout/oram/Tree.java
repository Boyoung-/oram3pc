package sprout.oram;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Tree {
	private int index;

	public Tree(int index) {
		this.index = index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getTreeIndex() {
		return index;
	}

	private byte[] getByteBucket(long bucketNum) throws TreeException {
		if (bucketNum < 0 || bucketNum >= ForestMetadata.getNumBuckets(index))
			throw new TreeException("Bucket number error");

		int bucketBytes = ForestMetadata.getBucketBytes(index);
		long start = ForestMetadata.getTreeOffset(index) + bucketNum
				* bucketBytes;
		return Forest.getForestData(start, bucketBytes);
	}

	public Bucket getBucket(long bucketNum) throws BucketException,
			TreeException {
		return new Bucket(index, getByteBucket(bucketNum));
	}

	private void setByteBucket(byte[] bucket, long bucketNum)
			throws TreeException {
		if (bucketNum < 0 || bucketNum >= ForestMetadata.getNumBuckets(index))
			throw new TreeException("Bucket number error");

		int bucketBytes = ForestMetadata.getBucketBytes(index);
		if (bucket.length != bucketBytes)
			throw new TreeException("Bucket length error");

		long start = ForestMetadata.getTreeOffset(index) + bucketNum
				* bucketBytes;
		Forest.setForestData(start, bucket);
	}

	public void setBucket(Bucket bucket, long bucketNum) throws TreeException {
		setByteBucket(bucket.toByteArray(), bucketNum);
	}

	private List<Long> getBucketIndicesOnPath(long L) throws TreeException {
		List<Long> indices = new ArrayList<Long>();
		if (index == 0) {
			indices.add(0L);
			return indices;
		}

		if (L < 0 || L >= ForestMetadata.getNumLeaves(index))
			throw new TreeException("Invalid path");

		int e = ForestMetadata.getLeafExpansion();
		int lBits = ForestMetadata.getLBits(index);

		for (int i = 0; i < lBits; i++) {
			long bucketIndex = (L >> (lBits - i)) + (long) Math.pow(2, i) - 1;
			indices.add(bucketIndex);
		}

		long bucketIndex = ForestMetadata.getNumLeaves(index) - 1 + L * e;
		for (int i = 0; i < e; i++)
			indices.add(bucketIndex + i);

		return indices;
	}

	public Bucket[] getBucketsOnPath(BigInteger L) throws TreeException,
			BucketException {
		if (L == null && index != 0)
			throw new TreeException("L is null");
		else if (L == null && index == 0)
			return getBucketsOnPath(0);
		else
			return getBucketsOnPath(L.longValue());
	}

	public Bucket[] getBucketsOnPath(long L) throws TreeException,
			BucketException {
		List<Long> indices = getBucketIndicesOnPath(L);
		Bucket[] buckets = new Bucket[indices.size()];
		for (int i = 0; i < indices.size(); i++)
			buckets[i] = getBucket(indices.get(i));
		return buckets;
	}

	public Bucket[] getBucketsOnPath(String L) throws TreeException,
			BucketException {
		if (L == null)
			throw new TreeException("L is null");
		if (L.equals("") && index != 0)
			throw new TreeException("Invalid L");

		if (L.equals(""))
			return getBucketsOnPath(0);
		return getBucketsOnPath(new BigInteger(L, 2).longValue());
	}

	public void setBucketsOnPath(Bucket[] buckets, BigInteger L)
			throws TreeException {
		if (L == null && index != 0)
			throw new TreeException("L is null");
		else if (L == null && index == 0) {
			setBucketsOnPath(buckets, 0);
			return;
		} else
			setBucketsOnPath(buckets, L.longValue());
	}

	public void setBucketsOnPath(Bucket[] buckets, long L) throws TreeException {
		List<Long> indices = getBucketIndicesOnPath(L);
		if (indices.size() != buckets.length)
			throw new TreeException("Number of buckets is not correct");

		for (int i = 0; i < indices.size(); i++)
			setBucket(buckets[i], indices.get(i));
	}

	public void setBucketsOnPath(Bucket[] buckets, String L)
			throws TreeException {
		if (L == null)
			throw new TreeException("L is null");
		if (L.equals("") && index != 0)
			throw new TreeException("Invalid L");

		if (L.equals("")) {
			setBucketsOnPath(buckets, 0);
			return;
		}
		setBucketsOnPath(buckets, new BigInteger(L, 2).longValue());
	}

}
