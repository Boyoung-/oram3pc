package sprout.ui;

import sprout.crypto.PRG;
import sprout.oram.Forest;
import sprout.oram.Forest.TreeZero;
import sprout.oram.ForestMetadata;
import sprout.oram.Tree;
import sprout.util.Util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReshuffleTest
{
	static SecureRandom rnd = new SecureRandom();
	
	public static String[] execute(String secretC_P, String secretE_P, List<Integer> pi, TreeZero OT_0, Tree OT, ForestMetadata metadata) throws Exception {
		// parameters
		int tau 			= metadata.getTauExponent();
		int twotaupow 		= metadata.getTau();
		int h				= metadata.getLevels();
		int w 				= metadata.getBucketDepth();
		int e 				= metadata.getLeafExpansion();
		int treeLevel 		= -1;
		if (OT != null)
			treeLevel		= OT.getTreeLevel();
		else
			treeLevel		= h;
		int i 				= h - treeLevel;
		int d_i				= 0;
		if (i > 0)
			d_i				= OT.getNumLevels();
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
		int n				= d_i + e;
		
		// i = 0 case: no shuffle needed
		String[] output = new String[2];
		if (i == 0) {
			output[0] = secretC_P;
			output[1] = secretE_P;
			return output;
		}
		
		// protocol
		// step 1
		byte[] s1 = rnd.generateSeed(16);
		PRG G = new PRG(n*l);
		String p1 = G.generateBitString(n*l, s1);
		String z = Util.addZero(new BigInteger(secretC_P, 2).xor(new BigInteger(p1, 2)).toString(2), l);
		
		// step 2
		byte[] s2 = rnd.generateSeed(16);
		String p2 = G.generateBitString(n*l, s2);
		String a_all = Util.addZero(new BigInteger(p1, 2).xor(new BigInteger(p2, 2)).toString(2), n*l);
		String[] a = new String[n];
		for (int j=0; j<n; j++)
			a[j] = a_all.substring(j*l, (j+1)*l);
		String[] secretC_pi_P_arr = Util.permute(a, pi);
		String secretC_pi_P = "";
		for (int j=0; j<n; j++)
			secretC_pi_P += secretC_pi_P_arr[j];
		
		// step 3
		// C outputs secretC_pi_P
		
		// step 4
		String b_all = Util.addZero(new BigInteger(secretE_P, 2).xor(new BigInteger(z, 2)).xor(new BigInteger(p2, 2)).toString(2), n*l);
		String[] b = new String[n];
		for (int j=0; j<n; j++)
			b[j] = b_all.substring(j*l, (j+1)*l);
		String[] secretE_pi_P_arr = Util.permute(b, pi);
		String secretE_pi_P = "";
		for (int j=0; j<n; j++)
			secretE_pi_P += secretE_pi_P_arr[j];
		
		// outputs
		output[0] = secretC_pi_P;
		output[1] = secretE_pi_P;
		return output;
	}

	public static void main(String args[]) throws Exception {
		Forest forest = new Forest();
		forest.buildFromFile("config/smallConfig.yaml", "config/smallData.txt", "db.bin");
		System.out.println("Forest loaded.\n");
		
		int tau 			= forest.getMetadata().getTauExponent();
		int twotaupow 		= forest.getMetadata().getTau();
		int h				= forest.getMetadata().getLevels();
		int w 				= forest.getMetadata().getBucketDepth();
		int e 				= forest.getMetadata().getLeafExpansion();
		
		for (int treeLevel = forest.getNumberOfTrees()-1; treeLevel >= 0; treeLevel--) {
			Tree OT = forest.getTree(treeLevel);
			int i 				= h - treeLevel;
			int d_i				= OT.getNumLevels();
			int d_ip1 			= -1;
			if (i == h)
				d_ip1			= OT.getDBytes() * 8 / twotaupow;
			else
				d_ip1			= forest.getMetadata().getTupleBitsL(treeLevel-1);
			int ln 				= i * tau;					
			int ll 				= d_i;						
			int ld 				= twotaupow * d_ip1;					
			int tupleBitLength 	= 1 + ln + ll + ld;
			int l				= tupleBitLength * w;    // bucket size (bits)
			int n				= d_i + e;
			
			String secretC_P = Util.addZero(new BigInteger(l*n, rnd).toString(2), l*n);
			String secretE_P = Util.addZero(new BigInteger(l*n, rnd).toString(2), l*n);
			List<Integer> pi	= new ArrayList<Integer>();												
			for (int j=0; j<d_i+4; j++)
				pi.add(j);
			Collections.shuffle(pi); // random permutation
			System.out.println("i:" + i);
			Util.printArrV(execute(secretC_P, secretE_P, pi, forest.getInitialORAM(), OT, forest.getMetadata()));
		}
	}
}
