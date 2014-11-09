package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.oram.Tree;
import sprout.util.Timing;
import sprout.util.Util;

import org.apache.commons.lang3.tuple.Pair;

public class Reshuffle extends TreeOperation<String, Pair<String, List<Integer>>> {

	public Reshuffle() {
		super(null, null);
	}
	
  public Reshuffle(Communication con1, Communication con2) {
    super(con1, con2);
  }
  

  @Override
  public String executeCharlieSubTree(Communication debbie,
      Communication eddie, String Li, Tree OT, Pair<String, List<Integer>> extraArgs) {
    String secretC_P = extraArgs.getLeft();
    
    // i = 0 case: no shuffle needed
    if (i == 0) {
      return secretC_P;
    }
    
    // protocol
    // step 1
    // party C
    Timing.reshuffle_online.start();
    byte[] s1 = rnd.generateSeed(16);
    Timing.reshuffle_online.stop();
    PRG G;
    try {
      G = new PRG(pathBuckets*bucketBits);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
    Timing.reshuffle_online.start();
    String p1 = G.generateBitString(pathBuckets*bucketBits, s1);
    String z = Util.addZero(new BigInteger(secretC_P, 2).xor(new BigInteger(p1, 2)).toString(2), bucketBits);
    Timing.reshuffle_online.stop();
    // C sends z to E
    // C sends s1 to D
    Timing.reshuffle_write.start();
    eddie.write(z);
    debbie.write(s1);
    Timing.reshuffle_write.stop();
    
    // step 2 & 3
    // D sends secretC_pi_P to C
    // C outputs secretC_pi_P
    Timing.reshuffle_read.start();
    String secretC_pi_P = debbie.readString();
    Timing.reshuffle_read.stop();
    return secretC_pi_P;
  }

  @Override
  public String executeDebbieSubTree(Communication charlie,
      Communication eddie, BigInteger k, Tree OT,
      Pair<String, List<Integer>> extraArgs) {
    List<Integer> pi = extraArgs.getRight();
    
    // i = 0 case: no shuffle needed
    if (i == 0) {
      return null;
    }
    
    try { 
      // protocol
      // step 1
      // C sends D s1
    	Timing.reshuffle_read.start();
      byte[] s1 = charlie.read();
      Timing.reshuffle_read.stop();
      
      // step 2
      // party D
      PRG G1 = new PRG(pathBuckets*bucketBits);
      PRG G2 = new PRG(pathBuckets*bucketBits); // TODO: same issue: non-fresh -> non-deterministic
      Timing.reshuffle_online.start();
      String p1 = G1.generateBitString(pathBuckets*bucketBits, s1);
      byte[] s2 = rnd.generateSeed(16);
      String p2 = G2.generateBitString(pathBuckets*bucketBits, s2);
      String a_all = Util.addZero(new BigInteger(p1, 2).xor(new BigInteger(p2, 2)).toString(2), pathBuckets*bucketBits);
      String[] a = new String[pathBuckets];
      for (int j=0; j<pathBuckets; j++)
        a[j] = a_all.substring(j*bucketBits, (j+1)*bucketBits);
      String[] secretC_pi_P_arr = Util.permute(a, pi);
      Timing.reshuffle_online.stop();
      String secretC_pi_P = "";
      for (int j=0; j<pathBuckets; j++)
        secretC_pi_P += secretC_pi_P_arr[j];
      // D sends secretC_pi_P to C
      // D sends s2 to E
      Timing.reshuffle_write.start();
      charlie.write(secretC_pi_P);
      eddie.write(s2);
      Timing.reshuffle_write.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  @Override
  public String executeEddieSubTree(Communication charlie,
      Communication debbie, Tree OT, Pair<String, List<Integer>> extraArgs) {
    String secretE_P = extraArgs.getLeft();
    List<Integer> pi = extraArgs.getRight();
    
    // i = 0 case: no shuffle needed
    if (i == 0) {
      return secretE_P;
    }
    
    // protocol
    // step 1
    // C sends E z
    Timing.reshuffle_read.start();
    String z = charlie.readString();
    Timing.reshuffle_read.stop();
    
    // step 2
    // D sends s2 to E
    Timing.reshuffle_read.start();
    byte[] s2 = debbie.read();
    Timing.reshuffle_read.stop();
    
    // step 4
    // party E
    PRG G;
    try {
      G = new PRG(pathBuckets*bucketBits);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
    Timing.reshuffle_online.start();
    String p2 = G.generateBitString(pathBuckets*bucketBits, s2);
    String b_all = Util.addZero(new BigInteger(secretE_P, 2).xor(new BigInteger(z, 2)).xor(new BigInteger(p2, 2)).toString(2), pathBuckets*bucketBits);
    String[] b = new String[pathBuckets];
    for (int j=0; j<pathBuckets; j++)
      b[j] = b_all.substring(j*bucketBits, (j+1)*bucketBits);
    String[] secretE_pi_P_arr = Util.permute(b, pi);
    Timing.reshuffle_online.stop();
    String secretE_pi_P = "";
    for (int j=0; j<pathBuckets; j++)
      secretE_pi_P += secretE_pi_P_arr[j];
    // E outputs secretE_pi_P
    return secretE_pi_P;
  }

  @Override
  public Pair<String, List<Integer>> prepareArgs() {
    String secret_P = Util.addZero(new BigInteger(bucketBits*pathBuckets, rnd).toString(2), bucketBits*pathBuckets);
    List<Integer> pi  = new ArrayList<Integer>(); 
    for (int j=0; j<pathBuckets; j++)
      pi.add(j);
    Collections.shuffle(pi, rnd);
    
    
    if (print_out) {
      System.out.println("secret: " + secret_P);
      System.out.println("pi: " + pi);
    }
    
    return Pair.of(secret_P, pi);
  }
}
