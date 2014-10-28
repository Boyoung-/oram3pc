package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import sprout.communication.Communication;
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.util.Util;

public class Eviction extends TreeOperation<String[], String[]> {

  public Eviction(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  /*
  public Eviction(Communication con1, Communication con2, ForestMetadata meta) {
    super(con1, con2, meta);
  }
  */

  @Override
  public String[] executeCharlieSubTree(Communication debbie,
      Communication eddie, String unused1, Tree unused2, String[] extraArgs) {
	  if (i == 0)
		  return null;
	  
	  // protocol
	  // step 1
    String sC_P_p = extraArgs[0];
    String sC_T_p = extraArgs[1];
    
    for (int j=0; j<d_i; j++) {
		String sC_fb = "";
		String sC_dir = "";
		String sC_bucket = sC_P_p.substring(j*l, (j+1)*l);
		for (int o=0; o<w; o++) {
			String sC_tuple = sC_bucket.substring(o*tupleBitLength, (o+1)*tupleBitLength);
			sC_fb += sC_tuple.substring(0, 1);
			sC_dir += sC_tuple.substring(1+ln, 1+ln+ll).substring(j, j+1);
		}
		String sC_input = "00" + sC_dir + sC_fb; // enable + dir + fb
		GCF.executeC(debbie, eddie, w*2+2, sC_input);
	}
    
 // step 2
 		String sC_fb = "00";
 		for (int j=d_i; j<n; j++) {
 			String sC_bucket = sC_P_p.substring(j*l, (j+1)*l);
 			for (int o=0; o<w; o++) {
 				sC_fb += sC_bucket.substring(o*tupleBitLength, o*tupleBitLength+1);
 			}
 		}
 		GCF.executeC(debbie, eddie, w*expen+2, sC_fb);
 		
 	// step 3
 		int k = w * n;
 		
 	// step 4
 			String[] sC_a = new String[k+2];
 			for (int j=0; j<n; j++)
 				for (int o=0; o<w; o++) {
 					sC_a[w*j+o] = sC_P_p.substring(j*l, (j+1)*l).substring(o*tupleBitLength, (o+1)*tupleBitLength);
 				}
 			sC_a[k] = sC_T_p;
 			sC_a[k+1] = Util.addZero(new BigInteger(tupleBitLength-1, rnd).toString(2), tupleBitLength);
 			
 	// step 5
 	String[] sC_P_pp = SSOT.executeC(debbie, eddie, sC_a);
    
    return sC_P_pp;
  }

  // TODO: handle index -1 case
  // TODO: figure out step 3
  @Override
  public String[] executeDebbieSubTree(Communication charlie,
      Communication eddie, BigInteger unused1, Tree unused2,
      String[] unused3) {
	  if (i == 0)
		  return null;
    
    // protocol
 		// step 1
 		int[] alpha1_j = new int[w];
 		int[] alpha2_j = new int[w];
 		for (int j=0; j<d_i; j++) {
 			String GCFOutput = GCF.executeD(charlie, eddie, "F2FT", w*2+2);
 			alpha1_j[j] = GCFOutput.substring(2).indexOf('1');
 			alpha2_j[j] = GCFOutput.substring(2).indexOf('1', alpha1_j[j]+1);
 			System.out.println("--- D: alpha_j: " + alpha1_j[j] + " " + alpha2_j[j]);
 		}
 		
 	// step 2
 	String GCFOutput = GCF.executeD(charlie, eddie, "F2ET", w*expen+2);
 	int alpha1_d = GCFOutput.substring(2).indexOf('1');
 	int alpha2_d = GCFOutput.substring(2).indexOf('1', alpha1_d+1);
 	System.out.println("--- D: alpha_d: " + alpha1_d + " " + alpha2_d);
 	
 // step 3
 	// the o here is the l in writeup
 		int k = w * n;
 		Integer[] beta = new Integer[k];
 		for (int j=0; j<n; j++)
 			for (int o=0; o<w; o++) {
 				if (j == 0 && o == alpha1_j[0])
 					beta[w*j+o] = k;
 				else if (j == 0 && o == alpha2_j[0])
 					beta[w*j+o] = k + 1;
 				else if (1 <= j && j <= (d_i-1) && o == alpha1_j[j])
 					beta[w*j+o] = w * (j-1) + alpha1_j[j-1];
 				else if (1 <= j && j <= (d_i-1) && o == alpha2_j[j])
 					beta[w*j+o] = w * (j-1) + alpha2_j[j-1];
 				else if (j >= d_i && (w*(j-d_i)+o) == alpha1_d)
 					beta[w*j+o] = w * (d_i-1) + alpha1_j[d_i-1];
 				else if (j >= d_i && (w*(j-d_i)+o) == alpha2_d)
 					beta[w*j+o] = w * (d_i-1) + alpha2_j[d_i-1];
 				else
 					beta[w*j+o] = w * j + o;
 			}
 		Integer[] I = beta;
 		//System.out.println("k: " + k);
 		//Util.printArrH(I);
 		
 		// step 5
 		try {
			SSOT.executeI(charlie, eddie, I);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
    
    return null;
  }

  @Override
  public String[] executeEddieSubTree(Communication charlie,
      Communication debbie, Tree unused, String[] extraArgs) {
	  if (i == 0)
		  return null;
	  
	  // protocol
	  // step 1
    String sE_P_p = extraArgs[0];
    String sE_T_p = extraArgs[1];
    String Li = extraArgs[2];
    
    for (int j=0; j<d_i; j++) {
		String sE_fb = "";
		String sE_dir = "";
		String sE_bucket = sE_P_p.substring(j*l, (j+1)*l);
		for (int o=0; o<w; o++) {
			String sE_tuple = sE_bucket.substring(o*tupleBitLength, (o+1)*tupleBitLength);
			sE_fb += sE_tuple.substring(0, 1);
			sE_dir += (Character.getNumericValue(sE_tuple.substring(1+ln, 1+ln+ll).charAt(j))^1^Character.getNumericValue(Li.charAt(j)));
		}
		String sE_input = "00" + sE_dir + sE_fb; // enable + dir + fb
		GCF.executeE(charlie, debbie, "F2FT", w*2+2, sE_input);
	}
    
 // step 2
 		String sE_fb = "00";
 		for (int j=d_i; j<n; j++) {
 			String sE_bucket = sE_P_p.substring(j*l, (j+1)*l);
 			for (int o=0; o<w; o++) {
 				sE_fb += sE_bucket.substring(o*tupleBitLength, o*tupleBitLength+1);
 			}
 		}
 		GCF.executeE(charlie, debbie, "F2ET", w*expen+2, sE_fb);
 		
 	// step 3
 			int k = w * n;
 			
 			// step 4
 			String[] sE_a = new String[k+2];
 			for (int j=0; j<n; j++)
 				for (int o=0; o<w; o++) {
 					sE_a[w*j+o] = sE_P_p.substring(j*l, (j+1)*l).substring(o*tupleBitLength, (o+1)*tupleBitLength);
 				}
 			sE_a[k] = sE_T_p;
 			sE_a[k+1] = Util.addZero(new BigInteger(tupleBitLength-1, rnd).toString(2), tupleBitLength);
 			
 	// step 5
 	String[] sE_P_pp = SSOT.executeE(charlie, debbie, sE_a);
    
    return sE_P_pp;
  }

  @Override
  public String[] prepareArgs(Party party) {
    // Randomly generate a secret
      String s_P_p   = Util.addZero(new BigInteger(n*l, rnd).toString(2), n*l);
      String s_T_p   = Util.addZero(new BigInteger(tupleBitLength, rnd).toString(2), tupleBitLength);
      String Li = Util.addZero(new BigInteger(ll, rnd).toString(2), ll);
      
      return new String[]{s_P_p, s_T_p, Li};
  }
  
 @Override
 public void loadTreeSpecificParameters(int index) {
   super.loadTreeSpecificParameters(index);
   if (i > 0)
	   n = n/w;
 }

}
