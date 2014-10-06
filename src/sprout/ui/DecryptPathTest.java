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
	
	// testing now using passed arg Pbar
	// when Pbar can be retrieved using Li, it should be removed from the args
	public static DPOutput execute(String Li, BigInteger k, TreeZero OT_0, Tree OT, ForestMetadata metadata, EPath Pbar) throws Exception {
		// parameters
		int tau 			= metadata.getTauExponent();		// tau in the writeup
		int twotaupow 		= metadata.getTau();            	// 2^tau
		int h				= metadata.getLevels();         	// # trees
		int w 				= metadata.getBucketDepth();		// # tuples in each bucket
		int e 				= metadata.getLeafExpansion();		// # buckets in each leaf
		int treeLevel;
		if (OT != null)											// we are dealing with the initial tree
			treeLevel		= OT.getTreeLevel();				
		else
			treeLevel		= h;								// tree index in ORAM forest
		int i 				= h - treeLevel;					// tree index in the writeup
		int d_i				= 0;								// # levels in this tree (excluding the root level)
		if (i > 0)
			d_i				= OT.getNumLevels();
		int d_ip1;												// # levels in the next tree (excluding the root level)
		if (i == h)
			d_ip1			= OT.getDBytes() * 8 / twotaupow;
		else
			d_ip1			= metadata.getTupleBitsL(treeLevel-1);
		int ln 				= i * tau;							// # N bits
		int ll 				= d_i;								// # L bits
		int ld 				= twotaupow * d_ip1;				// # data bits
		int tupleBitLength 	= 1 + ln + ll + ld;					// # tuple size (bits)
		int l				= tupleBitLength * w;   		    // # bucket size (bits)

		// i = 0 case
		// TODO: encrypt data when generating the forest, then modify the code here
		// right now the nonce are all 0s
		List<Integer> sigma	= new ArrayList<Integer>();		
		if (i == 0) {
			String secretC_P = Util.addZero(OT_0.nonce.toString(2), ld);
			String secretE_P = Util.byteArraytoKaryString(OT_0.initialEntry, 2, ld);
			sigma.add(0);
			return new DPOutput(secretC_P, secretE_P, sigma);
		}
		
		// protocol
		// step 1
		// party C
		// C sends Li to E
		
		// step 2
		// party E
		// E retrieves encrypted path Pbar using Li
		// TODO: encrypt data when building the forest, so we can retrieve path using Li
		
		// step 3		
		// party E
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
		// E outputs sigma and secretE_P
		
		// step 4
		// party C and D run OPRF on C's input sigma_x and D's input k
		// TODO: add OPRF
		// PRG is used here instead of OPRF for testing purpose
		String secretC_P = "";
		for (int j=0; j<d_i+e; j++) {
			PRG G = new PRG(l); // non-fresh generated SecureRandom cannot guarantee determinism... (why???)
			secretC_P += G.generateBitString(l, sigma_x[j].modPow(k, CryptoParam.p));
		}
		// C outputs secretC_P
		
		// outputs	
		return new DPOutput(secretC_P, secretE_P, sigma);
	}

	public static void main(String[] args) throws Exception {
		// for testing
		Forest forest = new Forest();
		//forest.buildFromFile("config/smallConfig.yaml", "config/smallData.txt", "db.bin");
		forest.loadFile("config/smallConfig.yaml", "db.bin");
		System.out.println("Forest loaded.\n");
		
		int tau 			= forest.getMetadata().getTauExponent();
		int twotaupow 		= forest.getMetadata().getTau();
		int h				= forest.getMetadata().getLevels();
		int w 				= forest.getMetadata().getBucketDepth();
		int e 				= forest.getMetadata().getLeafExpansion();
		
		// test i = 0;
		BigInteger k = Util.randomBigInteger(CryptoParam.q);
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
			int l				= tupleBitLength * w;  
			int n				= d_i + e;
			
			String Li = Util.addZero(new BigInteger(ll, rnd).toString(2), ll);
			k = Util.randomBigInteger(CryptoParam.q);
			EPath Pbar = new EPath(n, l);
			out = execute(Li, k, null, OT, forest.getMetadata(), Pbar);
			System.out.println(out.secretC_P);
			System.out.println(out.secretE_P);
			Util.printListH(out.p);
		}
	}
}
