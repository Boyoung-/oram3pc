package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.crypto.SR;
import sprout.oram.PID;
import sprout.oram.Tree;
import sprout.util.Util;

import org.apache.commons.lang3.tuple.Pair;

public class Reshuffle extends TreeOperation<String, Pair<BigInteger, List<Integer>>> {

	public Reshuffle() {
		super(null, null);
	}
	
  public Reshuffle(Communication con1, Communication con2) {
    super(con1, con2);
  }
  

  @Override
  public String executeCharlieSubTree(Communication debbie,
      Communication eddie, String Li, Tree OT, Pair<BigInteger, List<Integer>> extraArgs) {
	    debbie.countBandwidth = false;
	    eddie.countBandwidth = false;
	  
    BigInteger secretC_P = extraArgs.getLeft();
    
    // i = 0 case: no shuffle needed
    if (i == 0) {
    	if (secretC_P == null)
    		return "";
      return Util.addZero(secretC_P.toString(2), tupleBits*pathTuples);
    }
    
    // precomputation
    timing.reshuffle_offline.start();
    PRG G = new PRG(pathBuckets*bucketBits);
    byte[] s1 = SR.rand.generateSeed(16);
    byte[] p1 = G.compute(s1);
    timing.reshuffle_offline.stop();
    
    timing.reshuffle_offline_write.start();
    // C sends s1 to D
    debbie.write(s1);
    timing.reshuffle_offline_write.stop();
    
    
    debbie.countBandwidth = true;
    eddie.countBandwidth = true;
    debbie.bandwidth[PID.reshuffle].start();
    eddie.bandwidth[PID.reshuffle].start();
    
    sanityCheck();
    
    // protocol
    // step 1
    // party C
    timing.reshuffle_online.start();
    BigInteger z = secretC_P.xor(new BigInteger(1, p1));
    timing.reshuffle_online.stop();
    // C sends z to E
    //sanityCheck(eddie);
    timing.reshuffle_write.start();
    eddie.write(z);
    timing.reshuffle_write.stop();
    
    // step 2 & 3
    // D sends secretC_pi_P to C
    // C outputs secretC_pi_P
    //sanityCheck(debbie);
    timing.reshuffle_read.start();
    BigInteger[] secretC_pi_P_arr = debbie.readBigIntegerArray();
    timing.reshuffle_read.stop();
    
    BigInteger secretC_pi_P = BigInteger.ZERO;
    timing.reshuffle_online.start();
    for (int j=0; j<pathBuckets; j++)
    	secretC_pi_P.shiftLeft(bucketBits).xor(secretC_pi_P_arr[j]);
    timing.reshuffle_online.stop();
    
    debbie.countBandwidth = false;
    eddie.countBandwidth = false;
    debbie.bandwidth[PID.reshuffle].stop();
    eddie.bandwidth[PID.reshuffle].stop();
    
    return Util.addZero(secretC_pi_P.toString(2), bucketBits*pathBuckets);
  }

  @Override
  public String executeDebbieSubTree(Communication charlie,
      Communication eddie, BigInteger k, Tree OT,
      Pair<BigInteger, List<Integer>> extraArgs) {
	    charlie.countBandwidth = false;
		  eddie.countBandwidth = false;	  
		  
    List<Integer> pi = extraArgs.getRight();
    
    // i = 0 case: no shuffle needed
    if (i == 0) {
      return null;
    }
	  
    byte[] p1 = null, p2 = null;
	  try { 	  
	  // precomputation
		  timing.reshuffle_offline.start();
	    PRG G1 = new PRG(pathBuckets*bucketBits);
	    PRG G2 = new PRG(pathBuckets*bucketBits); // TODO: same issue: non-fresh -> non-deterministic
	      byte[] s2 = SR.rand.generateSeed(16);
	      p2 = G2.compute(s2);
	      timing.reshuffle_offline.stop();
	      
	      timing.reshuffle_offline_write.start();
	      eddie.write(s2);
	      timing.reshuffle_offline_write.stop();
	      
	    timing.reshuffle_offline_read.start();
	    byte[] s1 = charlie.read();
	    timing.reshuffle_offline_read.stop();
	    
	    timing.reshuffle_offline.start();
	    p1 = G1.compute(s1);
	      timing.reshuffle_offline.stop();
	 } catch (Exception e) {
	        e.printStackTrace();
	 }
	    
	    charlie.countBandwidth = true;
		  eddie.countBandwidth = true;	  
		  charlie.bandwidth[PID.reshuffle].start();
		  eddie.bandwidth[PID.reshuffle].start();
		  
		  sanityCheck();
	      
      // protocol
      // step 1
      // C sends D s1
      
      // step 2
      // party D
      timing.reshuffle_online.start();      
      BigInteger a_all = new BigInteger(1, p1).xor(new BigInteger(1, p2));
      BigInteger[] a = new BigInteger[pathBuckets];
      BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(BigInteger.ONE);
      BigInteger tmp = a_all;
      for (int j=pathBuckets-1; j>=0; j--) {
      	a[j] = tmp.and(helper);
      	tmp = tmp.shiftRight(d_ip1);
      }
      
      BigInteger[] secretC_pi_P = Util.permute(a, pi);
      timing.reshuffle_online.stop();
      
      // D sends secretC_pi_P to C
      // D sends s2 to E
      //sanityCheck(charlie);
      timing.reshuffle_write.start();
      charlie.write(secretC_pi_P);
      timing.reshuffle_write.stop();
    
    charlie.countBandwidth = false;
	  eddie.countBandwidth = false;	  
	  charlie.bandwidth[PID.reshuffle].stop();
	  eddie.bandwidth[PID.reshuffle].stop();
    
    return null;
  }

