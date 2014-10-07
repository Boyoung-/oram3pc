package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.oram.Forest.TreeZero;
import sprout.oram.ForestMetadata;
import sprout.oram.Tree;
import sprout.util.Util;

import org.apache.commons.lang3.tuple.Pair;

public class Reshuffle extends Operation<String, Pair<String, List<Integer>>> {

  Reshuffle(Communication con1, Communication con2, ForestMetadata metadata) {
    super(con1, con2, metadata);
  }

  @Override
  public String executeCharlieSubTree(Communication debbie,
      Communication eddie, String Li, TreeZero OT_0, Tree OT, Pair<String, List<Integer>> extraArgs) {
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
      G = new PRG(n*l);
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
    String p1 = G.generateBitString(n*l, s1);
    String z = Util.addZero(new BigInteger(secretC_P, 2).xor(new BigInteger(p1, 2)).toString(2), l);
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
      Communication eddie, BigInteger k, TreeZero OT_0, Tree OT,
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
      PRG G = new PRG(n*l);
      String p1 = G.generateBitString(n*l, s1);
      byte[] s2 = rnd.generateSeed(16);
      String p2 = G.generateBitString(n*l, s2);
      String a_all = Util.addZero(new BigInteger(p1, 2).xor(new BigInteger(p2, 2)).toString(2), n*l);
      String[] a = new String[n];
      for (int j=0; j<n; j++)
        a[j] = a_all.substring(j*l, (j+1)*l);
      String[] secretC_pi_P_arr = Util.permute(a, pi);
      String secretC_pi_P = "";
      for (int j=0; j<n; j++)
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
      Communication debbie, TreeZero OT_0, Tree OT, Pair<String, List<Integer>> extraArgs) {
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
      G = new PRG(n*l);
    } catch (NoSuchAlgorithmException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
    String p2 = G.generateBitString(n*l, s2);
    
    // step 4
    // party E
    String b_all = Util.addZero(new BigInteger(secretE_P, 2).xor(new BigInteger(z, 2)).xor(new BigInteger(p2, 2)).toString(2), n*l);
    String[] b = new String[n];
    for (int j=0; j<n; j++)
      b[j] = b_all.substring(j*l, (j+1)*l);
    String[] secretE_pi_P_arr = Util.permute(b, pi);
    String secretE_pi_P = "";
    for (int j=0; j<n; j++)
      secretE_pi_P += secretE_pi_P_arr[j];
    // E outputs secretE_pi_P
    return secretE_pi_P;
  }
}
