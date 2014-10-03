package sprout.ui;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.math.BigInteger;

import sprout.oram.Forest;
import sprout.oram.Forest.TreeZero;
import sprout.oram.ForestMetadata;
import sprout.oram.Tree;
import sprout.ui.DecryptPathTest.DPOutput;
import sprout.ui.EncryptPathTest.EPath;
import sprout.util.Util;

public class AccessTest
{	
	static SecureRandom rnd = new SecureRandom();
	
	public static class AOutput {
		String Lip1;
		List<Integer> p;
		String secretC_Ti;
		String secretE_Ti;
		String secretC_P_p;
		String secretE_P_p;
		String data;
		
		AOutput() {}
		
		AOutput(String l, List<Integer> per, String ct, String et, String cp, String ep, String d) {
			Lip1 = l;
			p = new ArrayList<Integer>(per);
			secretC_Ti = ct;
			secretE_Ti = et;
			secretC_P_p = cp;
			secretE_P_p = ep;
			data = d;
		}
	}
	
	public static AOutput execute(String Li, String Nip1, BigInteger k, TreeZero OT_0, Tree OT, ForestMetadata metadata) throws Exception {
		// parameters
		int tau 			= metadata.getTauExponent();
		int twotaupow 		= metadata.getTau();
		int h				= metadata.getLevels();
		int w 				= metadata.getBucketDepth();
		int expen			= metadata.getLeafExpansion();
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
		int n				= w * (d_i + expen);     // # tuples in one path
		if (i == 0) {
			tupleBitLength 	= ld;
			l 				= ld;
			n 				= 1;
		}
		
		
		// prepare										             
		String Ni = Nip1.substring(0, ln);                         
		String Nip1_pr = Nip1.substring(ln);  
		
		// protocol
		// step 1
		DPOutput DecOut;
		if (i == 0)
			DecOut = DecryptPathTest.execute("", k, OT_0, null, metadata, null);
		else {
			EPath Pbar = new EPath(d_i+expen, l);
			DecOut = DecryptPathTest.execute(Li, k, OT_0, OT, metadata, Pbar);
		}
		String secretC_P = DecOut.secretC_P;
		String secretE_P = DecOut.secretE_P;
		
		// below are for checking correctness
		System.out.println("-----checking correctness-----");
		String sigmaPath = Util.addZero(new BigInteger(tupleBitLength*n, rnd).toString(2), tupleBitLength*n);
		String T_i = "1" + Ni + Li + Util.addZero(new BigInteger(ld, rnd).toString(2), ld);
		if (i == 0)
			T_i = Util.addZero(new BigInteger(ld, rnd).toString(2), ld);
		int test_j1 = rnd.nextInt(n);
		sigmaPath = sigmaPath.substring(0, test_j1*tupleBitLength) + T_i + sigmaPath.substring((test_j1+1)*tupleBitLength);
		if (i == 0)
			sigmaPath = T_i;
		secretC_P = Util.addZero(new BigInteger(tupleBitLength*n, rnd).toString(2), tupleBitLength*n);								
		secretE_P = Util.addZero(new BigInteger(sigmaPath, 2).xor(new BigInteger(secretC_P, 2)).toString(2), tupleBitLength*n);
		
		// step 2
		String[] y = new String[twotaupow];
		String y_all;
		if (i == 0) 
			y_all = secretE_P;
		else if (i < h)
			y_all = Util.addZero(new BigInteger(ld, rnd).toString(2), ld);
		else // i = h
			y_all = Util.addZero("", ld);
		for (int o=0; o<twotaupow; o++) {
			y[o] = y_all.substring(o*d_ip1, (o+1)*d_ip1);
		}
		
		String secretE_Ti = "0" + Util.addZero("", i*tau) + Util.addZero ("", d_i) + y_all;
		if (i == 0)
			secretE_Ti = y_all;
		String secretE_P_p = ""; //  i = 0 case
		if (i > 0) { 
			secretE_P_p = secretE_P;
		}
		
		// step 3
		int j_1 = 0; // i = 0 case; as the j_1 = 1 in the write up
		String[] a = new String[n];
		String[] b = new String[n];
		String[] c = new String[n];
		if (i > 0) {
			for (int j=0; j<n; j++) {
				a[j] = secretC_P.substring(j*tupleBitLength, j*tupleBitLength+1+ln);
				b[j] = secretE_P.substring(j*tupleBitLength, j*tupleBitLength+1+ln);
				c[j] = Util.addZero(new BigInteger(a[j], 2).xor(new BigInteger("1"+Ni, 2)).toString(2), 1+ln);
			}
			j_1 = PET.executePET(c, b);
		}
		if (j_1 < 0) {
			System.out.println("PET error!");
			return new AOutput();
		}
		
		// step 4
		String fbar = Util.addZero("", ld); // i = 0 case
		if (i > 0) {
			String[] e = new String[n];
			String[] f = new String[n];
			for (int o=0; o<n; o++) {
				e[o] = secretE_P.substring(o*tupleBitLength+1+ln+ll, (o+1)*tupleBitLength);
				f[o] = Util.addZero(new BigInteger(e[o], 2).xor(new BigInteger(y_all, 2)).toString(2), ld);
			}
			fbar = AOT.executeAOT(f, j_1);
		}
		
		// step 5
		int j_2 = new BigInteger(Nip1_pr, 2).intValue();
		String ybar_j2 = "";  // i = h case
		if (i < h) {
			ybar_j2 = AOT.executeAOT(y, j_2);
		}
		
		// step 6
		String ybar = "";
		String zeros = Util.addZero("", d_ip1);
		for (int o=0; o<twotaupow; o++) {
			if (o == j_2 && i < h)
				ybar += ybar_j2;
			else // i = h case
				ybar += zeros;
		}
		String secretC_Aj1;
		if (i == 0)
			secretC_Aj1 = secretC_P;
		else
			secretC_Aj1 = secretC_P.substring(j_1*tupleBitLength+1+ln+ll, (j_1+1)*tupleBitLength);
		String Abar = Util.addZero(new BigInteger(secretC_Aj1, 2).xor(new BigInteger(fbar, 2)).xor(new BigInteger(ybar, 2)).toString(2), ld);
		String d = "";
		String Lip1 = ""; // i = h case
		if (i < h) {
			Lip1 = Abar.substring(j_2*d_ip1, (j_2+1)*d_ip1);
		}
		else {
			d = Abar;
		}
		String secretC_Ti = "1" + Ni + Li + Util.addZero(new BigInteger(secretC_Aj1, 2).xor(new BigInteger(fbar, 2)).toString(2), ld);
		if (i == 0)
			secretC_Ti = Util.addZero(new BigInteger(secretC_Aj1, 2).xor(new BigInteger(fbar, 2)).toString(2), ld);
		String secretC_P_p = ""; // i = 0 case
		if (i > 0) {
			int flipBit = 1 - Integer.parseInt(secretC_P.substring(j_1*tupleBitLength, j_1*tupleBitLength+1));
			String newTuple = flipBit + Util.addZero(new BigInteger(tupleBitLength-1, rnd).toString(2), tupleBitLength-1);
			secretC_P_p = secretC_P.substring(0, j_1*tupleBitLength) + newTuple + secretC_P.substring((j_1+1)*tupleBitLength);
		}
		
		// for checking correctness
		System.out.println("j1: " + test_j1 + " =? " + j_1);
		if (i > 0) {
			for (int o=0; o<n; o++)
				if (b[o].equals(c[o]))
					System.out.println("  " + o + ":\tmatch");
		}
		System.out.println("Ti: " + Util.addZero(new BigInteger(secretC_Ti, 2).xor(new BigInteger(secretE_Ti, 2)).toString(2), tupleBitLength).equals(T_i));
		if (i == 0)
			System.out.println("Lip1: " + T_i.substring(j_2*d_ip1, (j_2+1)*d_ip1).equals(Lip1));
		else if (i < h)
			System.out.println("Lip1: " + T_i.substring(1+ln+ll).substring(j_2*d_ip1, (j_2+1)*d_ip1).equals(Lip1));
		System.out.println("------------------------------");
		
		// outputs
		return new AOutput(Lip1, DecOut.p, secretC_Ti, secretE_Ti, secretC_P_p, secretE_P_p, d);
	}

