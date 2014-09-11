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
	
	static String executeAOT_2tau(String j, String[] y) {
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
		boolean initialTree = false;    // OT_0 or not
		int h = forest.getNumberOfTrees();
		int i = 0; // current tree index
		Tree t = forest.getTree(i);
		String Ni = "";                                  // input
		String Nip1_p = "";                              // input
		String Nip1 = Ni + Nip1_p;                       // input
		String Li = "";                                  // input
		int d_i = t.getNumLevels();
		int n = t.getBucketDepth() * (d_i + 4);
		int ll = t.getLBytes() * 8;
		int ln = t.getNBytes() * 8;
		int ld = t.getDBytes() * 8;
		int tupleBitLength = 1 + ll + ln + ld;
		String secretC_P = addZero("", tupleBitLength);  // input
		String secretE_P = addZero("", tupleBitLength);  // input
		
		// start testing Access-2
		// step 1
		int tau = 5; // for testing
		int l = (int) Math.pow(2, tau); 
		int d_ip1 = forest.getTree(i+1).getNumLevels();
		SecureRandom rnd = new SecureRandom();
		String[] y = new String[l];
		String y_all = "";
		if (initialTree) { // i = 0 case
			String A_1 = forest.getInitialORAMTreeString();
			int length = A_1.length() / l;
			for (int k=0; k<l; k++) {
				y[k] = A_1.substring(k*length, (k+1)*length);
			}
			y_all = A_1;
		}
		else { // 0 < i < h case
			for (int k=0; k<l; k++) {
				y[k] = addZero(new BigInteger(d_ip1, rnd).toString(2), d_ip1);
				y_all += y[k];
			}
		}
		if (i == (h-1)){ // i = h case
			y_all = addZero("", y.length);
		}
		
		String secretE_Ti = "0" + addZero("", i*tau) + addZero ("", d_i) + y_all;
		String secretE_Pprime = "";
		if (!initialTree) { // i > 0
			secretE_Pprime = secretE_P;
		}
		
		// step 2
		int j_1 = 0; // first tuple when i = 0
		if (!initialTree) {
			String[] a = new String[n];
			String[] b = new String[n];
			String[] c = new String[n];
			for (int j=0; j<n; j++) {
				a[j] = secretC_P.substring(j*tupleBitLength, j*tupleBitLength+1) +  // fb
					   secretC_P.substring(j*tupleBitLength+1+ll, j*tupleBitLength+1+ll+ln); // N
				b[j] = secretE_P.substring(j*tupleBitLength, j*tupleBitLength+1) +  // fb
					   secretE_P.substring(j*tupleBitLength+1+ll, j*tupleBitLength+1+ll+ln); // N
				c[j] = new BigInteger(a[j], 2).xor(new BigInteger("1"+Ni, 2)).toString(2);
			}
			j_1 = PET.executePET(c, b);
		}
		
		// step 3
		String fbar = addZero("", y_all.length());
		if (!initialTree) {
			String[] e = new String[n];
			String[] f = new String[n];
			for (int k=0; k<n; k++) {
				e[k] = secretE_P.substring(k*tupleBitLength+1+ll+ln, (k+1)*tupleBitLength); // A
				f[k] = new BigInteger(e[k], 2).xor(new BigInteger(y_all, 2)).toString(2);
			}
			fbar = executeAOT_n(j_1, f);
		}
		
		// step 4
		String j_2 = Nip1_p;
		String ybar_j2 = "";
		if (i < (h-1)) {
			ybar_j2 = executeAOT_2tau(j_2, y);
		}
		
		// step 5
		String ybar = "";
		String zeros = addZero("", d_ip1);
		int j_2_int = new BigInteger(j_2, 2).intValue();
		for (int k=0; k<l; k++) {
			if (k == j_2_int && i < (h-1))
				ybar += ybar_j2;
			else
				ybar += zeros;
		}
		String secretC_Aj1 = secretC_P.substring(j_1*tupleBitLength, (j_1+1)*tupleBitLength);
		String Abar = new BigInteger(secretC_Aj1, 2).xor(new BigInteger(fbar, 2)).xor(new BigInteger(ybar, 2)).toString(2);
		String d = "";
		String Lip1 = "";
		if (i < (h-1)) {
			Lip1 = Abar.substring(j_2_int*d_ip1, (j_2_int+1)*d_ip1);
		}
		else {
			d = Abar;
		}
		String secretC_Ti = "1" + Ni + Li + new BigInteger(secretC_Aj1, 2).xor(new BigInteger(fbar, 2)).toString(2);
		String secretC_Pprime = "";
		if (!initialTree) {
			String newTuple = addZero(new BigInteger(tupleBitLength-1, rnd).toString(2), tupleBitLength);
			secretC_Pprime = secretC_P.substring(0, j_1*tupleBitLength) + newTuple + secretC_P.substring((j_1+1)*tupleBitLength);
		}
		
		// outputs
		System.out.println(Lip1);
		System.out.println(secretC_Ti);
		System.out.println(secretE_Ti);
		System.out.println(secretC_Pprime);
		System.out.println(secretE_Pprime);
		System.out.println(d);
	}

}
