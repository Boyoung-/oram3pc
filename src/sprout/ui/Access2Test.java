package sprout.ui;

import java.security.SecureRandom;
import java.math.BigInteger;

import sprout.util.Util;

public class Access2Test
{	
	static String executeAOT(int j, String[] f) {
		return f[j];
	}
	
	public static void main(String[] args) throws Exception {
		// TODO: tests for i=0 and i=h cases (hooking to ORAM forest)?
		SecureRandom rnd = new SecureRandom();
		
		// parameters
		int tau = 3; 						
		int l = (int) Math.pow(2, tau); 
		int h = 3;								// # of trees
		int i = 1; 								// current tree index
		int d_i = 1;							// # of tree level 
		int d_ip1 = 4;							// # of next tree level
		int w = 4;								// # tuples in one bucket
		int n = w * (d_i + 4);					// # tuples in one path
		int ln = i * tau;						// N bits
		int ll = d_i;							// L bits
		int ld = l * d_ip1;						// D/A bits 
		int tupleBitLength = 1 + ln + ll + ld;
		
		// Access-2 inputs
		String Nip1 = Util.addZero(new BigInteger(ln+tau, rnd).toString(2), ln+tau);														// input                          
		String Ni = Nip1.substring(0, ln);                         
		String Nip1_pr = Nip1.substring(ln);               
		String Li = Util.addZero(new BigInteger(ll, rnd).toString(2), ll); 																// input     
		String sigmaPath = Util.addZero(new BigInteger(tupleBitLength*n, rnd).toString(2), tupleBitLength*n);
		String T_i = "1" + Ni + Li + Util.addZero(new BigInteger(ld, rnd).toString(2), ld); // for testing correctness
		int test_j1 = rnd.nextInt(n);
		sigmaPath = sigmaPath.substring(0, test_j1*tupleBitLength) + T_i + sigmaPath.substring((test_j1+1)*tupleBitLength);
		String secretC_P = Util.addZero(new BigInteger(tupleBitLength*n, rnd).toString(2), tupleBitLength*n);							// input
		String secretE_P = Util.addZero(new BigInteger(sigmaPath, 2).xor(new BigInteger(secretC_P, 2)).toString(2), tupleBitLength*n);  	// input
		
		// start testing Access-2
		// step 1	
		String[] y = new String[l];
		String y_all = "";
		if (i == 0) { 
			int d_1 = 1; // suppose we know
			String A_1 = Util.addZero(new BigInteger(l*1, rnd).toString(2), l*1);
			for (int k=0; k<l; k++) {
				y[k] = A_1.substring(k*d_1, (k+1)*d_1);
			}
			y_all = A_1;
		}
		else {
			if (i < h)
				y_all = Util.addZero(new BigInteger(ld, rnd).toString(2), ld);
			else // i = h
				y_all = Util.addZero("", ld);
			for (int k=0; k<l; k++) {
				y[k] = y_all.substring(k*d_ip1, (k+1)*d_ip1);
			}
		}
		
		String secretE_Ti = "0" + Util.addZero("", i*tau) + Util.addZero ("", d_i) + y_all;
		String secretE_P_p = ""; //  i = 0 case
		if (i > 0) { 
			secretE_P_p = secretE_P;
		}
		
		// step 2
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
			return;
		}
		
		// step 3
		String fbar = Util.addZero("", ld); // i = 0 case
		if (i > 0) {
			String[] e = new String[n];
			String[] f = new String[n];
			for (int k=0; k<n; k++) {
				e[k] = secretE_P.substring(k*tupleBitLength+1+ln+ll, (k+1)*tupleBitLength);
				f[k] = Util.addZero(new BigInteger(e[k], 2).xor(new BigInteger(y_all, 2)).toString(2), ld);
			}
			fbar = executeAOT(j_1, f);
		}
		
		// step 4
		int j_2 = new BigInteger(Nip1_pr, 2).intValue();
		String ybar_j2 = "";  // i = h case
		if (i < h) {
			ybar_j2 = executeAOT(j_2, y);
		}
		
		// step 5
		String ybar = "";
		String zeros = Util.addZero("", d_ip1);
		for (int k=0; k<l; k++) {
			if (k == j_2 && i < h)
				ybar += ybar_j2;
			else // i = h case
				ybar += zeros;
		}
		String secretC_Aj1 = secretC_P.substring(j_1*tupleBitLength+1+ln+ll, (j_1+1)*tupleBitLength);
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
		String secretC_P_p = ""; // i = 0 case
		if (i > 0) {
			int flipBit = 1 - Integer.parseInt(secretC_P.substring(j_1*tupleBitLength, j_1*tupleBitLength+1));
			String newTuple = flipBit + Util.addZero(new BigInteger(tupleBitLength-1, rnd).toString(2), tupleBitLength-1);
			secretC_P_p = secretC_P.substring(0, j_1*tupleBitLength) + newTuple + secretC_P.substring((j_1+1)*tupleBitLength);
		}
		
		// outputs
		System.out.println(Lip1);
		System.out.println(secretC_Ti);
		System.out.println(secretE_Ti);
		System.out.println(secretC_P_p);
		System.out.println(secretE_P_p);
		System.out.println(d);				// i = h case
		
		// check correctness
		System.out.println("------------------------------------");
		System.out.println("j1: " + test_j1 + " =? " + j_1);
		for (int k=0; k<n; k++)
			if (b[k].equals(c[k]))
				System.out.println("  " + k + ":\tmatch");
		System.out.println("Ti: " + Util.addZero(new BigInteger(secretC_Ti, 2).xor(new BigInteger(secretE_Ti, 2)).toString(2), tupleBitLength).equals(T_i));
		System.out.println("Lip1: " + T_i.substring(1+ln+ll).substring(j_2*d_ip1, (j_2+1)*d_ip1).equals(Lip1));
		System.out.println("Original Ti:\t" + T_i);
		System.out.println("Ti in new path:\t" + Util.addZero(new BigInteger(secretC_P_p, 2).xor(new BigInteger(secretE_P_p, 2)).toString(2), n*tupleBitLength).substring(j_1*tupleBitLength, (j_1+1)*tupleBitLength));
	}

}