  @Override
  public String executeEddieSubTree(Communication charlie,
      Communication debbie, Tree OT, Pair<BigInteger, List<Integer>> extraArgs) {
	    charlie.countBandwidth = false;
		  debbie.countBandwidth = false;
		  
    BigInteger secretE_P = extraArgs.getLeft();
    List<Integer> pi = extraArgs.getRight();
    
    // i = 0 case: no shuffle needed
    if (i == 0) {
    	if (secretE_P == null)
    		return "";
      return Util.addZero(secretE_P.toString(2), tupleBits*pathTuples);
    }
	  
	  // precomputation
	    timing.reshuffle_offline_read.start();
	    byte[] s2 = debbie.read();
	    timing.reshuffle_offline_read.stop();
	    
	    timing.reshuffle_offline.start();
	    PRG G = new PRG(pathBuckets*bucketBits);
	    byte[] p2 = G.compute(s2);
	    timing.reshuffle_offline.stop();
	    
	    
	    charlie.countBandwidth = true;
		  debbie.countBandwidth = true;
		  charlie.bandwidth[PID.reshuffle].start();
		  debbie.bandwidth[PID.reshuffle].start();
		  
		  sanityCheck();
    
    // protocol
    // step 1
    // C sends E z
	//sanityCheck(charlie);
    timing.reshuffle_read.start();
    //String z = charlie.readString();
    BigInteger z = charlie.readBigInteger();
    timing.reshuffle_read.stop();
    
    //timing.reshuffle_online.start();
    //String z = Util.addZero(new BigInteger(1, z_byte).toString(2), bucketBits);
    //timing.reshuffle_online.stop();
    
    // step 2
    // D sends s2 to E
    
    // step 4
    // party E    
    timing.reshuffle_online.start();
    //String b_all = Util.addZero(secretE_P.xor(z).xor(new BigInteger(1, p2)).toString(2), pathBuckets*bucketBits);
    BigInteger b_all = secretE_P.xor(z).xor(new BigInteger(1, p2));
    //String[] b = new String[pathBuckets];
    BigInteger[] b = new BigInteger[pathBuckets];
    //for (int j=0; j<pathBuckets; j++)
    //  b[j] = b_all.substring(j*bucketBits, (j+1)*bucketBits);
    BigInteger helper = BigInteger.ONE.shiftLeft(bucketBits).subtract(BigInteger.ONE);
    BigInteger tmp = b_all;
    for (int j=pathBuckets-1; j>=0; j--) {
    	b[j] = tmp.and(helper);
    	tmp = tmp.shiftRight(d_ip1);
    }
    //String[] secretE_pi_P_arr = Util.permute(b, pi);
    //String secretE_pi_P = "";
    BigInteger[] secretE_pi_P_arr = Util.permute(b, pi);
    BigInteger secretE_pi_P = BigInteger.ZERO;
    for (int j=0; j<pathBuckets; j++)
      //secretE_pi_P += secretE_pi_P_arr[j];
    	secretE_pi_P.shiftLeft(bucketBits).xor(secretE_pi_P_arr[j]);
    timing.reshuffle_online.stop();
    
    charlie.countBandwidth = false;
	  debbie.countBandwidth = false;
	  charlie.bandwidth[PID.reshuffle].stop();
	  debbie.bandwidth[PID.reshuffle].stop();
    
    // E outputs secretE_pi_P
    return Util.addZero(secretE_pi_P.toString(2), bucketBits*pathBuckets);
  }

  /*
  @Override
  public Pair<String, List<Integer>> prepareArgs() {
    String secret_P = Util.addZero(new BigInteger(bucketBits*pathBuckets, SR.rand).toString(2), bucketBits*pathBuckets);
    List<Integer> pi  = new ArrayList<Integer>(); 
    for (int j=0; j<pathBuckets; j++)
      pi.add(j);
    Collections.shuffle(pi, SR.rand);
    
    
    if (print_out) {
      System.out.println("secret: " + secret_P);
      System.out.println("pi: " + pi);
    }
    
    return Pair.of(secret_P, pi);
  }
  */
}
