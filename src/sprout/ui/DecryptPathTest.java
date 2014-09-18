package sprout.ui;

import sprout.crypto.PRG;
import sprout.util.Util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DecryptPathTest
{
	public static void main(String[] args) throws Exception {
		SecureRandom rnd = new SecureRandom();
		
		// parameters
		BigInteger q 		= BigInteger.valueOf(953);  // prime: too small
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
		
		// inputs
		//String Li			= Util.addZero(new BigInteger(ll, rnd).toString(2), ll);
		BigInteger k		= BigInteger.valueOf(Math.abs(rnd.nextLong()) % q.longValue());	
		String Pbar			= Util.addZero(new BigInteger(l*(d_i+4)*2, rnd).toString(2), l*(d_i+4)*2);
		
		// protocol
		// step 1
		// C sends label Li to E
		
		// step 2
		// E retrieves the path Pbar in OT_i...
		
		// step 3
		List<Integer> sigma	= new ArrayList<Integer>();												// input
		for (int j=0; j<d_i+4; j++)
			sigma.add(j);
		if (i > 0)
			Collections.shuffle(sigma); // random permutation
		String[] x = new String[d_i+4];
		String[] Bbar = new String[d_i+4];
		for (int j=0; j<d_i+4; j++) {
			x[j] = Pbar.substring(j*2*l, j*2*l+l);
			Bbar[j] = Pbar.substring(j*2*l+l, (j+1)*2*l);
		}
		String[] sigma_x = Util.permute(x, sigma);
		String[] secretE_P_arr = Util.permute(Bbar, sigma);
		String secretE_P = "";
		for (int j=0; j<d_i+4; j++)
			secretE_P += secretE_P_arr[j];
		
		// step 4
		// PRG is used here instead of OPRF for testing purpose
		PRG G = new PRG(l);
		String secretC_P = "";
		for (int j=0; j<d_i+4; j++)
			secretC_P += G.generateBitString(l, new BigInteger(sigma_x[j], 2).modPow(k, q));
		
		// outputs
		System.out.println(secretE_P);
		System.out.println(secretC_P);
	}

}
