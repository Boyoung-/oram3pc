package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import sprout.communication.Communication;
import sprout.crypto.SR;
import sprout.oram.PID;
import sprout.oram.Party;
import sprout.oram.Tree;
import sprout.util.Util;

public class CopyOfEviction extends TreeOperation<BigInteger, BigInteger[]> {
	
	public CopyOfEviction() {
		super(null, null);
	}

  public CopyOfEviction(Communication con1, Communication con2) {
    super(con1, con2);
  }

  @Override
  public BigInteger executeCharlieSubTree(Communication debbie, Communication eddie, 
		  String unused1, Tree unused2, BigInteger[] extraArgs) {
	  if (i == 0)
		  return null;
	  
	  GCF gcf = new GCF(debbie, eddie);
	  
	  debbie.countBandwidth = true;
	    eddie.countBandwidth = true;
	    debbie.bandwidth[PID.eviction].start();
	    eddie.bandwidth[PID.eviction].start();
	    
	    sanityCheck();
	  
	  // protocol
	  // step 1
    BigInteger sC_P_p = extraArgs[0];
    BigInteger sC_T_p = extraArgs[1];
    
    for (int j=0; j<d_i; j++) {
		timing.eviction_online.start();
		//String sC_fb = "";
		//String sC_dir = "";
		//String sC_bucket = sC_P_p.substring(j*bucketBits, (j+1)*bucketBits);
		BigInteger sC_fb = BigInteger.ZERO;
		BigInteger sC_dir = BigInteger.ZERO;
		BigInteger sC_bucket = Util.getSubBits(sC_P_p, (d_i-j-1)*bucketBits, (d_i-j)*bucketBits);
		//for (int l=0; l<w; l++) {
		//	String sC_tuple = sC_bucket.substring(l*tupleBits, (l+1)*tupleBits);
		//	sC_fb += sC_tuple.substring(0, 1);
		//	sC_dir += sC_tuple.substring(1+nBits, 1+nBits+lBits).substring(j, j+1);
		//}
		//String sC_input = "00" + sC_dir + sC_fb; // enable + dir + fb
		for (int l=0; l<w; l++) {
			BigInteger sC_tuple = Util.getSubBits(sC_bucket, (w-l-1)*tupleBits, (w-l)*tupleBits);
			sC_fb = sC_fb.shiftLeft(1);
			if (sC_tuple.testBit(tupleBits-1))
				sC_fb.setBit(0);
			int bit = (sC_tuple.testBit(lBits-j-1+aBits)?1:0);
			sC_dir = sC_dir.shiftLeft(1);
			if (bit == 1)
				sC_dir.setBit(0);
		}
		BigInteger sC_input = sC_dir.shiftLeft(w).xor(sC_fb);
		timing.eviction_online.stop();
		
		timing.gcf.start();
		gcf.executeC(debbie, eddie, w*2+2, Util.addZero(sC_input.toString(2), w*2+2));
		timing.gcf.stop();
	}
    
    	// step 2
    	timing.eviction_online.start();
    	/*
 		String sC_fb = "00";
 		for (int j=d_i; j<pathBuckets; j++) {
 			String sC_bucket = sC_P_p.substring(j*bucketBits, (j+1)*bucketBits);
 			for (int l=0; l<w; l++) {
 				sC_fb += sC_bucket.substring(l*tupleBits, l*tupleBits+1);
 			}
 		}
 		*/
    	BigInteger sC_fb = BigInteger.ZERO;
 		for (int j=d_i; j<pathBuckets; j++) {
 			BigInteger sC_bucket = Util.getSubBits(sC_P_p, (pathBuckets-j-1)*bucketBits, (pathBuckets-j)*bucketBits);
 			for (int l=0; l<w; l++) {
 				sC_fb = sC_fb.shiftLeft(1);
 				if (sC_bucket.testBit((w-l)*tupleBits-1))
 					sC_fb.setBit(0);
 			}
 		}
 		timing.eviction_online.stop();
 		
 		timing.gcf.start();
 		gcf.executeC(debbie, eddie, w*expen+2, Util.addZero(sC_fb.toString(2), w*expen+2));
 		timing.gcf.stop();
 		
 	// step 3
 		int k = w * pathBuckets;
 		
 	// step 4
			timing.eviction_online.start();
 			String[] sC_a = new String[k+2];
 			for (int j=0; j<pathBuckets; j++)
 				for (int l=0; l<w; l++) {
 					//sC_a[w*j+l] = sC_P_p.substring(j*bucketBits, (j+1)*bucketBits).substring(l*tupleBits, (l+1)*tupleBits);
 					sC_a[w*j+l] = Util.addZero(Util.getSubBits(sC_P_p, (pathBuckets-j-1)*bucketBits+(w-l-1)*tupleBits, (pathBuckets-j-1)*bucketBits+(w-l)*tupleBits).toString(2), tupleBits);
 				}
 			sC_a[k] = Util.addZero(sC_T_p.toString(2), tupleBits);
 			sC_a[k+1] = Util.addZero(new BigInteger(tupleBits-1, SR.rand).toString(2), tupleBits);
 			timing.eviction_online.stop();
 			
 	// step 5
 	SSOT ssot = new SSOT(debbie, eddie);
 	timing.ssot.start();
 	String[] sC_P_pp = ssot.executeC(debbie, eddie, sC_a);
 	timing.ssot.stop();
 	
 	timing.eviction_online.start();
 	String secretC_P_pp = "";
 	for (int j=0; j<sC_P_pp.length; j++)
 		secretC_P_pp += sC_P_pp[j];
 	timing.eviction_online.stop();
 	
 	debbie.countBandwidth = false;
    eddie.countBandwidth = false;
    debbie.bandwidth[PID.eviction].stop();
    eddie.bandwidth[PID.eviction].stop();
 	
    return new BigInteger(secretC_P_pp, 2);
  }

