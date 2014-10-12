package sprout.oram;

import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import sprout.util.Util;

public class Tuple
{
	private byte[] nonce;
	private byte[] tuple;
	
	public Tuple(byte[] tuple, byte[] nonce)
	{
		this.nonce = nonce.clone();
		this.tuple = tuple.clone();
	}
	
	public void setTuple(byte[] tuple)
	{
		this.tuple = tuple.clone();
	}
	
	public void setNonce(byte[] nonce)
	{
		this.nonce = nonce.clone();
	}
	
	public byte[] getTuple()
	{
		return tuple;
	}
	
	public byte[] getNonce()
	{
		return nonce;
	}
	
	public void setWhole(byte[] tuple, byte[] nonce)
	{
		setTuple(tuple);
		setNonce(nonce);
	}
	
	public byte[] getWhole()
	{
		
	}
	
	public byte getFb() 
	{
		
	}
	
	public byte[] getN()
	{
		return leaf;
	}
	
	public byte[] getL()
	{
		
	}
	
	public byte[] getA()
	{
		
	}
	
	@Override
	public String toString()
	{
		String t = DatatypeConverter.printHexBinary()
		StringBuilder builder = new StringBuilder();
		
		builder.append("M = " + metadata);
		long l = Util.byteArrayToLong(L);
		builder.append(", L=" + Util.toKaryString(l, 2, L.length * 8));
		long n = Util.byteArrayToLong(N);
		builder.append(", N=" + Util.toKaryString(n, 2, N.length * 8));
		builder.append(", D=0x");
		for (int i = 0; i < D.length; i++)
		{
			builder.append(Integer.toHexString(D[i]));
		}
		
		// Append size in bytes
		builder.append(", [" + (1 + L.length + N.length + D.length) + "B]");
		
		return builder.toString();
	}
	
}
