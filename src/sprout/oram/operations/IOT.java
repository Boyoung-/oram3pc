package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sprout.communication.Communication;
import sprout.crypto.PRG;
import sprout.oram.Forest;
import sprout.oram.ForestException;
import sprout.oram.Party;
import sprout.util.Util;

public class IOT extends Operation {

  public IOT(Communication con1, Communication con2) {
    super(con1, con2);
  }
  
  public static void executeS(Communication R, Communication I, String[] m) {
    // parameters
    int N = m.length;
    int l = m[0].length();
    
    I.write(N);
    I.write(l);
    R.write(l);
    
    // Pre-computed inputs
    Integer[] pi = I.readIntegerArray();
    
    String[] r = I.readStringArray();
    
    // protocol
    // step 1
    // party S
    String[] a = new String[N];
    for (int o=0; o<N; o++)
      a[o] = Util.addZero(new BigInteger(m[pi[o]], 2).xor(new BigInteger(r[o], 2)).toString(2), l);
    // S sends a to R
    R.write(a);
  }
  
  public static String[] executeR(Communication I, Communication S) {
    // parameters // TODO: should not be transmitted
    int k = I.readInt();
    int l = S.readInt();
    // protocol
    // step 1
    // S sends a to R
    String[] a = S.readStringArray();
    
    // step 2
    // I sends j and p to R
    Integer[] j = I.readIntegerArray();
    String[] p = I.readStringArray();
    
    // step 3
    // party R
    String[] z = new String[k];
    for (int o=0; o<k; o++)
      z[o] = Util.addZero(new BigInteger(a[j[o]], 2).xor(new BigInteger(p[o], 2)).toString(2), l);
    // R output z

    return z;
  }
  
  public static void executeI(Communication R, Communication S, Integer[] i, String[] delta) throws NoSuchAlgorithmException {
    // parameters // TODO: these should not be transmitted at execution time
    int k = i.length;
    R.write(k);
    int N = S.readInt();
    int l = S.readInt();
    
    // pre-computed inputs
    // party I
    List<Integer> pi = new ArrayList<Integer>();            
    for (int o=0; o<N; o++)
      pi.add(o);
    Collections.shuffle(pi, rnd);  
    List<Integer> pi_ivs = Util.getInversePermutation(pi); // inverse permutation   
    byte[] s = rnd.generateSeed(16);
    PRG G = new PRG(N*l);
    String r_all = G.generateBitString(N*l, s);
    String[] r = new String[N];
    for (int o=0; o<N; o++)
      r[o] = r_all.substring(o*l, (o+1)*l);
    // I sends S pi and r
    S.write(pi.toArray(new Integer[0]));
    S.write(r);
    
    // step 2
    // party I
    Integer[] j = new Integer[k];
    String[] p = new String[k];
    for (int o=0; o<k; o++) {
      j[o] = pi_ivs.get(i[o]);
      p[o] = Util.addZero(new BigInteger(r[j[o]], 2).xor(new BigInteger(delta[o], 2)).toString(2), l);
    }
    // I sends j and p to R
    R.write(j);
    R.write(p);
  }

  @Override
  public void run(Party party, Forest forest) throws ForestException {
    switch (party) {
    case Debbie: // I
      Integer[] i = new Integer[]{0, 1, 3, 7};
      String[] delta = new String[]{"000", "111", "000", "111"};
      try {
        IOT.executeI(con1, con2, i, delta);
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
      break;
    case Charlie: // R
      // In current configuration expected output is [000, 110, 011, 000]
      System.out.println(Arrays.toString(IOT.executeR(con1, con2)));
      break;
    case Eddie: // S
      String[] m = new String[]{"000", "001", "010", "011", "100", "101", "110", "111"};
      IOT.executeS(con1, con2, m);
      break;
    }
    
    // Ensure we don't close the connection before processing is finished
    con1.write("finished");
    con2.write("finished");
    con1.readString();
    con2.readString();
  }

}
