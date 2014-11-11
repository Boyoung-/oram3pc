package sprout.oram;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.bouncycastle.asn1.nist.NISTNamedCurves;
import org.bouncycastle.math.ec.ECPoint;

import sprout.crypto.PRG;
import sprout.crypto.oprf.OPRF;
import sprout.oram.operations.OPRFHelper;
import sprout.util.Util;

public class Forest
{	
	static SecureRandom rnd = new SecureRandom();
	
	private static ArrayList<Tree> trees;	
	// TODO: write large data to disk
	private static ByteArray64 data;
	//private static byte[] data; 
								
	
	private String defaultFile = "files/forest.bin";
	
	private void initTrees() {
		int levels = ForestMetadata.getLevels();
		trees = new ArrayList<Tree>();
		for (int i=0; i<levels; i++)
			trees.add(new Tree(i));
	}
	
	public Forest(String filename) throws Exception
	{
		if (!ForestMetadata.getStatus())
			throw new ForestException("ForestMetadata is not setup");
		
		initTrees();
		
		readFromFile(filename);
	}
	
	@SuppressWarnings("unchecked")
	public Forest() throws Exception
	{		
		if (!ForestMetadata.getStatus())
			throw new ForestException("ForestMetadata is not setup");
		
		//data = new byte[(int) ForestMetadata.getForestBytes()];
		data = new ByteArray64(ForestMetadata.getForestBytes());
		
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
		
		int shiftN = ForestMetadata.getLastNBits() % tau;
		if (shiftN == 0)
			shiftN = tau;
		
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
					if (i == h)
						N[i] = BigInteger.valueOf(address);
					else if (i == h-1) 
						N[i] = N[i+1].shiftRight(shiftN);
					else
						N[i] = N[i+1].shiftRight(tau);
					//N[i] = BigInteger.valueOf(address >> ((h-i)*tau));
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
					//A = new BigInteger(ForestMetadata.getABits(i), rnd); // generate random record content
					A = BigInteger.valueOf(address); // for testing: record content is the same as its N
				else {
					BigInteger indexN = null;
					if (i == h-1)
						indexN = Util.getSubBits(N[i+1], 0, shiftN);
					else
						indexN = Util.getSubBits(N[i+1], 0, tau);
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
			}
			System.out.println("--------------------");
		}
		
		Util.disp("");
		
		//printToFile("files/test1.txt");
		
		encryptForest();
		
		//printToFile("files/test2.txt");
		
		writeToFile();
	}
	
	// for testing
	public void printToFile(String filename) throws BucketException, TreeException, IOException, TupleException
	{
		File file = new File(filename);
		FileUtils.writeStringToFile(file, trees.get(0).getBucket(0).getTuple(0) + "\n");
		int w = ForestMetadata.getBucketDepth();
		for (int i=1; i<trees.size(); i++) {
			Tree t = trees.get(i);
			for (long j=0; j<ForestMetadata.getNumBuckets(i); j++) {
				Bucket bucket = t.getBucket(j);
				for (int k=0; k<w; k++) {
					FileUtils.writeStringToFile(file, bucket.getTuple(k).toString() + "\n", true);
				}
			}
		}
	}
	
	
	// for testing
	public void printDecryptionToFile(String filename) throws BucketException, TreeException, NoSuchAlgorithmException, IOException, TupleException 
	{
		File file = new File(filename);
		OPRF oprf = OPRFHelper.getOPRF(false);
		BigInteger k = oprf.getK();
		int w = ForestMetadata.getBucketDepth();
		
		for (int i=0; i<trees.size(); i++) {
			Tree t = trees.get(i);
			int bucketTupleBits = ForestMetadata.getBucketTupleBits(i);
			for (long j=0; j<ForestMetadata.getNumBuckets(i); j++) {
				Bucket bucket = t.getBucket(j);
				ECPoint x = NISTNamedCurves.getByName("P-224").getCurve().decodePoint(bucket.getNonce());
				ECPoint v = x.multiply(k);
				PRG G = new PRG(bucketTupleBits); // TODO: why only fresh PRG works??
				BigInteger mask = new BigInteger(G.generateBitString(bucketTupleBits, v), 2);
				BigInteger ptext = new BigInteger(1, bucket.getByteTuples()).xor(mask);
				bucket.setBucket(new byte[0], Util.rmSignBit(ptext.toByteArray()));
				if (i == 0)
					FileUtils.writeStringToFile(file, bucket.getTuple(0) + "\n");
				else
					for (int o=0; o<w; o++) {
						FileUtils.writeStringToFile(file, bucket.getTuple(o).toString() + "\n", true);
					}
			}
		}
		
	}

	
	private void writeToFile() throws IOException
	{
		writeToFile(defaultFile);
	}
	
	private void writeToFile(String filename) throws IOException
	{
		//File file = new File(filename);
		//FileUtils.writeByteArrayToFile(file, data);
		data.writeToFile(filename);
	}
	
	private void readFromFile(String filename) throws IOException
	{
		//File file = new File(filename);
		//data = FileUtils.readFileToByteArray(file);
		data = new ByteArray64(filename);
	}
	
	private void encryptForest() throws BucketException, NoSuchAlgorithmException, TreeException
	{
		Util.disp("===== Encryption ===== ");
		OPRF oprf = OPRFHelper.getOPRF(false);
		System.out.println("RRRRRRRRRRRRRR: " + oprf.hasKey());
		ECPoint g = oprf.getG();
		ECPoint y = oprf.getY();
		
		for (int i=0; i<trees.size(); i++) {
			Tree t = trees.get(i);
			int bucketTupleBits = ForestMetadata.getBucketTupleBits(i);
			for (long j=0; j<ForestMetadata.getNumBuckets(i); j++) {
				Bucket bucket = t.getBucket(j);
				BigInteger r = oprf.randomExponent();
				ECPoint x = g.multiply(r);
				ECPoint v = y.multiply(r);
				PRG G = new PRG(bucketTupleBits); // TODO: why only fresh PRG works??
				BigInteger mask = new BigInteger(G.generateBitString(bucketTupleBits, v), 2);
				BigInteger ctext = new BigInteger(1, bucket.getByteTuples()).xor(mask);
				bucket.setBucket(x.getEncoded(), Util.rmSignBit(ctext.toByteArray()));
				Util.disp("Tree-" + i + " writing encrypted " + bucket);		
				t.setBucket(bucket, j);
			}
		}
		Util.disp("");
	}
	
	public Tree getTree(int index) throws ForestException
	{
		if (index < 0 || index >= trees.size())
		{
			throw new ForestException("Invalid tree index: " + index);
		}
		return trees.get(index);
	}
	
	// TODO: make the following non-static
	public static ByteArray64 getForestData()
	{
		return data;
	}
	
	public static byte[] getForestData(long offset, int length)
	{
		//byte[] tmp = new byte[length];
		//System.arraycopy(data, (int) offset, tmp, 0, length);
		//return tmp;
		return data.getBytes(offset, length);
	}
	
	public static void setForestData(long offset, byte[] newData) 
	{
		//System.arraycopy(newData, 0, data, (int) offset, newData.length); 
		data.setBytes(offset, newData);
	}
}