	public static void main(String[] args) throws Exception{      
		Forest forest = new Forest();
		forest.buildFromFile("config/smallConfig.yaml", "config/smallData.txt", "db.bin");
		System.out.println("Forest loaded.\n");
		
		int tau 			= forest.getMetadata().getTauExponent();
		int twotaupow 		= forest.getMetadata().getTau();
		int h				= forest.getMetadata().getLevels();
		int w 				= forest.getMetadata().getBucketDepth();
		int e 				= forest.getMetadata().getLeafExpansion();
		
		// i = 0 case
		String Nip1 = Util.addZero(new BigInteger(tau, rnd).toString(2), tau);	
		BigInteger k = BigInteger.valueOf(Math.abs(rnd.nextLong()) % CryptoParam.q.longValue());  
		AOutput AOut = execute("", Nip1, k, forest.getInitialORAM(), null, forest.getMetadata());
		
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
			int n				= w * (d_i + e);         // # tuples in one path 
		
			String Li = Util.addZero(new BigInteger(ll, rnd).toString(2), ll); 		
			Nip1 = Util.addZero(new BigInteger(ln+tau, rnd).toString(2), ln+tau);	
			k = BigInteger.valueOf(Math.abs(rnd.nextLong()) % CryptoParam.q.longValue());  	
			AOut = execute(Li, Nip1, k, forest.getInitialORAM(), OT, forest.getMetadata());
		}
	}
}
