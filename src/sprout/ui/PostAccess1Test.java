package sprout.ui;

import sprout.crypto.PRG;
import sprout.util.Util;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PostAccess1Test
{
	public static void main(String[] args) throws Exception {		
		// TODO: add i=0 and i=h cases
		SecureRandom rnd = new SecureRandom();
		
		// parameter
		int h				= 3;
		int i 				= 1;
		int d_i				= 1;
		int d_ip1 			= 4;
		int tau 			= 3;
		int twotaupow 		= (int) Math.pow(2, tau);
		int l 				= twotaupow * d_ip1;	
		int ln 				= i * tau;					
		int ll 				= d_i;						
		int ld 				= l;					
		int tupleBitLength 	= 1 + ln + ll + ld;
		
		// PostAccess-1 inputs
		String Li				= Util.addZero(new BigInteger(ll, rnd).toString(2), ll);													// input
		String Lip1				= Util.addZero(new BigInteger(d_ip1, rnd).toString(2), d_ip1);											// input
		String Nip1_pr			= Util.addZero(new BigInteger(tau, rnd).toString(2), tau);												// input
		int Nip1_pr_int			= new BigInteger(Nip1_pr, 2).intValue();
		String T_i_fb			= "1";
		String T_i_N			= Util.addZero(new BigInteger(ln, rnd).toString(2), ln);
		String T_i_L			= Li;
		String T_i_A			= Util.addZero(new BigInteger(ld, rnd).toString(2), ld);
		T_i_A					= T_i_A.substring(0, Nip1_pr_int*d_ip1) + Lip1 + T_i_A.substring((Nip1_pr_int+1)*d_ip1);
		String T_i 				= T_i_fb + T_i_N + T_i_L + T_i_A;
		String secretC_Ti 		= Util.addZero(new BigInteger(tupleBitLength, rnd).toString(2), tupleBitLength);							// input
		String secretE_Ti		= Util.addZero(new BigInteger(T_i, 2).xor(new BigInteger(secretC_Ti, 2)).toString(2), tupleBitLength);	// input
		String secretC_Li_p		= Util.addZero(new BigInteger(d_i, rnd).toString(2), d_i);												// input
		String secretE_Li_p		= Util.addZero(new BigInteger(d_i, rnd).toString(2), d_i);												// input
		String secretC_Lip1_p 	= Util.addZero(new BigInteger(d_ip1, rnd).toString(2), d_ip1);											// input
		String secretE_Lip1_p	= Util.addZero(new BigInteger(d_ip1, rnd).toString(2), d_ip1);
		
		// protocol
		// step 1
		String delta_C = Util.addZero(new BigInteger(d_ip1, rnd).toString(2), d_ip1);
		String delta_D = Util.addZero(new BigInteger(delta_C, 2).xor(new BigInteger(secretE_Lip1_p, 2)).toString(2), d_ip1);
		
		// step 2
		int alpha = rnd.nextInt(twotaupow) + 1;   // [1, 2^tau]
		int j_p = BigInteger.valueOf(Nip1_pr_int+alpha).mod(BigInteger.valueOf(twotaupow)).intValue();
		
		// step 3
		byte[] s = rnd.generateSeed(16);  // 128 bits
		PRG G = new PRG(l);
		String[] a = new String[twotaupow];
		String[] a_p = new String[twotaupow];
		String a_all = G.generateBitString(l, s);
		for (int k=0; k<twotaupow; k++) {
			a[k] = a_all.substring(k*d_ip1, (k+1)*d_ip1);
			if (k != j_p)
				a_p[k] = a[k];
			else
				a_p[k] = Util.addZero(new BigInteger(a[k], 2).xor(new BigInteger(delta_D, 2)).toString(2), d_ip1);
		}
		
		// step 4
		// C generating a_1 ... a_(2^tau) is omitted in this single file version
		String[] e = new String[twotaupow];
		String A_C = ""; 
		for (int k=0; k<twotaupow; k++) {
			e[k] = a[BigInteger.valueOf(k+alpha).mod(BigInteger.valueOf(twotaupow)).intValue()];
			if (k == Nip1_pr_int)
				e[k] = Util.addZero(new BigInteger(e[k], 2).xor(new BigInteger(Lip1, 2)).xor(new BigInteger(secretC_Lip1_p, 2)).xor(new BigInteger(delta_C, 2)).toString(2), d_ip1);
			A_C += e[k];
		}
		String triangle_C = "0" + Util.addZero("", i*tau) + Util.addZero(new BigInteger(Li, 2).xor(new BigInteger(secretC_Li_p, 2)).toString(2), ll) + A_C;
		String secretC_Ti_p = Util.addZero(new BigInteger(secretC_Ti, 2).xor(new BigInteger(triangle_C, 2)).toString(2), tupleBitLength);
		
		// step 5
		String A_E = "";
		for (int k=0; k<twotaupow+0; k++) {
			A_E += a_p[BigInteger.valueOf(k+alpha).mod(BigInteger.valueOf(twotaupow)).intValue()];
		}
		String triangle_E = "0" + Util.addZero("", i*tau) + secretE_Li_p + A_E;
		String secretE_Ti_p = Util.addZero(new BigInteger(secretE_Ti, 2).xor(new BigInteger(triangle_E, 2)).toString(2), tupleBitLength);
		
		// outputs
		System.out.println(secretC_Ti_p);
		System.out.println(secretE_Ti_p);
		
		// check correctness
		System.out.println("------------------------------");
		System.out.println("Li:\t" + Li);
		System.out.println("Li':\t" + Util.addZero(new BigInteger(secretC_Li_p, 2).xor(new BigInteger(secretE_Li_p, 2)).toString(2), d_i));
		System.out.println("Ti:\t" + T_i);
		String T_i_p = Util.addZero(new BigInteger(secretC_Ti_p, 2).xor(new BigInteger(secretE_Ti_p, 2)).toString(2), tupleBitLength);
		System.out.println("Ti':\t" + T_i_p);
		System.out.println("Nip1_pr_int:\t" + Nip1_pr_int);
		System.out.println("Lip1:\t" + Lip1);
		System.out.println("Lip1 in Ti:\t" + T_i.substring(1+ln+ll).substring(Nip1_pr_int*d_ip1, (Nip1_pr_int+1)*d_ip1));
		System.out.println("Lip1' in Ti':\t" + T_i_p.substring(1+ln+ll).substring(Nip1_pr_int*d_ip1, (Nip1_pr_int+1)*d_ip1));
	}

}
