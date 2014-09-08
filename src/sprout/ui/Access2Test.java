// run this program first
// when seeing the prompt for needing the client
// start EvictionTestClient
package sprout.ui;

import java.security.SecureRandom;

import sprout.oram.*;

import java.io.IOException;
import java.math.BigInteger;

public class Access2Test
{
	static String addZero(String s, int l) {
		for (int i=s.length(); i<l; i++)
			s = "0" + s;
		return s;
	}
	
	static int executePET(String[] c, String[] b) {
		// fake function
		return 0;
	}
	
	static String executeAOT_n(int j, String[] f) {
		// fake function
		return "";
	}
	
	static String executeAOT_2tau(int j, String[] y) {
		// fake function
		return "";
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
				
		
		// Access-2 inputs (suppose we know)
		int i = 1; // current tree index
		Tree t = forest.getTree(i);
		String Ni = "";   // ????
		String Nip1 = ""; // ????
		String Li = "";   // ????
		int d_i = forest.getTree(i).getNumLevels();
		int n = t.getBucketDepth() * (d_i + 4);
		int ll = t.getLBytes() * 8;
		int ln = t.getNBytes() * 8;
		int ld = t.getDBytes() * 8;
		int tupleBitLength = 1 + ll + ln + ld;
		String secretC_P = addZero("", tupleBitLength);
		String secretE_P = addZero("", tupleBitLength);
		
		// start testing Access-2
		// step 1
		int tau = 3; // for testing
		int l = (int) Math.pow(2, tau); // overflow???
		int d_ip1 = forest.getTree(i+1).getNumLevels();
		SecureRandom rnd = new SecureRandom();
		String[] y = new String[l];
		String y_all = "";
		for (int k=0; k<l; k++) {
			y[k] = addZero(new BigInteger(d_ip1, rnd).toString(2), d_ip1);
			y_all += y[k];
		}
		
		String secretE_Ti = "0" + addZero("", i*tau) + addZero ("", d_i) + y_all;
		String secretE_Pprime = secretE_P; // should be equal to secretE_P
		
		// step 2
		String[] a = new String[n];
		String[] b = new String[n];
		String[] c = new String[n];
		for (int j=0; j<n; j++) {
			a[j] = secretC_P.substring(j*tupleBitLength, j*tupleBitLength+1) +  // fb
				   secretC_P.substring(j*tupleBitLength+1+ll, j*tupleBitLength+1+ll+ln); // N
			b[j] = secretE_P.substring(j*tupleBitLength, j*tupleBitLength+1) +  // fb
				   secretE_P.substring(j*tupleBitLength+1+ll, j*tupleBitLength+1+ll+ln); // N
			c[j] = new BigInteger(a[j], 2).xor(new BigInteger("1"+addZero("", ln), 2)).toString(2);
		}
		int j_1 = executePET(c, b);
		
		// step 3
		String[] e = new String[n];
		String[] f = new String[n];
		for (int k=0; k<n; k++) {
			e[k] = secretE_P.substring(k*tupleBitLength+1+ll+ln, (k+1)*tupleBitLength);
			f[k] = new BigInteger(e[k], 2).xor(new BigInteger(y_all, 2)).toString(2);
		}
		String fbar = executeAOT_n(j_1, f);
		
		// step 4
		int j_2 = 0;
		String ybar_j2 = executeAOT_2tau(j_2, y);
		
		// step 5
		String ybar = "";
		String zeros = addZero("", d_ip1);
		for (int k=0; k<l; k++) {
			if (k == j_2)
				ybar += ybar_j2;
			else
				ybar += zeros;
		}
		String secretC_Aj1 = secretC_P.substring(j_1*tupleBitLength, (j_1+1)*tupleBitLength);
		String Abar = new BigInteger(secretC_Aj1, 2).xor(new BigInteger(fbar, 2)).xor(new BigInteger(ybar, 2)).toString(2);
		String Lip1 = Abar.substring(j_2*ll, (j_2+1)*ll); // ????
		String secretC_Ti = "1" + Ni + Li + new BigInteger(secretC_Aj1, 2).xor(new BigInteger(fbar, 2)).toString(2);
		String replace = "0" + addZero(new BigInteger(tupleBitLength-1, rnd).toString(2), tupleBitLength-1);
		String secretC_Pprime = secretC_P.substring(0, j_1*tupleBitLength) + replace + secretC_P.substring((j_1+1)*tupleBitLength, (j_1+2)*tupleBitLength);
		
		// outputs
		System.out.println(Lip1);
		System.out.println(secretC_Ti);
		System.out.println(secretE_Ti);
		System.out.println(secretC_Pprime);
		System.out.println(secretE_Pprime);
	}

}