  @Override
  public BigInteger executeDebbieSubTree(Communication charlie, Communication eddie, 
		  BigInteger unused1, Tree unused2, BigInteger[] unused3) {
	  if (i == 0)
		  return null;
	  
	  GCF gcf = new GCF(charlie, eddie);
	  
	  charlie.countBandwidth = true;
	  eddie.countBandwidth = true;	  
	  charlie.bandwidth[PID.eviction].start();
	  eddie.bandwidth[PID.eviction].start();
	  
	  sanityCheck();
    
    // protocol
 		// step 1
 		int[] alpha1_j = new int[d_i];
 		int[] alpha2_j = new int[d_i];
 		for (int j=0; j<d_i; j++) {
 			timing.gcf.start();
 			String GCFOutput = gcf.executeD(charlie, eddie, "F2FT", w*2+2);
 			timing.gcf.stop();
 			
 			timing.eviction_online.start();
 			alpha1_j[j] = GCFOutput.substring(2).indexOf('1');
 			if (alpha1_j[j] == -1)
 				alpha1_j[j] = SR.rand.nextInt(w);
 			alpha2_j[j] = GCFOutput.substring(2).indexOf('1', alpha1_j[j]+1);
 			while (alpha2_j[j] == -1 || alpha2_j[j] == alpha1_j[j])
 				alpha2_j[j] = SR.rand.nextInt(w);
 			timing.eviction_online.stop();
 			//System.out.println("--- D: alpha_j: " + alpha1_j[j] + " " + alpha2_j[j]);
 		}
 		
 	// step 2
 	timing.gcf.start();
 	String GCFOutput = gcf.executeD(charlie, eddie, "F2ET", w*expen+2);
 	timing.gcf.stop();
 	
 	timing.eviction_online.start();
 	int alpha1_d = GCFOutput.substring(2).indexOf('1');
 	int alpha2_d = GCFOutput.substring(2).indexOf('1', alpha1_d+1);
 	timing.eviction_online.stop();
 	if (alpha2_d == -1) {
 		try {
			throw new Exception("Overflow!");
		} catch (Exception e) {
			e.printStackTrace();
		}
 	}
 	//System.out.println("--- D: alpha_d: " + alpha1_d + " " + alpha2_d);
 	
 // step 3
 		int k = w * pathBuckets;
 		Integer[] beta = new Integer[k];
 		timing.eviction_online.start();
 		for (int j=0; j<pathBuckets; j++)
 			for (int l=0; l<w; l++) {
 				if (j == 0 && l == alpha1_j[0])
 					beta[w*j+l] = k;
 				else if (j == 0 && l == alpha2_j[0])
 					beta[w*j+l] = k + 1;
 				else if (1 <= j && j <= (d_i-1) && l == alpha1_j[j])
 					beta[w*j+l] = w * (j-1) + alpha1_j[j-1];
 				else if (1 <= j && j <= (d_i-1) && l == alpha2_j[j])
 					beta[w*j+l] = w * (j-1) + alpha2_j[j-1];
 				else if (j >= d_i && (w*(j-d_i)+l) == alpha1_d)
 					beta[w*j+l] = w * (d_i-1) + alpha1_j[d_i-1];
 				else if (j >= d_i && (w*(j-d_i)+l) == alpha2_d)
 					beta[w*j+l] = w * (d_i-1) + alpha2_j[d_i-1];
 				else
 					beta[w*j+l] = w * j + l;
 			}
 		timing.eviction_online.stop();
 		Integer[] I = beta;
 		//System.out.println("k: " + k);
 		//Util.printArrH(I);
 		
 		// step 5
 		try {
 			SSOT ssot = new SSOT(charlie, eddie);
 			timing.ssot.start();
			ssot.executeI(charlie, eddie, I);
			timing.ssot.stop();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
 		
 		charlie.countBandwidth = false;
 		  eddie.countBandwidth = false;	  
 		  charlie.bandwidth[PID.eviction].stop();
 		  eddie.bandwidth[PID.eviction].stop();
    
    return null;
  }

  @Override
  public BigInteger executeEddieSubTree(Communication charlie, Communication debbie, 
		  Tree unused, BigInteger[] extraArgs) {
	  if (i == 0)
		  return null;
	  
	  GCF gcf = new GCF(charlie, debbie);
	  
	  charlie.countBandwidth = true;
	  debbie.countBandwidth = true;
	  charlie.bandwidth[PID.eviction].start();
	  debbie.bandwidth[PID.eviction].start();
	  
	  sanityCheck();
	  
	  // protocol
	  // step 1
    BigInteger sE_P_p = extraArgs[0];
    BigInteger sE_T_p = extraArgs[1];
    BigInteger Li = extraArgs[2];
    
    for (int j=0; j<d_i; j++) {
		timing.eviction_online.start();
		BigInteger sE_fb = BigInteger.ZERO;
		BigInteger sE_dir = BigInteger.ZERO;
		BigInteger sE_bucket = Util.getSubBits(sE_P_p, (d_i-j-1)*bucketBits, (d_i-j)*bucketBits);
		for (int l=0; l<w; l++) {
			BigInteger sE_tuple = Util.getSubBits(sE_bucket, (w-l-1)*tupleBits, (w-l)*tupleBits);
			sE_fb = sE_fb.shiftLeft(1);
			if (sE_tuple.testBit(tupleBits-1))
				sE_fb.setBit(0);
			int bit = (sE_tuple.testBit(lBits-j-1+aBits)?1:0)^1^(Li.testBit(lBits-j-1)?1:0);
			sE_dir = sE_dir.shiftLeft(1);
			if (bit == 1)
				sE_dir.setBit(0);
		}
		BigInteger sE_input = sE_dir.shiftLeft(w).xor(sE_fb);
		timing.eviction_online.stop();
		
		timing.gcf.start();
		gcf.executeE(charlie, debbie, "F2FT", w*2+2, Util.addZero(sE_input.toString(2), w*2+2));
		timing.gcf.stop();
	}
    
    	// step 2
    	timing.eviction_online.start();
    	BigInteger sE_fb = BigInteger.ZERO;
 		for (int j=d_i; j<pathBuckets; j++) {
 			BigInteger sE_bucket = Util.getSubBits(sE_P_p, (pathBuckets-j-1)*bucketBits, (pathBuckets-j)*bucketBits);
 			for (int l=0; l<w; l++) {
 				sE_fb = sE_fb.shiftLeft(1);
 				if (sE_bucket.testBit((w-l)*tupleBits-1))
 					sE_fb.setBit(0);
 			}
 		}
 		timing.eviction_online.stop();
 		
 		timing.gcf.start();
 		gcf.executeE(charlie, debbie, "F2ET", w*expen+2, Util.addZero(sE_fb.toString(2), w*expen+2));
 		timing.gcf.stop();
 		
 	// step 3
 			int k = w * pathBuckets;
 			
 			// step 4
 			String[] sE_a = new String[k+2];
 			timing.eviction_online.start();
 			for (int j=0; j<pathBuckets; j++)
 				for (int l=0; l<w; l++) {
 					sE_a[w*j+l] = Util.addZero(Util.getSubBits(sE_P_p, (pathBuckets-j-1)*bucketBits+(w-l-1)*tupleBits, (pathBuckets-j-1)*bucketBits+(w-l)*tupleBits).toString(2), tupleBits);
 				}
 			sE_a[k] = Util.addZero(sE_T_p.toString(2), tupleBits);
 			sE_a[k+1] = Util.addZero(new BigInteger(tupleBits-1, SR.rand).toString(2), tupleBits);
 			timing.eviction_online.stop();
 			
 	// step 5
 	SSOT ssot = new SSOT(charlie, debbie);
 	timing.ssot.start();
 	String[] sE_P_pp = ssot.executeE(charlie, debbie, sE_a);
 	timing.ssot.stop();
 	
 	timing.eviction_online.start();
 	String secretE_P_pp = "";
 	for (int j=0; j<sE_P_pp.length; j++)
 		secretE_P_pp += sE_P_pp[j];
 	timing.eviction_online.stop();
 	
 	charlie.countBandwidth = false;
	  debbie.countBandwidth = false;
	  charlie.bandwidth[PID.eviction].stop();
	  debbie.bandwidth[PID.eviction].stop();
    
    return new BigInteger(secretE_P_pp, 2);
  }

  /*
  @Override
  public String[] prepareArgs(Party party) {
    // Randomly generate a secret
      String s_P_p   = Util.addZero(new BigInteger(pathBuckets*bucketBits, SR.rand).toString(2), pathBuckets*bucketBits);
      String s_T_p   = Util.addZero(new BigInteger(tupleBits, SR.rand).toString(2), tupleBits);
      String Li = Util.addZero(new BigInteger(lBits, SR.rand).toString(2), lBits);
      
      return new String[]{s_P_p, s_T_p, Li};
  }
*/
}
