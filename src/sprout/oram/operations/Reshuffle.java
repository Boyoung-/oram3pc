package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.oram.Tree;
import sprout.util.Util;

import org.apache.commons.lang3.tuple.Pair;

public class Reshuffle extends TreeOperation<String, Pair<String, List<Integer>>> {

	/*
  Reshuffle(Communication con1, Communication con2, ForestMetadata metadata) {
    super(con1, con2, metadata);
  }
  */
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
    byte[] s1 = rnd.generateSeed(16);
    PRG G;
    try {
      G = new PRG(pathBuckets*bucketBits);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
    String p1 = G.generateBitString(pathBuckets*bucketBits, s1);
    String z = Util.addZero(new BigInteger(secretC_P, 2).xor(new BigInteger(p1, 2)).toString(2), bucketBits);
    // C sends z to E
    // C sends s1 to D
    eddie.write(z);
    debbie.write(s1);
    
    // step 2 & 3
    // D sends secretC_pi_P to C
    // C outputs secretC_pi_P
    return debbie.readString();
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
      byte[] s1 = charlie.read();
      
      // step 2
      // party D
      PRG G = new PRG(pathBuckets*bucketBits);
      String p1 = G.generateBitString(pathBuckets*bucketBits, s1);
      byte[] s2 = rnd.generateSeed(16);
      String p2 = G.generateBitString(pathBuckets*bucketBits, s2);
      String a_all = Util.addZero(new BigInteger(p1, 2).xor(new BigInteger(p2, 2)).toString(2), pathBuckets*bucketBits);
      String[] a = new String[pathBuckets];
      for (int j=0; j<pathBuckets; j++)
        a[j] = a_all.substring(j*bucketBits, (j+1)*bucketBits);
      String[] secretC_pi_P_arr = Util.permute(a, pi);
      String secretC_pi_P = "";
      for (int j=0; j<pathBuckets; j++)
        secretC_pi_P += secretC_pi_P_arr[j];
      // D sends secretC_pi_P to C
      // D sends s2 to E
      charlie.write(secretC_pi_P);
      eddie.write(s2);
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
    String z = charlie.readString();
    
    // step 2
    // D sends s2 to E
    byte[] s2 = debbie.read();
    PRG G;
    try {
      G = new PRG(pathBuckets*bucketBits);
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
      return null;
    }
    String p2 = G.generateBitString(pathBuckets*bucketBits, s2);
    
    // step 4
    // party E
    String b_all = Util.addZero(new BigInteger(secretE_P, 2).xor(new BigInteger(z, 2)).xor(new BigInteger(p2, 2)).toString(2), pathBuckets*bucketBits);
    String[] b = new String[pathBuckets];
    for (int j=0; j<pathBuckets; j++)
      b[j] = b_all.substring(j*bucketBits, (j+1)*bucketBits);
    String[] secretE_pi_P_arr = Util.permute(b, pi);
    String secretE_pi_P = "";
    for (int j=0; j<pathBuckets; j++)
      secretE_pi_P += secretE_pi_P_arr[j];
    // E outputs secretE_pi_P
    return secretE_pi_P;
  }

  // Temporarily redefine n
  // We probably want to eventually unify the meaning of n
  @Override
  public void loadTreeSpecificParameters(int index) {
    super.loadTreeSpecificParameters(index);
    //if (i > 0)
    	//n = n/w;
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
