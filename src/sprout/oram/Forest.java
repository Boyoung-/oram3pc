package sprout.oram;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

import sprout.crypto.PRG;

public class Forest
{	
	static SecureRandom rnd = new SecureRandom();
	
	private ArrayList<Tree> trees;	
	private static byte[] data; // keep all data in memory for testing now
	
	public Forest() throws NoSuchAlgorithmException, TupleException, TreeException
	{		
		// TODO: overflow??
		data = new byte[(int) ForestMetadata.getForestBytes()];
		
		int levels = ForestMetadata.getLevels();
		int h = levels - 1;
		trees = new ArrayList<Tree>();
		for (int i=0; i<levels; i++)
			trees.add(new Tree(i));
		
		long addressSpace = ForestMetadata.getAddressSpace();
		int tau = ForestMetadata.getTau();
		BigInteger FB = null;
		BigInteger[] N = new BigInteger[levels];
		BigInteger[] L = new BigInteger[levels];
		BigInteger A;
		BigInteger nonce;
		BigInteger tuple;
		BigInteger mask;
		for (long address = 0L; address < addressSpace; address++)
		{
			for (int i=h; i>=0; i--) 
			{
				if (i > 0) 
				{
					FB = BigInteger.ONE;
					N[i] = BigInteger.valueOf(address >> ((h-i)*tau));
					L[i] = BigInteger.valueOf(2).pow(N[i].intValue()).divide(BigInteger.valueOf(ForestMetadata.getBucketDepth()*ForestMetadata.getLeafExpansion()));
				}
				if (i == h)
					A = new BigInteger(ForestMetadata.getABits(i), rnd);
				else {
					BigInteger indexN = getSubBits(N[i+1], 0, tau);
					int shift = (ForestMetadata.getTwoTauPow()-indexN.intValue()-1) * tau;
					A = indexN.shiftLeft(shift);
				}
				
				if (i == 0)
					tuple = A;
				else {
					tuple = FB.shiftLeft(ForestMetadata.getTupleBits(i)-1).or(
							N[i].shiftLeft(ForestMetadata.getLBits(i)+ForestMetadata.getABits(i))).or(
							L[i].shiftLeft(ForestMetadata.getABits(i))).or(
							A);
				}
					
				// encrypt tuple
				// TODO: change PRG
				nonce = new BigInteger(ForestMetadata.getNonceBits(), rnd);
				PRG G = new PRG(ForestMetadata.getTupleBits(i));
				mask = new BigInteger(G.generateBitString(ForestMetadata.getTupleBits(i), nonce), 2);
				//Tuple newTuple = new Tuple(i, nonce.toByteArray(), tuple.xor(mask).toByteArray());
				Tuple newTuple = new Tuple(i, null, tuple.toByteArray());
				trees.get(i).initialInsertTuple(newTuple, address);
			}
		}
	}
	
	public BigInteger getSubBits(BigInteger n, int i, int j)
	{
		return BigInteger.ONE.shiftLeft(j-i).subtract(BigInteger.ONE).shiftLeft(i).and(n).shiftRight(i);
	}
	
	/**
	 * Write the ORAM hierarchy tree to the specified file.
	 * 
	 * @param file - file to write tree contents to
	 * @return RC.SUCCESS on success, something else otherwise
	 * @throws IOException 
	 */
	public void writeFile(String file) throws IOException
	{
		if (ForestMetadata.getStatus())
		{
			ForestMetadata.write();
		}
	}
	
	public Tree getTree(int index) throws ForestException
	{
		if (index < 0 || index >= trees.size())
		{
			throw new ForestException("Invalid tree index: " + index);
		}
		return trees.get(index);
	}
	
	public static byte[] getForestData()
	{
		return data;
	}
	
	// TODO: overflow??
	public static byte[] getForestData(long offset, int length)
	{
		byte[] tmp = new byte[length];
		System.arraycopy(data, (int) offset, tmp, 0, length);
		return tmp;
	}
	
	// TODO: overflow??
	public static void setForestData(byte[] newData, long offset) 
	{
		System.arraycopy(newData, 0, data, (int) offset, newData.length); 
	}
}
