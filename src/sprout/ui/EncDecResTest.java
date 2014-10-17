// This file is not well-organized at all
// It is used only to verify the correctness of the following three methods:
// EncryptPath; DecryptPath; Reshuffle

package sprout.ui;

import sprout.oram.Forest;
import sprout.oram.Tree;
//import sprout.ui.DecryptPathTest.DPOutput;
//import sprout.ui.EncryptPathTest.EPath;
import sprout.util.Util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

public class EncDecResTest
{
	static SecureRandom rnd = new SecureRandom();
	
	/* Commented to compile
	public static void main(String[] args) throws Exception {
		Forest forest = new Forest();
		//forest.buildFromFile("config/smallConfig.yaml", "config/smallData.txt", "db.bin");
		forest.loadFile("config/smallConfig.yaml", "db.bin");
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
			
			String secretC_P 		= Util.addZero(new BigInteger(l*(n), rnd).toString(2), l*(n));
			String secretE_P		= Util.addZero(new BigInteger(l*(n), rnd).toString(2), l*(n));
			BigInteger k			= Util.randomBigInteger(CryptoParam.q);
			
			
			// start testing Enc/Dec/Res
			EPath EncOutput = EncryptPathTest.execute(secretC_P, secretE_P, k, OT, forest.getMetadata());
			DPOutput DecOutput = DecryptPathTest.execute(null, k, forest.getInitialORAM(), OT, forest.getMetadata(), EncOutput);
			List<Integer> pi = Util.getInversePermutation(DecOutput.p);
			String[] ResOutput = ReshuffleTest.execute(DecOutput.secretC_P, DecOutput.secretE_P, pi, forest.getInitialORAM(), OT, forest.getMetadata());
			
			// check correctness
			String in = new BigInteger(secretC_P, 2).xor(new BigInteger(secretE_P, 2)).toString(2);
			String out = new BigInteger(ResOutput[0], 2).xor(new BigInteger(ResOutput[1], 2)).toString(2);
			System.out.println("Checking correctness: in equals out?: " + in.equals(out));
		} 
	}*/
}
