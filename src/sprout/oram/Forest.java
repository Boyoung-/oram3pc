package sprout.oram;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;

import sprout.crypto.PRG;
import sprout.util.Util;

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
		BigInteger tuple;
		for (long address = 0L; address < addressSpace; address++)
		{
			System.out.println("--------------------");
			System.out.println("addr: " + address);
			for (int i=h; i>=0; i--) 
			{
				System.out.println("i: " + i);
				if (i == 0) 
				{
					//FB = BigInteger.ONE;
					N[i] = BigInteger.ZERO;
					//L[i] = BigInteger.ZERO;
				}
				else
				{
					FB = BigInteger.ONE;
					N[i] = BigInteger.valueOf(address >> ((h-i)*tau));
					//System.out.println("???");
					//System.out.println("ni: " + N[i]);
					L[i] = N[i].divide(BigInteger.valueOf(ForestMetadata.getBucketDepth()*ForestMetadata.getLeafExpansion()));
				}
				if (i == h)
					A = new BigInteger(ForestMetadata.getABits(i), rnd);
				else {
					BigInteger indexN = Util.getSubBits(N[i+1], 0, tau);
					int start = (ForestMetadata.getTwoTauPow()-indexN.intValue()-1) * ForestMetadata.getLBits(i+1);
					//System.out.println(N[i].longValue());
					Tuple old = trees.get(i).readLeafTuple(N[i].longValue());
					A = Util.setSubBits(new BigInteger(1, old.getA()), L[i+1], start, start+ForestMetadata.getLBits(i+1));
				}
				
				if (i == 0)
					tuple = A;
				else {
					tuple = FB.shiftLeft(ForestMetadata.getTupleBits(i)-1).or(
							N[i].shiftLeft(ForestMetadata.getLBits(i)+ForestMetadata.getABits(i))).or(
							L[i].shiftLeft(ForestMetadata.getABits(i))).or(
							A);
				}
				
				/*
				if (i > 0) {
					System.out.println(FB);
					System.out.println(Util.addZero(N[i].toString(2), ForestMetadata.getNBits(i)));
					System.out.println(Util.addZero(L[i].toString(2), ForestMetadata.getLBits(i)));
				}
				System.out.println(Util.addZero(A.toString(2), ForestMetadata.getABits(i)));
				System.out.println(tuple.toString(2));
				*/
					
				Tuple newTuple = new Tuple(i, BigInteger.ZERO.toByteArray(), tuple.toByteArray());
				trees.get(i).writeLeafTuple(newTuple, N[i].longValue());
			}
		}
		
		// TODO: encrypt all tuples in all trees
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
