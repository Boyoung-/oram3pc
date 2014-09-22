package sprout.ui;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import sprout.util.Util;

public class SSOT
{
	
	public static String[][] executeSSOT(String[] sC, String[] sE, Integer[] i) throws NoSuchAlgorithmException {
		SecureRandom rnd = new SecureRandom();
		String[][] ret = new String[2][];
		
		// parameters
    	int k = i.length;
    	int l = sC[0].length();
    	
    	// protocol
		// step 1
    	String[] delta = new String[k];
    	for (int o=0; o<k; o++)
    		delta[o] = Util.addZero(new BigInteger(l, rnd).toString(2), l);
    	
    	// step 2
    	String[] a = IOT.executeIOT(sE, i, delta);
    	ret[1] = a;
    	
    	// step 3
    	String[] b = IOT.executeIOT(sC, i, delta);
    	ret[0] = b;
    	
    	return ret;
	}
		
	public static void main(String[] args) throws NoSuchAlgorithmException {
		String[] sC = new String[]{"000", "001", "010", "011", "100", "101", "110", "111"};
		String[] sE = new String[]{"000", "000", "000", "000", "000", "000", "000", "000"};
		Integer[] i = new Integer[]{0, 1, 3, 6};
		String[][] out = executeSSOT(sC, sE, i);
		for (int k=0; k<out[0].length; k++)
			System.out.print(new BigInteger(out[0][k], 2).xor(new BigInteger(out[1][k], 2)) + " ");
		System.out.println();
	}

}
