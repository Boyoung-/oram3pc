// run this program first
// when seeing the prompt for needing the client
// start EvictionTestClient
package sprout.ui;

import sprout.crypto.Random;

import java.security.SecureRandom;

import sprout.oram.*;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Program.*;
import sprout.oram.Forest;
import sprout.oram.ForestException;

public class EvictionTest
{
	
	public static void main(String[] args) throws Exception {
		Forest forest = null;
		try
		{
			forest = new Forest();
			forest.buildFromFile("config/smallConfig.yaml", "config/smallData.txt", "db.bin");
		}
		catch (NumberFormatException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ForestException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// eviction takes place for OT in range 0 < i < h
		for (int i = 1; i < forest.getNumberOfTrees(); i++)
		{
			try
			{
				// 1. retrieve all buckets in the path from root to a random leaf L
				System.out.println("Working on ORAM-" + i);
				Tree t = forest.getTree(i);
				long numLeaves = t.getNumLeaves();
				long l = Random.generateRandomLong(0,  numLeaves - 1);
				System.out.println("\tFetch leaf path: " + l);
				List<Integer> buckets = t.getBucketIndicesOnPathToLeaf(l);
				//List<Tuple> tuples = t.getPathToLeaf(l);
				//System.out.println("numlevels: " + t.getNumLevels());
				//System.out.println("numtuples: " + tuples.size());
				
				// 2. decrypt all buckets
				// TODO: right now I'm not clear how the encryption/decryption work.
				//       Tree.getPathToLeaf() may be used to retrieve buckets and tuples
				//       in step 3 & 4 since on most levels we'll retrieve the same # of tuples
				
				// 3. for the leaf level, find two random empty tuples
				// first get tuples
				// TODO: should be 4 buckets instead of 1
				List<Tuple> leafTuples = new ArrayList<Tuple>();
				int leafBucket = buckets.get(buckets.size()-1);
				int bucketDepth = t.getBucketDepth();
		 		for (int tuple = leafBucket; tuple < leafBucket + bucketDepth; tuple++)
				{
		 			leafTuples.add(t.getTuple(tuple));
				}
		 		String leafL = leafTuples.get(0).getStringL();
		 		// get full bits and form a vector
		 		String bits = "00";
		 		for (int leaf = 0; leaf < leafTuples.size(); leaf++) {
		 			if (leafTuples.get(leaf).isOccupied())
		 				bits += "1";
		 			else
		 				bits += "0";
		 		}
		 		System.out.println("F2ET inputBits: " + bits);
		 		// feed to F2ET to find empty tuples
		 		ORAMTrialCommon.circuit = "F2ET";
		 		BigInteger inputBits = new BigInteger(new StringBuilder(bits).reverse().toString(), 2);
		 		ORAMTrialServer oramtrialserver = new ORAMTrialServer(inputBits, bits.length());
		 		oramtrialserver.run();
		 		System.out.println("F2ET outputBits:" + oramtrialserver.getOutput());
		 		String outputBits = oramtrialserver.getOutput();
		 		// find tuples' indices
		 		outputBits = outputBits.substring(2);
		 		int Epos1, Epos2;
		 		Epos1 = outputBits.indexOf('1');
		 		outputBits = outputBits.substring(Epos1+1);
		 		Epos2 = outputBits.indexOf('1') + Epos1 + 1;
		 		System.out.println("F2ET tuples positions: " + Epos1 + "  " + Epos2);
		 		
				// 4. TODO: for each non-leaf level, find two random full tuples
		 		//    now:  do (leaf - 1) level for testing
				//for (int j = 0; j < buckets.size(); j++) {
				//}
		 		List<Tuple> non_leafTuples = new ArrayList<Tuple>();
				int non_leafBucket = buckets.get(buckets.size()-2);
		 		for (int tuple = non_leafBucket; tuple < non_leafBucket + bucketDepth; tuple++)
				{
		 			non_leafTuples.add(t.getTuple(tuple));
				}
		 		// get full and direction bits and form a vector
		 		bits = "";
		 		String direcBits = "";
		 		for (int non_leaf = 0; non_leaf < non_leafTuples.size(); non_leaf++) {
		 			int level = buckets.size() - 2;
		 			String non_leafL = non_leafTuples.get(non_leaf).getStringL();
		 			if (non_leafL.charAt(level) == (leafL.charAt(level)))
		 				direcBits += "1";
		 			else
		 				direcBits += "0";
		 			if (non_leafTuples.get(non_leaf).isOccupied())
		 				bits += "1";
		 			else
		 				bits += "0";
		 		}
		 		bits = "00" + direcBits + bits;
		 		System.out.println("F2FT inputBits: " + bits);
		 		// feed to F2FT to find full tuples
		 		ORAMTrialCommon.circuit = "F2FT";
		 		inputBits = new BigInteger(new StringBuilder(bits).reverse().toString(), 2);
		 		oramtrialserver = new ORAMTrialServer(inputBits, bits.length());
		 		oramtrialserver.run();
		 		System.out.println("F2FT outputBits:" + oramtrialserver.getOutput());
		 		outputBits = oramtrialserver.getOutput();
		 		// find tuples' indices
		 		outputBits = outputBits.substring(2);
		 		int Fpos1, Fpos2;
		 		Fpos1 = outputBits.indexOf('1');
		 		outputBits = outputBits.substring(Fpos1+1);
		 		Fpos2 = outputBits.indexOf('1') + Fpos1 + 1;
		 		System.out.println("F2FT tuples positions: " + Fpos1 + "  " + Fpos2);
				
				// 5. push down
		 		leafTuples.set(Epos1, non_leafTuples.get(Fpos1));
		 		non_leafTuples.get(Fpos1).setOccupied(false);
		 		leafTuples.set(Epos2, non_leafTuples.get(Fpos2));
		 		non_leafTuples.get(Fpos2).setOccupied(false);
				
				// 6. re-encrypt all the buckets
				
				// 7. put them back to the tree
		 		// TODO: right now the only access provided is updatePathToLeaf()
		 		List<Tuple> allTuples = t.getPathToLeaf(l);
		 		for (int k=0; k<non_leafTuples.size(); k++) {
		 			allTuples.set(k+(buckets.size()-2)*bucketDepth, non_leafTuples.get(k));
		 		}
		 		for (int k=0; k<leafTuples.size(); k++) {
		 			allTuples.set(k+(buckets.size()-1)*bucketDepth, leafTuples.get(k));
		 		}
		 		t.updatePathToLeaf(allTuples, l);
		 		System.out.println("\ntuples updated");
		 		
		 		// for testing, only do one iteration
		 		break;
			}
			
			catch (ForestException e)
			{
				e.printStackTrace();
			}
		}
	}

}
