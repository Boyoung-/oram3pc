package sprout.oram.operations;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import sprout.communication.Communication;
import sprout.crypto.WeakPRF;
import sprout.util.Util;

public class AOT {
  static SecureRandom rnd = new SecureRandom();
  
  // TODO: Rename (C,D) -> (R,H) or S->E for consistency
  public static void executeS(Communication C, Communication D, String[] m) {
    int N = m.length;
    int l = m[0].length();
    
    // We may be able to do this without communication
    C.write(N);
    C.write(l);
    D.write(N);
    D.write(l);
    
    // pre-computed input
    // party S
    BigInteger k = new BigInteger(128, rnd); // is this right???
    // S sends k to D
    D.write(k);
    
    // step 1
    // party S
    BigInteger alpha = BigInteger.valueOf(rnd.nextInt(N));
    BigInteger[] m_p = new BigInteger[N];
    try {
      for (int t=0; t<N; t++) {
          WeakPRF f = new WeakPRF(k, l);
          m_p[t] = new BigInteger(f.compute(l, BigInteger.valueOf(t).add(alpha).mod(BigInteger.valueOf(N))), 2).xor(new BigInteger(m[t], 2));
      }
    } catch (NoSuchAlgorithmException e){
      e.printStackTrace();
    }
    C.write(m_p);
    C.write(alpha);
    // S sends m_p and alpha to C
    
  }
  
  public static String executeC(Communication D, Communication S, int j) {
    int N = S.readInt();
    int l = S.readInt();
    
    // step 1
    // S sends m_p and alpha to C
    BigInteger [] m_p = S.readBigIntegerArray();
    BigInteger alpha = S.readBigInteger();
    
    // step 2
    //party C
    BigInteger j_p = BigInteger.valueOf(j).add(alpha).mod(BigInteger.valueOf(N));
    // C sends j_p to D
    D.write(j_p);
    
    // step 3
    // D sends c to C
    BigInteger c = D.readBigInteger();
    
    String output = Util.addZero(c.xor(m_p[j]).toString(2), l);
    // C outputs output
    
    return output;
  }
  
  public static void executeD(Communication C, Communication S) {
    int N = S.readInt();
    int l = S.readInt();
    
    // pre-computed input
    BigInteger k = S.readBigInteger();
    
    // step 2
    // C sends j_p to D
    BigInteger j_p = C.readBigInteger();
    
    // step 3
    // party D
    try {
      WeakPRF f = new WeakPRF(k, l);
      BigInteger c = new BigInteger(f.compute(l, j_p), 2);
      // D sends c to C
      C.write(c);
    } catch (NoSuchAlgorithmException e){
      e.printStackTrace();
      System.out.println("Error occured, not completing AOT, C will block");
    }
    
  }
}