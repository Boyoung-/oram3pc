package sprout.ui;

import sprout.crypto.PRG;
import sprout.oram.*;
import sprout.util.Util;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EncryptPathTest
{
	static String[] reversePermutation(String[] s, List<Integer> p) {
		String[] s_new = new String[s.length];
		for (int i=0; i<s.length; i++) {
			s_new[p.get(i)] = s[i];
		}
		return s_new;
	}
	
	static BigInteger[] reversePermutation(BigInteger[] s, List<Integer> p) {
		BigInteger[] s_new = new BigInteger[s.length];
		for (int i=0; i<s.length; i++) {
			s_new[p.get(i)] = s[i];
		}
		return s_new;
	}
	
	public static void main(String[] args) throws Exception {
		SecureRandom rnd = new SecureRandom();
		
		// parameters
		BigInteger q 		= BigInteger.valueOf(953);  // prime: too small
		BigInteger g 		= BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());
		int h				= 3;
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
		int l				= tupleBitLength * w;  // bucket size
		
		// EncryptPath inputs
		String secretC_P_p 		= Util.addZero(new BigInteger(l*(d_i+4), rnd).toString(2), l*(d_i+4));	// input
		String secretE_P_p		= Util.addZero(new BigInteger(l*(d_i+4), rnd).toString(2), l*(d_i+4));	// input
		BigInteger k			= BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());			// input
		List<Integer> sigma		= new ArrayList<Integer>();												// input
		for (int j=0; j<d_i+4; j++)
			sigma.add(j);
		if (i > 0)
			Collections.shuffle(sigma);
		
		// protocol
		// step 1
		BigInteger y = g.modPow(k, q);
		byte[] s = rnd.generateSeed(16);  // 128 bits
		BigInteger[] r = new BigInteger[d_i+4];
		BigInteger[] x_p = new BigInteger[d_i+4];
		BigInteger[] v = new BigInteger[d_i+4];
		for (int j=0; j<d_i+4; j++) {
			r[j] = BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());	
			x_p[j] = g.modPow(r[j], q);
			v[j] = y.modPow(r[j], q);
		}
		PRG G1 = new PRG(l*(d_i+4));
		PRG G2 = new PRG(l);
		String a_all = G1.generateBitString(l*(d_i+4), s);
		String[] a = new String[d_i+4];
		String[] b = new String[d_i+4];
		String[] c = new String[d_i+4];
		for (int j=0; j<d_i+4; j++) {
			a[j] = a_all.substring(j*l, (j+1)*l);
			b[j] = G2.generateBitString(l, v[j]);
			c[j] = Util.addZero(new BigInteger(a[j], 2).xor(new BigInteger(b[j], 2)).toString(2), l);
		}
		
		// step 2
		String[] secretC_B = new String[d_i+4];
		String[] d = new String[d_i+4];
		for (int j=0; j<d_i+4; j++) {
			secretC_B[j] = secretC_P_p.substring(j*l, (j+1)*l);
			d[j] = Util.addZero(new BigInteger(c[j], 2).xor(new BigInteger(secretC_B[j], 2)).toString(2), l);
		}
		
		// step 3
		// generating a[] is omitted in this test version
		String[] secretE_B = new String[d_i+4];
		String[] Bbar = new String[d_i+4];
		for (int j=0; j<d_i+4; j++) {
			secretE_B[j] = secretE_P_p.substring(j*l, (j+1)*l);
			Bbar[j] = Util.addZero(new BigInteger(secretE_B[j], 2).xor(new BigInteger(a[j], 2)).xor(new BigInteger(d[j], 2)).toString(2), l);
		}
		String[] Bbar_pi = reversePermutation(Bbar, sigma);
		BigInteger[] x_p_pi = reversePermutation(x_p, sigma);
		String Pbar_p = "";
		for (int j=0; j<d_i+4; j++) {
			Pbar_p += Util.addZero(x_p_pi[j].toString(2), l) + Bbar_pi[j]; 
		}
		
		// outputs
		System.out.println(Pbar_p);
	}

}
