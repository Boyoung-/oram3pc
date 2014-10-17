package sprout.oram;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import sprout.util.Util;

public class Bucket
{
	private int treeIndex;
	private byte[] nonce;
	private byte[] tuples;
	
	public Bucket(int treeIndex, byte[] nonce, byte[] tuples) throws BucketException
	{
		this.treeIndex = treeIndex;
		setBucket(nonce, tuples);
	}
	
	public Bucket(int treeIndex, byte[] nt) throws BucketException
	{
		this.treeIndex = treeIndex;
		setBucket(nt);
	}
	
	public Bucket(int treeIndex, Tuple[] tuples)
	{
		// TODO
	}
		
	public void setTuples(byte[] tuples) throws BucketException
	{
		int bucketTupleBytes = ForestMetadata.getBucketTupleBytes(treeIndex);
		if (tuples.length > bucketTupleBytes)
			throw new BucketException("Tuples length error");
		else {
			this.tuples = new byte[bucketTupleBytes];
			System.arraycopy(tuples, 0, this.tuples, bucketTupleBytes-tuples.length, tuples.length);
		}
	}
	
	public void setNonce(byte[] nonce) throws BucketException
	{
		if (nonce == null) {
			this.nonce = null;
			return;
		}
		
		int nonceBytes = ForestMetadata.getNonceBytes();
		if (nonce.length > nonceBytes)
			throw new BucketException("Nonce length error");
		else {
			this.nonce = new byte[nonceBytes];
			System.arraycopy(nonce, 0, this.nonce, nonceBytes-nonce.length, nonce.length);
		}
	}
	
	public void setBucket(byte[] nonce, byte[] tuples) throws BucketException
	{
		setNonce(nonce);
		setTuples(tuples);
	}
	
	public void setBucket(byte[] nt) throws BucketException
	{
		int nonceBytes = ForestMetadata.getNonceBytes();
		int bucketTupleBytes = ForestMetadata.getBucketTupleBytes(treeIndex);
		if (nt.length != (nonceBytes+bucketTupleBytes))
			throw new BucketException("Bucket length error");
		
		byte[] n = Arrays.copyOfRange(nt, 0, nonceBytes);
		byte[] t = Arrays.copyOfRange(nt, nonceBytes, nt.length);
		setBucket(n, t);
	}
	
	public byte[] getNonce()
	{
		return nonce;
	}
	
	public byte[] getByteTuple(int tupleIndex) throws BucketException
	{
		if (tupleIndex < 0 || tupleIndex >= ForestMetadata.getBucketDepth())
			throw new BucketException("Tuple index error");
		
		int tupleBits = ForestMetadata.getTupleBits(treeIndex);
		BigInteger bts = new BigInteger(1, tuples);
		return Util.rmSignBit(Util.getSubBits(bts, tupleIndex*tupleBits, (tupleIndex+1)*tupleBits).toByteArray());
	}
	
	public Tuple getTuple(int tupleIndex) throws TupleException, BucketException
	{
		return new Tuple(treeIndex, getByteTuple(tupleIndex));
	}
	
	public byte[] getByteTuples()
	{
		return tuples;
	}
	
	public Tuple[] getTuples() throws TupleException, BucketException
	{
		int w = ForestMetadata.getBucketDepth();
		Tuple[] ts = new Tuple[w];
		for (int i=0; i<w; i++) 
			ts[i] = getTuple(i);
		return ts;
	}
	
	public byte[] toByteArray()
	{
		return ArrayUtils.addAll(nonce, tuples);
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("Bucket: ");
		
		builder.append("Nonce(16)=");
		if (nonce == null)
			builder.append("null, ");
		else
			builder.append(new BigInteger(1, nonce).toString(16) + ", ");
		
		builder.append("Nonce(16)=" + new BigInteger(1, nonce).toString(16) + ", ");
		
		builder.append("Tuples(16)=" + new BigInteger(1, tuples).toString(16));

		return builder.toString();
	}
	
}
