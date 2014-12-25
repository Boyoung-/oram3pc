package sprout.oram;

import java.math.BigInteger;
import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;

import sprout.util.Util;

public class Bucket {
	private int treeIndex;
	private byte[] nonce;
	private byte[] tuples;

	public Bucket(int treeIndex, byte[] nonce, byte[] tuples)
			throws BucketException {
		this.treeIndex = treeIndex;
		setBucket(nonce, tuples);
	}

	public Bucket(int treeIndex, byte[] nt) throws BucketException {
		this.treeIndex = treeIndex;
		setBucket(nt);
	}

	public Bucket(int treeIndex, byte[] nonce, Tuple[] tuples)
			throws BucketException {
		this.treeIndex = treeIndex;
		setBucket(nonce, tuples);
	}

	public void setIndex(int treeIndex) {
		this.treeIndex = treeIndex;
	}

	public void setBucket(byte[] nonce, byte[] tuples) throws BucketException {
		setNonce(nonce);
		setTuples(tuples);
	}

	public void setBucket(byte[] nonce, Tuple[] tuples) throws BucketException {
		setNonce(nonce);
		setTuples(tuples);
	}

	public void setBucket(byte[] nt) throws BucketException {
		int nonceBytes = ForestMetadata.getNonceBytes();
		int bucketTupleBytes = ForestMetadata.getBucketTupleBytes(treeIndex);
		if (nt.length != (nonceBytes + bucketTupleBytes))
			throw new BucketException("Bucket length error");

		byte[] n = Arrays.copyOfRange(nt, 0, nonceBytes);
		byte[] t = Arrays.copyOfRange(nt, nonceBytes, nt.length);
		setBucket(n, t);
	}

	public void setNonce(byte[] nonce) throws BucketException {
		if (nonce == null) {
			this.nonce = null;
			return;
		}

		int nonceBytes = ForestMetadata.getNonceBytes();
		if (nonce.length > nonceBytes)
			throw new BucketException("Nonce length error");
		else {
			this.nonce = new byte[nonceBytes];
			System.arraycopy(nonce, 0, this.nonce, nonceBytes - nonce.length,
					nonce.length);
		}
	}

	public void setTuples(byte[] tuples) throws BucketException {
		int bucketTupleBytes = ForestMetadata.getBucketTupleBytes(treeIndex);
		if (tuples.length > bucketTupleBytes)
			throw new BucketException("Tuples length error");
		else {
			this.tuples = new byte[bucketTupleBytes];
			System.arraycopy(tuples, 0, this.tuples, bucketTupleBytes
					- tuples.length, tuples.length);
		}
	}

	public void setTuples(Tuple[] tuples) throws BucketException {
		if (treeIndex == 0) {
			if (tuples.length != 1)
				throw new BucketException("Tuple array length error");
			setTuples(tuples[0].toByteArray());
			return;
		}

		int w = ForestMetadata.getBucketDepth();
		if (tuples.length != w)
			throw new BucketException("Tuple array length error");

		int tupleBits = ForestMetadata.getTupleBits(treeIndex);
		BigInteger bts = new BigInteger(1, tuples[w - 1].toByteArray());
		for (int i = w - 2; i >= 0; i--) {
			bts = new BigInteger(1, tuples[i].toByteArray()).shiftLeft(
					(w - 1 - i) * tupleBits).xor(bts);
		}
		byte[] ts = Util.rmSignBit(bts.toByteArray());
		int bucketTupleBytes = ForestMetadata.getBucketTupleBytes(treeIndex);
		this.tuples = new byte[bucketTupleBytes];
		System.arraycopy(ts, 0, this.tuples, bucketTupleBytes - ts.length,
				ts.length);
	}

	public void setByteTuple(byte[] t, int tupleIndex) throws BucketException {
		if (treeIndex == 0) {
			if (tupleIndex != 0)
				throw new BucketException("Tuple index error");
			setTuples(t);
			return;
		}

		int w = ForestMetadata.getBucketDepth();
		if (tupleIndex < 0 || tupleIndex >= w)
			throw new BucketException("Tuple index error");

		int tupleBits = ForestMetadata.getTupleBits(treeIndex);
		BigInteger bts = new BigInteger(1, tuples);
		BigInteger bt = new BigInteger(1, t);
		int start = (w - tupleIndex - 1) * tupleBits;
		byte[] newTuples = Util.rmSignBit(Util.setSubBits(bts, bt, start,
				start + tupleBits).toByteArray());
		int bucketTupleBytes = ForestMetadata.getBucketTupleBytes(treeIndex);
		tuples = new byte[bucketTupleBytes];
		System.arraycopy(newTuples, 0, tuples, bucketTupleBytes
				- newTuples.length, newTuples.length);
	}

	public void setTuple(Tuple t, int tupleIndex) throws BucketException {
		setByteTuple(t.toByteArray(), tupleIndex);
	}

	public int getTreeIndex() {
		return treeIndex;
	}

	public byte[] getNonce() {
		return nonce;
	}

	public byte[] getByteTuples() {
		return tuples;
	}

	public byte[] getByteTuple(int tupleIndex) throws BucketException {
		if (treeIndex == 0) {
			if (tupleIndex != 0)
				throw new BucketException("Tuple index error");
			return tuples;
		}

		int w = ForestMetadata.getBucketDepth();
		if (tupleIndex < 0 || tupleIndex >= w)
			throw new BucketException("Tuple index error");

		int tupleBits = ForestMetadata.getTupleBits(treeIndex);
		BigInteger bts = new BigInteger(1, tuples);
		int start = (w - tupleIndex - 1) * tupleBits;
		return Util.rmSignBit(Util.getSubBits(bts, start, start + tupleBits)
				.toByteArray());
	}

	public Tuple getTuple(int tupleIndex) throws TupleException,
			BucketException {
		return new Tuple(treeIndex, getByteTuple(tupleIndex));
	}

	public Tuple[] getTuples() throws TupleException, BucketException {
		int w = ForestMetadata.getBucketDepth();
		if (treeIndex == 0)
			w = 1;
		Tuple[] ts = new Tuple[w];
		for (int i = 0; i < w; i++)
			ts[i] = getTuple(i);
		return ts;
	}

	public byte[] toByteArray() {
		return ArrayUtils.addAll(nonce, tuples);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Bucket-" + treeIndex + ": ");

		builder.append("Nonce(16)=");
		if (nonce == null)
			builder.append("null, ");
		else
			builder.append(new BigInteger(1, nonce).toString(16) + ", ");

		builder.append("Tuples(16)=" + new BigInteger(1, tuples).toString(16));
		// builder.append("Tuples(2)=" + Util.addZero(new BigInteger(1,
		// tuples).toString(2), ForestMetadata.getBucketTupleBits(treeIndex)));

		return builder.toString();
	}

}
