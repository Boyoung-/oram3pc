package sprout.oram;

import java.util.Arrays;

import sprout.util.Util;

public class Tuple
{
	private static final int MASK_FULL = 0;
	
	// Internals
	private byte metadata;
	private byte[] L;
	private long leaf;
	private byte[] N;
	private byte[] D;
	
	public Tuple(byte[] ll, byte[] nn, byte[] dd)
	{
		metadata = 0x00 | (1 << MASK_FULL); // add any other bit masks here
		L = Arrays.copyOf(ll, ll.length);
		leaf = sprout.util.Util.byteArrayToLong(L);
		N = Arrays.copyOf(nn, nn.length);
		D = Arrays.copyOf(dd, dd.length);
	}
	
	public Tuple(byte[] ll, byte[] nn, byte[] dataBuffer, int dBytes)
	{
		metadata = 0x00 | (1 << MASK_FULL); // add any other bit masks here
		L = Arrays.copyOf(ll, ll.length);
		leaf = sprout.util.Util.byteArrayToLong(ll);
		N = Arrays.copyOf(nn, nn.length);
		D = Arrays.copyOfRange(dataBuffer, 0, dBytes);
	}
	
	public Tuple(byte[] ll, byte[] nn, byte[] dataBuffer, int dBytes, int totalLength)
	{
		metadata = 0x00 | (1 << MASK_FULL); // add any other bit masks here
		L = Arrays.copyOf(ll, ll.length);
		leaf = sprout.util.Util.byteArrayToLong(ll);
		N = Arrays.copyOf(nn, nn.length);
		
		// Check to see if we need to pad the data
		if ((1 + L.length + N.length + dBytes) < totalLength)
		{
			int padSize = totalLength - (1 + L.length + N.length + dBytes);
			D = new byte[dBytes + padSize];
			for (int i = 0; i < dBytes; i++)
			{
				D[i] = dataBuffer[i];
			}
		}
		else
		{
			D = Arrays.copyOfRange(dataBuffer, 0, dBytes);
		}
	}

	public Tuple(byte[] buffer, int lBytes, int nBytes, int dBytes)
	{
		L = new byte[lBytes];
		N = new byte[nBytes];
		D = new byte[dBytes];
		metadata = buffer[0];
		
		int offset = 1; 
		for (int i = 0; i < lBytes; offset++, i++)
		{
			L[i] = buffer[offset];
		}
		for (int i = 0; i < nBytes; offset++, i++)
		{
			N[i] = buffer[offset];
		}
		for (int i = 0; i < dBytes; offset++, i++)
		{
			D[i] = buffer[offset];
		}
		
		// extract the leaf number
		this.leaf = sprout.util.Util.byteArrayToLong(L);
	}
	
	/**
	 * Concatenate the L/N/D byte arrays into a single structure.
	 * 
	 * @return - the concatenated arrays
	 */
	public byte[] toArray()
	{
		byte[] concat = new byte[L.length + N.length + D.length + 1];
		int offset = 1;
		concat[0] = metadata;
		for (int i = 0; i < L.length; i++)
		{
			concat[offset++] = L[i];
		}
		for (int i = 0; i < N.length; i++)
		{
			concat[offset++] = N[i];
		}
		for (int i = 0; i < D.length; i++)
		{
			concat[offset++] = D[i];
		}
		return concat;
	}
	
	public long getLeaf()
	{
		return leaf;
	}
	
	public byte[] getRawLeaf()
	{
		return L;
	}
	
	public byte[] getRawTag()
	{
		return N;
	}
	
	public String getKAryRep(int k, int pad) 
	{
		String karyRep = Long.toString(leaf, k);
		if (karyRep.length() < (pad + 1))
		{
			StringBuilder builder = new StringBuilder(karyRep);
			for (int i = 0; i < pad - karyRep.length() + 1; i++)
			{
				builder.insert(0, "0");
			}
			karyRep = builder.toString();
		}
		
		assert(karyRep.length() == pad);
		
		return karyRep;
	}
	
	public boolean matchesLeafOf(Tuple other)
	{
		if ((metadata & (1 << MASK_FULL)) > 0) // check to make sure we're actually full...
		{
			return Arrays.equals(L, other.getRawLeaf());
		}
		return false;
	}
	
	public boolean matchesRawLeaf(byte[] otherLeaf)
	{
		if ((metadata & (1 << MASK_FULL)) > 0) // check to make sure we're actually full...
		{
			return Arrays.equals(L, otherLeaf);
		}
		return false;
	}
	
	public boolean matchesTagOf(Tuple other)
	{
		return Arrays.equals(N, other.getRawTag());
	}
	
	public boolean matchesRawTag(byte[] otherTag)
	{
		return Arrays.equals(N, otherTag);
	}
	
	public boolean isOccupied()
	{
		return (metadata & (1 << MASK_FULL)) > 0;
	}
	
	public void setOccupied(boolean occupied)
	{
		if (occupied) 
		{
			metadata |= (1 << MASK_FULL);
		}
		else 
		{
			metadata &= ~(1 << MASK_FULL);
		}
	}
	
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		
		// Append tuple contents in binary/hex for readability
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
	
	public String getStringL() {
		long l = Util.byteArrayToLong(L);
		return Util.toKaryString(l, 2, L.length * 8);
	}
	
	public String getStringN() {
		long n = Util.byteArrayToLong(N);
		return Util.toKaryString(n, 2, N.length * 8);
	}
}
