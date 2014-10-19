package sprout.ui;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import sprout.oram.Bucket;
import sprout.oram.Forest;
import sprout.oram.ForestMetadata;
import sprout.oram.Tree;
import sprout.oram.Tuple;
import sprout.util.Util;

public class ForestTest
{
	static SecureRandom rnd = new SecureRandom();
	
	public static void main(String[] args) throws Exception
	{
		ForestMetadata.setup("config/newConfig.yaml");
		
		// test tuple
		/*
		BigInteger input = new BigInteger(80, rnd).setBit(80);
		Tuple t = new Tuple(3, Util.rmSignBit(input.toByteArray()));
		System.out.println(input.toString(2));
		System.out.println(t);
		*/
		
		// test bucket
		/*
		Tuple[] tuples = new Tuple[4];
		for (int i=0; i<4; i++) {
			BigInteger input = new BigInteger(80, rnd).setBit(80);
			tuples[i] = new Tuple(3, Util.rmSignBit(input.toByteArray()));
			System.out.println(tuples[i]);
		}
		byte[] nonce = Util.rmSignBit(new BigInteger(128, rnd).toByteArray());
		System.out.println(new BigInteger(1, nonce).toString(16));
		Bucket b = new Bucket(3, nonce, tuples);
		Tuple[] tuples2 = b.getTuples();
		for (int i=0; i<4; i++) {
			System.out.println(new BigInteger(1, tuples2[i].toByteArray()).compareTo(new BigInteger(1, tuples[i].toByteArray())) == 0);
		}
		System.out.println(b);
		
		byte[] n = b.getNonce();
		byte[] ts = b.getByteTuples();
		Bucket b2 = new Bucket(3, n, ts);
		System.out.println(b2);
		
		Bucket b3 = new Bucket(3, b2.toByteArray());
		System.out.println(b3);
		
		int bucketTupleBytes = ForestMetadata.getBucketTupleBytes(3);
		b3.setTuples(new byte[bucketTupleBytes]);
		for (int i=0; i<4; i++) {
			b3.setTuple(tuples[i], i);
		}
		System.out.println(b3);
		
		for (int i=0; i<4; i++) {
			Tuple tmp = b3.getTuple(i);
			System.out.println(new BigInteger(1, tmp.toByteArray()).compareTo(new BigInteger(1, tuples[i].toByteArray())) == 0);
		}
		*/
		
		// test forest generation
		long start = System.currentTimeMillis();;
		
		Forest forest = new Forest();
		
	    long end = System.currentTimeMillis();;
	    System.out.println("Execution time: " + (end-start)/1000 + "s");
		
		// test get/set path
		/*
		System.out.println("===== Testing get/set path =====");
		Tree t = forest.getTree(3);
		List<Bucket> b1 = t.getBucketsOnPath(1);
		t.setBucketsOnPath(b1, 3);
		List<Bucket> b2 = t.getBucketsOnPath(3);
		Util.printListV(b1);
		System.out.println();
		Util.printListV(b2);
		System.out.println();
		*/
	}

}
