// This file is not well-organized at all
// It is used only to verify the correctness of the following three methods:
// EncryptPath; DecryptPath; Reshuffle

package sprout.ui;

import sprout.crypto.PRG;
import sprout.util.Util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DecResEncTest
{
	public static void main(String[] args) throws Exception {
		SecureRandom rnd = new SecureRandom();
		
		// parameters
		BigInteger q 		= BigInteger.valueOf(953);  // prime: too small
		BigInteger g 		= BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());
		int i 				= 1;
		int d_i				= 1;
		int d_ip1 			= 4;
		int tau 			= 3;
		int twotaupow 		= (int) Math.pow(2, tau);
		int ln 				= i * tau;					
		int ll 				= d_i;						
		int ld 				= twotaupow * d_ip1;					
		int tupleBitLength 	= 1 + ln + ll + ld;
		int w 				= 4;
		int l				= tupleBitLength * w; 
		int n				= d_i + 4;

		///////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////       EncryptPath      ///////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////
		// inputs
		String secretC_P	= Util.addZero(new BigInteger(l*(d_i+4), rnd).toString(2), l*(d_i+4));
		String secretE_P	= Util.addZero(new BigInteger(l*(d_i+4), rnd).toString(2), l*(d_i+4));
		BigInteger k 		= BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());			
		
		String in	= Util.addZero(new BigInteger(secretC_P, 2).xor(new BigInteger(secretE_P, 2)).toString(2), n*l);
		String inC = secretC_P;
		String inE = secretE_P;
		
		// protocol
		// step 1
		BigInteger y = g.modPow(k, q);
		byte[] s = rnd.generateSeed(16);  // 128 bits
		BigInteger[] r = new BigInteger[d_i+4];
		BigInteger[] x = new BigInteger[d_i+4];
		BigInteger[] v = new BigInteger[d_i+4];
		for (int j=0; j<d_i+4; j++) {
			r[j] = BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());	
			x[j] = g.modPow(r[j], q);
			v[j] = y.modPow(r[j], q);
		}
		PRG G1 = new PRG(l*(d_i+4));
		
		String a_all = G1.generateBitString(l*(d_i+4), s);
		String[] a = new String[d_i+4];
		String[] b = new String[d_i+4];
		String[] c = new String[d_i+4];
		for (int j=0; j<d_i+4; j++) {
			PRG G2 = new PRG(l);
			a[j] = a_all.substring(j*l, (j+1)*l);
			b[j] = G2.generateBitString(l, v[j]);
			c[j] = Util.addZero(new BigInteger(a[j], 2).xor(new BigInteger(b[j], 2)).toString(2), l);
		}
		//printArr(b);
		//System.out.println();
		
		// step 2
		String[] secretC_B = new String[d_i+4];
		String[] d = new String[d_i+4];
		for (int j=0; j<d_i+4; j++) {
			secretC_B[j] = secretC_P.substring(j*l, (j+1)*l);
			d[j] = Util.addZero(new BigInteger(c[j], 2).xor(new BigInteger(secretC_B[j], 2)).toString(2), l);
		}
		
		// step 3
		// generating a[] is omitted in this test version
		String[] secretE_B = new String[d_i+4];
		String[] Bbar = new String[d_i+4];
		for (int j=0; j<d_i+4; j++) {
			secretE_B[j] = secretE_P.substring(j*l, (j+1)*l);
			Bbar[j] = Util.addZero(new BigInteger(secretE_B[j], 2).xor(new BigInteger(a[j], 2)).xor(new BigInteger(d[j], 2)).toString(2), l);
		}
		//String[] Bbar_pi = reversePermutation(Bbar, sigma);
		//BigInteger[] x_p_pi = reversePermutation(x_p, sigma);
		String Pbar = "";
		for (int j=0; j<d_i+4; j++) {
			Pbar += Util.addZero(x[j].toString(2), l) + Bbar[j]; 
		}		
		///////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////     END EncryptPath      /////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////
		
		///////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////       DecryptPath      ///////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////
		// protocol
		// step 1
		// C sends label Li to E
		
		// step 2
		// E retrieves the path Pbar in OT_i...
		
		// step 3
		List<Integer> sigma	= new ArrayList<Integer>();												// input
		for (int j=0; j<d_i+4; j++)
			sigma.add(j);
		if (i > 0);
			Collections.shuffle(sigma); // random permutation
		System.out.println("sigma: ");
		for (int j=0; j<d_i+4; j++)
			System.out.print(sigma.get(j) + " ");
		System.out.println();
		String[] xx = new String[d_i+4];
		//String[] Bbar = new String[d_i+4];
		for (int j=0; j<d_i+4; j++) {
			xx[j] = Pbar.substring(j*2*l, j*2*l+l);
			Bbar[j] = Pbar.substring(j*2*l+l, (j+1)*2*l);
		}
		
		
		String[] t = new String[n];
		String t_all = "";
		System.out.println("f xx: ");
		for (int j=0; j<n; j++) {
			PRG Gt = new PRG(l);
			String tmp = Gt.generateBitString(l, new BigInteger(xx[j], 2).modPow(k, q));
			t[j] = Util.addZero(new BigInteger(tmp, 2).xor(new BigInteger(Bbar[j], 2)).toString(2), l);
			t_all += t[j];
			System.out.println(tmp);
		}
		
		
		//printArr(Bbar);
		String[] sigma_x = Util.permute(xx, sigma);
		//System.out.println();
		//printArr(sigma_x);
		String[] secretE_P_arr = Util.permute(Bbar, sigma);
		//printArr(secretE_P_arr);
		secretE_P = "";
		for (int j=0; j<d_i+4; j++)
			secretE_P += secretE_P_arr[j];
		
		// step 4
		// PRG is used here instead of OPRF for testing purpose
		
		//PRG G4 = new PRG(l);
		secretC_P = "";
		System.out.println("f sigma_x: ");
		for (int j=0; j<d_i+4; j++) {
			PRG G = new PRG(l);
			//System.out.println(G4.generateBitString(l, new BigInteger(xx[j], 2).modPow(k, q)).equals(b[j]));
			String tmp = G.generateBitString(l, new BigInteger(sigma_x[j], 2).modPow(k, q));
			secretC_P += tmp;
			System.out.println(tmp);
		}
		
		System.out.println("xx: ");
		Util.printArrV(xx);
		System.out.println("sigma_x: ");
		Util.printArrV(sigma_x);
		System.out.println("Bbar: ");
		Util.printArrV(Bbar);
		System.out.println("secretE_P_arr: ");
		Util.printArrV(secretE_P_arr);
		
		String td = Util.addZero(new BigInteger(secretC_P, 2).xor(new BigInteger(secretE_P, 2)).toString(2), n*l);
		String[] tt = new String[n];
		for (int j=0; j<d_i+4; j++) {
			tt[j] = td.substring(j*l, (j+1)*l);
		}
		tt = Util.reversePermutation(tt, sigma);
		String tt_all = "";
		for (int j=0; j<d_i+4; j++) {
			tt_all += tt[j];
		}
		///////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////     END DecryptPath      /////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////
		
		String test_all = Util.addZero(new BigInteger(secretC_P, 2).xor(new BigInteger(secretE_P, 2)).toString(2), n*l);
		String[] test = new String[n];
		for (int j=0; j<n; j++) {
			test[j] = test_all.substring(j*l, (j+1)*l);
			//ttt += test[j];
		}
		String ttt_all = "";
		String[] ttt = Util.reversePermutation(test, sigma);
		for (int j=0; j<n; j++) {
			ttt_all += ttt[j];
		}

		///////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////       Reshuffle      /////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////
		//System.out.println("in shuffle: " + Util.addZero(new BigInteger(secretC_P, 2).xor(new BigInteger(secretE_P, 2)).toString(2), n*l));
		
		// protocol
		// step 1
		byte[] s1 = rnd.generateSeed(16);
		PRG G3 = new PRG(n*l);
		String p1 = G3.generateBitString(n*l, s1);
		String z = Util.addZero(new BigInteger(secretC_P, 2).xor(new BigInteger(p1, 2)).toString(2), l);
		
		// step 2
		byte[] s2 = rnd.generateSeed(16);
		String p2 = G3.generateBitString(n*l, s2);
		a_all = Util.addZero(new BigInteger(p1, 2).xor(new BigInteger(p2, 2)).toString(2), n*l);
		//String[] a = new String[n];
		for (int j=0; j<n; j++)
			a[j] = a_all.substring(j*l, (j+1)*l);
		//printArr(a);
		//System.out.println();
		String[] secretC_pi_P_arr = Util.reversePermutation(a, sigma);
		//printArr(secretC_pi_P_arr);
		String secretC_pi_P = "";
		for (int j=0; j<n; j++)
			secretC_pi_P += secretC_pi_P_arr[j];
		
		// step 3
		// C outputs secretC_pi_P
		
		// step 4
		String b_all = Util.addZero(new BigInteger(secretE_P, 2).xor(new BigInteger(z, 2)).xor(new BigInteger(p2, 2)).toString(2), n*l);
		//String[] b = new String[n];
		for (int j=0; j<n; j++)
			b[j] = b_all.substring(j*l, (j+1)*l);
		//printArr(b);
		//System.out.println();
		String[] secretE_pi_P_arr = Util.reversePermutation(b, sigma);
		//printArr(secretE_pi_P_arr);
		String secretE_pi_P = "";
		for (int j=0; j<n; j++)
			secretE_pi_P += secretE_pi_P_arr[j];			
		
		//System.out.println("in shuffle: " + Util.addZero(new BigInteger(a_all, 2).xor(new BigInteger(b_all, 2)).toString(2), n*l));
		String[] bb = Util.permute(secretE_pi_P_arr, sigma);
		String[] aa = Util.permute(secretC_pi_P_arr, sigma);
		//for (int j=0; j<n; j++) {
			//System.out.println(bb[j].equals(b[j]));
			//System.out.println(aa[j].equals(a[j]));
		//}
		///////////////////////////////////////////////////////////////////////////////////////////
		////////////////////////////////     END Reshuffle      ///////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////
		//for (int j=0; j<d_i+4; j++)
			//System.out.print(sigma.get(j) + " ");
		//System.out.println();
		
		// Check correctness
		String out	= Util.addZero(new BigInteger(secretC_pi_P, 2).xor(new BigInteger(secretE_pi_P, 2)).toString(2), n*l);
		System.out.println(in);
		System.out.println(t_all);
		System.out.println(tt_all);
		//System.out.println(out);
		//System.out.println(in.equals(out));
		System.out.println(in.equals(t_all));
		System.out.println(t_all.equals(tt_all));
		System.out.println(tt_all.equals(out));
		//System.out.println(inC);
		//System.out.println(secretC_pi_P);
		//System.out.println(inE);
		//System.out.println(secretE_pi_P);
		
		/*
		int nn = 100;
		List<Integer> pmt	= new ArrayList<Integer>();												// input
		for (int j=0; j<nn; j++)
			pmt.add(j);
		Collections.shuffle(pmt); // random permutation
		System.out.print("pmt: ");
		for (int j=0; j<nn; j++)
			System.out.print(pmt.get(j) + " ");
		System.out.println();
		
		String[] A = new String[nn];
		System.out.print("A: ");
		for (int j=0; j<nn; j++) {
			A[j] = new BigInteger(5, rnd).toString();
			System.out.print(A[j] + " ");
		}
		System.out.println();
		
		String[] B = Util.permute(A, pmt);
		B = Util.permute(B, pmt);
		//System.out.print("B: ");
		for (int j=0; j<nn; j++) {
			//System.out.print(B[j] + " ");
		}
		//System.out.println();
		
		String[] C = Util.reversePermutation(B, pmt);
		C = Util.reversePermutation(C, pmt);
		System.out.print("C: ");
		for (int j=0; j<nn; j++) {
			System.out.print(C[j] + " ");
		}
		System.out.println();
		*/
	}
}
