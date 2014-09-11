// run this program first
// when seeing the prompt for needing the client
// start EvictionTestClient
package sprout.ui;

import sprout.crypto.PRG;
import sprout.oram.*;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;

public class PostAccess1Test
{
	static String addZero(String s, int l) {
		for (int i=s.length(); i<l; i++)
			s = "0" + s;
		return s;
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
				
		
		// PostAccess-1 inputs
		String secretC_Ti 		= "";
		String secretC_Li_p		= "";
		String secretC_Lip1_p 	= "";
		String Li 				= "";
		String Lip1 			= "";
		String Nip1_pr 			= "";
		String secretE_Ti 		= "";
		String secretE_Li_p		= "";
		String secretE_Lip1_p	= "";		
		
		// parameter
		int i 			= 0;
		int tau 		= 5;
		int dip1 		= forest.getTree(i+1).getNumLevels();
		int twotaupow 	= (int) Math.pow(2, tau);
		int l = twotaupow * dip1;
		
		// protocol
		// step 1
		SecureRandom rnd = new SecureRandom();
		String delta_C = addZero(new BigInteger(dip1, rnd).toString(2), dip1);
		String delta_D = new BigInteger(delta_C, 2).xor(new BigInteger(secretE_Lip1_p, 1)).toString(2);
		
		// step 2
		int alpha = rnd.nextInt(twotaupow) + 1;   // [1, 2^tau]
		int j_p = new BigInteger(Nip1_pr, 2).add(BigInteger.valueOf(alpha)).mod(BigInteger.valueOf(twotaupow)).intValue();
		
		// step 3
		byte[] s = rnd.generateSeed(10);  // seed bytes?
		PRG G = new PRG(l, s);
		String[] a = new String[twotaupow];
		String[] a_p = new String[twotaupow];
		for (int k=0; k<twotaupow; k++) {
			a[k] = G.generateBitString(dip1);
			if (k != j_p)
				a_p[k] = a[k];
			else
				a_p[k] = new BigInteger(a[k], 2).xor(new BigInteger(delta_D, 2)).toString(2);
		}
		
		// step 4
		// C generating a_1 ... a_(2^tau) is omitted in this single file version
		String[] e = new String[twotaupow];
		String A_C = ""; 
		int Nip1_pr_int = new BigInteger(Nip1_pr, 2).intValue();
		for (int k=0; k<twotaupow; k++) {
			e[k] = a[BigInteger.valueOf(k+alpha).mod(BigInteger.valueOf(twotaupow)).intValue()];
			if (k == Nip1_pr_int)
				e[k] = new BigInteger(e[k], 2).xor(new BigInteger(Lip1, 2)).xor(new BigInteger(secretC_Lip1_p, 2)).xor(new BigInteger(delta_C, 2)).toString(2);
			A_C += e[k];
		}
		String triangle_C = "0" + addZero("", i*tau) + new BigInteger(Li, 2).xor(new BigInteger(secretC_Li_p, 2)).toString(2) + A_C;
		String secretC_Ti_p = new BigInteger(secretC_Ti, 2).xor(new BigInteger(triangle_C, 2)).toString(2);
		
		// step 5
		String A_E = "";
		for (int k=0; k<twotaupow; k++) {
			A_E += a_p[BigInteger.valueOf(k+alpha).mod(BigInteger.valueOf(twotaupow)).intValue()];
		}
		String triangle_E = "0" + addZero("", i*tau) + secretE_Li_p + A_E;
		String secretE_Ti_p = new BigInteger(secretE_Ti, 2).xor(new BigInteger(triangle_E, 2)).toString(2);
		
		// outputs
		System.out.println(secretC_Ti_p);
		System.out.println(secretE_Ti_p);
	}

}
