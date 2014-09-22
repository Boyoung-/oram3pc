package sprout.ui;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sprout.crypto.PRG;
import sprout.util.Util;

public class IOT
{
	
	public static String[] executeIOT(String[] m, Integer[] i, String[] delta) throws NoSuchAlgorithmException {
		SecureRandom rnd = new SecureRandom();
		
		// parameters
    	int N = m.length;
    	int k = i.length;
    	int l = m[0].length();
    	
    	// pre-computation
    	List<Integer> pi = new ArrayList<Integer>();												// input
		for (int o=0; o<N; o++)
			pi.add(o);
		Collections.shuffle(pi); // random permutation
		List<Integer> pi_ivs = Util.getInversePermutation(pi); // inverse permutation
		
		byte[] s = rnd.generateSeed(16);
		PRG G = new PRG(N*l);
		String r_all = G.generateBitString(N*l, s);
		String[] r = new String[N];
		for (int o=0; o<N; o++)
			r[o] = r_all.substring(o*l, (o+1)*l);
    	
    	// protocol
		// step 1
		String[] a = new String[N];
		for (int o=0; o<N; o++)
			a[o] = Util.addZero(new BigInteger(m[pi.get(o)], 2).xor(new BigInteger(r[o], 2)).toString(2), l);
		
		// step 2
		Integer[] j = new Integer[k];
		String[] p = new String[k];
		for (int o=0; o<k; o++) {
			j[o] = pi_ivs.get(i[o]);
			p[o] = Util.addZero(new BigInteger(r[j[o]], 2).xor(new BigInteger(delta[o], 2)).toString(2), l);
		}
		
		// step 3
		String[] z = new String[k];
		for (int o=0; o<k; o++)
			z[o] = Util.addZero(new BigInteger(a[j[o]], 2).xor(new BigInteger(p[o], 2)).toString(2), l);
    	
    	return z;
	}
		
	public static void main(String[] args) throws NoSuchAlgorithmException {
		String[] m = new String[]{"000", "001", "010", "011", "100", "101", "110", "111"};
		Integer[] i = new Integer[]{0, 1, 3, 7};
		String[] delta = new String[]{"000", "000", "000", "000", "000", "000", "000", "000"};
		Util.printArrH(executeIOT(m, i, delta));
	}

}
