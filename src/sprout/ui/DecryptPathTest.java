package sprout.ui;

import sprout.crypto.PRG;
import sprout.oram.Forest;
import sprout.oram.ForestMetadata;
import sprout.oram.Tree;
import sprout.util.Util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DecryptPathTest
{
	public static class DPOutput {
		public String secretC_P;
		public String secretE_P;
		List<Integer> p;
		
		DPOutput(String c, String e, List<Integer> per) {
			secretC_P = c;
			secretE_P = e;
			p = new ArrayList<Integer>(per);
		}
	}
	
	static SecureRandom rnd = new SecureRandom();
	static BigInteger q = BigInteger.valueOf(953);  // small prime for testing
	
	public static DPOutput execute(String Li, BigInteger k, Tree OT, ForestMetadata metadata) throws Exception {
		// TODO: i = 0 case
		
		// parameters
		int tau 			= metadata.getTauExponent();
		int twotaupow 		= metadata.getTau();
		int h				= metadata.getLevels();
		int w 				= metadata.getBucketDepth();
		int e 				= metadata.getLeafExpansion();
		int treeLevel 		= OT.getTreeLevel();
		int i 				= h - treeLevel;
		int d_i				= OT.getNumLevels();
		int d_ip1 			= -1;
		if (i == h)
			d_ip1			= OT.getDBytes() * 8 / twotaupow;
		else
			d_ip1			= metadata.getTupleBitsL(treeLevel-1);
		int ln 				= i * tau;					
		int ll 				= d_i;						
		int ld 				= twotaupow * d_ip1;					
		int tupleBitLength 	= 1 + ln + ll + ld;
		int l				= tupleBitLength * w;    // bucket size (bits)
		
		
		// protocol
		// step 1
		// C sends label Li to E
		
		// step 2
		// TODO: encrypt data when building the forest, so we can retrieve path using Li
		//String Pbar	= Util.addZero(new BigInteger(l*(d_i+e)*2, rnd).toString(2), l*(d_i+e)*2);
		String Pbar = Li; // for testing correctness
		
		// step 3
		List<Integer> sigma	= new ArrayList<Integer>();												// input
		for (int j=0; j<d_i+e; j++)
			sigma.add(j);
		// TODO: fix the shuffled case
		//Collections.shuffle(sigma); // random permutation
		String[] x = new String[d_i+e];
		String[] Bbar = new String[d_i+e];
		for (int j=0; j<d_i+e; j++) {
			x[j] = Pbar.substring(j*2*l, j*2*l+l);
			Bbar[j] = Pbar.substring(j*2*l+l, (j+1)*2*l);
		}
		String[] sigma_x = Util.permute(x, sigma);
		String[] secretE_P_arr = Util.permute(Bbar, sigma);
		String secretE_P = "";
		for (int j=0; j<d_i+e; j++)
			secretE_P += secretE_P_arr[j];
		
		// step 4
		// PRG is used here instead of OPRF for testing purpose
		String secretC_P = "";
		for (int j=0; j<d_i+e; j++) {
			PRG G = new PRG(l); // non-fresh SecureRandom cannot guarantee determinism... (why???)
			secretC_P += G.generateBitString(l, new BigInteger(sigma_x[j], 2).modPow(k, q));
		}
		
		// outputs	
		return new DPOutput(secretC_P, secretE_P, sigma);
	}

	public static void main(String[] args) throws Exception {	
		Forest forest = new Forest();
		forest.buildFromFile("config/smallConfig.yaml", "config/smallData.txt", "db.bin");
		System.out.println("Forest loaded.\n");
		
		for (int i = forest.getNumberOfTrees()-1; i >= 0; i--) {
			Tree OT = forest.getTree(i);
			int ll = OT.getNumLevels();
			String Li = Util.addZero(new BigInteger(ll, rnd).toString(2), ll);
			BigInteger k = BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());
			DPOutput out = execute(Li, k, OT, forest.getMetadata());
			System.out.println(out.secretC_P);
			System.out.println(out.secretE_P);
			Util.printListH(out.p);
		}
	}
}
