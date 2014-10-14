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
	
	private static ArrayList<Tree> trees;	
	private static byte[] data; // keep all data in memory for testing now
	
	// TODO: randomize L
	public Forest() throws NoSuchAlgorithmException, TupleException, TreeException, ForestException
	{		
		if (!ForestMetadata.getStatus())
			throw new ForestException("ForestMetadata is not setup");
		
		// TODO: overflow??
		data = new byte[(int) ForestMetadata.getForestBytes()];
		
		int levels = ForestMetadata.getLevels();
		int h = levels - 1;
		trees = new ArrayList<Tree>();
		for (int i=0; i<levels; i++)
			trees.add(new Tree(i));
		
		long addressSpace = ForestMetadata.getAddressSpace();
		long numInsert = ForestMetadata.getNumInsert();
		if (numInsert < 0 || numInsert > addressSpace)
			numInsert = addressSpace;
		int tau = ForestMetadata.getTau();
		BigInteger FB = null;
		BigInteger[] N = new BigInteger[levels];
		BigInteger[] L = new BigInteger[levels];
		BigInteger A;
		BigInteger tuple;
		for (long address = 0L; address < numInsert; address++)
		{
			System.out.println("--------------------");
			System.out.println("addr: " + address);
			for (int i=h; i>=0; i--) 
			{
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
					L[i] = N[i].divide(BigInteger.valueOf(ForestMetadata.getBucketDepth()*ForestMetadata.getLeafExpansion()));
				}
				if (i == h)
					A = new BigInteger(ForestMetadata.getABits(i), rnd);
				else {
					BigInteger indexN = Util.getSubBits(N[i+1], 0, tau);
					int start = (ForestMetadata.getTwoTauPow()-indexN.intValue()-1) * ForestMetadata.getLBits(i+1);
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
				
				Tuple newTuple = new Tuple(i, BigInteger.ZERO.toByteArray(), Util.rmSignBit(tuple.toByteArray()));
				Util.disp("ORAM-" + i + " writing: " + newTuple);		
				trees.get(i).writeLeafTuple(newTuple, N[i].longValue());
			}
		}
		
		// TODO: encrypt all tuples in all trees??
		Util.disp("");
		Util.disp("===== Encryption ===== ");
		encryptFullTuples();
	}
	
	// TODO: change PRG
	private static void encryptFullTuples() throws TupleException, TreeException, NoSuchAlgorithmException
	{
		for (int i=0; i<trees.size(); i++) {
			Tree t = trees.get(i);
			for (int j=0; j<ForestMetadata.getNumTuples(i); j++) {
				Tuple tp = new Tuple(i, t.readTuple(j));
				if (new BigInteger(1, tp.getFB()).intValue() == 1 || i == 0) {
					BigInteger nonce = new BigInteger(ForestMetadata.getNonceBits(), rnd);
					PRG G = new PRG(ForestMetadata.getTupleBits(i));
					BigInteger mask = new BigInteger(G.generateBitString(ForestMetadata.getTupleBits(i), nonce), 2);
					BigInteger ctext = new BigInteger(1, tp.getTuple()).xor(mask);
					tp.setWhole(Util.rmSignBit(nonce.toByteArray()), Util.rmSignBit(ctext.toByteArray()));
					Util.disp("ORAM-" + i + " writing encrypted " + tp);		
					t.writeTuple(tp.toByteArray(), j);
				}
			}
		}
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
