package sprout.ui;

import sprout.crypto.PRG;
import sprout.oram.Forest;
import sprout.oram.ForestMetadata;
import sprout.oram.Tree;
import sprout.util.Util;

import java.math.BigInteger;
import java.security.SecureRandom;

public class EncryptPathTest
{
	static SecureRandom rnd = new SecureRandom();
	static BigInteger q = BigInteger.valueOf(953);  // small prime for testing
	static BigInteger g = BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());
	
	public static String execute(String secretC_P, String secretE_P, BigInteger k, Tree OT, ForestMetadata metadata) throws Exception {
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
		int n				= d_i + e;
		
		
		// protocol
		// step 1
		BigInteger y = g.modPow(k, q);
		byte[] s = rnd.generateSeed(16);  // 128 bits
		BigInteger[] r = new BigInteger[n];
		BigInteger[] x = new BigInteger[n];
		BigInteger[] v = new BigInteger[n];
		for (int j=0; j<n; j++) {
			r[j] = BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());	
			x[j] = g.modPow(r[j], q);
			v[j] = y.modPow(r[j], q);
		}
		PRG G1 = new PRG(l*(n));
		String a_all = G1.generateBitString(l*(n), s);
		String[] a = new String[n];
		String[] b = new String[n];
		String[] c = new String[n];
		for (int j=0; j<n; j++) {
			a[j] = a_all.substring(j*l, (j+1)*l);
			PRG G2 = new PRG(l); // non-fresh SecureRandom cannot guarantee determinism... (why???)
			b[j] = G2.generateBitString(l, v[j]);
			c[j] = Util.addZero(new BigInteger(a[j], 2).xor(new BigInteger(b[j], 2)).toString(2), l);
		}
		
		// step 2
		String[] secretC_B = new String[n];
		String[] d = new String[n];
		for (int j=0; j<n; j++) {
			secretC_B[j] = secretC_P.substring(j*l, (j+1)*l);
			d[j] = Util.addZero(new BigInteger(c[j], 2).xor(new BigInteger(secretC_B[j], 2)).toString(2), l);
		}
		
		// step 3
		// generating a[] is omitted in this test version
		String[] secretE_B = new String[n];
		String[] Bbar = new String[n];
		for (int j=0; j<n; j++) {
			secretE_B[j] = secretE_P.substring(j*l, (j+1)*l);
			Bbar[j] = Util.addZero(new BigInteger(secretE_B[j], 2).xor(new BigInteger(a[j], 2)).xor(new BigInteger(d[j], 2)).toString(2), l);
		}
		//String[] Bbar_pi = reversePermutation(Bbar, sigma);
		//BigInteger[] x_p_pi = reversePermutation(x_p, sigma);
		String Pbar = "";
		for (int j=0; j<n; j++) {
			Pbar += Util.addZero(x[j].toString(2), l) + Bbar[j]; 
		}
		
		// outputs
		return Pbar;
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
			
			String secretC_P 		= Util.addZero(new BigInteger(l*(n), rnd).toString(2), l*(n));
			String secretE_P		= Util.addZero(new BigInteger(l*(n), rnd).toString(2), l*(n));
			BigInteger k			= BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());	
			System.out.println(execute(secretC_P, secretE_P, k, OT, forest.getMetadata()));
		}
	}
}
