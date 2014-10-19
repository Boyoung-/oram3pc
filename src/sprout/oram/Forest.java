package sprout.oram;

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import sprout.crypto.PRG;
import sprout.util.Util;

public class Forest
{	
	static SecureRandom rnd = new SecureRandom();
	
	private static ArrayList<Tree> trees;	
	private static byte[] data; // keep all data in memory for testing now
	
	@SuppressWarnings("unchecked")
	public Forest() throws Exception
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
		int w = ForestMetadata.getBucketDepth();
		int e = ForestMetadata.getLeafExpansion();
		// following used to hold new tuple content
		BigInteger FB = null;
		BigInteger[] N = new BigInteger[levels];
		BigInteger[] L = new BigInteger[levels];
		BigInteger A;
		BigInteger tuple; // new tuple to be inserted
		Bucket bucket; // bucket to be updated
		long bucketIndex; // bucket index in the tree
		int tupleIndex; // tuple index in the bucket
		
		HashMap<Long, Long>[] nToSlot = new HashMap[levels];
		for (int i=1; i<levels; i++)
			nToSlot[i] = new HashMap<Long, Long>();
		
		System.out.println("===== Forest Generation =====");
		for (long address = 0L; address < numInsert; address++)
		{
			System.out.println("record: " + address);
			for (int i=h; i>=0; i--) 
			{
				if (i == 0) 
				{
					//FB = BigInteger.ONE;
					N[i] = BigInteger.ZERO;
					//L[i] = BigInteger.ZERO;
					bucketIndex = 0;
					tupleIndex = 0;
				}
				else
				{
					FB = BigInteger.ONE;
					N[i] = BigInteger.valueOf(address >> ((h-i)*tau));
					Long slot = nToSlot[i].get(N[i].longValue());
					if (slot == null) {
						do {
							slot = Util.nextLong(ForestMetadata.getNumLeafTuples(i));
						} while (nToSlot[i].containsValue(slot));
						nToSlot[i].put(N[i].longValue(), slot);
					}
					L[i] = BigInteger.valueOf(slot / (w * e));
					bucketIndex = slot / w + ForestMetadata.getNumLeaves(i) - 1;
					tupleIndex = (int) (slot % w);
				}
				
				bucket = trees.get(i).getBucket(bucketIndex);
				bucket.setIndex(i);
				
				if (i == h)
					A = new BigInteger(ForestMetadata.getABits(i), rnd); // generate random record content
				else {
					BigInteger indexN = Util.getSubBits(N[i+1], 0, tau);
					int start = (ForestMetadata.getTwoTauPow()-indexN.intValue()-1) * ForestMetadata.getLBits(i+1);
					Tuple old = bucket.getTuple(tupleIndex);
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
				
				Tuple newTuple = new Tuple(i, Util.rmSignBit(tuple.toByteArray()));
				bucket.setTuple(newTuple, tupleIndex);
				Util.disp("Tree-" + i + " writing " + newTuple);		
				trees.get(i).setBucket(bucket, bucketIndex);
				//System.out.println(bucket.getTuple(tupleIndex));
				//System.out.println(bucket);
				//System.out.println(trees.get(i).getBucket(bucketIndex));
			}
			System.out.println("--------------------");
		}
		
		Util.disp("");
		
		encryptAllBuckets();
	}
	
	// TODO: correct encryption
	private static void encryptAllBuckets() throws BucketException, NoSuchAlgorithmException, TreeException
	{
		Util.disp("===== Encryption ===== ");
		for (int i=0; i<trees.size(); i++) {
			Tree t = trees.get(i);
			int bucketTupleBits = ForestMetadata.getBucketTupleBits(i);
			for (long j=0; j<ForestMetadata.getNumBuckets(i); j++) {
				Bucket bucket = t.getBucket(j);
				BigInteger nonce = new BigInteger(ForestMetadata.getNonceBits(), rnd);
				PRG G = new PRG(bucketTupleBits);
				BigInteger mask = new BigInteger(G.generateBitString(bucketTupleBits, nonce), 2);
				BigInteger ctext = new BigInteger(1, bucket.toByteArray()).xor(mask);
				bucket.setBucket(Util.rmSignBit(nonce.toByteArray()), Util.rmSignBit(ctext.toByteArray()));
				Util.disp("Tree-" + i + " writing encrypted " + bucket);		
				t.setBucket(bucket, j);
			}
		}
		Util.disp("");
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
