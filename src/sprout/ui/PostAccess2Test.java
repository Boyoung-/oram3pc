package sprout.ui;

import sprout.crypto.PRG;
import sprout.oram.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostAccess2Test
{
	static String addZero(String s, int l) {
		for (int i=s.length(); i<l; i++)
			s = "0" + s;
		return s;
	}
	
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
		
		Forest forest = null;
		try
		{
			forest = new Forest();
			forest.buildFromFile("config/smallConfig.yaml", "config/smallData.txt", "db.bin");
		}
		catch (NumberFormatException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ForestException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
		SecureRandom rnd = new SecureRandom();
		BigInteger q = BigInteger.valueOf(953);  // prime
		BigInteger g = BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());
		
		// PostAccess-2 inputs
		String secretC_P_p 		= "";
		List<Integer> sigma		= new ArrayList<Integer>();
		String secretE_P_p		= "";
		BigInteger k			= BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());
		
		// parameter
		int i 					= 0;
		Tree t					= forest.getTree(i);
		int di		 			= t.getNumLevels();
		int l 					= t.getBucketSize();
		
		// fake the permutation
		for (int j=0; j<di+4; j++)
			sigma.add(j);
		Collections.shuffle(sigma);
		
		// protocol
		// step 1
		BigInteger y = g.modPow(k, q);
		byte[] s = rnd.generateSeed(16);  // 128 bits
		BigInteger[] r = new BigInteger[di+4];
		BigInteger[] x_p = new BigInteger[di+4];
		BigInteger[] v = new BigInteger[di+4];
		for (int j=0; j<di+4; j++) {
			r[j] = BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());	
			x_p[j] = g.modPow(r[j], q);
			v[j] = y.modPow(r[j], q);
		}
		PRG G1 = new PRG(l*(di+4));
		PRG G2 = new PRG(l);
		String a_all = G1.generateBitString(l*(di+4), s);
		String[] a = new String[di+4];
		String[] b = new String[di+4];
		String[] c = new String[di+4];
		for (int j=0; j<di+4; j++) {
			a[j] = a_all.substring(j*l, (j+1)*l);
			b[j] = G2.generateBitString(l, v[j]);
			c[j] = new BigInteger(a[j], 2).xor(new BigInteger(b[j], 2)).toString(2);
		}
		
		// step 2
		String[] secretC_B = new String[di+4];
		String[] d = new String[di+4];
		for (int j=0; j<di+4; j++) {
			secretC_B[j] = secretC_P_p.substring(j*l, (j+1)*l);
			d[j] = new BigInteger(c[j], 2).xor(new BigInteger(secretC_B[j], 2)).toString(2);
		}
		
		// step 3
		String[] secretE_B = new String[di+4];
		String[] Bbar = new String[di+4];
		for (int j=0; j<di+4; j++) {
			secretE_B[j] = secretE_P_p.substring(j*l, (j+1)*l);
			Bbar[j] = new BigInteger(secretE_B[j], 2).xor(new BigInteger(a[j], 2)).xor(new BigInteger(d[j], 2)).toString(2);
		}
		String[] Bbar_pi = reversePermutation(Bbar, sigma);
		BigInteger[] x_p_pi = reversePermutation(x_p, sigma);
		String Pbar_p = "";
		for (int j=0; j<di+4; j++) {
			Pbar_p += addZero(x_p_pi[j].toString(2), l) + Bbar_pi[j]; 
		}
		
		// outputs
		System.out.println(Pbar_p);
	}

}
