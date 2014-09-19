package sprout.ui;

import sprout.crypto.PRG;
import sprout.util.Util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReshuffleTest
{
	public static void main(String[] args) throws Exception {
		SecureRandom rnd = new SecureRandom();
		
		// parameters
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
		
		// inputs
		String secretC_P = Util.addZero(new BigInteger(l*n, rnd).toString(2), l*n);
		String secretE_P = Util.addZero(new BigInteger(l*n, rnd).toString(2), l*n);
		List<Integer> pi	= new ArrayList<Integer>();												
		for (int j=0; j<d_i+4; j++)
			pi.add(j);
		if (i > 0)
			Collections.shuffle(pi); // random permutation
		
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
		String[] secretC_pi_P_arr = Util.reversePermutation(a, pi);
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
		String[] secretE_pi_P_arr = Util.reversePermutation(b, pi);
		String secretE_pi_P = "";
		for (int j=0; j<n; j++)
			secretE_pi_P += secretE_pi_P_arr[j];
		
		// outputs
		System.out.println(secretE_pi_P);
		System.out.println(secretC_pi_P);
	}

}
