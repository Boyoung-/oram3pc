package sprout.oram;

import java.math.BigInteger;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.util.Arrays;

import sprout.util.Util;

public class Tuple
{
	private int treeIndex;
	private byte[] nonce;
	private byte[] tuple;
	
	public Tuple(int treeIndex, byte[] nonce, byte[] tuple) throws TupleException
	{
		this.treeIndex = treeIndex;
		setWhole(nonce, tuple);
	}
	
	public Tuple(int treeIndex, byte[] nt) throws TupleException
	{
		this.treeIndex = treeIndex;
		setWhole(nt);
	}
	
	// TODO: add tuple(ti, nonce, fb, n, l, a)???
	
	public void setTuple(byte[] tuple) throws TupleException
	{
		int tupleBytes = ForestMetadata.getTupleBytes(treeIndex);
		if (tuple.length > tupleBytes)
			throw new TupleException("Tuple length error");
		else {
			this.tuple = new byte[tupleBytes];
			System.arraycopy(tuple, 0, this.tuple, tupleBytes-tuple.length, tuple.length);
		}
	}
	
	public void setNonce(byte[] nonce) throws TupleException
	{
		if (nonce == null) {
			this.nonce = null;
			return;
		}
		
		int nonceBytes = ForestMetadata.getNonceBytes();
		if (nonce.length > nonceBytes)
			throw new TupleException("Nonce length error");
		else {
			this.nonce = new byte[nonceBytes];
			System.arraycopy(nonce, 0, this.nonce, nonceBytes-nonce.length, nonce.length);
		}
	}
	
	public byte[] getTuple()
	{
		return tuple;
	}
	
	public byte[] getNonce()
	{
		return nonce;
	}
	
	public void setWhole(byte[] nonce, byte[] tuple) throws TupleException
	{
		setTuple(tuple);
		setNonce(nonce);
	}
	
	public void setWhole(byte[] nt) throws TupleException
	{
		int nonceBytes = ForestMetadata.getNonceBytes();
		int tupleBytes = ForestMetadata.getTupleBytes(treeIndex);
		if (nt.length != (nonceBytes+tupleBytes))
			throw new TupleException("Whole tuple length error");
		
		byte[] n = Arrays.copyOfRange(nt, 0, nonceBytes);
		byte[] t = Arrays.copyOfRange(nt, nonceBytes, nt.length);
		setWhole(n, t);
	}
	
	public byte[] toByteArray()
	{
		return ArrayUtils.addAll(nonce, tuple);
	}
	
	public byte[] getFB() 
	{
		if (treeIndex == 0)
			return new byte[0];
		
		int tupleBits = ForestMetadata.getTupleBits(treeIndex);
		BigInteger t = new BigInteger(1, tuple);
		return Util.getSubBits(t, tupleBits-1, tupleBits).toByteArray();
	}
	
	public byte[] getN()
	{
		if (treeIndex == 0)
			return new byte[0];
		
		int tupleBits = ForestMetadata.getTupleBits(treeIndex);
		int nBits = ForestMetadata.getNBits(treeIndex);
		BigInteger t = new BigInteger(1, tuple);
		return Util.rmSignBit(Util.getSubBits(t, tupleBits-1-nBits, tupleBits-1).toByteArray());
	}
	
	public byte[] getL()
	{
		if (treeIndex == 0)
			return new byte[0];
		
		int aBits = ForestMetadata.getABits(treeIndex);
		int lBits = ForestMetadata.getLBits(treeIndex);
		BigInteger t = new BigInteger(1, tuple);
		return Util.rmSignBit(Util.getSubBits(t, aBits, aBits+lBits).toByteArray());
	}
	
	public byte[] getA()
	{
		int aBits = ForestMetadata.getABits(treeIndex);
		BigInteger t = new BigInteger(1, tuple);
		return Util.rmSignBit(Util.getSubBits(t, 0, aBits).toByteArray());
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		builder.append("Tuple: TreeIndex=" + treeIndex + ", ");
		
		builder.append("Nonce(16)=");
		if (nonce == null)
			builder.append("null, ");
		else
			builder.append(new BigInteger(1, nonce).toString(16) + ", ");
		
		if (treeIndex > 0) {
			builder.append("FB(2)=" + new BigInteger(1, getFB()).toString(2) + ", ");
		
			builder.append("N(2)=" + Util.addZero(new BigInteger(1, getN()).toString(2), ForestMetadata.getNBits(treeIndex)) + ", ");
		
			builder.append("L(2)=" + Util.addZero(new BigInteger(1, getL()).toString(2), ForestMetadata.getLBits(treeIndex)) + ", ");
		}
		
		//builder.append("A(16)=" + new BigInteger(1, getA()).toString(16));
		builder.append("A(2)=" + Util.addZero(new BigInteger(1, getA()).toString(2), ForestMetadata.getABits(treeIndex)));
		
		return builder.toString();
	}
	
}
