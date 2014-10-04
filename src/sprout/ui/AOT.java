package sprout.ui;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import sprout.crypto.WeakPRF;
import sprout.util.Util;

public class AOT
{
	static SecureRandom rnd = new SecureRandom();
	
	// AOT(S, C, D)
	public static String executeAOT(String[] m, int j) throws NoSuchAlgorithmException {		
		// parameters
		int N = m.length;
		int l = m[0].length();
    	
    	// pre-computed input
		// party S
    	BigInteger k = new BigInteger(128, rnd); // is this right???
    	// S sends k to D

    	// step 1
    	// party S
    	BigInteger alpha = BigInteger.valueOf(rnd.nextInt(N));
    	BigInteger[] m_p = new BigInteger[N];
    	for (int t=0; t<N; t++) {
        	WeakPRF f = new WeakPRF(k, l);
        	m_p[t] = new BigInteger(f.compute(l, BigInteger.valueOf(t).add(alpha).mod(BigInteger.valueOf(N))), 2).xor(new BigInteger(m[t], 2));
    	}
    	// S sends m_p and alpha to C
   
    	// step 2
    	//party C
    	BigInteger j_p = BigInteger.valueOf(j).add(alpha).mod(BigInteger.valueOf(N));
    	// C sends j_p to D
    	
    	// step 3
    	// party D
    	WeakPRF f = new WeakPRF(k, l);
    	BigInteger c = new BigInteger(f.compute(l, j_p), 2);
    	// D sends c to C
    	String output = Util.addZero(c.xor(m_p[j]).toString(2), l);
    	// C outputs output
    	
    	return output;
	}
		
	public static void main(String args[]) throws NoSuchAlgorithmException {
		// for testing
		String t = Util.addZero(new BigInteger(50, rnd).toString(2), 50);
		String[] m = new String[10];
		int j = rnd.nextInt(10);
		for (int i=0; i<10; i++)
			m[i] = t.substring(i*5, (i+1)*5);
		System.out.println(j);
		Util.printArrH(m);
		System.out.println(executeAOT(m, j));
	}
}
