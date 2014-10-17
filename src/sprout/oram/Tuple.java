package sprout.oram;

import java.math.BigInteger;
import sprout.util.Util;

public class Tuple
{
	private int treeIndex;
	private byte[] tuple;
	
	public Tuple(int treeIndex, byte[] tuple) throws TupleException
	{
		this.treeIndex = treeIndex;
		setTuple(tuple);
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
	
	public byte[] getTuple()
	{
		return tuple;
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
		
		builder.append("Tuple: ");
		
		if (treeIndex > 0) {
			builder.append("FB(2)=" + new BigInteger(1, getFB()).toString(2) + ", ");
		
			builder.append("N(2)=" + Util.addZero(new BigInteger(1, getN()).toString(2), ForestMetadata.getNBits(treeIndex)) + ", ");
		
			builder.append("L(2)=" + Util.addZero(new BigInteger(1, getL()).toString(2), ForestMetadata.getLBits(treeIndex)) + ", ");
		}
		
		builder.append("A(2)=" + Util.addZero(new BigInteger(1, getA()).toString(2), ForestMetadata.getABits(treeIndex)));
		//builder.append("A(16)=" + new BigInteger(1, getA()).toString(16));

		return builder.toString();
	}
	
}
