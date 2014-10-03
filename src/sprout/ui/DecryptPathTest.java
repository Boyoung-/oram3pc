package sprout.ui;

import sprout.crypto.PRG;
import sprout.oram.Forest;
import sprout.oram.Forest.TreeZero;
import sprout.oram.ForestMetadata;
import sprout.oram.Tree;
import sprout.ui.EncryptPathTest.EPath;
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
	
	// testing now
	// when Pbar can be retrieved using Li, it should be removed from the args
	public static DPOutput execute(String Li, BigInteger k, TreeZero OT_0, Tree OT, ForestMetadata metadata, EPath Pbar) throws Exception {
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
		
		// i = 0 case
		// TODO: get encrypt data done in forest, then modify the code here
		List<Integer> sigma	= new ArrayList<Integer>();		
		if (i == 0) {
			String secretC_P = Util.addZero(OT_0.nonce.toString(2), ld);
			String secretE_P = Util.byteArraytoKaryString(OT_0.initialEntry, 2, ld);
			sigma.add(0);
			return new DPOutput(secretC_P, secretE_P, sigma);
		}
		
		// protocol
		// step 1
		// C sends label Li to E
		
		// step 2
		// retrieve encrypted path Pbar using Li
		// TODO: encrypt data when building the forest, so we can retrieve path using Li
		
		// step 3								
		for (int j=0; j<d_i+e; j++)
			sigma.add(j);
		Collections.shuffle(sigma); // random permutation
		BigInteger[] x = Pbar.x.clone();
		String[] Bbar = Pbar.Bbar.clone();
		BigInteger[] sigma_x = Util.permute(x, sigma);
		String[] secretE_P_arr = Util.permute(Bbar, sigma);
		String secretE_P = "";
		for (int j=0; j<d_i+e; j++)
			secretE_P += secretE_P_arr[j];
		
		// step 4
		// TODO: add OPRF
		// PRG is used here instead of OPRF for testing purpose
		String secretC_P = "";
		for (int j=0; j<d_i+e; j++) {
			PRG G = new PRG(l); // non-fresh SecureRandom cannot guarantee determinism... (why???)
			secretC_P += G.generateBitString(l, sigma_x[j].modPow(k, CryptoParam.p));
		}
		
		// outputs	
		return new DPOutput(secretC_P, secretE_P, sigma);
	}

	public static void main(String[] args) throws Exception {	
		Forest forest = new Forest();
		forest.buildFromFile("config/smallConfig.yaml", "config/smallData.txt", "db.bin");
		System.out.println("Forest loaded.\n");
		
		int tau 			= forest.getMetadata().getTauExponent();
		int twotaupow 		= forest.getMetadata().getTau();
		int h				= forest.getMetadata().getLevels();
		int w 				= forest.getMetadata().getBucketDepth();
		int e 				= forest.getMetadata().getLeafExpansion();
		
		// test i = 0;
		BigInteger k = BigInteger.valueOf(Math.abs(rnd.nextLong()) % CryptoParam.q.longValue());
		DPOutput out = execute("", k, forest.getInitialORAM(), null, forest.getMetadata(), null);
		System.out.println(out.secretC_P);
		System.out.println(out.secretE_P);
		Util.printListH(out.p);
		
		// i > 0 cases
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
			
			String Li = Util.addZero(new BigInteger(ll, rnd).toString(2), ll);
			k = BigInteger.valueOf(Math.abs(rnd.nextLong()) % CryptoParam.q.longValue());
			EPath Pbar = new EPath(n, l);
			out = execute(Li, k, null, OT, forest.getMetadata(), Pbar);
			System.out.println(out.secretC_P);
			System.out.println(out.secretE_P);
			Util.printListH(out.p);
		}
	}
}
