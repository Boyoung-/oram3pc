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
	
	public static class EPath {
		BigInteger[] x;
		String[] Bbar;
		
		EPath(BigInteger[] xx, String[] bb) {
			x = xx.clone();
			Bbar = bb.clone();
		}
		
		// random generation for testing purpose
		EPath(int n, int l) {
			x = new BigInteger[n];
			Bbar = new String[n];
			for (int i=0; i<n; i++) {
				x[i] = BigInteger.valueOf(Math.abs(rnd.nextLong()) % CryptoParam.q.longValue());
				Bbar[i] = Util.addZero(new BigInteger(l, rnd).toString(2), l);
			}
		}
	}
	
	public static EPath execute(String secretC_P, String secretE_P, BigInteger k, Tree OT, ForestMetadata metadata) throws Exception {
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
		if (i == 0) {
			tupleBitLength 	= ld;
			l 				= ld;
			n 				= 1;
		}
		
		
		// protocol
		// step 1
		BigInteger y = CryptoParam.g.modPow(k, CryptoParam.p);
		byte[] s = rnd.generateSeed(16);  // 128 bits
		BigInteger[] r = new BigInteger[n];
		BigInteger[] x = new BigInteger[n];
		BigInteger[] v = new BigInteger[n];
		for (int j=0; j<n; j++) {
			r[j] = BigInteger.valueOf(Math.abs(rnd.nextLong()) % CryptoParam.q.longValue());	
			x[j] = CryptoParam.g.modPow(r[j], CryptoParam.p);
			v[j] = y.modPow(r[j], CryptoParam.p);
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
		
		// outputs
		return new EPath(x, Bbar);
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
		
		// i = 0 case
		int ldata 				= twotaupow * forest.getMetadata().getTupleBitsL(h-1);
		BigInteger k			= BigInteger.valueOf(Math.abs(rnd.nextLong()) % CryptoParam.q.longValue());	
		String secretC_P		 = Util.addZero(new BigInteger(ldata, rnd).toString(2), ldata);
		String secretE_P		 = Util.addZero(new BigInteger(ldata, rnd).toString(2), ldata);
		EPath Pbar = execute(secretC_P, secretE_P, k, null, forest.getMetadata());
		System.out.println("i: " + 0);
		Util.printArrH(Pbar.x);
		Util.printArrH(Pbar.Bbar);
		
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
			
			secretC_P 			= Util.addZero(new BigInteger(l*(n), rnd).toString(2), l*(n));
			secretE_P			= Util.addZero(new BigInteger(l*(n), rnd).toString(2), l*(n));
			k					= BigInteger.valueOf(Math.abs(rnd.nextLong()) % CryptoParam.q.longValue());	
			Pbar = execute(secretC_P, secretE_P, k, OT, forest.getMetadata());
			System.out.println("i: " + i);
			Util.printArrH(Pbar.x);
			Util.printArrH(Pbar.Bbar);
		}
	}
}
