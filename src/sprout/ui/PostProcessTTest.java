package sprout.ui;

import sprout.crypto.PRG;
import sprout.oram.Forest;
import sprout.oram.ForestMetadata;
import sprout.oram.Tree;
import sprout.util.Util;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PostProcessTTest
{	
	static SecureRandom rnd = new SecureRandom();
	
	public static String[] execute(String Li, String Lip1, String Nip1_pr, String secretC_Ti, String secretE_Ti, 
			String secretC_Li_p, String secretE_Li_p, String secretC_Lip1_p, String secretE_Lip1_p, 
			Tree OT, ForestMetadata metadata) throws Exception {			
		// parameter
		int tau 			= metadata.getTauExponent();		// tau in the writeup
		int twotaupow 		= metadata.getTau();            	// 2^tau
		int h				= metadata.getLevels();         	// # trees
		int w 				= metadata.getBucketDepth();		// # tuples in each bucket
		int expen			= metadata.getLeafExpansion();		// # buckets in each leaf
		int treeLevel;
		if (OT != null)											// we are dealing with the initial tree
			treeLevel		= OT.getTreeLevel();				
		else
			treeLevel		= h;								// tree index in ORAM forest
		int i 				= h - treeLevel;					// tree index in the writeup
		int d_i				= 0;								// # levels in this tree (excluding the root level)
		if (i > 0)
			d_i				= OT.getNumLevels();
		int d_ip1;												// # levels in the next tree (excluding the root level)
		if (i == h)
			d_ip1			= OT.getDBytes() * 8 / twotaupow;
		else
			d_ip1			= metadata.getTupleBitsL(treeLevel-1);
		int ln 				= i * tau;							// # N bits
		int ll 				= d_i;								// # L bits
		int ld 				= twotaupow * d_ip1;				// # data bits
		int tupleBitLength 	= 1 + ln + ll + ld;					// # tuple size (bits)
		int l				= ld;								// # data bits
		if (i == 0)
			tupleBitLength	= ld;
		
		
		int Nip1_pr_int			= new BigInteger(Nip1_pr, 2).intValue();
		
		
		//////////////////////// below are for checking correctness /////////////////////
		String T_i_fb			= "1";
		String T_i_N			= Util.addZero(new BigInteger(ln, rnd).toString(2), ln);
		String T_i_L			= Li;
		String T_i_A			= Util.addZero(new BigInteger(ld, rnd).toString(2), ld);
		T_i_A					= T_i_A.substring(0, Nip1_pr_int*d_ip1) + Lip1 + T_i_A.substring((Nip1_pr_int+1)*d_ip1);
		String T_i;
		if (i == 0)
			T_i					= T_i_A;
		else
			T_i					= T_i_fb + T_i_N + T_i_L + T_i_A;
		secretE_Ti		= Util.addZero(new BigInteger(T_i, 2).xor(new BigInteger(secretC_Ti, 2)).toString(2), tupleBitLength);	
		//////////////////////// above are for checking correctness /////////////////////

		// protocol
		// i = 0 case
		if (i == 0) {
			Li = "";
			secretC_Li_p = "";
			secretE_Li_p = "";
		}
		
		// protocol doesn't run for i=h case
		String[] output = new String[2];
		if (i == h) {
			int d_size = 9;
			// party C
			String triangle_C = "0" + Util.addZero("", i*tau) + Util.addZero(new BigInteger(Li, 2).xor(new BigInteger(secretC_Li_p, 2)).toString(2), ll) + Util.addZero("", d_size);	
			String secretC_Ti_p = Util.addZero(new BigInteger(secretC_Ti, 2).xor(new BigInteger(triangle_C, 2)).toString(2), tupleBitLength);
			// party E
			String triangle_E = "0" + Util.addZero("", i*tau) + secretE_Li_p + Util.addZero("", d_size);
			String secretE_Ti_p = Util.addZero(new BigInteger(secretE_Ti, 2).xor(new BigInteger(triangle_E, 2)).toString(2), tupleBitLength);
			output[0] = secretC_Ti_p; // C's output
			output[1] = secretE_Ti_p; // E's output
			return output;			
		}
		
		// step 1
		// party E
		String delta_D = Util.addZero(new BigInteger(d_ip1, rnd).toString(2), d_ip1);
		String delta_C = Util.addZero(new BigInteger(delta_D, 2).xor(new BigInteger(secretE_Lip1_p, 2)).toString(2), d_ip1);
		// E sends delta_C
		
		// step 2
		// party C
		int alpha = rnd.nextInt(twotaupow) + 1;   // [1, 2^tau]
		int j_p = BigInteger.valueOf(Nip1_pr_int+alpha).mod(BigInteger.valueOf(twotaupow)).intValue();
		// C sends j_p to D
		// C sends alpha to E
		
		// step 3
		// party D
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
		// D sends s to C
		// D sends a_p to E
		
		// step 4
		// party C
		// C generating a_1 ... a_(2^tau) is omitted in this single file version
		String[] e = new String[twotaupow];
		String A_C = ""; 
		for (int k=0; k<twotaupow; k++) {
			e[k] = a[BigInteger.valueOf(k+alpha).mod(BigInteger.valueOf(twotaupow)).intValue()];
			if (k == Nip1_pr_int)
				e[k] = Util.addZero(new BigInteger(e[k], 2).xor(new BigInteger(Lip1, 2)).xor(new BigInteger(secretC_Lip1_p, 2)).xor(new BigInteger(delta_C, 2)).toString(2), d_ip1);
			A_C += e[k];
		}
		String triangle_C;
		if (i == 0)
			triangle_C = A_C;
		else
			triangle_C = "0" + Util.addZero("", i*tau) + Util.addZero(new BigInteger(Li, 2).xor(new BigInteger(secretC_Li_p, 2)).toString(2), ll) + A_C;
		String secretC_Ti_p = Util.addZero(new BigInteger(secretC_Ti, 2).xor(new BigInteger(triangle_C, 2)).toString(2), tupleBitLength);
		// C outputs secretC_Ti_p
		
		// step 5
		// party E
		String A_E = "";
		for (int k=0; k<twotaupow+0; k++) {
			A_E += a_p[BigInteger.valueOf(k+alpha).mod(BigInteger.valueOf(twotaupow)).intValue()];
		}
		String triangle_E;
		if (i == 0)
			triangle_E = A_E;
		else
			triangle_E = "0" + Util.addZero("", i*tau) + secretE_Li_p + A_E;
		String secretE_Ti_p = Util.addZero(new BigInteger(secretE_Ti, 2).xor(new BigInteger(triangle_E, 2)).toString(2), tupleBitLength);
		// E outputs secretE_Ti_p
		
		//////////////////////// below are for checking correctness /////////////////////
		System.out.println("------------------------------");
		if (i > 0) {
			System.out.println("Li:\t" + Li);
			System.out.println("Li':\t" + Util.addZero(new BigInteger(secretC_Li_p, 2).xor(new BigInteger(secretE_Li_p, 2)).toString(2), d_i));
		}
		System.out.println("Ti:\t" + T_i);
		String T_i_p = Util.addZero(new BigInteger(secretC_Ti_p, 2).xor(new BigInteger(secretE_Ti_p, 2)).toString(2), tupleBitLength);
		System.out.println("Ti':\t" + T_i_p);
		System.out.println("Nip1_pr_int:\t" + Nip1_pr_int);
		System.out.println("Lip1:\t" + Lip1);
		if (i == 0) {
			System.out.println("Lip1 in Ti:\t" + T_i.substring(Nip1_pr_int*d_ip1, (Nip1_pr_int+1)*d_ip1));
			System.out.println("Lip1' in Ti':\t" + T_i_p.substring(Nip1_pr_int*d_ip1, (Nip1_pr_int+1)*d_ip1));
		}
		else {
			System.out.println("Lip1 in Ti:\t" + T_i.substring(1+ln+ll).substring(Nip1_pr_int*d_ip1, (Nip1_pr_int+1)*d_ip1));
			System.out.println("Lip1' in Ti':\t" + T_i_p.substring(1+ln+ll).substring(Nip1_pr_int*d_ip1, (Nip1_pr_int+1)*d_ip1));
		}
		//////////////////////// above are for checking correctness /////////////////////

		// outputs
		output[0] = secretC_Ti_p;
		output[1] = secretE_Ti_p;
		return output;	
	}
	
	public static void main(String[] args) throws Exception {
		Forest forest = new Forest();
		//forest.buildFromFile("config/smallConfig.yaml", "config/smallData.txt", "db.bin");
		forest.loadFile("config/smallConfig.yaml", "db.bin");
		System.out.println("Forest loaded.\n");
		
		int tau 			= forest.getMetadata().getTauExponent();
		int twotaupow 		= forest.getMetadata().getTau();
		int h				= forest.getMetadata().getLevels();
		int w 				= forest.getMetadata().getBucketDepth();
		int e 				= forest.getMetadata().getLeafExpansion();
		
		for (int treeLevel = forest.getNumberOfTrees(); treeLevel >= 0; treeLevel--) {
			Tree OT = null;
			if (treeLevel < forest.getNumberOfTrees())
				OT = forest.getTree(treeLevel);
			int i 				= h - treeLevel;
			int d_i				= 0;
			if (i > 0)
				d_i				= OT.getNumLevels();
			int d_ip1 			= -1;
			if (i == h)
				d_ip1			= OT.getDBytes() * 8 / twotaupow;
			else
				d_ip1			= forest.getMetadata().getTupleBitsL(treeLevel-1);
			int ln 				= i * tau;					
			int ll 				= d_i;						
			int ld 				= twotaupow * d_ip1;					
			int tupleBitLength 	= 1 + ln + ll + ld;
			int l				= ld;
			
			String Li				= Util.addZero(new BigInteger(ll, rnd).toString(2), ll);											
			String Lip1				= Util.addZero(new BigInteger(d_ip1, rnd).toString(2), d_ip1);											
			String Nip1_pr			= Util.addZero(new BigInteger(tau, rnd).toString(2), tau);	
			String secretC_Ti 		= Util.addZero(new BigInteger(tupleBitLength, rnd).toString(2), tupleBitLength);						
			//String secretE_Ti		= Util.addZero(new BigInteger(T_i, 2).xor(new BigInteger(secretC_Ti, 2)).toString(2), tupleBitLength);	
			String secretC_Li_p		= Util.addZero(new BigInteger(d_i, rnd).toString(2), d_i);												
			String secretE_Li_p		= Util.addZero(new BigInteger(d_i, rnd).toString(2), d_i);											
			String secretC_Lip1_p 	= Util.addZero(new BigInteger(d_ip1, rnd).toString(2), d_ip1);											
			String secretE_Lip1_p	= Util.addZero(new BigInteger(d_ip1, rnd).toString(2), d_ip1);
			execute(Li, Lip1, Nip1_pr, secretC_Ti, "", secretC_Li_p, secretE_Li_p, secretC_Lip1_p, secretE_Lip1_p, OT, forest.getMetadata());
		}
	}
}
